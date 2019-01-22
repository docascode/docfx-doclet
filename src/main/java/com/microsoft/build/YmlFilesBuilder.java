package com.microsoft.build;

import com.microsoft.lookup.BaseLookup;
import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TocFile;
import com.microsoft.model.TocItem;
import com.microsoft.util.ElementUtil;
import com.microsoft.util.FileUtil;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class YmlFilesBuilder {

    private final static String[] LANGS = {"java"};

    private DocletEnvironment environment;
    private String outputPath;
    private ElementUtil elementUtil;
    private PackageLookup packageLookup;
    private ClassItemsLookup classItemsLookup;
    private ClassLookup classLookup;

    public YmlFilesBuilder(DocletEnvironment environment, String outputPath,
        String[] excludePackages, String[] excludeClasses) {
        this.environment = environment;
        this.outputPath = outputPath;
        this.elementUtil = new ElementUtil(excludePackages, excludeClasses);
        this.packageLookup = new PackageLookup(environment);
        this.classItemsLookup = new ClassItemsLookup(environment);
        this.classLookup = new ClassLookup(environment);
    }

    public boolean build() {
        TocFile tocFile = new TocFile(outputPath);
        for (PackageElement packageElement : elementUtil.extractPackageElements(environment.getIncludedElements())) {
            String uid = packageLookup.extractUid(packageElement);
            buildPackageYmlFile(packageElement);

            TocItem packageTocItem = new TocItem(uid, uid);
            buildFilesForInnerClasses(packageElement, packageTocItem.getItems());
            tocFile.addTocItem(packageTocItem);
        }
        FileUtil.dumpToFile(tocFile);
        return true;
    }

    void buildFilesForInnerClasses(Element element, List<TocItem> listToAddItems) {
        for (TypeElement classElement : elementUtil.extractSortedElements(element)) {
            String uid = classLookup.extractUid(classElement);
            String name = classLookup.extractTocName(classElement);

            listToAddItems.add(new TocItem(uid, name));

            buildClassYmlFile(classElement);
            buildFilesForInnerClasses(classElement, listToAddItems);
        }
    }

    void buildPackageYmlFile(PackageElement packageElement) {
        String fileName = packageLookup.extractHref(packageElement);
        MetadataFile packageMetadataFile = new MetadataFile(outputPath, fileName);
        MetadataFileItem packageItem = new MetadataFileItem(LANGS, packageLookup.extractUid(packageElement));
        packageItem.setId(packageLookup.extractId(packageElement));
        addChildrenReferences(packageElement, packageItem.getChildren(), packageMetadataFile.getReferences());
        populateItemFields(packageItem, packageLookup, packageElement);
        packageMetadataFile.getItems().add(packageItem);

        FileUtil.dumpToFile(packageMetadataFile);
    }

    void addChildrenReferences(Element element, List<String> packageChildren,
        Set<MetadataFileItem> referencesCollector) {
        for (TypeElement classElement : elementUtil.extractSortedElements(element)) {
            referencesCollector.add(buildClassReference(classElement));

            packageChildren.add(classLookup.extractUid(classElement));
            addChildrenReferences(classElement, packageChildren, referencesCollector);
        }
    }

    MetadataFileItem buildClassReference(TypeElement classElement) {
        MetadataFileItem referenceItem = new MetadataFileItem(classLookup.extractUid(classElement));
        referenceItem.setName(classLookup.extractName(classElement));
        referenceItem.setNameWithType(classLookup.extractNameWithType(classElement));
        referenceItem.setFullName(classLookup.extractFullName(classElement));
        return referenceItem;
    }

    <T> void populateItemFields(MetadataFileItem item, BaseLookup<T> lookup, T element) {
        item.setName(lookup.extractName(element));
        item.setNameWithType(lookup.extractNameWithType(element));
        item.setFullName(lookup.extractFullName(element));
        item.setType(lookup.extractType(element));
        item.setSummary(lookup.extractSummary(element));
        item.setContent(lookup.extractContent(element));
    }

    void buildClassYmlFile(TypeElement classElement) {
        String fileName = classLookup.extractHref(classElement);
        MetadataFile classMetadataFile = new MetadataFile(outputPath, fileName);
        addClassInfo(classElement, classMetadataFile);
        addConstructorsInfo(classElement, classMetadataFile);
        addMethodsInfo(classElement, classMetadataFile);
        addFieldsInfo(classElement, classMetadataFile);
        addReferencesInfo(classElement, classMetadataFile);
        applyPostProcessing(classMetadataFile);

        FileUtil.dumpToFile(classMetadataFile);
    }

    void addClassInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        MetadataFileItem classItem = new MetadataFileItem(LANGS, classLookup.extractUid(classElement));
        classItem.setId(classLookup.extractId(classElement));
        classItem.setParent(classLookup.extractParent(classElement));
        addChildren(classElement, classItem.getChildren());
        populateItemFields(classItem, classLookup, classElement);
        classItem.setPackageName(classLookup.extractPackageName(classElement));
        classItem.setTypeParameters(classLookup.extractTypeParameters(classElement));
        classItem.setInheritance(classLookup.extractSuperclass(classElement));
        classMetadataFile.getItems().add(classItem);
    }

    void addChildren(TypeElement classElement, List<String> children) {
        collect(classElement, children, ElementFilter::constructorsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::methodsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::fieldsIn, classItemsLookup::extractUid);
        collect(classElement, children, ElementFilter::typesIn, String::valueOf);
    }

    List<? extends Element> filterPrivateElements(List<? extends Element> elements) {
        return elements.stream()
            .filter(element -> !element.getModifiers().contains(Modifier.PRIVATE)).collect(Collectors.toList());
    }

    void collect(TypeElement classElement, List<String> children,
        Function<Iterable<? extends Element>, List<? extends Element>> selectFunc,
        Function<? super Element, String> mapFunc) {

        List<? extends Element> elements = selectFunc.apply(classElement.getEnclosedElements());
        children.addAll(filterPrivateElements(elements).stream()
            .map(mapFunc).collect(Collectors.toList()));
    }

    void addConstructorsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            MetadataFileItem constructorItem = buildMetadataFileItem(constructorElement);
            constructorItem.setOverload(classItemsLookup.extractOverload(constructorElement));
            constructorItem.setContent(classItemsLookup.extractConstructorContent(constructorElement));
            constructorItem.setParameters(classItemsLookup.extractParameters(constructorElement));
            classMetadataFile.getItems().add(constructorItem);

            addParameterReferences(constructorItem, classMetadataFile);
        }
    }

    void addMethodsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        ElementFilter.methodsIn(classElement.getEnclosedElements()).stream()
            .filter(methodElement -> !methodElement.getModifiers().contains(Modifier.PRIVATE))
            .forEach(methodElement -> {
                MetadataFileItem methodItem = buildMetadataFileItem(methodElement);
                methodItem.setOverload(classItemsLookup.extractOverload(methodElement));
                methodItem.setContent(classItemsLookup.extractMethodContent(methodElement));
                methodItem.setExceptions(classItemsLookup.extractExceptions(methodElement));
                methodItem.setParameters(classItemsLookup.extractParameters(methodElement));
                methodItem.setReturn(classItemsLookup.extractReturn(methodElement));
                classMetadataFile.getItems().add(methodItem);
                addExceptionReferences(methodItem, classMetadataFile);
                addParameterReferences(methodItem, classMetadataFile);
                addReturnReferences(methodItem, classMetadataFile);
            });
    }

    void addFieldsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        ElementFilter.fieldsIn(classElement.getEnclosedElements()).stream()
            .filter(fieldElement -> !fieldElement.getModifiers().contains(Modifier.PRIVATE))
            .forEach(fieldElement -> {
                MetadataFileItem fieldItem = buildMetadataFileItem(fieldElement);
                fieldItem.setContent(classItemsLookup.extractFieldContent(fieldElement));
                fieldItem.setReturn(classItemsLookup.extractReturn(fieldElement));
                classMetadataFile.getItems().add(fieldItem);
                addReturnReferences(fieldItem, classMetadataFile);
            });
    }

    void addReferencesInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        MetadataFileItem classReference = new MetadataFileItem(classLookup.extractUid(classElement));
        classReference.setParent(classLookup.extractParent(classElement));
        populateItemFields(classReference, classLookup, classElement);
        classReference.setTypeParameters(classLookup.extractTypeParameters(classElement));

        addTypeParameterReferences(classReference, classMetadataFile);
        addSuperclassAndInterfacesReferences(classElement, classMetadataFile);
        addInnerClassesReferences(classElement, classMetadataFile);
    }

    MetadataFileItem buildMetadataFileItem(Element element) {
        return new MetadataFileItem(LANGS, classItemsLookup.extractUid(element)) {{
            setId(classItemsLookup.extractId(element));
            setParent(classItemsLookup.extractParent(element));
            setName(classItemsLookup.extractName(element));
            setNameWithType(classItemsLookup.extractNameWithType(element));
            setFullName(classItemsLookup.extractFullName(element));
            setType(classItemsLookup.extractType(element));
            setPackageName(classItemsLookup.extractPackageName(element));
            setSummary(classItemsLookup.extractSummary(element));
        }};
    }

    void addParameterReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
            methodItem.getSyntax().getParameters().stream()
                .map(parameter -> buildRefItem(parameter.getType()))
                .filter(o -> !classMetadataFile.getItems().contains(o))
                .collect(Collectors.toList()));
    }

    void addReturnReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
            Stream.of(methodItem.getSyntax().getReturnValue())
                .filter(Objects::nonNull)
                .map(returnValue -> buildRefItem(returnValue.getReturnType()))
                .filter(o -> !classMetadataFile.getItems().contains(o))
                .collect(Collectors.toList()));
    }

    void addExceptionReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
            methodItem.getExceptions().stream()
                .map(exceptionItem -> buildRefItem(exceptionItem.getType()))
                .filter(o -> !classMetadataFile.getItems().contains(o))
                .collect(Collectors.toList()));
    }

    void addTypeParameterReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
            methodItem.getSyntax().getTypeParameters().stream()
                .map(typeParameter -> {
                    String id = typeParameter.getId();
                    return new MetadataFileItem(id, id, false);
                }).collect(Collectors.toList()));
    }

    void addSuperclassAndInterfacesReferences(TypeElement classElement, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(classLookup.extractReferences(classElement));
    }

    void addInnerClassesReferences(TypeElement classElement, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
            ElementFilter.typesIn(classElement.getEnclosedElements()).stream()
                .map(this::buildClassReference)
                .collect(Collectors.toList()));
    }

    void applyPostProcessing(MetadataFile classMetadataFile) {
        expandComplexGenericsInReferences(classMetadataFile);
    }

    /**
     * Replace one record in 'references' with several records in this way:
     * <pre>
     * a.b.c.List<df.mn.ClassOne<tr.T>> ->
     *     - a.b.c.List
     *     - df.mn.ClassOne
     *     - tr.T
     * </pre>
     */
    void expandComplexGenericsInReferences(MetadataFile classMetadataFile) {
        Set<MetadataFileItem> additionalItems = new LinkedHashSet<>();
        Iterator<MetadataFileItem> iterator = classMetadataFile.getReferences().iterator();
        while (iterator.hasNext()) {
            MetadataFileItem item = iterator.next();
            String uid = item.getUid();
            if (uid.contains("<")) {
                iterator.remove();

                List<String> classNames = splitUidWithGenericsIntoClassNames(uid);
                additionalItems.addAll(classNames.stream()
                    .map(s -> new MetadataFileItem(s, classLookup.makeTypeShort(s), true))
                    .collect(Collectors.toSet()));
            }
        }
        // Remove items which already exist in 'items' section (compared by 'uid' field)
        additionalItems.removeAll(classMetadataFile.getItems());

        classMetadataFile.getReferences().addAll(additionalItems);
    }

    List<String> splitUidWithGenericsIntoClassNames(String uid) {
        uid = RegExUtils.removeAll(uid, "[>]+$");
        return Arrays.asList(StringUtils.split(uid, "<"));
    }

    MetadataFileItem buildRefItem(String value) {
        value = RegExUtils.removeAll(value, "\\[\\]$");
        return new MetadataFileItem(value, classLookup.makeTypeShort(value), true);
    }
}
