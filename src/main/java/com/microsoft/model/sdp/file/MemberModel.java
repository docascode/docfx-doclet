package com.microsoft.model.sdp.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RegExUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.YmlFile;
import com.microsoft.model.sdp.BaseModel;
import com.microsoft.model.sdp.ExecutableModel;
import com.microsoft.util.YamlUtil;

public class MemberModel extends BaseModel implements YmlFile {

    //<editor-fold desc="Properties">
    private final static String METADATA_FILE_HEADER = "### YamlMime:JavaMember\n";
    private final String outputPath;

    private List<ExecutableModel> members = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("metadata")
    private Object metadata = new Object();
    @JsonProperty("package")
    private String packageName;
    private String type;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public MemberModel(String uid, MetadataFileItem item, String outputPath) {
        super(uid, item.getName());
        this.outputPath = outputPath;
        this.setFullName(RegExUtils.removeAll(item.getFullName(), "\\(.*\\)$"));
        this.setName(RegExUtils.removeAll(item.getName(), "\\(.*\\)$"));
        this.setNameWithType(RegExUtils.removeAll(item.getNameWithType(), "\\(.*\\)$"));
        this.setPackageName(item.getPackageName());
        this.setType(item.getType());

        members.add(new ExecutableModel(item));

    }

    public MemberModel(MetadataFileItem item, String outputPath) {
        super(item.getUid(), item.getName());
        this.outputPath = outputPath;
        this.setFullName(item.getFullName());
        this.setNameWithType(item.getNameWithType());
        this.setPackageName(item.getPackageName());
        this.setType(item.getType());

        members.add(new ExecutableModel(item));

    }
    //</editor-fold>

    //<editor-fold desc="Property Accessor">
    public List<ExecutableModel> getMembers() {
        return members;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getType() {
        return type.toLowerCase();
    }

    public void setType(String type) {
        this.type = type;
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
        return this.outputPath + File.separator + this.getFileName();
    }

    private String getFileName() {
        return this.getUid().replace("*", "") + ".yml";
    }
}
