package com.microsoft.model;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;

public class TocItem {

    private String uid;
    private String name;
    private String href;
    private List<TocItem> items = new ArrayList<>();

    public static class Builder {

        private TocItem tocItem;

        public Builder() {
            tocItem = new TocItem();
        }

        public Builder setUid(String uid) {
            tocItem.setUid(uid);
            return this;
        }

        public Builder setName(String name) {
            tocItem.setName(name);
            return this;
        }

        public Builder setHref(String href) {
            tocItem.setHref(href);
            return this;
        }

        public TocItem build() {
            if (isBlank(tocItem.uid) || isBlank(tocItem.name) || isBlank(tocItem.href)) {
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
}
