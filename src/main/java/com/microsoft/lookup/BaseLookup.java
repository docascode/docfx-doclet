package com.microsoft.lookup;

import static com.sun.source.doctree.DocTree.Kind.LINK;
import static com.sun.source.doctree.DocTree.Kind.LINK_PLAIN;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.microsoft.model.TypeParameter;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.LinkTree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseLookup<T> {

    protected final Map<ElementKind, String> elementKindLookup = new HashMap<>() {{
        put(ElementKind.PACKAGE, "Namespace");
        put(ElementKind.CLASS, "Class");
        put(ElementKind.ENUM, "Enum");
        put(ElementKind.ENUM_CONSTANT, "Field");
        put(ElementKind.INTERFACE, "Interface");
        put(ElementKind.ANNOTATION_TYPE, "Interface");
        put(ElementKind.CONSTRUCTOR, "Constructor");
        put(ElementKind.METHOD, "Method");
        put(ElementKind.FIELD, "Field");
    }};

    protected Map<T, ExtendedMetadataFileItem> map = new HashMap<>();
    private final DocletEnvironment environment;
    private final Set<Kind> LINK_KINDS_SET = Set.of(LINK, LINK_PLAIN);

    protected BaseLookup(DocletEnvironment environment) {
        this.environment = environment;
    }

    protected ExtendedMetadataFileItem resolve(T key) {
        ExtendedMetadataFileItem value = map.get(key);
        if (value == null) {
            value = buildMetadataFileItem(key);
            map.put(key, value);
        }
        return value;
    }

    protected abstract ExtendedMetadataFileItem buildMetadataFileItem(T key);

    public String extractPackageName(T key) {
        return resolve(key).getPackageName();
    }

    public String extractFullName(T key) {
        return resolve(key).getFullName();
    }

    public String extractName(T key) {
        return resolve(key).getName();
    }

    public String extractHref(T key) {
        return resolve(key).getHref();
    }

    public String extractParent(T key) {
        return resolve(key).getParent();
    }

    public String extractId(T key) {
        return resolve(key).getId();
    }

    public String extractUid(T key) {
        return resolve(key).getUid();
    }

    public String extractNameWithType(T key) {
        return resolve(key).getNameWithType();
    }

    public String extractMethodContent(T key) {
        return resolve(key).getMethodContent();
    }

    public String extractFieldContent(T key) {
        return resolve(key).getFieldContent();
    }

    public String extractConstructorContent(T key) {
        return resolve(key).getConstructorContent();
    }

    public String extractOverload(T key) {
        return resolve(key).getOverload();
    }

    public List<MethodParameter> extractParameters(T key) {
        return resolve(key).getParameters();
    }

    public List<ExceptionItem> extractExceptions(T key) {
        return resolve(key).getExceptions();
    }

    public Return extractReturn(T key) {
        return resolve(key).getReturn();
    }

    public String extractSummary(T key) {
        return resolve(key).getSummary();
    }

    public String extractType(T key) {
        return resolve(key).getType();
    }

    public String extractContent(T key) {
        return resolve(key).getContent();
    }

    public List<TypeParameter> extractTypeParameters(T key) {
        return resolve(key).getTypeParameters();
    }

    public String extractSuperclass(T key) {
        return resolve(key).getSuperclassValue();
    }

    public String extractTocName(T key) {
        return resolve(key).getTocName();
    }

    public Set<MetadataFileItem> extractReferences(T key) {
        return resolve(key).getReferences();
    }

    protected String determineType(Element element) {
        return elementKindLookup.get(element.getKind());
    }

    protected String determinePackageName(Element element) {
        return String.valueOf(environment.getElementUtils().getPackageOf(element));
    }

    protected String determineComment(Element element) {
        return getDocCommentTree(element).map(docTree ->
            docTree.getFullBody().stream()
                .map(bodyItem -> {
                    if (LINK_KINDS_SET.contains(bodyItem.getKind())) {
                        return buildXrefTag((LinkTree) bodyItem);
                    }
                    return String.valueOf(bodyItem);
                }).collect(Collectors.joining())
        ).orElse(null);
    }

    private String buildXrefTag(LinkTree linkTree) {
        String signature = linkTree.getReference().getSignature();
        String label = linkTree.getLabel().stream().map(String::valueOf).collect(Collectors.joining(" "));
        if (StringUtils.isEmpty(label)) {
            label = signature;
        }
        return String.format("<xref uid=\"%s\" data-throw-if-not-resolved=\"false\">%s</xref>", signature, label);
    }

    protected Optional<DocCommentTree> getDocCommentTree(Element element) {
        return Optional.ofNullable(environment.getDocTrees().getDocCommentTree(element));
    }

    public String makeTypeShort(String value) {
        if (!value.contains(".")) {
            return value;
        }
        return Stream.of(StringUtils.split(value, "<"))
            .map(s -> RegExUtils.replaceAll(s, "\\b[a-z.]+\\.", ""))
            .collect(Collectors.joining("<"));
    }
}
