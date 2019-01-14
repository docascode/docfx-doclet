package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class TocItem {

    private final String uid;
    private final String name;
    private final String href;
    private List<TocItem> items = new ArrayList<>();

    public TocItem(String uid, String name, String href) {
        this.uid = uid;
        this.name = name;
        this.href = href;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public List<TocItem> getItems() {
        return items;
    }
}
