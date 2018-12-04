package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class TocFile {

    public final static String TOC_FILE_HEADER = "### YamlMime:TableOfContent\n";

    private List<TocItem> items = new ArrayList<>();

    public List<TocItem> getItems() {
        return items;
    }

    public void setItems(List<TocItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        String result = TOC_FILE_HEADER;
        for (TocItem tocItem : items) {
            result += String.valueOf(tocItem);
        }
        return result;
    }
}
