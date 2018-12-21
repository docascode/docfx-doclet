package com.microsoft.lookup;

import static com.microsoft.util.ElementUtil.convertFullNameToOverload;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.util.ElementUtil;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class ClassItemsLookup extends BaseLookup<Element> {

    @Override
    protected ExtendedMetadataFileItem buildMetadataFileItem(Element element) {
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
