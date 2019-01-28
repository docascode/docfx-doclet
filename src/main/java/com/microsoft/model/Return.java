package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Return {

    @JsonProperty("type")
    private final String returnType;
    @JsonProperty("description")
    private String returnDescription;

    public Return(String returnType, String returnDescription) {
        this.returnType = returnType;
        this.returnDescription = returnDescription;
    }

    public Return(String returnType) {
        this.returnType = returnType;
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
}
