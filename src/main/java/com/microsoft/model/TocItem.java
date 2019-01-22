package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class TocItem {

    private final String uid;
    private final String name;
    private List<TocItem> items = new ArrayList<>();

    public TocItem(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public List<TocItem> getItems() {
        return items;
    }
}
