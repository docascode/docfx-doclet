package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;

public class TocItem {

    private String uid;
    private String name;
    private String href;
    private List<TocItem> items = new ArrayList<>();

    public TocItem(String uid, String name, String href) {
        this.uid = uid;
        this.name = name;
        this.href = href;
    }

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

    public List<TocItem> getItems() {
        return items;
    }

    public void setItems(List<TocItem> items) {
        this.items = items;
    }
}
