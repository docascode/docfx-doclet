package com.microsoft.model;

public class TypeParameter {

    public TypeParameter() {
    }

    public TypeParameter(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public TypeParameter(String id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }

    private String id;
    private String type;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
