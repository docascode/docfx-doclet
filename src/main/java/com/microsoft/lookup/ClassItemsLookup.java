package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.ThrowsTree;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.StringUtils;

public class ClassItemsLookup extends BaseLookup<Element> {

    public ClassItemsLookup(DocletEnvironment environment) {
        super(environment);
    }

    @Override
    protected ExtendedMetadataFileItem buildMetadataFileItem(Element element) {
        String packageName = determinePackageName(element);
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String classQName = String.valueOf(classElement.getQualifiedName());
        String elementQName = String.valueOf(element);
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        String classSNameWithGenericsSupport = classQNameWithGenericsSupport.replace(packageName.concat("."), "");
        String uid = String.format("%s.%s", classQName, elementQName);

        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem(uid) {{
            setId(elementQName);
            setParent(classQName);
            setHref(classQName + ".yml");
            setName(elementQName);
            setType(determineType(element));
            setPackageName(packageName);
            setSummary(determineComment(element));
        }};

        String modifiers = element.getModifiers().stream().map(String::valueOf).collect(Collectors.joining(" "));
        if (element instanceof ExecutableElement) {
            ExecutableElement exeElement = (ExecutableElement) element;
            List<MethodParameter> parameters = extractParameters(exeElement);
            String paramsString = parameters.stream()
                .map(parameter -> String.format("%s %s", makeTypeShort(parameter.getType()), parameter.getId()))
                .collect(Collectors.joining(", "));
            String nameWithoutBrackets = elementQName.replaceAll("\\(.*\\)", "");
            String methodName = String.format("%s(%s)", nameWithoutBrackets, paramsString);

            result.setName(methodName);
            result.setMethodContent(String.format("%s %s %s", modifiers,
                makeTypeShort(String.valueOf(exeElement.getReturnType())), result.getName()));
            result.setConstructorContent(String.format("%s %s", modifiers, result.getName()));
            result.setParameters(parameters);
            result.setExceptions(extractExceptions(exeElement));
            result.setReturn(extractReturn(exeElement));
        }
        result.setNameWithType(String.format("%s.%s", classSNameWithGenericsSupport, result.getName()));
        result.setFullName(String.format("%s.%s", classQNameWithGenericsSupport, result.getName()));
        result.setOverload(convertFullNameToOverload(result.getFullName()));

        if (element instanceof VariableElement) {
            String type = makeTypeShort(String.valueOf(element.asType()));
            result.setFieldContent(String.format("%s %s %s", modifiers, type, elementQName));
            result.setReturn(extractReturn((VariableElement) element));
        }
        return result;
    }

    List<MethodParameter> extractParameters(ExecutableElement element) {
        return element.getParameters().stream().map(o -> {
            String paramName = String.valueOf(o.getSimpleName());
            String paramType = String.valueOf(o.asType());
            return new MethodParameter(paramName, paramType, extractParameterDescription(element, paramName));
        }).collect(Collectors.toList());
    }

    String extractParameterDescription(ExecutableElement method, String paramName) {
        return getDocCommentTree(method).map(docTree -> docTree.getBlockTags().stream()
            .filter(o -> o.getKind() == Kind.PARAM)
            .map(o -> (ParamTree) o)
            .filter(o -> paramName.equals(String.valueOf(o.getName())))
            .map(o -> replaceLinksWithXrefTags(o.getDescription()))
            .findFirst().orElse(null)
        ).orElse(null);
    }

    List<ExceptionItem> extractExceptions(ExecutableElement methodElement) {
        return methodElement.getThrownTypes().stream().map(o -> {
            String exceptionType = String.valueOf(o);
            return new ExceptionItem(exceptionType, extractExceptionDescription(methodElement));
        }).collect(Collectors.toList());
    }

    String extractExceptionDescription(ExecutableElement methodElement) {
        return getDocCommentTree(methodElement).map(docTree -> docTree.getBlockTags().stream()
            .filter(o -> o.getKind() == Kind.THROWS)
            .map(o -> (ThrowsTree) o)
            .map(o -> replaceLinksWithXrefTags(o.getDescription()))
            .findFirst().orElse(null)
        ).orElse(null);
    }

    Return extractReturn(ExecutableElement methodElement) {
        if (methodElement.getReturnType().getKind() == TypeKind.VOID) {
            return null;
        }
        return new Return(String.valueOf(methodElement.getReturnType()), extractReturnDescription(methodElement));
    }

    String extractReturnDescription(ExecutableElement methodElement) {
        return getDocCommentTree(methodElement).map(docTree -> docTree.getBlockTags().stream()
            .filter(o -> o.getKind() == Kind.RETURN)
            .map(o -> (ReturnTree)o)
            .map(o -> replaceLinksWithXrefTags(o.getDescription()))
            .findFirst().orElse(null)
        ).orElse(null);
    }

    Return extractReturn(VariableElement fieldElement) {
        return new Return(String.valueOf(fieldElement.asType()));
    }

    String convertFullNameToOverload(String fullName) {
        return fullName.replaceAll("\\(.*\\)", "*");
    }
}
