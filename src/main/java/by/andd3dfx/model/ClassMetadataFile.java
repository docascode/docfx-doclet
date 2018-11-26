package by.andd3dfx.model;

import java.util.ArrayList;
import java.util.List;

public class ClassMetadataFile {

    private List<Item> items = new ArrayList<>();
    private List<Item> references = new ArrayList<>();

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Item> getReferences() {
        return references;
    }

    public void setReferences(List<Item> references) {
        this.references = references;
    }
}
