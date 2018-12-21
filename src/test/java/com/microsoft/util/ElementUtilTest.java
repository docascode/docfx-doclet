package com.microsoft.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.microsoft.model.TypeParameter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ElementUtilTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;

    @Before
    public void setup() {
        elements = rule.getElements();
    }

    @Test
    public void extractTypeParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");

        List<TypeParameter> result = ElementUtil.extractTypeParameters(element);

        assertThat("Wrong type params size", result.size(), is(1));
        assertThat("Wrong type parameter id", result.get(0).getId(), is("T"));
        assertThat("Wrong type parameter type", result.get(0).getType(), is("bb61488d"));
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
    public void extractExceptions() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        List<ExceptionItem> result = ElementUtil.extractExceptions(
            ElementFilter.methodsIn(element.getEnclosedElements()).get(0)
        );

        assertThat("Wrong exceptions count", result.size(), is(1));
        assertThat("Wrong type", result.get(0).getType(), is("java.lang.Exception"));
        assertThat("Wrong description", result.get(0).getDescription(), is("-=TBD=-"));
    }

    @Test
    public void extarctParameters() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        List<MethodParameter> result = ElementUtil.extractParameters(
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
    public void extractReturnForExecutableElement() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        checkReturnForExecutableElement(element, 0, "int", "-=TBD=-");
        checkReturnForExecutableElement(element, 1, "java.lang.String", "-=TBD=-");
    }

    @Test
    public void extractReturnForVariableElement() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        checkReturnForVariableElement(element, 0, "java.lang.String");
        checkReturnForVariableElement(element, 1, "java.lang.String");
        checkReturnForVariableElement(element, 2, "int");
        checkReturnForVariableElement(element, 3, "int");
        checkReturnForVariableElement(element, 4, "java.lang.String");
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

    private void checkReturnForExecutableElement(TypeElement element, int methodNumber, String expectedType,
        String expectedDescription) {
        ExecutableElement executableElement = ElementFilter.methodsIn(element.getEnclosedElements()).get(methodNumber);

        Return result = ElementUtil.extractReturn(executableElement);

        assertThat(result.getReturnType(), is(expectedType));
        assertThat(result.getReturnDescription(), is(expectedDescription));
    }

    private void checkReturnForVariableElement(TypeElement element, int variableNumber, String expectedType) {
        VariableElement variableElement = ElementFilter.fieldsIn(element.getEnclosedElements()).get(variableNumber);

        Return result = ElementUtil.extractReturn(variableElement);

        assertThat(result.getReturnType(), is(expectedType));
    }
}
