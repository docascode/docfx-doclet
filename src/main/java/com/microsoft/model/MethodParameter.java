package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.microsoft.util.XrefHelper;

public class MethodParameter {

    @JsonProperty("name")
    private final String id;
    @JsonIgnore
    private final String type;
    private String description;
    @JsonProperty("type")
    private String typeXrefSting;

    public MethodParameter(String id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.typeXrefSting = XrefHelper.generateXrefString(type, XrefHelper.XrefOption.SHORTNAME);
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
        this.description = description;
    }
}
