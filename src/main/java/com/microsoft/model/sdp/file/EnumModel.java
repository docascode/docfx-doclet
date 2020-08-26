package com.microsoft.model.sdp.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.model.MetadataFileItem;

import com.microsoft.model.YmlFile;
import com.microsoft.model.sdp.ExecutableModel;
import com.microsoft.model.sdp.FieldModel;
import com.microsoft.model.sdp.TypeBaseModel;
import com.microsoft.util.XrefHelper;
import com.microsoft.util.YamlUtil;

public class EnumModel extends TypeBaseModel implements YmlFile {

    //<editor-fold desc="Properties">
    private final static String METADATA_FILE_HEADER = "### YamlMime:JavaEnum\n";
    private final String fileNameWithPath;

    private List<FieldModel> fields = new ArrayList<>();
    private List<ExecutableModel> methods = new ArrayList<>();
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public EnumModel(MetadataFileItem item, String fileNameWithPath) {
        super(item.getUid(), item.getName());
        this.fileNameWithPath = fileNameWithPath;
        this.setFullName(item.getFullName());
        this.setNameWithType(item.getNameWithType());
        this.setPackageName(item.getPackageName());

        if (!(item.getSummary() == null || item.getSummary().isEmpty())) {
            this.setSummary(item.getSummary());
        }

        item.getInterfaces().forEach(
                type -> this.getImplementedInterfaces().add(XrefHelper.generateXrefString(type, XrefHelper.XrefOption.SHORTNAME)));
        item.getInheritance().forEach(
                type -> this.getInheritances().add(XrefHelper.generateXrefString(type, XrefHelper.XrefOption.DEFAULT)));

        this.getInheritedMembers().addAll(item.getInheritedMethods());

        if (item.getSyntax() != null) {
            this.setSyntax(item.getSyntax().getContent());
        }

        for (var child : item.getChildren()) {
            String type = child.getType();
            switch (type.toLowerCase()) {
                case "field":
                    this.getFields().add(new FieldModel(child));
                    break;
                case "method":
                    this.getMethods().add(new ExecutableModel(child));
                    break;
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="Property Accessors">
    public List<FieldModel> getFields() {
        Collections.sort(fields);
        return fields;
    }

    public List<ExecutableModel> getMethods() {
        Collections.sort(methods);
        return methods;
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
