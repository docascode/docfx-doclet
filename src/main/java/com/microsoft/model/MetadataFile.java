package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.util.YamlUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MetadataFile implements YmlFile {

    private final static String METADATA_FILE_HEADER = "### YamlMime:ManagedReference\n";
    private final String outputPath;
    private final String fileName;
    private Set<MetadataFileItem> items = new LinkedHashSet<>();
    private Set<MetadataFileItem> references = new LinkedHashSet<>();

    public MetadataFile(String outputPath, String fileName) {
        this.outputPath = outputPath;
        this.fileName = fileName;
    }

    public Set<MetadataFileItem> getItems() {
        return items;
    }

    public Set<MetadataFileItem> getReferences() {
        return references;
    }

    @JsonIgnore
    @Override
    public String getFileContent() {
        return METADATA_FILE_HEADER + YamlUtil.objectToYamlString(this);
    }

    @JsonIgnore
    @Override
    public String getFileNameWithPath() {
        return outputPath + File.separator + fileName;
    }
}
