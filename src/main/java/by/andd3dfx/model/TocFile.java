package by.andd3dfx.model;

import java.util.ArrayList;
import java.util.List;

public class TocFile {

    private String uid;
    private String name;
    private String href;
    private List<TocFile> items = new ArrayList<>();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<TocFile> getItems() {
        return items;
    }

    public void setItems(List<TocFile> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return buildTocFileChunk(0, this);
    }

    private String buildTocFileChunk(int spacesCount, TocFile tocFile) {
        String spaces = new String(new char[spacesCount]).replace('\0', ' ');
        return spaces + "- uid: " + tocFile.uid + "\n" +
            spaces + "  name: " + tocFile.name + "\n" +
            spaces + "  href: " + tocFile.href + "\n" +
            buildItemsChunk(spacesCount + 2, tocFile.items);
    }

    private String buildItemsChunk(int spacesCount, List<TocFile> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        String spaces = new String(new char[spacesCount]).replace('\0', ' ');
        String result = spaces + "items: \n";
        for (TocFile tocFile : items) {
            result += buildTocFileChunk(spacesCount, tocFile);
        }
        return result;
    }
}
