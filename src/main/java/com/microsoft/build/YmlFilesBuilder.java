package com.microsoft.build;

import static com.microsoft.util.ElementUtil.determineClassSimpleName;
import static com.microsoft.util.ElementUtil.extractClassContent;
import static com.microsoft.util.ElementUtil.extractComment;
import static com.microsoft.util.ElementUtil.extractPackageContent;
import static com.microsoft.util.ElementUtil.extractPackageElements;
import static com.microsoft.util.ElementUtil.extractSortedElements;
import static com.microsoft.util.ElementUtil.extractSuperclass;
import static com.microsoft.util.ElementUtil.extractType;
import static com.microsoft.util.ElementUtil.extractTypeParameters;

import com.microsoft.lookup.ClassItemsLookup;
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
            String packageQName = String.valueOf(packageElement.getQualifiedName());
            String packageYmlFileName = packageQName + ".yml";
            buildPackageYmlFile(packageElement, packageYmlFileName);

            TocItem packageTocItem = new TocItem(packageQName, packageQName, packageYmlFileName);
            buildFilesForInnerClasses("", packageElement, packageTocItem.getItems());
            tocFile.addTocItem(packageTocItem);
        }
        FileUtil.dumpToFile(tocFile);
        return true;
    }

    void buildFilesForInnerClasses(String namePrefix, Element element, List<TocItem> listToAddItems) {
        for (TypeElement classElement : extractSortedElements(element)) {
            String classQName = String.valueOf(classElement.getQualifiedName());
            String classSimpleName = determineClassSimpleName(namePrefix, classElement);
            String classYmlFileName = classQName + ".yml";
            buildClassYmlFile(classElement, classYmlFileName);

            TocItem classTocItem = new TocItem(classQName, classSimpleName, classYmlFileName);
            listToAddItems.add(classTocItem);

            buildFilesForInnerClasses(classSimpleName, classElement, listToAddItems);
        }
    }

    void buildPackageYmlFile(PackageElement packageElement, String fileName) {
        MetadataFile metadataFile = new MetadataFile(outputPath, fileName);
        String qName = String.valueOf(packageElement.getQualifiedName());
        String sName = String.valueOf(packageElement.getSimpleName());

        MetadataFileItem packageItem = new MetadataFileItem(LANGS);
        packageItem.setUid(qName);
        packageItem.setId(sName);
        addChildrenReferences("", packageElement, packageItem.getChildren(), metadataFile.getReferences());
        packageItem.setHref(qName + ".yml");
        packageItem.setName(qName);
        packageItem.setNameWithType(qName);
        packageItem.setFullName(qName);
        packageItem.setType(extractType(packageElement));
        packageItem.setSummary(extractComment(packageElement));
        packageItem.setContent(extractPackageContent(packageElement));
        metadataFile.getItems().add(packageItem);
        FileUtil.dumpToFile(metadataFile);
    }

    void addChildrenReferences(String namePrefix, Element element, List<String> packageChildren,
        List<MetadataFileItem> referencesCollector) {
        for (TypeElement classElement : extractSortedElements(element)) {
            String qName = String.valueOf(classElement.getQualifiedName());
            String sName = determineClassSimpleName(namePrefix, classElement);

            MetadataFileItem reference = buildClassReference(classElement);
            referencesCollector.add(reference);

            packageChildren.add(qName);
            addChildrenReferences(sName, classElement, packageChildren, referencesCollector);
        }
    }

    MetadataFileItem buildClassReference(TypeElement classElement) {
        MetadataFileItem referenceItem = new MetadataFileItem();
        String packageName = String.valueOf(environment.getElementUtils().getPackageOf(classElement));
        String qNameWithGenericsSupport = String.valueOf(classElement.asType());
        String shortNameWithGenericsSupport = qNameWithGenericsSupport.replace(packageName + ".", "");
        String qName = String.valueOf(classElement.getQualifiedName());

        referenceItem.setUid(qName);
        referenceItem.setParent(packageName);
        referenceItem.setHref(qName + ".yml");
        referenceItem.setName(shortNameWithGenericsSupport);
        referenceItem.setNameWithType(shortNameWithGenericsSupport);
        referenceItem.setFullName(qNameWithGenericsSupport);
        referenceItem.setType(extractType(classElement));
        referenceItem.setSummary(extractComment(classElement));
        referenceItem.setContent(extractClassContent(classElement, shortNameWithGenericsSupport));
        referenceItem.setTypeParameters(extractTypeParameters(classElement));
        return referenceItem;
    }

    void buildClassYmlFile(TypeElement classElement, String fileName) {
        MetadataFile classMetadataFile = new MetadataFile(outputPath, fileName);
        String packageName = String.valueOf(environment.getElementUtils().getPackageOf(classElement));
        String classQName = String.valueOf(classElement.getQualifiedName());
        String classSName = String.valueOf(classElement.getSimpleName());
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        String classSNameWithGenericsSupport = classQNameWithGenericsSupport.replace(packageName + ".", "");

        // Add class info
        MetadataFileItem classItem = new MetadataFileItem(LANGS);
        classItem.setUid(classQName);
        classItem.setId(classSName);
        classItem.setParent(packageName);

        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(classQName + "." + constructorElement);
        }
        for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(classQName + "." + methodElement);
        }
        for (VariableElement fieldElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(classQName + "." + fieldElement);
        }
        for (TypeElement innerClassElement : ElementFilter.typesIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(String.valueOf(innerClassElement));
        }

        classItem.setHref(classQName + ".yml");
        classItem.setName(classSNameWithGenericsSupport);
        classItem.setNameWithType(classSNameWithGenericsSupport);
        classItem.setFullName(classQNameWithGenericsSupport);
        classItem.setType(extractType(classElement));
        classItem.setPackageName(packageName);
        classItem.setSummary(extractComment(classElement));
        classItem.setContent(extractClassContent(classElement, classSNameWithGenericsSupport));
        classItem.setSuperclass(extractSuperclass(classElement));
        classItem.setTypeParameters(extractTypeParameters(classElement));
        classMetadataFile.getItems().add(classItem);

        // Add constructors info
        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            MetadataFileItem constructorItem = buildMetadataFileItem(constructorElement);
            constructorItem.setOverload(ClassItemsLookup.extractOverload(constructorElement));
            constructorItem.setContent(ClassItemsLookup.extractConstructorContent(constructorElement));
            constructorItem.setParameters(ClassItemsLookup.extractParameters(constructorElement));
            classMetadataFile.getItems().add(constructorItem);
        }

        // Add methods info
        for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            MetadataFileItem methodItem = buildMetadataFileItem(methodElement);
            methodItem.setOverload(ClassItemsLookup.extractOverload(methodElement));
            methodItem.setContent(ClassItemsLookup.extractMethodContent(methodElement));
            methodItem.setExceptions(ClassItemsLookup.extractExceptions(methodElement));
            methodItem.setParameters(ClassItemsLookup.extractParameters(methodElement));
            methodItem.setReturn(ClassItemsLookup.extractReturn(methodElement));
            classMetadataFile.getItems().add(methodItem);
        }

        // Add fields info
        for (VariableElement fieldElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            MetadataFileItem fieldItem = buildMetadataFileItem(fieldElement);
            fieldItem.setContent(ClassItemsLookup.extractFieldContent(fieldElement));
            fieldItem.setReturn(ClassItemsLookup.extractReturn(fieldElement));
            classMetadataFile.getItems().add(fieldItem);
        }

        // Add references info
        // Owner class reference
        classMetadataFile.getReferences().add(buildClassReference(classElement));
        // Inner classes references
        classMetadataFile.getReferences().addAll(
            ElementFilter.typesIn(classElement.getEnclosedElements()).stream()
                .map(this::buildClassReference)
                .collect(Collectors.toList()));

        // Owner class methods references
        classMetadataFile.getReferences().addAll(buildMethodsReferences(classElement));

        FileUtil.dumpToFile(classMetadataFile);
    }

    private List<MetadataFileItem> buildMethodsReferences(TypeElement classElement) {
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
            setType(extractType(element));
            setPackageName(ClassItemsLookup.extractPackageName(element));
            setSummary(ClassItemsLookup.extractSummary(element));
        }};
    }
}
