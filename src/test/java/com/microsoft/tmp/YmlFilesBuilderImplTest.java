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
    public void extractTypeParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        List<TypeParameter> result = ymlFilesBuilder.extractTypeParameters(element);

        assertThat("Wrong type params size", result.size(), is(1));
        assertThat("Wrong type parameter id", result.get(0).getId(), is("T"));
        assertThat("Wrong type parameter type", result.get(0).getType(), is("bb61488d"));
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
