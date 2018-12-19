package com.microsoft.tmp;

import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.microsoft.model.TocItem;
import com.microsoft.model.TypeParameter;
import com.microsoft.util.FileUtil;
import com.microsoft.util.YamlUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.StringUtils;

public class YmlFilesBuilderImpl implements YmlFilesBuilder {

    private final static String TOC_FILE_HEADER = "### YamlMime:TableOfContent\n";
    private final static String METADATA_FILE_HEADER = "### YamlMime:ManagedReference\n";

    private DocletEnvironment environment;
    private String outputPath;

    private Map<ElementKind, String> elementKindLookup = new HashMap<>() {{
        put(ElementKind.PACKAGE, "Namespace");
        put(ElementKind.CLASS, "Class");
        put(ElementKind.ENUM, "Enum");
        put(ElementKind.ENUM_CONSTANT, "Enum constant");
        put(ElementKind.INTERFACE, "Interface");
        put(ElementKind.ANNOTATION_TYPE, "Annotation");
        put(ElementKind.CONSTRUCTOR, "Constructor");
        put(ElementKind.METHOD, "Method");
        put(ElementKind.FIELD, "Field");
    }};
    private Map<String, String> typeParamsLookup = new HashMap<>();
    private Random random = new Random(21);

    public YmlFilesBuilderImpl(DocletEnvironment environment, String outputPath) {
        this.environment = environment;
        this.outputPath = outputPath;
    }

    // Fot testing purposes
    YmlFilesBuilderImpl() {
    }

    public boolean build() {
        List<TocItem> tocItems = new ArrayList<>();
        Set<PackageElement> packageElements = new LinkedHashSet<>(
            ElementFilter.packagesIn(environment.getIncludedElements()));

        for (PackageElement packageElement : packageElements) {
            String packageQName = String.valueOf(packageElement.getQualifiedName());
            String packageYmlFileName = packageQName + ".yml";
            buildPackageYmlFile(packageElement, outputPath + File.separator + packageYmlFileName);

            TocItem packageTocItem = new TocItem(packageQName, packageQName, packageYmlFileName);
            buildFilesForInnerClasses("", packageElement, this, packageTocItem.getItems());
            tocItems.add(packageTocItem);
        }
        String fileContent = TOC_FILE_HEADER + YamlUtil.objectToYamlString(tocItems);
        FileUtil.dumpToFile(fileContent, outputPath + File.separator + "toc.yml");
        return true;
    }

    void buildFilesForInnerClasses(String namePrefix, Element element,
        YmlFilesBuilder ymlFilesBuilder,
        List<TocItem> listToAddItems) {
        for (TypeElement classElement : extractSortedElements(element)) {
            String classSimpleName = determineClassSimpleName(namePrefix, classElement);
            String classQName = String.valueOf(classElement.getQualifiedName());

            String classYmlFileName = classQName + ".yml";
            ymlFilesBuilder.buildClassYmlFile(classElement, outputPath + File.separator + classYmlFileName);

            TocItem classTocItem = new TocItem(classQName, classSimpleName, classYmlFileName);
            listToAddItems.add(classTocItem);

            buildFilesForInnerClasses(classSimpleName, classElement, ymlFilesBuilder, listToAddItems);
        }
    }

    @Override
    public void buildPackageYmlFile(PackageElement element, String outputPath) {
        MetadataFile metadataFile = new MetadataFile();

        String qName = String.valueOf(element.getQualifiedName());
        String sName = String.valueOf(element.getSimpleName());
        String type = elementKindLookup.get(element.getKind());
        String summary = extractComment(element);

        MetadataFileItem item = new MetadataFileItem();
        item.setUid(qName);
        item.setId(sName);
        addPackageChildren(qName, "", element, item.getChildren(), metadataFile.getReferences());
        item.setHref(qName + ".yml");
        item.setLangs(new String[]{"java"});
        item.setName(qName);
        item.setNameWithType(qName);
        item.setFullName(qName);
        item.setType(type);
        item.setSummary(summary);
        item.setContent("package " + qName);
        metadataFile.getItems().add(item);

        String fileContent = METADATA_FILE_HEADER + YamlUtil.objectToYamlString(metadataFile);
        FileUtil.dumpToFile(fileContent, outputPath);
    }

