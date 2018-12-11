package com.microsoft.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MetadataFileTest {

    @Test
    public void testToString() {
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.getItems().add(buildMetadataFileItem("Item uid"));
        metadataFile.getReferences().add(buildMetadataFileItem("Reference uid"));

        String result = metadataFile.toString();

        assertThat("Wrong result", result, is(""
            + "### YamlMime:ManagedReference\n"
            + "items:\n"
            + "- uid: Item uid\n"
            + "  id: null\n"
            + "  parent: Some parent\n"
            + "  href: null\n"
            + "  langs:\n"
            + "  - java\n"
            + "  name: null\n"
            + "  nameWithType: null\n"
            + "  fullName: null\n"
            + "  type: null\n"
            + "  summary: null\n"
            + "  syntax:\n"
            + "    content: null\n"
            + "references:\n"
            + "- uid: Reference uid\n"
            + "  parent: Some parent\n"
            + "  href: null\n"
            + "  name: null\n"
            + "  nameWithType: null\n"
            + "  fullName: null\n"
            + "  type: null\n"
            + "  summary: null\n"
            + "  syntax:\n"
            + "    content: null\n"));
    }

    private MetadataFileItem buildMetadataFileItem(String uid) {
        MetadataFileItem result = new MetadataFileItem();
        result.setUid(uid);
        result.setParent("Some parent");
        return result;
    }
}
