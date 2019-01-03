package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.TypeParameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.StringUtils;

public class ClassLookup extends BaseLookup<TypeElement> {

    private static final String JAVA_LANG_OBJECT = "java.lang.Object";
    private Map<String, String> typeParamsLookup = new HashMap<>();

    public ClassLookup(DocletEnvironment environment) {
        super(environment);
    }

    @Override
    protected ExtendedMetadataFileItem buildMetadataFileItem(TypeElement classElement) {
        String packageName = determinePackageName(classElement);
        String classQName = String.valueOf(classElement.getQualifiedName());
        String classSName = String.valueOf(classElement.getSimpleName());
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        String classSNameWithGenericsSupport = classQNameWithGenericsSupport.replace(packageName.concat("."), "");

        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem();
        result.setUid(classQName);
        result.setId(classSName);
        result.setParent(packageName);
        result.setHref(classQName + ".yml");
        result.setName(classSNameWithGenericsSupport);
        result.setNameWithType(classSNameWithGenericsSupport);
        result.setFullName(classQNameWithGenericsSupport);
        result.setType(determineType(classElement));
        result.setPackageName(packageName);
        result.setSummary(determineComment(classElement));
        result.setContent(determineClassContent(classElement, classSNameWithGenericsSupport));
        result.setSuperclass(determineSuperclass(classElement));
        result.setTypeParameters(determineTypeParameters(classElement));
        result.setTocName(classQName.replace(packageName.concat("."), ""));

        return result;
    }

    String determineClassContent(TypeElement classElement, String shortNameWithGenericsSupport) {
        String type = elementKindLookup.get(classElement.getKind());
        return String.format("%s %s %s",
            classElement.getModifiers().stream().map(String::valueOf)
                .filter(modifier -> !("Interface".equals(type) && "abstract".equals(modifier)))
                .filter(modifier -> !("Enum".equals(type) && ("static".equals(modifier) || "final".equals(modifier))))
                .collect(Collectors.joining(" ")),
            StringUtils.lowerCase(type), shortNameWithGenericsSupport);
    }

    String determineSuperclass(TypeElement classElement) {
        if (classElement.getKind() == ElementKind.ENUM) {
            return JAVA_LANG_OBJECT;
        }
        TypeMirror superclass = classElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return JAVA_LANG_OBJECT;
        }
        return String.valueOf(superclass);
    }

    List<TypeParameter> determineTypeParameters(TypeElement element) {
        List<TypeParameter> result = new ArrayList<>();
        for (TypeParameterElement typeParameter : element.getTypeParameters()) {
            String key = String.valueOf(typeParameter);
            if (!typeParamsLookup.containsKey(key)) {
                typeParamsLookup.put(key, generateHexString(key));
            }
            String value = typeParamsLookup.get(key);
            result.add(new TypeParameter(key, value));
        }
        return result;
    }

    String generateHexString(String key) {
        return String.valueOf(key.hashCode());
    }
}
