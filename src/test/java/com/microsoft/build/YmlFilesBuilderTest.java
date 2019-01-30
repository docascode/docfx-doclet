package com.microsoft.build;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.sun.source.util.DocTrees;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

        assertThat("Wrong file name", container.getFileName(), is("output" + File.separator + "name"));
        assertThat("Container should contain constructor item", container.getItems().size(), is(1));
    }

    @Test
    public void addConstructorsInfo() {
        TypeElement element = elements.getTypeElement("com.microsoft.samples.SuperHero");
        MetadataFile container = new MetadataFile("output", "name");
        when(environment.getElementUtils()).thenReturn(elements);
        when(environment.getDocTrees()).thenReturn(docTrees);

        ymlFilesBuilder.addConstructorsInfo(element, container);

        assertThat("Wrong file name", container.getFileName(), is("output" + File.separator + "name"));
        Collection<MetadataFileItem> constructorItems = container.getItems();
        assertThat("Container should contain 2 constructor items", constructorItems.size(), is(2));
    }

    @Test
    public void buildRefItem() {
        buildRefItemAndCheckAssertions("java.lang.Some.String", "java.lang.Some.String", "Some.String");
        buildRefItemAndCheckAssertions("java.lang.Some.String[]", "java.lang.Some.String", "Some.String");
    }

    private void buildRefItemAndCheckAssertions(String initialValue, String expectedUid, String expectedName) {
        MetadataFileItem result = ymlFilesBuilder.buildRefItem(initialValue);

        assertThat("Wrong uid", result.getUid(), is(expectedUid));
        assertThat("Wrong name", result.getName(), is(expectedName));
        assertThat("Wrong fullName", result.getFullName(), is(expectedUid));
        assertThat("Wrong nameWithType", result.getNameWithType(), is(expectedName));
    }

    @Test
    public void populateUidValues() {
        MetadataFile classMetadataFile = new MetadataFile("output", "name");

        MetadataFileItem ownerClassItem = buildMetadataFileItem("a.b.OwnerClass", "Not important summary value");
        ownerClassItem.setNameWithType("OwnerClass");
        MetadataFileItem item1 = buildMetadataFileItem("UID unknown class", "UnknownClass");
        MetadataFileItem item2 = buildMetadataFileItem("UID known class", "SomeClass#someMethod(String param)");
        MetadataFileItem item3 = buildMetadataFileItem("UID method only", "#someMethod2(String p1, String p2)");
        classMetadataFile.getItems().addAll(Arrays.asList(ownerClassItem, item1, item2, item3));

        MetadataFileItem reference1 = new MetadataFileItem("a.b.SomeClass.someMethod(String param)");
        reference1.setNameWithType("SomeClass.someMethod(String param)");
        MetadataFileItem reference2 = new MetadataFileItem("a.b.OwnerClass.someMethod2(String p1, String p2)");
        reference2.setNameWithType("OwnerClass.someMethod2(String p1, String p2)");
        classMetadataFile.getReferences().addAll(Arrays.asList(reference1, reference2));

        ymlFilesBuilder.populateUidValues(classMetadataFile);

        assertThat("Wrong summary for unknown class", item1.getSummary(),
            is("Bla bla <xref uid=\"\" data-throw-if-not-resolved=\"false\">UnknownClass</xref> bla"));
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

    @Test
    public void determineUidByLinkContent() {
        Map<String, String> lookup = new HashMap<>() {{
            put("SomeClass", "a.b.c.SomeClass");
            put("SomeClass.someMethod()", "a.b.c.SomeClass.someMethod()");
            put("SomeClass.someMethod(String param)", "a.b.c.SomeClass.someMethod(String param)");
        }};

        assertThat("Wrong result for class", ymlFilesBuilder.
            resolveUidByLookup("SomeClass", lookup), is("a.b.c.SomeClass"));
        assertThat("Wrong result for method", ymlFilesBuilder.
            resolveUidByLookup("SomeClass#someMethod()", lookup), is("a.b.c.SomeClass.someMethod()"));
        assertThat("Wrong result for method with param", ymlFilesBuilder.
                resolveUidByLookup("SomeClass#someMethod(String param)", lookup),
            is("a.b.c.SomeClass.someMethod(String param)"));

        assertThat("Wrong result for unknown class", ymlFilesBuilder.
            resolveUidByLookup("UnknownClass", lookup), is(""));
        assertThat("Wrong result for null", ymlFilesBuilder.resolveUidByLookup(null, lookup), is(""));
        assertThat("Wrong result for whitespace", ymlFilesBuilder.resolveUidByLookup(" ", lookup), is(""));
    }

    @Test
    public void splitUidWithGenericsIntoClassNames() {
        List<String> result = ymlFilesBuilder.splitUidWithGenericsIntoClassNames("a.b.c.List<df.mn.ClassOne<tr.T>>");

        assertThat("Wrong result list size", result.size(), is(3));
        assertThat("Wrong result list content", result, hasItems("a.b.c.List", "df.mn.ClassOne", "tr.T"));
    }

    @Test
    public void applyPostProcessing() {
        MetadataFile classMetadataFile = new MetadataFile("path", "name");
        MetadataFileItem referenceItem = new MetadataFileItem("a.b.c.List<df.mn.ClassOne<tr.T>>");
        Set<MetadataFileItem> references = classMetadataFile.getReferences();
        references.add(referenceItem);

        ymlFilesBuilder.applyPostProcessing(classMetadataFile);

        assertThat("Wrong references amount", references.size(), is(3));
        assertThat("Wrong references content",
            references.stream().map(MetadataFileItem::getUid).collect(Collectors.toList()),
            hasItems("a.b.c.List", "df.mn.ClassOne", "tr.T"));
    }
}
