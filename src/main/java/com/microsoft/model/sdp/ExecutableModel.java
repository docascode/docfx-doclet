package com.microsoft.model.sdp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.*;

public class ExecutableModel extends BaseModel implements Comparable<ExecutableModel> {

    //<editor-fold desc="Properties">
    private String overridden;
    private List<MethodParameter> parameters;
    @JsonProperty("returns")
    private Return returnType;
    private Field field;
    private String syntax;
    private List<TypeParameter> typeParameters = new ArrayList<>();
    private List<ExceptionItem> exceptions = new ArrayList<>();
    //</editor-fold>

    //<editor-fold desc="Constructor">
    public ExecutableModel(MetadataFileItem item) {
        super(item.getUid(), item.getName(), item.getFullName(), item.getNameWithType());
        this.overridden = item.getOverridden();
        this.syntax = ((ExtendedMetadataFileItem) item).getSyntaxContent();
        if (item.getType().toLowerCase().equals("field")) {
            this.field = item.getField();
        } else {
            this.returnType = ((ExtendedMetadataFileItem) item).getReturn();
            this.parameters = ((ExtendedMetadataFileItem) item).getParameters();
        }
        if (!(item.getSummary() == null || item.getSummary().isEmpty())) {
            this.setSummary(item.getSummary());
        }

        this.exceptions.addAll(item.getExceptions());
    }
    //</editor-fold>

    //<editor-fold desc="Property Accessors">
    public List<ExceptionItem> getExceptions() {
        return exceptions;
    }

    public String getOverridden() {
        return overridden;
    }

    public void setOverridden(String overridden) {
        this.overridden = overridden;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<MethodParameter> parameters) {
        this.parameters = parameters;
    }

    public Return getReturnType() {
        return returnType;
    }

    public void setReturnType(Return returnType) {
        this.returnType = returnType;
    }

    public String getSyntax() {
        return syntax;
    }

    public void setSyntax(String syntax) {
        this.syntax = syntax;
    }

    public List<TypeParameter> getTypeParameters() {
        Collections.sort(typeParameters);
        return typeParameters;
    }
    //</editor-fold>

    @Override
    public int compareTo(ExecutableModel item) {
        return this.getUid().compareTo(item.getUid());
    }
}
