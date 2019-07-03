package com.microsoft.lookup;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.testing.compile.CompilationRule;
import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.microsoft.model.TypeParameter;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTrees;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import jdk.javadoc.doclet.DocletEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseLookupTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private DocletEnvironment environment;
    private DocTrees docTrees;
    private DocCommentTree docCommentTree;
    private TextTree textTree;
    private LinkTree linkTree;
    private ReferenceTree referenceTree;
    private LiteralTree literalTree;
    private BaseLookup<Element> baseLookup;
    private ExtendedMetadataFileItem lastBuiltItem;

    @Before
    public void setup() {
        elements = rule.getElements();
        environment = Mockito.mock(DocletEnvironment.class);
        docTrees = Mockito.mock(DocTrees.class);
        docCommentTree = Mockito.mock(DocCommentTree.class);
        textTree = Mockito.mock(TextTree.class);
        linkTree = Mockito.mock(LinkTree.class);
        referenceTree = Mockito.mock(ReferenceTree.class);
        literalTree = Mockito.mock(LiteralTree.class);

        baseLookup = new BaseLookup<>(environment) {
            @Override
            protected ExtendedMetadataFileItem buildMetadataFileItem(Element element) {
                lastBuiltItem = buildExtendedMetadataFileItem(element);
                return lastBuiltItem;
            }
        };
    }

    @Test
    public void determineComment() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");
        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(element)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(textTree, linkTree)).when(docCommentTree).getFullBody();
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        when(linkTree.getKind()).thenReturn(Kind.LINK);
        when(linkTree.getReference()).thenReturn(referenceTree);
        when(referenceTree.getSignature()).thenReturn("Some#signature");
        when(textTree.toString()).thenReturn("Some text 1");

        String result = baseLookup.determineComment(element);

        verify(environment).getDocTrees();
        verify(docTrees).getDocCommentTree(element);
        verify(docCommentTree).getFullBody();
        verify(textTree).getKind();
        verify(linkTree).getKind();
        verify(linkTree).getReference();
        verify(linkTree).getLabel();
        assertThat("Wrong result", result,
            is("Some text 1<xref uid=\"Some#signature\" data-throw-if-not-resolved=\"false\">Some#signature</xref>"));
    }

    @Test
    public void makeTypeShort() {
        assertThat("Wrong result for primitive type", baseLookup.makeTypeShort("int"), is("int"));
        assertThat("Wrong result", baseLookup.makeTypeShort("java.lang.String"), is("String"));
        assertThat("Wrong result for inner class",
            baseLookup.makeTypeShort("org.apache.commons.lang3.arch.Processor.Arch"), is("Processor.Arch"));
        assertThat("Wrong result for class with generic",
            baseLookup.makeTypeShort("java.util.List<java.lang.String>"), is("List<String>"));
        assertThat("Wrong result for inner class with generic",
            baseLookup.makeTypeShort("java.util.List.Custom<java.lang.Some.String>"), is("List.Custom<Some.String>"));
        assertThat("Wrong result for inner class with complex generic",
            baseLookup.makeTypeShort("a.b.c.D.E.G<m.n.A.B<c.d.D.G<a.F.Z>>>"), is("D.E.G<A.B<D.G<F.Z>>>"));
        assertThat("Wrong result for inner class with generic & inheritance",
            baseLookup.makeTypeShort("a.b.G<? extends a.b.List>"), is("G<? extends List>"));
    }

    @Test
    public void buildXrefTag() {
        when(linkTree.getReference()).thenReturn(referenceTree);
        when(referenceTree.getSignature()).thenReturn("Some#signature");
        when(linkTree.getLabel()).thenReturn(Collections.emptyList());

        String result = baseLookup.buildXrefTag(linkTree);

        assertThat("Wrong result", result,
            is("<xref uid=\"Some#signature\" data-throw-if-not-resolved=\"false\">Some#signature</xref>"));
    }

    @Test
    public void buildXrefTagWhenLabelPresents() {
        when(linkTree.getReference()).thenReturn(referenceTree);
        when(referenceTree.getSignature()).thenReturn("Some#signature");
        doReturn(Arrays.asList(textTree)).when(linkTree).getLabel();
        String labelValue = "IamLabel";
        when(textTree.toString()).thenReturn(labelValue);

        String result = baseLookup.buildXrefTag(linkTree);

        assertThat("Wrong result", result,
            is("<xref uid=\"Some#signature\" data-throw-if-not-resolved=\"false\">" + labelValue + "</xref>"));
    }

    @Test
    public void buildCodeTag() {
        String tagContent = "Some text";
        when(literalTree.getBody()).thenReturn(textTree);
        when(textTree.toString()).thenReturn(tagContent);

        String result = baseLookup.buildCodeTag(literalTree);

        assertThat("Wrong result", result, is("<code>" + tagContent + "</code>"));
    }

    @Test
    public void expandLiteralBody() {
        String tagContent = "Some text";
        when(literalTree.getBody()).thenReturn(textTree);
        when(textTree.toString()).thenReturn(tagContent);

        String result = baseLookup.expandLiteralBody(literalTree);

        assertThat("Wrong result", result, is(tagContent));
    }

    @Test
    public void replaceLinksAndCodes() {
        when(linkTree.getReference()).thenReturn(referenceTree);
        when(referenceTree.getSignature()).thenReturn("Some#signature");
        when(linkTree.getLabel()).thenReturn(Collections.emptyList());
        String textTreeContent = "Some text content";
        when(literalTree.getBody()).thenReturn(textTree);
        when(textTree.toString()).thenReturn(textTreeContent);
        when(linkTree.getKind()).thenReturn(Kind.LINK);
        when(literalTree.getKind()).thenReturn(Kind.CODE);
        when(textTree.getKind()).thenReturn(Kind.TEXT);

        String result = baseLookup.replaceLinksAndCodes(Arrays.asList(linkTree, literalTree, textTree));

        assertThat("Wrong result", result, is("<xref uid=\"Some#signature\" data-throw-if-not-resolved=\"false\">"
            + "Some#signature</xref><code>Some text content</code>" + textTreeContent));
    }

    @Test
    public void resolve() {
        TypeElement element1 = elements.getTypeElement("com.microsoft.samples.subpackage.Person");
        TypeElement element2 = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

        ExtendedMetadataFileItem resultForKey1 = baseLookup.resolve(element1);
        ExtendedMetadataFileItem resultForKey2 = baseLookup.resolve(element2);
        ExtendedMetadataFileItem consequenceCallResultForKey1 = baseLookup.resolve(element1);

        assertThat("Consequence call should return same instance", resultForKey1, is(consequenceCallResultForKey1));
        assertThat("Resolve for another key should return another instance", resultForKey2, not((resultForKey1)));
    }

    @Test
    public void testExtractMethods() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        assertThat("Wrong packageName", baseLookup.extractPackageName(element), is(lastBuiltItem.getPackageName()));
        assertThat("Wrong fullName", baseLookup.extractFullName(element), is(lastBuiltItem.getFullName()));
        assertThat("Wrong name", baseLookup.extractName(element), is(lastBuiltItem.getName()));
        assertThat("Wrong href", baseLookup.extractHref(element), is(lastBuiltItem.getHref()));
        assertThat("Wrong parent", baseLookup.extractParent(element), is(lastBuiltItem.getParent()));
        assertThat("Wrong id", baseLookup.extractId(element), is(lastBuiltItem.getId()));
        assertThat("Wrong uid", baseLookup.extractUid(element), is(lastBuiltItem.getUid()));
        assertThat("Wrong nameWithType", baseLookup.extractNameWithType(element), is(lastBuiltItem.getNameWithType()));
        assertThat("Wrong methodContent", baseLookup.extractMethodContent(element),
            is(lastBuiltItem.getMethodContent()));
        assertThat("Wrong fieldContent", baseLookup.extractFieldContent(element), is(lastBuiltItem.getFieldContent()));
        assertThat("Wrong constructorContent", baseLookup.extractConstructorContent(element),
            is(lastBuiltItem.getConstructorContent()));
        assertThat("Wrong overload", baseLookup.extractOverload(element), is(lastBuiltItem.getOverload()));
        assertThat("Wrong parameters", baseLookup.extractParameters(element), is(lastBuiltItem.getParameters()));
        assertThat("Wrong exceptions", baseLookup.extractExceptions(element), is(lastBuiltItem.getExceptions()));

        assertThat("Wrong return", baseLookup.extractReturn(element).getReturnType(),
            is(lastBuiltItem.getReturn().getReturnType()));
        assertThat("Wrong return", baseLookup.extractReturn(element).getReturnDescription(),
            is(lastBuiltItem.getReturn().getReturnDescription()));

        assertThat("Wrong summary", baseLookup.extractSummary(element), is(lastBuiltItem.getSummary()));
        assertThat("Wrong type", baseLookup.extractType(element), is(lastBuiltItem.getType()));
        assertThat("Wrong content", baseLookup.extractContent(element), is(lastBuiltItem.getContent()));
        assertThat("Wrong typeParameters", baseLookup.extractTypeParameters(element),
            is(lastBuiltItem.getTypeParameters()));
        assertThat("Wrong superclass", baseLookup.extractSuperclass(element), is(lastBuiltItem.getSuperclass()));
        assertThat("Wrong interfaces", baseLookup.extractInterfaces(element), is(lastBuiltItem.getInterfaces()));
        assertThat("Wrong tocName", baseLookup.extractTocName(element), is(lastBuiltItem.getTocName()));
        assertThat("Wrong references", baseLookup.extractReferences(element), is(lastBuiltItem.getReferences()));
    }

    private ExtendedMetadataFileItem buildExtendedMetadataFileItem(Element element) {
        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem(String.valueOf(element));
        result.setPackageName("Some package name");
        result.setFullName("Some full name");
        result.setName("Some name");
        result.setHref("Some href");
        result.setParent("Some parent");
        result.setId("Some id");
        result.setNameWithType("Some name with type");
        result.setMethodContent("Some method content");
        result.setFieldContent("Some field content");
        result.setConstructorContent("Some constructor content");
        result.setOverload("Some overload");
        result.setParameters(Arrays.asList(new MethodParameter("method id", "method type", "method desc")));
        result.setExceptions(Arrays.asList(new ExceptionItem("ex type", "ex desc")));
        result.setReturn(new Return("return type", "return desc"));
        result.setSummary("Some summary");
        result.setType("Some type");
        result.setContent("Some content");
        result.setTypeParameters(Arrays.asList(new TypeParameter("type param id")));
        result.setSuperclass(Arrays.asList("Some "));
        result.setInterfaces(Arrays.asList("Some interface"));
        result.setTocName("Some toc name");
        result.addReferences(Set.of(new MetadataFileItem("ref uid")));
        return result;
    }
}
