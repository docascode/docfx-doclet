package com.microsoft.model.sdp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TypeBaseModel extends BaseModel {

    //<editor-fold desc="Properties">
    @JsonProperty("implements")
    private List<String> implementedInterfaces = new ArrayList<>();
    private List<String> inheritances = new ArrayList<>();
    private List<String> inheritedMembers = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("metadata")
    private Object metadata = new Object();
    @JsonProperty("package")
    private String packageName;
    private String syntax;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public TypeBaseModel(String uid, String name) {
        super(uid, name);
    }
    //</editor-fold>

    //<editor-fold desc="Property Accessors">

    public List<String> getImplementedInterfaces() {
        Collections.sort(implementedInterfaces);
        return implementedInterfaces;
    }

    public List<String> getInheritances() {
        return inheritances;
    }

    public List<String> getInheritedMembers() {
        return inheritedMembers;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSyntax() {
        return syntax;
    }

    public void setSyntax(String syntax) {
        this.syntax = syntax;
    }
    //</editor-fold>

}
