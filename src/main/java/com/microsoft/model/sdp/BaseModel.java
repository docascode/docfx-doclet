package com.microsoft.model.sdp;

public class BaseModel {

    //<editor-fold desc="Properties">
    private String uid;
    private String fullName;
    private String name;
    private String nameWithType;
    private String summary;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public BaseModel(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public BaseModel(String uid, String name, String fullName, String nameWithType) {
        this.uid = uid;
        this.fullName = fullName;
        this.name = name;
        this.nameWithType = nameWithType;
    }
    //</editor-fold>

    //<editor-fold desc="Property Accessors">
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameWithType() {
        return nameWithType;
    }

    public void setNameWithType(String nameWithType) {
        this.nameWithType = nameWithType;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
    //</editor-fold>
}
