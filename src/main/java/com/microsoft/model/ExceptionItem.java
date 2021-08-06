package com.microsoft.model;

import org.apache.commons.text.StringEscapeUtils;

public class ExceptionItem {

    private final String type;
    private final String description;

    public ExceptionItem(String type, String description) {
        this.type = type;
        this.description = StringEscapeUtils.unescapeJava(description);
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