    void addPackageChildren(String packageName, String namePrefix, Element packageElement, List<String> packageChildren,
        List<MetadataFileItem> references) {
        for (TypeElement classElement : extractSortedElements(packageElement)) {
            String qName = String.valueOf(classElement.getQualifiedName());
            String sName = determineClassSimpleName(namePrefix, classElement);

            MetadataFileItem reference = buildClassReference(packageName, classElement, qName);
            references.add(reference);

            packageChildren.add(qName);
            addPackageChildren(packageName, sName, classElement, packageChildren, references);
        }
    }

    List<TypeElement> extractSortedElements(Element element) {
        // Need to apply sorting, because order of result items for PackageElement.getEnclosedElements() depend on JDK implementation
        List<TypeElement> elements = ElementFilter.typesIn(element.getEnclosedElements());
        elements.sort((o1, o2) ->
            StringUtils.compare(String.valueOf(o1.getSimpleName()), String.valueOf(o2.getSimpleName()))
        );
        return elements;
    }

    MetadataFileItem buildClassReference(String packageName, TypeElement classElement, String qName) {
        String qNameWithGenericsSupport = String.valueOf(classElement.asType());
        String shortNameWithGenericsSupport = qNameWithGenericsSupport.replace(packageName + ".", "");
        String type = elementKindLookup.get(classElement.getKind());
        String summary = extractComment(classElement);
        String content = String.format("public %s %s", type.toLowerCase(), shortNameWithGenericsSupport);

        MetadataFileItem referenceItem = new MetadataFileItem();
        referenceItem.setUid(qName);
        referenceItem.setParent(packageName);
        referenceItem.setHref(qName + ".yml");
        referenceItem.setName(shortNameWithGenericsSupport);
        referenceItem.setNameWithType(shortNameWithGenericsSupport);
        referenceItem.setFullName(qNameWithGenericsSupport);
        referenceItem.setType(type);
        referenceItem.setSummary(summary);
        referenceItem.setContent(content);
        referenceItem.setTypeParameters(extractTypeParameters(classElement));
        return referenceItem;
    }

    String extractComment(Element element) {
        return environment.getElementUtils().getDocComment(element);
    }

    List<TypeParameter> extractTypeParameters(TypeElement element) {
        List<TypeParameter> result = new ArrayList<>();
        for (TypeParameterElement typeParameter : element.getTypeParameters()) {
            String key = String.valueOf(typeParameter);
            if (!typeParamsLookup.containsKey(key)) {
                typeParamsLookup.put(key, generateRandomHexString());
            }
            String value = typeParamsLookup.get(key);
            result.add(new TypeParameter(key, value));
        }
        return result;
    }

    private String generateRandomHexString() {
        return Integer.toHexString(random.nextInt());
    }

    @Override
    public void buildClassYmlFile(TypeElement classElement, String outputPath) {
        MetadataFile metadataFile = new MetadataFile();

        String packageName = String.valueOf(environment.getElementUtils().getPackageOf(classElement));
        String classQName = String.valueOf(classElement.getQualifiedName());
        String classSName = String.valueOf(classElement.getSimpleName());
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        String classSNameWithGenericsSupport = classQNameWithGenericsSupport.replace(packageName + ".", "");
        String type = elementKindLookup.get(classElement.getKind());
        String classContentValue = String.format("public %s %s", type.toLowerCase(), classSNameWithGenericsSupport);

        // Add class info
        MetadataFileItem classItem = new MetadataFileItem();
        classItem.setUid(classQName);
        classItem.setId(classSName);
        classItem.setParent(packageName);
        for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            classItem.getChildren().add(String.valueOf(methodElement));
        }
        classItem.setHref(classQName + ".yml");
        classItem.setLangs(new String[]{"java"});
        classItem.setName(classSNameWithGenericsSupport);
        classItem.setNameWithType(classSNameWithGenericsSupport);
        classItem.setFullName(classQNameWithGenericsSupport);
        classItem.setType(type);
        classItem.setPackageName(packageName);
        classItem.setSummary(extractComment(classElement));
        classItem.setContent(classContentValue);
        classItem.setSuperclass(extractSuperclass(classElement));
        classItem.setTypeParameters(extractTypeParameters(classElement));
        metadataFile.getItems().add(classItem);

        // Add constructors info
        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            MetadataFileItem constructorItem = buildMetadataFileItem(classQName, classQNameWithGenericsSupport, constructorElement, packageName);
            String constructorQName = String.valueOf(constructorElement);
            String fullName = String.format("%s.%s", classQNameWithGenericsSupport, constructorQName);

