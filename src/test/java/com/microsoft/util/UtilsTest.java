package com.microsoft.util;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;

import org.junit.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.junit.MockitoJUnitRunner;

import com.google.testing.compile.CompilationRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private List<Element> allElements;

    @Before
    public void setup() {
        elements = rule.getElements();
        Element element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        allElements = element.getEnclosedElements().stream().collect(Collectors.toList());
    }

    // Test isPackagePrivate() method
    @Test
    public void isPackagePrivate_True_PackagePrivateMethod() {
        Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "getHobby()");
        assertThat(Utils.isPackagePrivate(method), is(true));
    }

    @Test
    public void isPackagePrivate_True_PackagePrivateField() {
        Element field = getElementByKindAndName(allElements, ElementKind.FIELD, "hobby");
        assertThat(Utils.isPackagePrivate(field), is(true));
    }

    // Test isPrivate() method
    @Test
    public void isPrivate_True_PrivateMethod() {
        Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "setHobby(java.lang.String)");
        assertThat(Utils.isPrivate(method), is(true));
    }

    @Test
    public void isPrivate_True_PrivateField() {
        Element field = getElementByKindAndName(allElements, ElementKind.FIELD, "uniquePower");
        assertThat(Utils.isPrivate(field), is(true));
    }

    // Test isPrivateOrPackagePrivate() method
    @Test
    public void isPrivateOrPackagePrivate_True_PackagePrivateMethod() {
        Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "getHobby()");
        assertThat(Utils.isPrivateOrPackagePrivate(method), is(true));
    }

    @Test
    public void isPrivateOrPackagePrivate_True_PrivateFiled() {
        Element field = getElementByKindAndName(allElements, ElementKind.FIELD, "uniquePower");
        assertThat(Utils.isPrivateOrPackagePrivate(field), is(true));
    }

    @Test
    public void isPrivateOrPackagePrivate_False_PublicMethod() {
        Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "getUniquePower()");
        assertThat(Utils.isPrivateOrPackagePrivate(method), is(false));
    }

    @Test
    public void isPrivateOrPackagePrivate_False_PublicField() {
        Element field = getElementByKindAndName(allElements, ElementKind.FIELD, "SOME_PUBLIC_STRING");
        assertThat(Utils.isPrivateOrPackagePrivate(field), is(false));
    }

    @Test
    public void isPrivateOrPackagePrivate_False_ProtectedMethod() {
        Element method = getElementByKindAndName(allElements, ElementKind.METHOD, "getHealth()");
        assertThat(Utils.isPrivateOrPackagePrivate(method), is(false));
    }

    private Element getElementByKindAndName(List<? extends Element> elements, ElementKind elementKind, String name) {
        return elements.stream()
                .filter(e -> e.toString().equals(name))
                .filter(e -> e.getKind() == elementKind)
                .findFirst().orElse(null);
    }
}
