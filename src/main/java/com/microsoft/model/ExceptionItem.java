package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.microsoft.util.XrefHelper;

public class ExceptionItem {

    @JsonIgnore
    private final String type;
    @JsonProperty("type")
    private String exceptionXrefString;
    private final String description;

    public ExceptionItem(String type, String description) {
        this.type = type;
        this.description = description;
        this.exceptionXrefString = XrefHelper.generateXrefString(type, XrefHelper.XrefOption.SHORTNAME);
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getExceptionXrefString() {
        return exceptionXrefString;
    }
}
