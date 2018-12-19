package com.microsoft.tmp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.TypeParameter;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
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
    public void extractExceptions() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        List<ExceptionItem> result = ymlFilesBuilder.extractExceptions(
            ElementFilter.methodsIn(element.getEnclosedElements()).get(0)
        );

        assertThat("Wrong exceptions count", result.size(), is(1));
        assertThat("Wrong type", result.get(0).getType(), is("java.lang.Exception"));
        assertThat("Wrong description", result.get(0).getDescription(), is("-=TBD=-"));
    }

    @Test
    public void extarctParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        List<MethodParameter> result = ymlFilesBuilder.extractParameters(
            ElementFilter.methodsIn(element.getEnclosedElements()).get(0)
        );

        assertThat("Wrong parameters count", result.size(), is(2));

        assertThat("Wrong first param id", result.get(0).getId(), is("incomingDamage"));
        assertThat("Wrong first param type", result.get(0).getType(), is("int"));
        assertThat("Wrong first param description", result.get(0).getDescription(), is("-=TBD=-"));

        assertThat("Wrong second param id", result.get(1).getId(), is("damageType"));
        assertThat("Wrong second param type", result.get(1).getType(), is("java.lang.String"));
        assertThat("Wrong second param description", result.get(1).getDescription(), is("-=TBD=-"));
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
            ymlFilesBuilder.determineClassSimpleName("bla-bla-prefix", element), is("bla-bla-prefix.SuperHero"));
        assertThat("Wrong result for empty prefix",
            ymlFilesBuilder.determineClassSimpleName("", element), is("SuperHero"));
    }

    @Test
    public void extractClassContent() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        String result = ymlFilesBuilder.extractClassContent(element, "SuperHero");

        assertThat("Wrong result", result, is("public class SuperHero"));
    }

    @Test
    public void extractClassContentForInterface() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

        String result = ymlFilesBuilder.extractClassContent(element, "Display<T, R>");

        assertThat("Wrong result", result, is("public interface Display<T, R>"));
    }

    @Test
    public void extractClassContentForEnum() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        String result = ymlFilesBuilder.extractClassContent(element, "Person.IdentificationInfo.Gender");

        assertThat("Wrong result", result, is("public enum Person.IdentificationInfo.Gender"));
    }

    @Test
    public void extractClassContentForStaticClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");

        String result = ymlFilesBuilder.extractClassContent(element, "Person.IdentificationInfo");

        assertThat("Wrong result", result, is("public static class Person.IdentificationInfo"));
    }
}
