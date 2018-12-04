package com.microsoft.tmp;

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TypeParameter;
import com.microsoft.util.FileUtil;
import com.microsoft.util.StringUtil;
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
import org.apache.commons.lang3.StringUtils;

public class YmlFilesBuilderImpl implements YmlFilesBuilder {

    private Map<ElementKind, String> elementKindLookup = new HashMap<>() {{
        put(ElementKind.PACKAGE, "Namespace");  // TODO: 'Namespace' or 'Package'?
        put(ElementKind.CLASS, "Class");
        put(ElementKind.ENUM, "Enum");
        put(ElementKind.INTERFACE, "Interface");
        put(ElementKind.ANNOTATION_TYPE, "Annotation");
        put(ElementKind.METHOD, "Method");
    }};
    private Map<String, String> typeParamsLookup = new HashMap<>();
    private Random random = new Random();

    @Override
    public void buildPackageYmlFile(PackageElement packageElement, String outputPath) {
        MetadataFile packageMetadataFile = new MetadataFile();

        MetadataFileItem packageItem = new MetadataFileItem();
        String packageQName = String.valueOf(packageElement.getQualifiedName());
        packageItem.setUid(packageQName);
        packageItem.setId(String.valueOf(packageElement.getSimpleName()));
        addPackageChildren(packageQName, "", packageElement, packageItem.getChildren(),
            packageMetadataFile.getReferences());
        packageItem.setHref(packageQName + ".yml");
        packageItem.setName(packageQName);
        packageItem.setNameWithType(packageQName);
        packageItem.setFullName(packageQName);
        packageItem.setType(elementKindLookup.get(packageElement.getKind()));
        packageItem.setSummary("-=TBD=-");     // TODO: TBD
        packageItem.setContent("package " + packageQName);

        packageMetadataFile.getItems().add(packageItem);

        String content = String.valueOf(packageMetadataFile);
        FileUtil.dumpToFile(content, outputPath);
    }

    void addPackageChildren(String packageName, String namePrefix, Element element, List<String> packageChildren,
        List<MetadataFileItem> references) {
        for (TypeElement classElement : ElementFilter.typesIn(element.getEnclosedElements())) {
            String classQName = determineClassQName(namePrefix, classElement);
            String classSimpleName = determineClassSimpleName(namePrefix, classElement);
            MetadataFileItem reference = buildClassReference(packageName, classElement, classQName);

            packageChildren.add(classQName);
            references.add(reference);
            addPackageChildren(packageName, classSimpleName, classElement, packageChildren, references);
        }
    }

    MetadataFileItem buildClassReference(String packageName, TypeElement classElement, String classQName) {
        MetadataFileItem referenceItem = new MetadataFileItem();
        referenceItem.setUid(classQName);
        referenceItem.setParent(packageName);
        referenceItem.setHref(classQName + ".yml");
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        referenceItem.setName(classQNameWithGenericsSupport.replace(packageName + ".", ""));
        referenceItem.setNameWithType(classQNameWithGenericsSupport.replace(packageName + ".", ""));
        referenceItem.setFullName(classQNameWithGenericsSupport);
        referenceItem.setType(elementKindLookup.get(classElement.getKind()));
        referenceItem.setSummary("-=TBD=-");   // TODO: TBD
        referenceItem.setContent("-=TBD=-");   // TODO: TBD
        for (TypeParameterElement typeParameter : classElement.getTypeParameters()) {
            String key = String.valueOf(typeParameter);
            if (!typeParamsLookup.containsKey(key)) {
                typeParamsLookup.put(key, generateRandomHexString());
            }
            String value = typeParamsLookup.get(key);
            referenceItem.getTypeParameters().add(new TypeParameter(key, value));
        }
        return referenceItem;
    }

    private String generateRandomHexString() {
        return Integer.toHexString(random.nextInt());
    }

    @Override
    public void buildClassYmlFile(TypeElement typeElement, String outputPath) {
        // TODO: Add implementation
        String content = "";

        FileUtil.dumpToFile(content, outputPath);
    }

    public static String determineClassSimpleName(String namePrefix, Element classElement) {
        return String.format("%s%s%s",
            namePrefix,
            StringUtils.isEmpty(namePrefix) ? "" : ".",
            String.valueOf(classElement.getSimpleName()));
    }

    public static String determineClassQName(String namePrefix, TypeElement classElement) {
        String classQName = String.valueOf(classElement.getQualifiedName());
        String classSimpleName = determineClassSimpleName(namePrefix, classElement);

        return classQName.replace(classSimpleName,
            StringUtil.replaceUppercaseWithUnderscoreWithLowercase(classSimpleName));
    }
}
