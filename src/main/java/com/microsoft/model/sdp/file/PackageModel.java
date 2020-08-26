package com.microsoft.model.sdp.file;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.YmlFile;
import com.microsoft.model.sdp.BaseModel;
import com.microsoft.util.YamlUtil;


public class PackageModel extends BaseModel implements YmlFile {

    //<editor-fold desc="Properties">
    private final static String METADATA_FILE_HEADER = "### YamlMime:JavaPackage\n";
    private final String fileNameWithPath;

    private List<String> classes = new ArrayList<>();
    private List<String> enums = new ArrayList<>();
    private List<String> interfaces = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("metadata")
    private Object metadata = new Object();
    @JsonProperty("package")
    private String packageName;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public PackageModel(MetadataFileItem item, String fileNameWithPath) {
        super(item.getUid(), item.getName());
        this.fileNameWithPath = fileNameWithPath;
        this.setFullName(item.getFullName());
        this.setPackageName(item.getFullName());

        if (!(item.getSummary() == null || item.getSummary().isEmpty())) {
            this.setSummary(item.getSummary());
        }

        for (var child : item.getChildren()) {
            String type = child.getType();
            switch (type.toLowerCase()) {
                case "class":
                    this.getClasses().add(child.getUid());
                    break;
                case "enum":
                    this.getEnums().add(child.getUid());
                    break;
                case "interface":
                    this.getInterfaces().add(child.getUid());
                    break;
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Property Accessor">
    public List<String> getClasses() {
        Collections.sort(classes);
        return classes;

    }

    public List<String> getEnums() {
        Collections.sort(enums);
        return enums;
    }

    public List<String> getInterfaces() {

        Collections.sort(interfaces);
        return interfaces;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    //</editor-fold>

    @JsonIgnore
    @Override
    public String getFileContent() {
        return METADATA_FILE_HEADER + YamlUtil.objectToYamlString(this, this.getFileNameWithPath());
    }

    @JsonIgnore
    @Override
    public String getFileNameWithPath() {
        return fileNameWithPath;
    }
}
