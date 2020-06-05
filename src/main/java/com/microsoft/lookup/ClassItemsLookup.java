package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;

import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;

import com.microsoft.util.CommentHelper;
import com.microsoft.util.Utils;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.ThrowsTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;

import jdk.javadoc.doclet.DocletEnvironment;

public class ClassItemsLookup extends BaseLookup<Element> {
    private Utils utils;

    public ClassItemsLookup(DocletEnvironment environment) {
        super(environment);
        utils = new Utils(environment);
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
            if (exeElement.getKind() == ElementKind.METHOD) {
                result.setOverridden(extractOverriddenUid(utils.overriddenMethod(exeElement)));
                result.setSummary(getInheritedInlineCommentString(exeElement));
            }
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
                .map(o -> replaceLinksAndCodes(o.getDescription()))
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
                .map(o -> replaceLinksAndCodes(o.getDescription()))
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
                .map(o -> (ReturnTree) o)
                .map(o -> replaceLinksAndCodes(o.getDescription()))
                .findFirst().orElse(null)
        ).orElse(null);
    }

    Return extractReturn(VariableElement fieldElement) {
        return new Return(String.valueOf(fieldElement.asType()));
    }

    String convertFullNameToOverload(String fullName) {
        return fullName.replaceAll("\\(.*\\)", "*");
    }

    String extractOverriddenUid(ExecutableElement ovr) {
        if (ovr != null) {
            TypeElement te = utils.getEnclosingTypeElement(ovr);
            String uid = te.getQualifiedName().toString().concat(".") + String.valueOf(ovr);
            return uid;
        }

        return "";
    }

    /**
     * If the item being inherited from is declared from external compiled package,
     * or is declared in the packages like java.lang.Object,
     * comments may be not available as doclet resolves from byte code.
     */
    String getInheritedInlineCommentString(ExecutableElement exeElement) {
        CommentHelper ch = getInheritedInlineTags(new CommentHelper(exeElement, utils));
        // Remove unresolved "@inheritDoc" tag.
        List<? extends DocTree> dctree = utils.removeBlockTag(ch.inlineTags, DocTree.Kind.INHERIT_DOC);
        return replaceLinksAndCodes(dctree);
    }

    CommentHelper getInheritedInlineTags(CommentHelper input) {
        CommentHelper output = input.copy();
        if (!output.hasInheritDocTag()&& !output.isSimpleOverride()) {
            return output;
        }

        CommentHelper inheritedSearchInput = input.copy();
        ExecutableElement overriddenMethod = utils.overriddenMethod((ExecutableElement) input.element);

        if (overriddenMethod != null) {
            inheritedSearchInput.element = overriddenMethod;
            CommentHelper ch = getInheritedInlineTags(inheritedSearchInput);
            if (!ch.isSimpleOverride()) {
                output = output.inherit(ch);
            }
        }

        TypeElement encl = utils.getEnclosingTypeElement(input.element);
        List<Element> implementedMethods = utils.getImplementedMethods(input.element.toString(), encl, new ArrayList<Element>());
        for (Element implementedMethod : implementedMethods) {
            inheritedSearchInput.element = implementedMethod;
            CommentHelper ch = getInheritedInlineTags(inheritedSearchInput);
            if (!ch.isSimpleOverride()) {
                output = output.inherit(ch);
            }
        }

        return output;
    }
}
