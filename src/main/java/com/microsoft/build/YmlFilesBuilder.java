package com.microsoft.build;

import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.SpecJava;
import com.microsoft.model.TocFile;
import com.microsoft.model.TocItem;
import com.microsoft.util.ElementUtil;
import com.microsoft.util.FileUtil;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import jdk.javadoc.doclet.DocletEnvironment;

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
            String href = packageLookup.extractHref(packageElement);
            buildPackageYmlFile(packageElement, href);

            TocItem packageTocItem = new TocItem(uid, uid, href);
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
            String href = classLookup.extractHref(classElement);

            listToAddItems.add(new TocItem(uid, name, href));

            buildClassYmlFile(classElement, href);
            buildFilesForInnerClasses(classElement, listToAddItems);
        }
    }

    void buildPackageYmlFile(PackageElement packageElement, String fileName) {
        MetadataFile metadataFile = new MetadataFile(outputPath, fileName);
        MetadataFileItem packageItem = new MetadataFileItem(LANGS, packageLookup.extractUid(packageElement));
        packageItem.setId(packageLookup.extractId(packageElement));
        addChildrenReferences(packageElement, packageItem.getChildren(),
            metadataFile.getReferences());
        packageItem.setHref(packageLookup.extractHref(packageElement));
        packageItem.setName(packageLookup.extractName(packageElement));
        packageItem.setNameWithType(packageLookup.extractNameWithType(packageElement));
        packageItem.setFullName(packageLookup.extractFullName(packageElement));
        packageItem.setType(packageLookup.extractType(packageElement));
        packageItem.setSummary(packageLookup.extractSummary(packageElement));
        packageItem.setContent(packageLookup.extractContent(packageElement));
        metadataFile.getItems().add(packageItem);

        FileUtil.dumpToFile(metadataFile);
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
        referenceItem.setParent(classLookup.extractParent(classElement));
        referenceItem.setHref(classLookup.extractHref(classElement));
        referenceItem.setName(classLookup.extractName(classElement));
        referenceItem.setNameWithType(classLookup.extractNameWithType(classElement));
        referenceItem.setFullName(classLookup.extractFullName(classElement));
        referenceItem.setType(classLookup.extractType(classElement));
        referenceItem.setSummary(classLookup.extractSummary(classElement));
        referenceItem.setContent(classLookup.extractContent(classElement));
        referenceItem.setTypeParameters(classLookup.extractTypeParameters(classElement));
        return referenceItem;
    }

    void buildClassYmlFile(TypeElement classElement, String fileName) {
        MetadataFile classMetadataFile = new MetadataFile(outputPath, fileName);
        addClassInfo(classElement, classMetadataFile);
        addConstructorsInfo(classElement, classMetadataFile);
        addMethodsInfo(classElement, classMetadataFile);
        addFieldsInfo(classElement, classMetadataFile);
        addReferencesInfo(classElement, classMetadataFile);

        FileUtil.dumpToFile(classMetadataFile);
    }

    void addClassInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        MetadataFileItem classItem = new MetadataFileItem(LANGS, classLookup.extractUid(classElement));
        classItem.setId(classLookup.extractId(classElement));
        classItem.setParent(classLookup.extractParent(classElement));
        addChildren(classElement, classItem.getChildren());
        classItem.setHref(classLookup.extractHref(classElement));
        classItem.setName(classLookup.extractName(classElement));
        classItem.setNameWithType(classLookup.extractNameWithType(classElement));
        classItem.setFullName(classLookup.extractFullName(classElement));
        classItem.setType(classLookup.extractType(classElement));
        classItem.setPackageName(classLookup.extractPackageName(classElement));
        classItem.setSummary(classLookup.extractSummary(classElement));
        classItem.setContent(classLookup.extractContent(classElement));
        classItem.setTypeParameters(classLookup.extractTypeParameters(classElement));
        classItem.setSuperclass(classLookup.extractSuperclass(classElement));
        classMetadataFile.getItems().add(classItem);
    }

    void addChildren(TypeElement classElement, List<String> children) {
        collect(ElementFilter.constructorsIn(classElement.getEnclosedElements()), children,
            classItemsLookup::extractUid);
        collect(ElementFilter.methodsIn(classElement.getEnclosedElements()), children, classItemsLookup::extractUid);
        collect(ElementFilter.fieldsIn(classElement.getEnclosedElements()), children, classItemsLookup::extractUid);
        collect(ElementFilter.typesIn(classElement.getEnclosedElements()), children, String::valueOf);
    }

    void collect(List<? extends Element> elements, List<String> children, Function<? super Element, String> func) {
        children.addAll(elements.stream().map(func).collect(Collectors.toList()));
    }

    void addConstructorsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            MetadataFileItem constructorItem = buildMetadataFileItem(constructorElement);
            constructorItem.setOverload(classItemsLookup.extractOverload(constructorElement));
            constructorItem.setContent(classItemsLookup.extractConstructorContent(constructorElement));
            constructorItem.setParameters(classItemsLookup.extractParameters(constructorElement));
            classMetadataFile.getItems().add(constructorItem);

