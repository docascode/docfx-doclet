package com.microsoft.build;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.testing.compile.CompilationRule;
import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.sun.source.util.DocTrees;
import java.io.File;
import java.util.List;
import java.util.function.Function;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import jdk.javadoc.doclet.DocletEnvironment;
import org.apache.commons.lang3.StringUtils;
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
        List<MetadataFileItem> constructorItems = container.getItems();
        assertThat("Container should contain 2 constructor items", constructorItems.size(), is(2));
    }

    @Test
    public void buildSpecJavaRefItemAndReplaceField() {
        CustomClass customClass = new CustomClass();
        String originalValue = "initial value";
        String expectedUpdatedValue = originalValue.toUpperCase() + "!";
        customClass.setSomeField(originalValue);
        Function<String, String> conversionFunc = s -> StringUtils.upperCase(s) + "!";

        MetadataFileItem result = ymlFilesBuilder
            .buildSpecJavaRefItemAndReplaceField(customClass, "someField", conversionFunc);

        assertThat("Field value should be changed", customClass.getSomeField(), is(expectedUpdatedValue));
        assertThat("Wrong uid", result.getUid(), is(expectedUpdatedValue));
        assertThat("Wrong name", result.getSpecJava().getName(), is(originalValue));
        assertThat("Wrong fullName", result.getSpecJava().getFullName(), is(originalValue));
    }

    private class CustomClass {

        private String someField;

        public String getSomeField() {
            return someField;
        }

        public void setSomeField(String someField) {
            this.someField = someField;
        }
    }
}
