package com.microsoft.model;

public class TypeParameter {

    private final String id;
    private String type;

    public TypeParameter(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