//            addParameterReferences(constructorItem, classMetadataFile);
        }
    }

    void addMethodsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            MetadataFileItem methodItem = buildMetadataFileItem(methodElement);
            methodItem.setOverload(classItemsLookup.extractOverload(methodElement));
            methodItem.setContent(classItemsLookup.extractMethodContent(methodElement));
            methodItem.setExceptions(classItemsLookup.extractExceptions(methodElement));
            methodItem.setParameters(classItemsLookup.extractParameters(methodElement));
            methodItem.setReturn(classItemsLookup.extractReturn(methodElement));
            classMetadataFile.getItems().add(methodItem);

//            addExceptionReferences(methodItem, classMetadataFile);
//            addParameterReferences(methodItem, classMetadataFile);
//            addReturnReferences(methodItem, classMetadataFile);
        }
    }

    void addFieldsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (VariableElement fieldElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            MetadataFileItem fieldItem = buildMetadataFileItem(fieldElement);
            fieldItem.setContent(classItemsLookup.extractFieldContent(fieldElement));
            fieldItem.setReturn(classItemsLookup.extractReturn(fieldElement));
            classMetadataFile.getItems().add(fieldItem);

//            addReturnReferences(fieldItem, classMetadataFile);
        }
    }

    void addReferencesInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        // Owner class reference
        MetadataFileItem classReference = buildClassReference(classElement);
        classMetadataFile.getReferences().add(classReference);
        // Type parameter references
        addTypeParameterReferences(classReference, classMetadataFile);

        // Inner classes references
        classMetadataFile.getReferences().addAll(
            ElementFilter.typesIn(classElement.getEnclosedElements()).stream()
                .map(this::buildClassReference)
                .collect(Collectors.toList()));

        // Owner class methods references
        classMetadataFile.getReferences().addAll(buildMethodsReferences(classElement));
    }

    List<MetadataFileItem> buildMethodsReferences(TypeElement classElement) {
        return ElementFilter.methodsIn(classElement.getEnclosedElements()).stream()
            .map(methodElement -> new MetadataFileItem(classItemsLookup.extractUid(methodElement)) {{
                setName(classItemsLookup.extractName(methodElement));
                setNameWithType(classItemsLookup.extractNameWithType(methodElement));
                setFullName(classItemsLookup.extractFullName(methodElement));
                setPackageName(classItemsLookup.extractPackageName(methodElement));
            }}).collect(Collectors.toList());
    }

    MetadataFileItem buildMetadataFileItem(Element element) {
        return new MetadataFileItem(LANGS, classItemsLookup.extractUid(element)) {{
            setId(classItemsLookup.extractId(element));
            setParent(classItemsLookup.extractParent(element));
            setHref(classItemsLookup.extractHref(element));
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
                .map(parameter -> buildSpecJavaRefItemAndReplaceField(parameter, "type", this::generateHexString))
                .collect(Collectors.toList()));
    }

    void addReturnReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
            Stream.of(methodItem.getSyntax().getReturnValue())
                .filter(Objects::nonNull)
                .map(returnValue -> buildSpecJavaRefItemAndReplaceField(returnValue, "returnType",
                    this::generateHexString))
                .collect(Collectors.toList()));
    }

    void addExceptionReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
            methodItem.getExceptions().stream()
                .map(exceptionItem -> buildSpecJavaRefItemAndReplaceField(exceptionItem, "type",
                    this::generateHexString))
                .collect(Collectors.toList()));
    }

    void addTypeParameterReferences(MetadataFileItem methodItem, MetadataFile classMetadataFile) {
        classMetadataFile.getReferences().addAll(
            methodItem.getSyntax().getTypeParameters().stream()
                .map(typeParameter -> {
                    String id = typeParameter.getId();
                    MetadataFileItem metadataFileItem = new MetadataFileItem(id);
                    metadataFileItem.setName(id);
                    metadataFileItem.setNameWithType(id);
                    metadataFileItem.setFullName(id);
                    metadataFileItem.setIsExternal(false);
                    return metadataFileItem;
                }).collect(Collectors.toList()));
    }

    MetadataFileItem buildSpecJavaRefItemAndReplaceField(Object object, String fieldName,
        Function<String, String> conversionFunc) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            boolean accessible = field.canAccess(object);
            field.setAccessible(true);
            String value = String.valueOf(field.get(object));
            String convertedValue = conversionFunc.apply(value);
            field.set(object, convertedValue);
            field.setAccessible(accessible);

            MetadataFileItem metadataFileItem = new MetadataFileItem(convertedValue);
            metadataFileItem.setSpecJava(new SpecJava(value, value));
            return metadataFileItem;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error during field replacement", e);
        }
    }

    String generateHexString(String key) {
        return String.valueOf(key.hashCode());
    }
}
