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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
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
        put(ElementKind.INTERFACE, "Interface");
        put(ElementKind.ANNOTATION_TYPE, "Annotation");
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

    void buildFilesForInnerClasses(String namePrefix, Element element, YmlFilesBuilder ymlFilesBuilder,
        List<TocItem> listToAddItems) {
        for (TypeElement classElement : ElementFilter.typesIn(element.getEnclosedElements())) {
            String classSimpleName = YmlFilesBuilderImpl.determineClassSimpleName(namePrefix, classElement);
            String classQName = String.valueOf(classElement.getQualifiedName());

            String classYmlFileName = classQName + ".yml";
            ymlFilesBuilder.buildClassYmlFile(classElement, outputPath + File.separator + classYmlFileName);

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
        String result = StringUtils.trimToEmpty(comment)
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
    public void buildClassYmlFile(TypeElement typeElement, String outputPath) {
        MetadataFile metadataFile = new MetadataFile();

        // TODO: Add implementation


        String content = String.valueOf(metadataFile);
        FileUtil.dumpToFile(content, outputPath);
    }

    public static String determineClassSimpleName(String namePrefix, Element classElement) {
        return String.format("%s%s%s",
            namePrefix,
            StringUtils.isEmpty(namePrefix) ? "" : ".",
            String.valueOf(classElement.getSimpleName()));
    }
}
