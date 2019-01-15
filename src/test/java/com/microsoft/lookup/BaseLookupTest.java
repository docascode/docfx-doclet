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
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTrees;
import java.util.Arrays;
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
    private BaseLookup baseLookup;

    @Before
    public void setup() {
        elements = rule.getElements();
        environment = Mockito.mock(DocletEnvironment.class);
        docTrees = Mockito.mock(DocTrees.class);
        docCommentTree = Mockito.mock(DocCommentTree.class);
        textTree = Mockito.mock(TextTree.class);
        linkTree = Mockito.mock(LinkTree.class);

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
        when(textTree.toString()).thenReturn("Some text 1");
        when(linkTree.toString()).thenReturn("Some text 2");

        String result = baseLookup.determineComment(element);

        verify(environment).getDocTrees();
        verify(docTrees).getDocCommentTree(element);
        verify(docCommentTree).getFullBody();
        verify(textTree).getKind();
        verify(linkTree).getKind();
        assertThat("Wrong result", result,
            is("Some text 1<xref uid=\"\" data-throw-if-not-resolved=\"false\">Some text 2</xref>"));
    }

    @Test
    public void replaceLinkWithXrefTag() {
        assertThat("Wrong result", baseLookup.replaceLinkWithXrefTag("{@link Class1#method10()}"),
            is("<xref uid=\"\" data-throw-if-not-resolved=\"false\">Class1#method10()</xref>"));
        assertThat("Wrong result for method with parameter",
            baseLookup.replaceLinkWithXrefTag("{@link Class2#method15(java.lang.String)}"),
            is("<xref uid=\"\" data-throw-if-not-resolved=\"false\">Class2#method15(java.lang.String)</xref>"));
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
}
