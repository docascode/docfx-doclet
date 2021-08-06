package com.microsoft.model;

import org.apache.commons.text.StringEscapeUtils;

public class MethodParameter {

    private final String id;
    private final String type;
    private String description;

    public MethodParameter(String id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringEscapeUtils.unescapeJava(description);
    }
}
