package com.microsoft.util;

import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {


    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private ElementUtil elementUtil;

    @Before
    public void setup() {
        elements = rule.getElements();
        elementUtil = new ElementUtil(
                new String[]{"samples\\.someexcludedpackage"},
                new String[]{"com\\.microsoft\\..*SomeExcludedClass"});
    }

    @Test
    public void isPackagePrivate() {
        Element element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        List<Element> result = element.getEnclosedElements()
                .stream().filter(e -> Utils.isPackagePrivate(e)).collect(Collectors.toList());

        assertThat("Wrong result list size", result.size(), is(2));
        assertThat("Unexpected package private field", String.valueOf(result.get(0)), is("hobby"));
        assertThat("Unexpected package private method", String.valueOf(result.get(1)), is("somePackagePrivateMethod()"));
    }

    @Test
    public void isPrivateOrPackagePrivate() {
        Element element = elements.getTypeElement("com.microsoft.samples.SuperHero");

        List<Element> result = element.getEnclosedElements()
                .stream().filter(e -> Utils.isPrivateOrPackagePrivate(e)).collect(Collectors.toList());

        assertThat("Wrong result list size", result.size(), is(7));
        assertThat("Unexpected private method", String.valueOf(result.get(5)), is("somePrivateMethod()"));
        assertThat("Unexpected package private method", String.valueOf(result.get(6)), is("somePackagePrivateMethod()"));
    }
}
