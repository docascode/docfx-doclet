package com.microsoft.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.TypeParameter;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTrees;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
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
public class ElementUtilTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private DocletEnvironment environment;
    private ElementUtil elementUtil;
    private DocTrees docTrees;
    private DocCommentTree docCommentTree;
    private TextTree textTree;
    private LinkTree linkTree;

    @Before
    public void setup() {
        elements = rule.getElements();
        environment = Mockito.mock(DocletEnvironment.class);
        docTrees = Mockito.mock(DocTrees.class);
        docCommentTree = Mockito.mock(DocCommentTree.class);
        textTree = Mockito.mock(TextTree.class);
        linkTree = Mockito.mock(LinkTree.class);
        elementUtil = new ElementUtil(environment, new String[]{}, new String[]{});
    }

    @Test
    public void extractTypeParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        List<TypeParameter> result = ElementUtil.extractTypeParameters(element);

        assertThat("Wrong type params size", result.size(), is(1));
        assertThat("Wrong type parameter id", result.get(0).getId(), is("T"));
        assertThat("Wrong type parameter type", result.get(0).getType(), is("84"));
    }

    @Test
    public void generateHexString() {
        assertThat("Wrong result for simple string", ElementUtil.generateHexString("T"), is("84"));
        assertThat("Wrong result for complex string", ElementUtil.generateHexString("? extends SomeClass"),
            is("-819114916"));
    }

    @Test
    public void extractSuperclass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        String result = ElementUtil.extractSuperclass(element);

        assertThat("Wrong result", result, is("java.lang.Object"));
    }

    @Test
    public void extractSuperclassForChildClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        String result = ElementUtil.extractSuperclass(element);

        assertThat("Wrong result", result, is("com.microsoft.samples.subpackage.Person"));
    }

    @Test
    public void convertFullNameToOverload() {
        assertThat("Wrong result", ElementUtil.convertFullNameToOverload(
            "com.microsoft.samples.SuperHero.successfullyAttacked(int,java.lang.String)"), is(
            "com.microsoft.samples.SuperHero.successfullyAttacked*"));

        assertThat("Wrong result for case with generics", ElementUtil.convertFullNameToOverload(
            "com.microsoft.samples.subpackage.Display<T,R>.show()"), is(
            "com.microsoft.samples.subpackage.Display<T,R>.show*"));

        assertThat("Wrong result for constructor case", ElementUtil.convertFullNameToOverload(
            "com.microsoft.samples.SuperHero.SuperHero()"), is(
            "com.microsoft.samples.SuperHero.SuperHero*"));
    }

    @Test
    public void extractPackageContent() {
        PackageElement element = elements.getPackageElement("com.microsoft.samples");

        String result = ElementUtil.extractPackageContent(element);

        assertThat("Wrong resulr", result, is("package com.microsoft.samples"));
    }

    @Test
    public void extractClassContent() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        String result = ElementUtil.extractClassContent(element, "SuperHero");

        assertThat("Wrong result", result, is("public class SuperHero"));
    }

    @Test
    public void extractClassContentForInterface() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

        String result = ElementUtil.extractClassContent(element, "Display<T, R>");

        assertThat("Wrong result", result, is("public interface Display<T, R>"));
    }

    @Test
    public void extractClassContentForEnum() {
        TypeElement element = elements
            .getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        String result = ElementUtil.extractClassContent(element, "Person.IdentificationInfo.Gender");

        assertThat("Wrong result", result, is("public enum Person.IdentificationInfo.Gender"));
    }

    @Test
    public void extractClassContentForStaticClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");

        String result = ElementUtil.extractClassContent(element, "Person.IdentificationInfo");

        assertThat("Wrong result", result, is("public static class Person.IdentificationInfo"));
    }

    @Test
    public void extractTypeForInterface() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

        assertThat(ElementUtil.extractType(element), is("Interface"));
    }

    @Test
    public void extractTypeForEnum() {
        TypeElement element = elements
            .getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        assertThat(ElementUtil.extractType(element), is("Enum"));
    }

    @Test
    public void extractTypeForClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");

        assertThat(ElementUtil.extractType(element), is("Class"));
    }

    @Test
    public void extractPackageElements() {
        Set<? extends Element> elementsSet = new HashSet<>() {{
            add(elements.getPackageElement("com.microsoft.samples"));
            add(elements.getTypeElement("com.microsoft.samples.SuperHero"));
            add(elements.getPackageElement("com.microsoft.samples.subpackage"));
        }};

        List<String> result = ElementUtil.extractPackageElements(elementsSet)
            .stream().map(String::valueOf).collect(Collectors.toList());

        assertThat("Wrong result list size", result.size(), is(2));
        assertThat("Unexpected first item", result.get(0), is("com.microsoft.samples"));
        assertThat("Unexpected second item", result.get(1), is("com.microsoft.samples.subpackage"));
    }

    @Test
    public void matchAnyPattern() {
        HashSet<Pattern> patterns = new HashSet<>(
            Arrays.asList(Pattern.compile("com\\.ms\\.Some.*"), Pattern.compile(".*UsualClass")));
        assertTrue(ElementUtil.matchAnyPattern(patterns, "com.ms.SomeStrangeClass"));
        assertTrue(ElementUtil.matchAnyPattern(patterns, "UsualClass"));
        assertFalse(ElementUtil.matchAnyPattern(patterns, "EngineFive"));
        assertFalse(ElementUtil.matchAnyPattern(patterns, "com.ms.Awesome"));
    }

    @Test
    public void extractComment() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");
        when(environment.getDocTrees()).thenReturn(docTrees);
        when(docTrees.getDocCommentTree(element)).thenReturn(docCommentTree);
        doReturn(Arrays.asList(textTree, linkTree)).when(docCommentTree).getFullBody();
        when(textTree.getKind()).thenReturn(Kind.TEXT);
        when(linkTree.getKind()).thenReturn(Kind.LINK);
        when(textTree.toString()).thenReturn("Some text 1");
        when(linkTree.toString()).thenReturn("Some text 2");

        String result = ElementUtil.extractComment(element);

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
        assertThat("Wrong result", ElementUtil.replaceLinkWithXrefTag("{@link Class1#method10()}"),
            is("<xref uid=\"\" data-throw-if-not-resolved=\"false\">Class1#method10()</xref>"));
        assertThat("Wrong result for method with parameter",
            ElementUtil.replaceLinkWithXrefTag("{@link Class2#method15(java.lang.String)}"),
            is("<xref uid=\"\" data-throw-if-not-resolved=\"false\">Class2#method15(java.lang.String)</xref>"));
    }
}
