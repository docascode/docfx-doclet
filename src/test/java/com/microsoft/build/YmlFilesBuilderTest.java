package com.microsoft.build;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Syntax;
import com.sun.source.util.DocTrees;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.RegExUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class YmlFilesBuilderTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private YmlFilesBuilder ymlFilesBuilder;
    private DocletEnvironment environment;
    private DocTrees docTrees;

    @Before
    public void setup() {
        elements = rule.getElements();
        environment = Mockito.mock(DocletEnvironment.class);
        docTrees = Mockito.mock(DocTrees.class);
        ymlFilesBuilder = new YmlFilesBuilder(environment, "./target", new String[]{}, new String[]{});
    }

    @Test
    public void addConstructorsInfoWhenOnlyDefaultConstructor() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.subpackage.Person");
        MetadataFile container = new MetadataFile("output", "name");
        when(environment.getElementUtils()).thenReturn(elements);
        when(environment.getDocTrees()).thenReturn(docTrees);

        ymlFilesBuilder.addConstructorsInfo(element, container);

        assertThat("Wrong file name", container.getFileNameWithPath(), is("output" + File.separator + "name"));
        assertThat("Container should contain constructor item", container.getItems().size(), is(1));
    }

    @Test
    public void addConstructorsInfo() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        MetadataFile container = new MetadataFile("output", "name");
        when(environment.getElementUtils()).thenReturn(elements);
        when(environment.getDocTrees()).thenReturn(docTrees);

        ymlFilesBuilder.addConstructorsInfo(element, container);

        assertThat("Wrong file name", container.getFileNameWithPath(), is("output" + File.separator + "name"));
        Collection<MetadataFileItem> constructorItems = container.getItems();
        assertThat("Container should contain 2 constructor items", constructorItems.size(), is(2));
    }

    //todo add test case to cover reference item with in package
    @Test
    public void buildRefItem() {
        buildRefItemAndCheckAssertions("java.lang.Some.String", "java.lang.Some.String", "String");
        buildRefItemAndCheckAssertions("java.lang.Some.String[]", "java.lang.Some.String[]", "String");
    }

    private void buildRefItemAndCheckAssertions(String initialValue, String expectedUid, String expectedName) {
        MetadataFileItem result = ymlFilesBuilder.buildRefItem(initialValue);

        assertThat("Wrong uid", result.getUid(), is(expectedUid));
        assertThat("Wrong name", result.getSpecForJava().iterator().next().getUid(), is(RegExUtils.removeAll(expectedUid, "\\[\\]$")));
        assertThat("Wrong name", result.getSpecForJava().iterator().next().getName(), is(expectedName));
        assertThat("Wrong fullName", result.getSpecForJava().iterator().next().getFullName(), is(RegExUtils.removeAll(expectedUid, "\\[\\]$")));
    }

    @Test
    public void populateUidValues() {
        MetadataFile classMetadataFile = new MetadataFile("output", "name");

        MetadataFileItem ownerClassItem = buildMetadataFileItem("a.b.OwnerClass", "Not important summary value");
        ownerClassItem.setNameWithType("OwnerClass");
        MetadataFileItem item1 = buildMetadataFileItem("UID unknown class", "UnknownClass");
        populateSyntax(item1, "SomeClass#someMethod(String param)");
        MetadataFileItem item2 = buildMetadataFileItem("UID known class", "SomeClass#someMethod(String param)");
        MetadataFileItem item3 = buildMetadataFileItem("UID method only", "#someMethod2(String p1, String p2)");
        classMetadataFile.getItems().addAll(Arrays.asList(ownerClassItem, item1, item2, item3));

        MetadataFileItem reference1 = new MetadataFileItem("a.b.SomeClass.someMethod(String param)");
        reference1.setNameWithType("SomeClass.someMethod(String param)");
        MetadataFileItem reference2 = new MetadataFileItem("a.b.OwnerClass.someMethod2(String p1, String p2)");
        reference2.setNameWithType("OwnerClass.someMethod2(String p1, String p2)");
        classMetadataFile.getReferences().addAll(Arrays.asList(reference1, reference2));

        ymlFilesBuilder.populateUidValues(Collections.emptyList(), Arrays.asList(classMetadataFile));

        assertThat("Wrong summary for unknown class", item1.getSummary(),
                is("Bla bla <xref uid=\"\" data-throw-if-not-resolved=\"false\">UnknownClass</xref> bla"));
        assertThat("Wrong syntax description", item1.getSyntax().getParameters().get(0).getDescription(),
                is("One two <xref uid=\"a.b.SomeClass.someMethod(String param)\" data-throw-if-not-resolved=\"false\">SomeClass#someMethod(String param)</xref> three"));
        assertThat("Wrong summary for known class", item2.getSummary(),
                is("Bla bla <xref uid=\"a.b.SomeClass.someMethod(String param)\" data-throw-if-not-resolved=\"false\">SomeClass#someMethod(String param)</xref> bla"));
        assertThat("Wrong summary for method", item3.getSummary(),
                is("Bla bla <xref uid=\"a.b.OwnerClass.someMethod2(String p1, String p2)\" data-throw-if-not-resolved=\"false\">#someMethod2(String p1, String p2)</xref> bla"));

    }

    private MetadataFileItem buildMetadataFileItem(String uid, String value) {
        MetadataFileItem item = new MetadataFileItem(uid);
        item.setSummary(
                String.format("Bla bla <xref uid=\"%s\" data-throw-if-not-resolved=\"false\">%s</xref> bla", value, value));
        return item;
    }

    private void populateSyntax(MetadataFileItem item, String value) {
        Syntax syntax = new Syntax();
        String methodParamDescription = String
                .format("One two <xref uid=\"%s\" data-throw-if-not-resolved=\"false\">%s</xref> three", value, value);
        syntax.setParameters(
                Arrays.asList(new MethodParameter("method param id", "method param type", methodParamDescription)));
        item.setSyntax(syntax);
    }

    @Test
    public void determineUidByLinkContent() {
        Map<String, String> lookup = new HashMap<>() {{
            put("SomeClass", "a.b.c.SomeClass");
            put("SomeClass.someMethod()", "a.b.c.SomeClass.someMethod()");
            put("SomeClass.someMethod(String param)", "a.b.c.SomeClass.someMethod(String param)");
        }};

        LookupContext lookupContext = new LookupContext(lookup, lookup);
        assertThat("Wrong result for class", ymlFilesBuilder.
                resolveUidByLookup("SomeClass", lookupContext), is("a.b.c.SomeClass"));
        assertThat("Wrong result for method", ymlFilesBuilder.
                resolveUidFromLinkContent("SomeClass#someMethod()", lookupContext), is("a.b.c.SomeClass.someMethod()"));
        assertThat("Wrong result for method with param", ymlFilesBuilder.
                        resolveUidFromLinkContent("SomeClass#someMethod(String param)", lookupContext),
                is("a.b.c.SomeClass.someMethod(String param)"));

        assertThat("Wrong result for unknown class", ymlFilesBuilder.
                resolveUidByLookup("UnknownClass", lookupContext), is(""));
        assertThat("Wrong result for null", ymlFilesBuilder.resolveUidByLookup(null, lookupContext), is(""));
        assertThat("Wrong result for whitespace", ymlFilesBuilder.resolveUidByLookup(" ", lookupContext), is(""));
    }

    @Test
    public void splitUidWithGenericsIntoClassNames() {
        List<String> result = ymlFilesBuilder.splitUidWithGenericsIntoClassNames("a.b.c.List<df.mn.ClassOne<tr.T>>");

        assertThat("Wrong result list size", result.size(), is(3));
        assertThat("Wrong result list content", result, hasItems("a.b.c.List", "df.mn.ClassOne", "tr.T"));
    }

    @Test
    public void expandComplexGenericsInReferences() {
        MetadataFile classMetadataFile = new MetadataFile("path", "name");
        MetadataFileItem referenceItem = new MetadataFileItem("a.b.c.List<df.mn.ClassOne<tr.T>>");
        Set<MetadataFileItem> references = classMetadataFile.getReferences();
        references.add(referenceItem);

        ymlFilesBuilder.expandComplexGenericsInReferences(classMetadataFile);

        assertThat("Wrong references amount", references.size(), is(4));
        assertThat("Wrong references content",
                references.stream().map(MetadataFileItem::getUid).collect(Collectors.toList()),
                hasItems("a.b.c.List", "df.mn.ClassOne", "tr.T", "a.b.c.List<df.mn.ClassOne<tr.T>>"));
    }
}
