package com.microsoft.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TocItem implements Comparable<TocItem>{

    private final String uid;
    private final String name;
    @JsonProperty("type")
    private String type;
    private Set<TocItem> items = new HashSet<>();

    public TocItem(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public TocItem(String uid, String name, String type) {
        this.uid = uid;
        this.name = name;
        this.type = type.toLowerCase();
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public Set<TocItem> getItems() {
        Set<TocItem> sortedSet = new TreeSet<>(this.items);
        this.items = sortedSet;
        return items;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TocItem that = (TocItem) o;

        return uid.equals(that.uid);
    }

    @Override
    public int compareTo(TocItem item) {
        return this.getUid().compareTo(item.getUid());
        }
}
