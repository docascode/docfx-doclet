package com.microsoft.build;

import static com.microsoft.util.ElementUtil.determineClassSimpleName;
import static com.microsoft.util.ElementUtil.extractPackageElements;
import static com.microsoft.util.ElementUtil.extractSortedElements;

import com.microsoft.lookup.ClassItemsLookup;
import com.microsoft.lookup.ClassLookup;
import com.microsoft.lookup.PackageLookup;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TocFile;
import com.microsoft.model.TocItem;
import com.microsoft.util.ElementUtil;
import com.microsoft.util.FileUtil;
import java.util.List;
import java.util.stream.Collectors;
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
    private final ElementUtil elementUtil;

    public YmlFilesBuilder(DocletEnvironment environment, String outputPath) {
        this.environment = environment;
        this.outputPath = outputPath;
        this.elementUtil = new ElementUtil(environment);
    }

    public boolean build() {
        TocFile tocFile = new TocFile(outputPath);
        for (PackageElement packageElement : extractPackageElements(environment.getIncludedElements())) {
            String uid = PackageLookup.extractUid(packageElement);
            String href = PackageLookup.extractHref(packageElement);
            buildPackageYmlFile(packageElement, href);

            TocItem packageTocItem = new TocItem(uid, uid, href);
            buildFilesForInnerClasses("", packageElement, packageTocItem.getItems());
            tocFile.addTocItem(packageTocItem);
        }
        FileUtil.dumpToFile(tocFile);
        return true;
    }

    void buildFilesForInnerClasses(String namePrefix, Element element, List<TocItem> listToAddItems) {
        for (TypeElement classElement : extractSortedElements(element)) {
            String uid = ClassLookup.extractUid(classElement);
            String id = determineClassSimpleName(namePrefix, classElement);
            String href = ClassLookup.extractHref(classElement);

            listToAddItems.add(new TocItem(uid, id, href));

            buildClassYmlFile(classElement, href);
            buildFilesForInnerClasses(id, classElement, listToAddItems);
        }
    }

    void buildPackageYmlFile(PackageElement packageElement, String fileName) {
        MetadataFile metadataFile = new MetadataFile(outputPath, fileName);
        MetadataFileItem packageItem = new MetadataFileItem(LANGS);
        packageItem.setUid(PackageLookup.extractUid(packageElement));
        packageItem.setId(PackageLookup.extractId(packageElement));
        addChildrenReferences(packageElement, packageItem.getChildren(), metadataFile.getReferences());
        packageItem.setHref(PackageLookup.extractHref(packageElement));
        packageItem.setName(PackageLookup.extractName(packageElement));
        packageItem.setNameWithType(PackageLookup.extractNameWithType(packageElement));
        packageItem.setFullName(PackageLookup.extractFullName(packageElement));
        packageItem.setType(PackageLookup.extractType(packageElement));
        packageItem.setSummary(PackageLookup.extractSummary(packageElement));
        packageItem.setContent(PackageLookup.extractContent(packageElement));
        metadataFile.getItems().add(packageItem);

        FileUtil.dumpToFile(metadataFile);
    }

    void addChildrenReferences(Element element, List<String> packageChildren,
        List<MetadataFileItem> referencesCollector) {
        for (TypeElement classElement : extractSortedElements(element)) {
            referencesCollector.add(buildClassReference(classElement));

            packageChildren.add(ClassLookup.extractUid(classElement));
            addChildrenReferences(classElement, packageChildren, referencesCollector);
        }
    }

    MetadataFileItem buildClassReference(TypeElement classElement) {
        MetadataFileItem referenceItem = new MetadataFileItem();
        referenceItem.setUid(ClassLookup.extractUid(classElement));
        referenceItem.setParent(ClassLookup.extractParent(classElement));
        referenceItem.setHref(ClassLookup.extractHref(classElement));
        referenceItem.setName(ClassLookup.extractName(classElement));
        referenceItem.setNameWithType(ClassLookup.extractNameWithType(classElement));
        referenceItem.setFullName(ClassLookup.extractFullName(classElement));
        referenceItem.setType(ClassLookup.extractType(classElement));
        referenceItem.setSummary(ClassLookup.extractSummary(classElement));
        referenceItem.setContent(ClassLookup.extractContent(classElement));
        referenceItem.setTypeParameters(ClassLookup.extractTypeParameters(classElement));
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
        MetadataFileItem classItem = new MetadataFileItem(LANGS);
        classItem.setUid(ClassLookup.extractUid(classElement));
        classItem.setId(ClassLookup.extractId(classElement));
        classItem.setParent(ClassLookup.extractParent(classElement));

        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(ClassItemsLookup.extractUid(constructorElement));
        }
        for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(ClassItemsLookup.extractUid(methodElement));
        }
        for (VariableElement fieldElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(ClassItemsLookup.extractUid(fieldElement));
        }
        for (TypeElement innerClassElement : ElementFilter.typesIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(String.valueOf(innerClassElement));
        }

        classItem.setHref(ClassLookup.extractHref(classElement));
        classItem.setName(ClassLookup.extractName(classElement));
        classItem.setNameWithType(ClassLookup.extractNameWithType(classElement));
        classItem.setFullName(ClassLookup.extractFullName(classElement));
        classItem.setType(ClassLookup.extractType(classElement));
        classItem.setPackageName(ClassLookup.extractPackageName(classElement));
        classItem.setSummary(ClassLookup.extractSummary(classElement));
        classItem.setContent(ClassLookup.extractContent(classElement));
        classItem.setTypeParameters(ClassLookup.extractTypeParameters(classElement));
        classItem.setSuperclass(ClassLookup.extractSuperclass(classElement));
        classMetadataFile.getItems().add(classItem);
    }

    void addConstructorsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            MetadataFileItem constructorItem = buildMetadataFileItem(constructorElement);
            constructorItem.setOverload(ClassItemsLookup.extractOverload(constructorElement));
            constructorItem.setContent(ClassItemsLookup.extractConstructorContent(constructorElement));
            constructorItem.setParameters(ClassItemsLookup.extractParameters(constructorElement));
            classMetadataFile.getItems().add(constructorItem);
        }
    }

    void addMethodsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            MetadataFileItem methodItem = buildMetadataFileItem(methodElement);
            methodItem.setOverload(ClassItemsLookup.extractOverload(methodElement));
            methodItem.setContent(ClassItemsLookup.extractMethodContent(methodElement));
            methodItem.setExceptions(ClassItemsLookup.extractExceptions(methodElement));
            methodItem.setParameters(ClassItemsLookup.extractParameters(methodElement));
            methodItem.setReturn(ClassItemsLookup.extractReturn(methodElement));
            classMetadataFile.getItems().add(methodItem);
        }
    }

    void addFieldsInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        for (VariableElement fieldElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            MetadataFileItem fieldItem = buildMetadataFileItem(fieldElement);
            fieldItem.setContent(ClassItemsLookup.extractFieldContent(fieldElement));
            fieldItem.setReturn(ClassItemsLookup.extractReturn(fieldElement));
            classMetadataFile.getItems().add(fieldItem);
        }
    }

    void addReferencesInfo(TypeElement classElement, MetadataFile classMetadataFile) {
        // Owner class reference
        classMetadataFile.getReferences().add(buildClassReference(classElement));

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
            .map(methodElement -> new MetadataFileItem() {{
                setUid(ClassItemsLookup.extractUid(methodElement));
                setName(ClassItemsLookup.extractName(methodElement));
                setNameWithType(ClassItemsLookup.extractNameWithType(methodElement));
                setFullName(ClassItemsLookup.extractFullName(methodElement));
                setPackageName(ClassItemsLookup.extractPackageName(methodElement));
            }}).collect(Collectors.toList());
    }

    MetadataFileItem buildMetadataFileItem(Element element) {
        return new MetadataFileItem(LANGS) {{
            setUid(ClassItemsLookup.extractUid(element));
            setId(ClassItemsLookup.extractId(element));
            setParent(ClassItemsLookup.extractParent(element));
            setHref(ClassItemsLookup.extractHref(element));
            setName(ClassItemsLookup.extractName(element));
            setNameWithType(ClassItemsLookup.extractNameWithType(element));
            setFullName(ClassItemsLookup.extractFullName(element));
            setType(ClassItemsLookup.extractType(element));
            setPackageName(ClassItemsLookup.extractPackageName(element));
            setSummary(ClassItemsLookup.extractSummary(element));
        }};
    }
}
