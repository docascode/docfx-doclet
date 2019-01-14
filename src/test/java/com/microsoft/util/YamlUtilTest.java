package com.microsoft.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import java.util.Collections;
import org.junit.Test;

public class YamlUtilTest {

    @Test
    public void objectToYamlString() {
        MetadataFile metadataFile = new MetadataFile("", "SomeFileName");
        metadataFile.getItems().add(buildMetadataFileItem(3));
        metadataFile.getReferences().add(buildMetadataFileItem(5));

        String result = YamlUtil.objectToYamlString(metadataFile);

        assertThat("Wrong result", result, is(""
            + "items:\n"
            + "- uid: Some uid 3\n"
            + "  id: Some id3\n"
            + "  href: Some href3\n"
            + "  syntax:\n"
            + "    parameters:\n"
            + "    - id: Some id 3\n"
            + "      type: Some type 3\n"
            + "      description: Some desc 3\n"
            + "references:\n"
            + "- uid: Some uid 5\n"
            + "  id: Some id5\n"
            + "  href: Some href5\n"
            + "  syntax:\n"
            + "    parameters:\n"
            + "    - id: Some id 5\n"
            + "      type: Some type 5\n"
            + "      description: Some desc 5\n"));
    }

    private MetadataFileItem buildMetadataFileItem(int seed) {
        MetadataFileItem metadataFileItem = new MetadataFileItem("Some uid " + seed);
        metadataFileItem.setId("Some id" + seed);
        metadataFileItem.setHref("Some href" + seed);
        metadataFileItem.setParameters(Collections.singletonList(
            new MethodParameter("Some id " + seed, "Some type " + seed, "Some desc " + seed)));

        return metadataFileItem;
    }
}
