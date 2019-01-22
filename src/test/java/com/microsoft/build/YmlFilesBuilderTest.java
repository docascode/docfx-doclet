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
import java.util.Collection;
import java.util.List;
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

    void buildRefItemAndCheckAssertions(String initialValue, String expectedUid, String expectedName) {
        MetadataFileItem result = ymlFilesBuilder.buildRefItem(initialValue);

        assertThat("Wrong uid", result.getUid(), is(expectedUid));
        assertThat("Wrong name", result.getName(), is(expectedName));
        assertThat("Wrong fullName", result.getFullName(), is(expectedUid));
        assertThat("Wrong nameWithType", result.getNameWithType(), is(expectedName));
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
