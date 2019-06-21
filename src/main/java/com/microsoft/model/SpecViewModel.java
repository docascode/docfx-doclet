package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"uid", "name", "fullName", "isExternal"})
public class SpecViewModel {

    private String Uid;

    private String Name;

    private String FullName;

    private boolean isExternal;

    public SpecViewModel(String uid, String name, String fullName) {
        this.Uid = uid;
        this.Name = name;
        this.FullName = fullName;
    }

    public SpecViewModel(String name, String fullName) {
        this.Name = name;
        this.FullName = fullName;
    }

    public String getUid() {
        return Uid;
    }

    public String getName() {
        return Name;
    }

    public String getFullName() {
        return FullName;
    }
}
