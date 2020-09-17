package com.microsoft.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class YamlUtilTest {

    @Test
    public void objectToYamlString() {
        MetadataFile metadataFile = new MetadataFile("", "SomeFileName");
        metadataFile.getItems().add(buildMetadataFileItem(3, "method"));
        metadataFile.getReferences().add(buildMetadataFileItem(5, ""));

        String result = YamlUtil.objectToYamlString(metadataFile, "SomeFileName");

        assertThat("Wrong result", result, is(""
            + "items:\n"
            + "- uid: \"Some uid 3\"\n"
            + "  id: \"Some id3\"\n"
            + "  href: \"Some href3\"\n"
            + "  type: \"method\"\n"
            + "  syntax:\n"
            + "    parameters:\n"
            + "    - description: \"Some desc 3\"\n"
            + "      name: \"Some name 3\"\n"
            + "      type: \"<xref href=\\\"Some type 3?alt=Some type 3&text=Some type 3\\\" data-throw-if-not-resolved=\\\"False\\\" />\"\n"
            + "references:\n"
            + "- uid: \"Some uid 5\"\n"
            + "  id: \"Some id5\"\n"
            + "  href: \"Some href5\"\n"
            + "  syntax:\n"
            + "    parameters:\n"
            + "    - description: \"Some desc 5\"\n"
            + "      name: \"Some name 5\"\n"
            + "      type: \"<xref href=\\\"Some type 5?alt=Some type 5&text=Some type 5\\\" data-throw-if-not-resolved=\\\"False\\\" />\"\n"));
    }

    @Test
    public void convertHtmlToMarkdown() throws IOException {
        String text = FileUtils.readFileToString(new File("target/test-classes/html2md/initial.html"), UTF_8);
        String expectedResult = FileUtils.readFileToString(new File("target/test-classes/html2md/converted.md"), UTF_8);

        String result = YamlUtil.convertHtmlToMarkdown(text);

        assertThat("Wrong result", result, is(expectedResult));
    }

    @Test
    public void convertHtmlToMarkdownForBlankParam() {
        assertThat("Wrong result for null", YamlUtil.convertHtmlToMarkdown(null), is(nullValue()));
        assertThat("Wrong result for empty string", YamlUtil.convertHtmlToMarkdown(""), is(""));
    }

    private MetadataFileItem buildMetadataFileItem(int seed, String type) {
        MetadataFileItem metadataFileItem = new MetadataFileItem("Some uid " + seed);
        metadataFileItem.setId("Some id" + seed);
        metadataFileItem.setHref("Some href" + seed);
        metadataFileItem.setType(type);
        metadataFileItem.setParameters(Collections.singletonList(
            new MethodParameter("Some name " + seed, "Some type " + seed, "Some desc " + seed)));

        return metadataFileItem;
    }
}
