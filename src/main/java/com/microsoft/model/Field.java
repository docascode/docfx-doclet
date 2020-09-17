package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.util.XrefHelper;

public class Field {

    @JsonIgnore
    private final String fieldType;
    @JsonProperty("description")
    private String fieldDescription;
    @JsonProperty("type")
    private String fieldXrefString;
    private String value;

    public String getFieldDescription() {
        return fieldDescription;
    }

    public String getFieldXrefString() {
        return fieldXrefString;
    }

    public String getValue() {
        return value;
    }

    public Field(String fieldType, String value) {
        this.fieldType = fieldType;
        this.value = value ;
        this.fieldXrefString = XrefHelper.generateXrefString(fieldType, XrefHelper.XrefOption.SHORTNAME);
    }
}