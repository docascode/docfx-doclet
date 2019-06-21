package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.util.YamlUtil;

import java.io.File;
import java.util.*;

public class MetadataFile implements YmlFile {

    private final static String METADATA_FILE_HEADER = "### YamlMime:ManagedReference\n";
    private final String outputPath;
    private String fileName;
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
        Set<MetadataFileItem> sortedSet = new TreeSet<>(this.items);
        this.items = sortedSet;
        return METADATA_FILE_HEADER + YamlUtil.objectToYamlString(this);
    }

    @JsonIgnore
    @Override
    public String getFileNameWithPath() {
        return outputPath + File.separator + fileName;
    }

    @JsonIgnore
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
