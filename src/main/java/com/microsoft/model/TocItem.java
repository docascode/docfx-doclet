package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class TocItem {

    private String uid;
    private String name;
    private String href;
    private List<TocItem> items = new ArrayList<>();

    public static class Builder {

        private TocItem tocItem;

        public Builder() {
            this.tocItem = new TocItem();
        }

        public Builder setUid(String uid) {
            this.tocItem.setUid(uid);
            return this;
        }

        public Builder setName(String name) {
            this.tocItem.setName(name);
            return this;
        }

        public Builder setHref(String href) {
            this.tocItem.setHref(href);
            return this;
        }

        public TocItem build() {
            if (StringUtils.isBlank(this.tocItem.uid) || StringUtils.isBlank(this.tocItem.name)
                || StringUtils.isBlank(this.tocItem.href)) {
                throw new IllegalArgumentException("TocItem could not constructed: not enough info");
            }
            return tocItem;
        }
    }

    private TocItem() {
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

    @Override
    public String toString() {
        return buildItemChunk(0);
    }

    String buildItemChunk(int spacesCount) {
        String spaces = new String(new char[spacesCount]).replace('\0', ' ');
        return spaces + "- uid: " + uid + "\n" +
            spaces + "  name: " + name + "\n" +
            spaces + "  href: " + href + "\n" +
            buildItemsChunk(spacesCount + 2, items);
    }

    String buildItemsChunk(int spacesCount, List<TocItem> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        String spaces = new String(new char[spacesCount]).replace('\0', ' ');
        String result = spaces + "items: \n";
        for (TocItem tocItem : items) {
            result += tocItem.buildItemChunk(spacesCount);
        }
        return result;
    }
}
