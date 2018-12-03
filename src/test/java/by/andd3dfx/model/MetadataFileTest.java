package by.andd3dfx.model;

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
            + "references:\n"
            + "- uid: Reference uid\n"));
    }

    private MetadataFileItem buildMetadataFileItem(String uid) {
        MetadataFileItem result = new MetadataFileItem();
        result.setUid(uid);
        return result;
    }
}
