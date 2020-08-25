package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.microsoft.util.XrefHelper;

public class Return {

    @JsonIgnore
    private final String returnType;
    @JsonProperty("description")
    private String returnDescription;
    @JsonProperty("type")
    private String returnXrefString;

    public Return(String returnType, String returnDescription) {
        this.returnType = returnType;
        this.returnDescription = returnDescription;
        this.returnXrefString = XrefHelper.generateXrefString(returnType, XrefHelper.XrefOption.SHORTNAME);
    }

    public Return(String returnType) {
        this.returnType = returnType;
        this.returnXrefString = XrefHelper.generateXrefString(returnType, XrefHelper.XrefOption.SHORTNAME);
    }

    public String getReturnType() {
        return returnType;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public void setReturnDescription(String returnDescription) {
        this.returnDescription = returnDescription;
    }

    public String getReturnXrefString() {
        return returnXrefString;
    }
}
