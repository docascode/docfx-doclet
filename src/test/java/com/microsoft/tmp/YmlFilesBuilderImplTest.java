package com.microsoft.tmp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.TypeParameter;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class YmlFilesBuilderImplTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private YmlFilesBuilderImpl ymlFilesBuilder;

    @Before
    public void setup() {
        elements = rule.getElements();
        ymlFilesBuilder = new YmlFilesBuilderImpl();
    }

    @Test
    public void cleanupComment() {
        String result = ymlFilesBuilder.cleanupComment("Some one-line comment\n");

        assertThat("Wrong result", result, is("\"<p>Some one-line comment</p>\""));
    }

    @Test
    public void cleanupCommentForMultilineCase() {
        String result = ymlFilesBuilder.cleanupComment("Some multiline\n\n comment\n");

        assertThat("Wrong result", result, is("\"<p>Some multiline</p><p> comment</p>\""));
    }

    @Test
    public void cleanupCommentForEmptyCase() {
        String result = ymlFilesBuilder.cleanupComment("");

        assertThat("Wrong result", result, is(""));
    }

    @Test
    public void extractTypeParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        List<TypeParameter> result = ymlFilesBuilder.extractTypeParameters(element);

        assertThat("Wrong type params size", result.size(), is(1));
        assertThat("Wrong type parameter id", result.get(0).getId(), is("T"));
        assertThat("Wrong type parameter type", result.get(0).getType(), is("bb61488d"));
    }

    @Test
    public void extractSuperclass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        String result = ymlFilesBuilder.extractSuperclass(element);

        assertThat("Wrong result", result, is("java.lang.Object"));
    }

    @Test
    public void extractSuperclassForChildClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        String result = ymlFilesBuilder.extractSuperclass(element);

        assertThat("Wrong result", result, is("com.microsoft.samples.subpackage.Person"));
    }

    @Test
    public void convertFullNameToOverload() {
        assertThat("Wrong result", ymlFilesBuilder.convertFullNameToOverload(
            "com.microsoft.samples.SuperHero.successfullyAttacked(int,java.lang.String)"), is(
            "com.microsoft.samples.SuperHero.successfullyAttacked*"));

        assertThat("Wrong result for case with generics", ymlFilesBuilder.convertFullNameToOverload(
            "com.microsoft.samples.subpackage.Display<T,R>.show()"), is(
            "com.microsoft.samples.subpackage.Display<T,R>.show*"));

        assertThat("Wrong result for constructor case", ymlFilesBuilder.convertFullNameToOverload(
            "com.microsoft.samples.SuperHero.SuperHero()"), is(
            "com.microsoft.samples.SuperHero.SuperHero*"));
    }

    @Test
    public void determineClassSimpleName() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        assertThat("Wrong result",
            YmlFilesBuilderImpl.determineClassSimpleName("bla-bla-prefix", element), is("bla-bla-prefix.SuperHero"));
        assertThat("Wrong result for empty prefix",
            YmlFilesBuilderImpl.determineClassSimpleName("", element), is("SuperHero"));
    }
}
