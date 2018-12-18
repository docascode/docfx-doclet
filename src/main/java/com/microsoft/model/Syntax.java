package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Syntax {

    private String content;
    private List<TypeParameter> parameters = new ArrayList<>();
    @JsonProperty("return")
    private Return returnValue;
    private List<TypeParameter> typeParameters = new ArrayList<>();

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<TypeParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<TypeParameter> parameters) {
        this.parameters = parameters;
    }

    public Return getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Return returnValue) {
        this.returnValue = returnValue;
    }

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(List<TypeParameter> typeParameters) {
        this.typeParameters = typeParameters;
    }
}
