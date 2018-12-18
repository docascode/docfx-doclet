package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class MetadataFile {

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
}