            constructorItem.setNameWithType(classSNameWithGenericsSupport + "." + constructorQName);
            constructorItem.setOverload(convertFullNameToOverload(fullName));
            String constructorContentValue = String.format("%s %s",
                constructorElement.getModifiers().stream().map(String::valueOf).collect(Collectors.joining(" ")),
                constructorQName);
            constructorItem.setContent(constructorContentValue);
            constructorItem.setParameters(extractParameters(constructorElement));
            metadataFile.getItems().add(constructorItem);
        }

        // Add methods info
        for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            MetadataFileItem methodItem = buildMetadataFileItem(classQName, classQNameWithGenericsSupport, methodElement, packageName);
            String methodQName = String.valueOf(methodElement);
            String fullName = String.format("%s.%s", classQNameWithGenericsSupport, methodQName);

            methodItem.setNameWithType(classSNameWithGenericsSupport + "." + methodQName);
            methodItem.setOverload(convertFullNameToOverload(fullName));
            String methodContentValue = String.format("%s %s %s",
                methodElement.getModifiers().stream().map(String::valueOf).collect(Collectors.joining(" ")),
                methodElement.getReturnType(), methodQName);
            methodItem.setContent(methodContentValue);
            methodItem.setExceptions(extractExceptions(methodElement));
            methodItem.setParameters(extractParameters(methodElement));
            methodItem.setReturn(new Return(String.valueOf(methodElement.getReturnType()), "-=TBD=-"));     // TODO: TBD
            metadataFile.getItems().add(methodItem);
        }

        // Add fields info
        for (VariableElement fieldElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            MetadataFileItem fieldItem = buildMetadataFileItem(classQName, classQNameWithGenericsSupport, fieldElement, packageName);
            String fieldQName = String.valueOf(fieldElement);

            fieldItem.setNameWithType(classSNameWithGenericsSupport + "." + fieldQName);
            String fieldContentValue = String.format("%s %s",
                fieldElement.getModifiers().stream().map(String::valueOf).collect(Collectors.joining(" ")),
                fieldQName);
            fieldItem.setContent(fieldContentValue);
            fieldItem.setReturn(new Return(String.valueOf(fieldElement.asType())));
            metadataFile.getItems().add(fieldItem);
        }

        String fileContent = METADATA_FILE_HEADER + YamlUtil.objectToYamlString(metadataFile);
        FileUtil.dumpToFile(fileContent, outputPath);
    }

    MetadataFileItem buildMetadataFileItem(String classQName, String classQNameWithGenericsSupport,
        Element element, String packageName) {
        MetadataFileItem metadataFileItem = new MetadataFileItem();
        String elementQName = String.valueOf(element);
        String fullName = String.format("%s.%s", classQNameWithGenericsSupport, elementQName);

        metadataFileItem.setUid(String.format("%s.%s", classQName, elementQName));
        metadataFileItem.setId(elementQName);
        metadataFileItem.setParent(classQName);
        metadataFileItem.setHref(classQName + ".yml");
        metadataFileItem.setLangs(new String[]{"java"});
        metadataFileItem.setName(elementQName);
        metadataFileItem.setFullName(fullName);
        metadataFileItem.setType(elementKindLookup.get(element.getKind()));
        metadataFileItem.setPackageName(packageName);
        metadataFileItem.setSummary(extractComment(element));

        return metadataFileItem;
    }

    String convertFullNameToOverload(String fullName) {
        return fullName.replaceAll("\\(.*", "*");
    }

    String extractSuperclass(TypeElement classElement) {
        TypeMirror superclass = classElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return "java.lang.Object";
        }
        return String.valueOf(superclass);
    }

    List<ExceptionItem> extractExceptions(ExecutableElement methodElement) {
        return methodElement.getThrownTypes().stream()
            .map(o -> new ExceptionItem(String.valueOf(o), "-=TBD=-"))    // TODO: TBD
            .collect(Collectors.toList());
    }

    List<MethodParameter> extractParameters(ExecutableElement element) {
        return element.getParameters().stream().map(o -> new MethodParameter(
            String.valueOf(o.getSimpleName()),
            String.valueOf(o.asType()),
            "-=TBD=-"               // TODO: TBD
        )).collect(Collectors.toList());
    }

    String determineClassSimpleName(String namePrefix, Element classElement) {
        return String.format("%s%s%s",
            namePrefix,
            StringUtils.isEmpty(namePrefix) ? "" : ".",
            String.valueOf(classElement.getSimpleName()));
    }
}
