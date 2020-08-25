package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TypeParameter implements Comparable<TypeParameter> {

    @JsonProperty("name")
    private final String id;
    private String description;

    public TypeParameter(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(TypeParameter item) {
        return this.getId().compareTo(item.getId());
    }
}
