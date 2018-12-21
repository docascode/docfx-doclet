package com.microsoft.lookup;

import com.microsoft.model.TypeParameter;
import com.microsoft.util.ElementUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;

public class ClassLookup {

    private static Map<TypeElement, ExtendedMetadataFileItem> map = new HashMap<>();

    public static String extractUid(TypeElement key) {
        return resolve(key).getUid();
    }

    public static String extractId(TypeElement key) {
        return resolve(key).getId();
    }

    public static String extractParent(TypeElement key) {
        return resolve(key).getParent();
    }

    public static String extractHref(TypeElement key) {
        return resolve(key).getHref();
    }

    public static String extractName(TypeElement key) {
        return resolve(key).getName();
    }

    public static String extractNameWithType(TypeElement key) {
        return resolve(key).getNameWithType();
    }

    public static String extractFullName(TypeElement key) {
        return resolve(key).getFullName();
    }

    public static String extractType(TypeElement key) {
        return resolve(key).getType();
    }

    public static String extractSummary(TypeElement key) {
        return resolve(key).getSummary();
    }

    public static String extractContent(TypeElement key) {
        return resolve(key).getContent();
    }

    public static List<TypeParameter> extractTypeParameters(TypeElement key) {
        return resolve(key).getTypeParameters();
    }

    public static String extractPackageName(TypeElement key) {
        return resolve(key).getPackageName();
    }

    public static String extractSuperclass(TypeElement key) {
        return resolve(key).getSuperclassValue();
    }

    private static ExtendedMetadataFileItem resolve(TypeElement key) {
        ExtendedMetadataFileItem value = map.get(key);
        if (value == null) {
            value = buildMetadataFileItem(key);
            map.put(key, value);
        }
        return value;
    }

    private static ExtendedMetadataFileItem buildMetadataFileItem(TypeElement classElement) {
        String packageName = ElementUtil.extractPackageName(classElement);
        String classQName = String.valueOf(classElement.getQualifiedName());
        String classSName = String.valueOf(classElement.getSimpleName());
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        String classSNameWithGenericsSupport = classQNameWithGenericsSupport.replace(packageName + ".", "");

        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem();
        result.setUid(classQName);
        result.setId(classSName);
        result.setParent(packageName);
        result.setHref(classQName + ".yml");
        result.setName(classSNameWithGenericsSupport);
        result.setNameWithType(classSNameWithGenericsSupport);
        result.setFullName(classQNameWithGenericsSupport);
        result.setType(ElementUtil.extractType(classElement));
        result.setPackageName(packageName);
        result.setSummary(ElementUtil.extractComment(classElement));
        result.setContent(ElementUtil.extractClassContent(classElement, classSNameWithGenericsSupport));
        result.setSuperclass(ElementUtil.extractSuperclass(classElement));
        result.setTypeParameters(ElementUtil.extractTypeParameters(classElement));

        return result;
    }
}
