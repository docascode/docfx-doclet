package com.microsoft.model.sdp.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.TypeParameter;
import com.microsoft.model.YmlFile;
import com.microsoft.model.sdp.ExecutableModel;
import com.microsoft.model.sdp.TypeBaseModel;
import com.microsoft.model.sdp.file.MemberModel;
import com.microsoft.util.FileUtil;
import com.microsoft.util.XrefHelper;
import com.microsoft.util.YamlUtil;

import java.io.File;
import java.util.*;

public class TypeModel extends TypeBaseModel implements YmlFile {

    //<editor-fold desc="Properties">
    private final static String METADATA_FILE_HEADER = "### YamlMime:JavaType\n";
    private final String fileNameWithPath;
    private String outputPath;

    private List<String> constructors = new ArrayList<>();
    private List<String> fields = new ArrayList<>();
    private List<String> methods = new ArrayList<>();
    private String type;
    private List<TypeParameter> typeParameters = new ArrayList<>();

    @JsonIgnore
    private Map<String, MemberModel> memberCache = new HashMap<>();
    //</editor-fold>

    @JsonIgnore
    //<editor-fold desc="Constructors">
    public TypeModel(MetadataFileItem item, String fileNameWithPath, String outputPath) {
        super(item.getUid(), item.getName());
        this.outputPath = outputPath;
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
        this.setType(item.getType());

        if (item.getSyntax() != null) {
            this.setSyntax(item.getSyntax().getContent());
            this.getTypeParameters().addAll(item.getSyntax().getTypeParameters());
        }

        for (var child : item.getChildren()) {
            String type = child.getType();
            switch (type.toLowerCase()) {
                case "constructor":
                    if (this.type.toLowerCase().equals("class")) {
                        this.getConstructors().add(child.getUid());
                        buildMember(child, memberCache);
                    }
                    break;
                case "field":
                    this.getFields().add(child.getUid());
                    buildField(child);
                    break;
                case "method":
                    this.getMethods().add(child.getUid());
                    buildMember(child, memberCache);
                    break;
            }

            memberCache.forEach((k, v) -> FileUtil.dumpToFile(v));
        }
    }
    //</editor-fold>

    //<editor-fold desc="Property Accessor">
    public List<String> getConstructors() {
        Collections.sort(constructors);
        return constructors;
    }

    public List<String> getFields() {
        Collections.sort(fields);
        return fields;
    }

    public List<String> getMethods() {
        Collections.sort(methods);
        return methods;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(List<TypeParameter> typeParameters) {
        this.typeParameters = typeParameters;
    }
    //</editor-fold>


    private void buildMember(MetadataFileItem child, Map<String, MemberModel> memberCache) {
        String methodUid = child.getOverload();
        if (memberCache.containsKey(methodUid)) {
            memberCache.get(methodUid).getMembers().add(new ExecutableModel(child));
        } else {
            memberCache.put(methodUid, new MemberModel(methodUid, child, this.outputPath));
        }
    }

    private void buildField(MetadataFileItem child) {
        MemberModel fieldModel =  new MemberModel(child, this.outputPath);
        FileUtil.dumpToFile(fieldModel);

    }

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
