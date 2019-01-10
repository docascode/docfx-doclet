package com.microsoft.lookup;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.TypeParameter;
import java.util.List;
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
public class ClassLookupTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private ClassLookup classLookup;
    private DocletEnvironment environment;

    @Before
    public void setup() {
        elements = rule.getElements();
        environment = Mockito.mock(DocletEnvironment.class);
        classLookup = new ClassLookup(environment);
    }

    @Test
    public void determineTypeParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        List<TypeParameter> result = classLookup.determineTypeParameters(element);

        assertThat("Wrong type params size", result.size(), is(1));
        assertThat("Wrong type parameter id", result.get(0).getId(), is("T"));
        assertThat("Wrong type parameter type", result.get(0).getType(), is("84"));
    }

    @Test
    public void determineSuperclass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        String result = classLookup.determineSuperclass(element);

        assertThat("Wrong result", result, is("java.lang.Object"));
    }

    @Test
    public void determineSuperclassForChildClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        String result = classLookup.determineSuperclass(element);

        assertThat("Wrong result", result, is("com.microsoft.samples.subpackage.Person"));
    }

    @Test
    public void determineSuperclassForEnum() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        String result = classLookup.determineSuperclass(element);

        assertThat("Wrong result", result, is("java.lang.Object"));
    }

    @Test
    public void determineClassContent() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        String result = classLookup.determineClassContent(element, "SuperHero");

        assertThat("Wrong result", result, is("public class SuperHero extends Person implements Serializable, Cloneable"));
    }

    @Test
    public void determineClassContentForInterface() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

        String result = classLookup.determineClassContent(element, "Display<T,R>");

        assertThat("Wrong result", result, is("public interface Display<T,R> extends Serializable, List<Person<T>>"));
    }

    @Test
    public void determineClassContentForEnum() {
        TypeElement element = elements
            .getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        String result = classLookup.determineClassContent(element, "Person.IdentificationInfo.Gender");

        assertThat("Wrong result", result, is("public enum Person.IdentificationInfo.Gender"));
    }

    @Test
    public void determineClassContentForStaticClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");

        String result = classLookup.determineClassContent(element, "Person.IdentificationInfo");

        assertThat("Wrong result", result, is("public static class Person.IdentificationInfo"));
    }

    @Test
    public void determineTypeForInterface() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Display");

        assertThat(classLookup.determineType(element), is("Interface"));
    }

    @Test
    public void determineTypeForEnum() {
        TypeElement element = elements
            .getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo.Gender");

        assertThat(classLookup.determineType(element), is("Enum"));
    }

    @Test
    public void determineTypeForClass() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person.IdentificationInfo");

        assertThat(classLookup.determineType(element), is("Class"));
    }


    @Test
    public void generateHexString() {
        assertThat("Wrong result for simple string", classLookup.generateHexString("T"), is("84"));
        assertThat("Wrong result for complex string", classLookup.generateHexString("? extends SomeClass"),
            is("-819114916"));
    }
}
