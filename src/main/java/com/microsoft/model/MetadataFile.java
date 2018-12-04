package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class MetadataFile {

    public final static String METADATA_FILE_HEADER = "### YamlMime:ManagedReference\n";

    private List<MetadataFileItem> items = new ArrayList<>();
    private List<MetadataFileItem> references = new ArrayList<>();

    public List<MetadataFileItem> getItems() {
        return items;
    }

    public void setItems(List<MetadataFileItem> items) {
        this.items = items;
    }

    public List<MetadataFileItem> getReferences() {
        return references;
    }

    public void setReferences(List<MetadataFileItem> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        String result = METADATA_FILE_HEADER;

        result += "items:\n";
        for (MetadataFileItem item : items) {
            result += item.toItemString();
        }

        result += "references:\n";
        for (MetadataFileItem reference : references) {
            result += reference.toReferenceString();
        }
        return result;
    }
}
