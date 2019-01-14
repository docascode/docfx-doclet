package com.microsoft.model;

public class ExceptionItem {

    private final String type;
    private final String description;

    public ExceptionItem(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
