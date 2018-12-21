package com.microsoft.lookup;

import static com.microsoft.util.ElementUtil.convertFullNameToOverload;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.microsoft.util.ElementUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class ClassItemsLookup {

    private static Map<Element, ExtendedMetadataFileItem> map = new HashMap<>();

    public static String extractPackageName(Element key) {
        return resolve(key).getPackageName();
    }

    public static String extractFullName(Element key) {
        return resolve(key).getFullName();
    }

    public static String extractName(Element key) {
        return resolve(key).getName();
    }

    public static String extractHref(Element key) {
        return resolve(key).getHref();
    }

    public static String extractParent(Element key) {
        return resolve(key).getParent();
    }

    public static String extractId(Element key) {
        return resolve(key).getId();
    }

    public static String extractUid(Element key) {
        return resolve(key).getUid();
    }

    public static String extractNameWithType(Element key) {
        return resolve(key).getNameWithType();
    }

    public static String extractMethodContent(Element key) {
        return resolve(key).getMethodContent();
    }

    public static String extractFieldContent(Element key) {
        return resolve(key).getFieldContent();
    }

    public static String extractConstructorContent(Element key) {
        return resolve(key).getConstructorContent();
    }

    public static String extractOverload(Element key) {
        return resolve(key).getOverload();
    }

    public static List<MethodParameter> extractParameters(Element key) {
        return resolve(key).getParameters();
    }

    public static List<ExceptionItem> extractExceptions(Element key) {
        return resolve(key).getExceptions();
    }

    public static Return extractReturn(Element key) {
        return resolve(key).getReturn();
    }

    public static String extractSummary(Element key) {
        return resolve(key).getSummary();
    }

    public static String extractType(Element key) {
        return resolve(key).getType();
    }

    private static ExtendedMetadataFileItem resolve(Element key) {
        ExtendedMetadataFileItem value = map.get(key);
        if (value == null) {
            value = buildMetadataFileItem(key);
            map.put(key, value);
        }
        return value;
    }

    private static ExtendedMetadataFileItem buildMetadataFileItem(Element element) {
        String packageName = ElementUtil.extractPackageName(element);
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String classQName = String.valueOf(classElement.getQualifiedName());
        String elementQName = String.valueOf(element);
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        String classSNameWithGenericsSupport = classQNameWithGenericsSupport.replace(packageName.concat("."), "");
        String uid = String.format("%s.%s", classQName, elementQName);
        String fullName = String.format("%s.%s", classQNameWithGenericsSupport, elementQName);
        String nameWithType = String.format("%s.%s", classSNameWithGenericsSupport, elementQName);

        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem() {{
            setUid(uid);
            setId(elementQName);
            setParent(classQName);
            setHref(classQName + ".yml");
            setName(elementQName);
            setNameWithType(nameWithType);
            setFullName(fullName);
            setType(ElementUtil.extractType(element));
            setPackageName(packageName);
            setSummary(ElementUtil.extractComment(element));
            setOverload(convertFullNameToOverload(fullName));
        }};

        String modifiers = element.getModifiers().stream().map(String::valueOf).collect(Collectors.joining(" "));
        if (element instanceof ExecutableElement) {
            result.setConstructorContent(String.format("%s %s", modifiers, elementQName));

            ExecutableElement exeElement = (ExecutableElement) element;
            result.setMethodContent(String.format("%s %s %s", modifiers, exeElement.getReturnType(), elementQName));
            result.setParameters(ElementUtil.extractParameters(exeElement));
            result.setExceptions(ElementUtil.extractExceptions(exeElement));
            result.setReturn(ElementUtil.extractReturn(exeElement));
        }
        if (element instanceof VariableElement) {
            result.setFieldContent(String.format("%s %s", modifiers, elementQName));
            result.setReturn(ElementUtil.extractReturn((VariableElement) element));
        }
        return result;
    }
}
