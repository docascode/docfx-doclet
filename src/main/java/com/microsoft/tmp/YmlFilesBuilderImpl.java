package com.microsoft.tmp;

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TocFile;
import com.microsoft.model.TocItem;
import com.microsoft.model.TypeParameter;
import com.microsoft.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.StringUtils;

public class YmlFilesBuilderImpl implements YmlFilesBuilder {

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
        TocFile resultTocFile = new TocFile();
        for (PackageElement packageElement : ElementFilter.packagesIn(environment.getIncludedElements())) {
            String packageQName = String.valueOf(packageElement.getQualifiedName());
            String packageYmlFileName = packageQName + ".yml";
            buildPackageYmlFile(packageElement, outputPath + File.separator + packageYmlFileName);

            TocItem packageTocItem = new TocItem.Builder()
                .setUid(packageQName)
                .setName(packageQName)
                .setHref(packageYmlFileName)
                .build();

            buildFilesForInnerClasses("", packageElement, this, packageTocItem.getItems());

            resultTocFile.getItems().add(packageTocItem);
        }
        FileUtil.dumpToFile(String.valueOf(resultTocFile), outputPath + File.separator + "toc.yml");
        return true;
    }

    void buildFilesForInnerClasses(String namePrefix, Element element,
        YmlFilesBuilder ymlFilesBuilder,
        List<TocItem> listToAddItems) {
        for (TypeElement classElement : ElementFilter.typesIn(element.getEnclosedElements())) {
            String classSimpleName = YmlFilesBuilderImpl.determineClassSimpleName(namePrefix, classElement);
            String classQName = String.valueOf(classElement.getQualifiedName());

            String classYmlFileName = classQName + ".yml";
            ymlFilesBuilder
                .buildClassYmlFile(classElement, outputPath + File.separator + classYmlFileName);

            TocItem classTocItem = new TocItem.Builder()
                .setUid(classQName)
                .setName(classSimpleName)
                .setHref(classYmlFileName)
                .build();
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
        item.setName(qName);
        item.setNameWithType(qName);
        item.setFullName(qName);
        item.setType(type);
        item.setSummary(summary);
        item.setContent("package " + qName);
        metadataFile.getItems().add(item);

        String content = String.valueOf(metadataFile);
        FileUtil.dumpToFile(content, outputPath);
    }

    void addPackageChildren(String packageName, String namePrefix, Element packageElement, List<String> packageChildren,
        List<MetadataFileItem> references) {
        for (TypeElement classElement : ElementFilter.typesIn(packageElement.getEnclosedElements())) {
            String qName = String.valueOf(classElement.getQualifiedName());
            String sName = determineClassSimpleName(namePrefix, classElement);

            MetadataFileItem reference = buildClassReference(packageName, classElement, qName);
            references.add(reference);

            packageChildren.add(qName);
            addPackageChildren(packageName, sName, classElement, packageChildren, references);
        }
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
        referenceItem.getTypeParameters().addAll(extractTypeParameters(classElement));
        return referenceItem;
    }

    String extractComment(Element element) {
        return cleanupComment(environment.getElementUtils().getDocComment(element));
    }

    String cleanupComment(String comment) {
        String result = StringUtils.trimToEmpty(comment);
        if (StringUtils.isEmpty(result)) {
            return "";
        }
        result = result
            .replace("\r\n", "\n")
            .replaceAll("\\n+", "</p><p>");

        return String.format("\"<p>%s</p>\"", result);
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
        classItem.setName(classSNameWithGenericsSupport);
        classItem.setNameWithType(classSNameWithGenericsSupport);
        classItem.setFullName(classQNameWithGenericsSupport);
        classItem.setType(type);
        classItem.setPackageName(packageName);
        classItem.setSummary(extractComment(classElement));
        classItem.setContent(classContentValue);
        classItem.setSuperclass(String.valueOf(classElement.getSuperclass()));
        classItem.getTypeParameters().addAll(extractTypeParameters(classElement));
        metadataFile.getItems().add(classItem);

        // Add constructors info
        for (ExecutableElement constructorElement : ElementFilter.constructorsIn(classElement.getEnclosedElements())) {
            MetadataFileItem constructorItem = new MetadataFileItem();
            String constructorQName = String.valueOf(constructorElement);
            constructorItem.setUid(String.format("%s.%s", classQName, constructorQName));
            constructorItem.setId(constructorQName);
            constructorItem.setParent(classQName);
            constructorItem.setHref(classQName + ".yml");
            constructorItem.setName(constructorQName);
            constructorItem.setNameWithType(classSNameWithGenericsSupport + "." + constructorQName);
            constructorItem.setFullName(classQNameWithGenericsSupport + "." + constructorQName);
            constructorItem.setOverload("-=TBD=-");      // TODO: TBD
            constructorItem.setType(elementKindLookup.get(constructorElement.getKind()));
            constructorItem.setPackageName(packageName);
            constructorItem.setSummary(extractComment(constructorElement));
            String constructorContentValue = String.format("%s %s",
                constructorElement.getModifiers().stream().map(String::valueOf).collect(Collectors.joining(" ")),
                constructorQName);
            constructorItem.setContent(constructorContentValue);
            constructorItem.getParameters().addAll(extractParameters(constructorElement));
            metadataFile.getItems().add(constructorItem);
        }

        // Add methods info
        for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            MetadataFileItem methodItem = new MetadataFileItem();
            String methodQName = String.valueOf(methodElement);
            methodItem.setUid(String.format("%s.%s", classQName, methodQName));
            methodItem.setId(methodQName);
            methodItem.setParent(classQName);
            methodItem.setHref(classQName + ".yml");
            methodItem.setName(methodQName);
            methodItem.setNameWithType(classSNameWithGenericsSupport + "." + methodQName);
            methodItem.setFullName(classQNameWithGenericsSupport + "." + methodQName);
            methodItem.setOverload("-=TBD=-");       // TODO: TBD
            methodItem.setType(elementKindLookup.get(methodElement.getKind()));
            methodItem.setPackageName(packageName);
            methodItem.setSummary(extractComment(methodElement));
            String methodContentValue = String.format("%s %s %s",
                methodElement.getModifiers().stream().map(String::valueOf).collect(Collectors.joining(" ")),
                methodElement.getReturnType(), methodQName);
            methodItem.setContent(methodContentValue);
            methodItem.getParameters().addAll(extractParameters(methodElement));
            methodItem.setReturnType(String.valueOf(methodElement.getReturnType()));
            methodItem.setReturnDescription("-=TBD=-");     // TODO: TBD
            metadataFile.getItems().add(methodItem);
        }

        // Add fields info
        for (VariableElement fieldElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            MetadataFileItem fieldItem = new MetadataFileItem();
            String fieldQName = String.valueOf(fieldElement);
            fieldItem.setUid(String.format("%s.%s", classQName, fieldQName));
            fieldItem.setId(fieldQName);
            fieldItem.setParent(classQName);
            fieldItem.setHref(classQName + ".yml");
            fieldItem.setName(fieldQName);
            fieldItem.setNameWithType(classSNameWithGenericsSupport + "." + fieldQName);
            fieldItem.setFullName(classQNameWithGenericsSupport + "." + fieldQName);
            fieldItem.setType(elementKindLookup.get(fieldElement.getKind()));
            fieldItem.setPackageName(packageName);
            fieldItem.setSummary(extractComment(fieldElement));
            String fieldContentValue = String.format("%s %s",
                fieldElement.getModifiers().stream().map(String::valueOf).collect(Collectors.joining(" ")),
                fieldQName);
            fieldItem.setContent(fieldContentValue);
            metadataFile.getItems().add(fieldItem);
        }

        String metadataFileContent = String.valueOf(metadataFile);
        FileUtil.dumpToFile(metadataFileContent, outputPath);
    }

    private List<TypeParameter> extractParameters(ExecutableElement element) {
        List<TypeParameter> result = new ArrayList<>();
        for (VariableElement parameter : element.getParameters()) {
            String id = String.valueOf(parameter.getSimpleName());
            String type = String.valueOf(parameter.asType());
            String description = "-=TBD=-";     // TODO: TBD
            result.add(new TypeParameter(id, type, description));
        }
        return result;
    }

    public static String determineClassSimpleName(String namePrefix, Element classElement) {
        return String.format("%s%s%s",
            namePrefix,
            StringUtils.isEmpty(namePrefix) ? "" : ".",
            String.valueOf(classElement.getSimpleName()));
    }
}
