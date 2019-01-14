package com.microsoft.model;

public class TypeParameter {

    private final String id;
    private final String type;

    public TypeParameter(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
