package com.microsoft.util;

import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ElementUtilTest {

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
    public void extractPackageElements() {
        Set<? extends Element> elementsSet = new HashSet<>() {{
            add(elements.getPackageElement("com.microsoft.samples"));
            add(elements.getTypeElement("com.microsoft.samples.SuperHero"));
            add(elements.getPackageElement("com.microsoft.samples.subpackage"));
        }};

        List<String> result = elementUtil.extractPackageElements(elementsSet)
            .stream().map(String::valueOf).collect(Collectors.toList());

        assertThat("Wrong result list size", result.size(), is(2));
        assertThat("Unexpected first item", result.get(0), is("com.microsoft.samples"));
        assertThat("Unexpected second item", result.get(1), is("com.microsoft.samples.subpackage"));
    }

    @Test
    public void extractSortedElements() {
        Element element = elements.getPackageElement("com.microsoft.samples.subpackage");

        List<String> allElements = element.getEnclosedElements()
                .stream().map(String::valueOf).collect(Collectors.toList());

        // Ensure items to exclude exist.
        assertThat("Wrong enclosed elements number", allElements.size(), is(6));
        assertThat("Unexpected package private class", allElements.contains("com.microsoft.samples.subpackage.InternalException"), is(true));
        assertThat("Unexpected to-exclude class", allElements.contains("com.microsoft.samples.subpackage.SomeExcludedClass"), is(true));


        List<String> extractedElements = elementUtil.extractSortedElements(element)
            .stream().map(String::valueOf).collect(Collectors.toList());

        // Verify filtered and sorted result
        assertThat("Wrong result list size", extractedElements.size(), is(4));
        assertThat("Unexpected first item in the result list after invoke method extractSortedElements()", extractedElements.get(0), is("com.microsoft.samples.subpackage.CustomException"));
        assertThat("Unexpected second item in the result list after invoke method extractSortedElements()", extractedElements.get(1), is("com.microsoft.samples.subpackage.Display"));
        assertThat("Unexpected third item in the result list after invoke method extractSortedElements()", extractedElements.get(2), is("com.microsoft.samples.subpackage.Person"));
    }

    @Test
    public void matchAnyPattern() {
        HashSet<Pattern> patterns = new HashSet<>(
            Arrays.asList(Pattern.compile("com\\.ms\\.Some.*"), Pattern.compile(".*UsualClass")));
        assertTrue(elementUtil.matchAnyPattern(patterns, "com.ms.SomeStrangeClass"));
        assertTrue(elementUtil.matchAnyPattern(patterns, "UsualClass"));
        assertFalse(elementUtil.matchAnyPattern(patterns, "EngineFive"));
        assertFalse(elementUtil.matchAnyPattern(patterns, "com.ms.Awesome"));
    }
}
