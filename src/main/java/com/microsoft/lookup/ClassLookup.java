package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.TypeParameter;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ClassLookup extends BaseLookup<TypeElement> {

    private static final String JAVA_LANG_OBJECT = "java.lang.Object";

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
        String result = String.format("%s %s %s",
            classElement.getModifiers().stream().map(String::valueOf)
                .filter(modifier -> !("Interface".equals(type) && "abstract".equals(modifier)))
                .filter(modifier -> !("Enum".equals(type) && ("static".equals(modifier) || "final".equals(modifier))))
                .collect(Collectors.joining(" ")),
            StringUtils.lowerCase(type), shortNameWithGenericsSupport);

        String superclass = determineSuperclass(classElement);
        if (!JAVA_LANG_OBJECT.equals(superclass)) {
            result += " extends " + makeTypeShort(superclass);
        }

        List<? extends TypeMirror> interfaces = classElement.getInterfaces();
        if (CollectionUtils.isNotEmpty(interfaces)) {
            String prefix = (classElement.getKind() == ElementKind.INTERFACE) ? " extends " : " implements ";
            result += prefix + interfaces.stream().map(String::valueOf).map(this::makeTypeShort)
                .collect(Collectors.joining(", "));
        }

        return result;
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
        return element.getTypeParameters().stream().map(typeParameter -> {
            String key = String.valueOf(typeParameter);
            String value = generateHexString(key);
            return new TypeParameter(key, value);
        }).collect(Collectors.toList());
    }

    String generateHexString(String key) {
        return String.valueOf(key.hashCode());
    }
}
