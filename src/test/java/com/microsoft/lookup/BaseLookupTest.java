package com.microsoft.lookup;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.testing.compile.CompilationRule;
import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTrees;
import java.util.Arrays;
import java.util.Collections;
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
    private BaseLookup baseLookup;

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

        baseLookup = new BaseLookup(environment) {
            @Override
            protected ExtendedMetadataFileItem buildMetadataFileItem(Object key) {
                return null;
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
            baseLookup.makeTypeShort("com.ms.pack.Custom.Type"), is("Custom.Type"));
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
}
