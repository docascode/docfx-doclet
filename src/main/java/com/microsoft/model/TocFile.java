package com.microsoft.model;

import com.microsoft.util.YamlUtil;
import java.io.File;
import java.util.*;

public class TocFile extends ArrayList<TocItem> implements YmlFile {

    private final static String TOC_FILE_HEADER = "### YamlMime:TableOfContent\n";
    private final static String TOC_FILE_NAME = "toc.yml";
    private final String outputPath;
    private  ArrayList<TocItem> items = new ArrayList<>();

    public TocFile(String outputPath) {
        this.outputPath = outputPath;
    }

    public void addTocItem(TocItem packageTocItem) {
        add(packageTocItem);
    }

    public ArrayList<TocItem> getItems() {
        Collections.sort(items, Comparator.comparing(TocItem::getUid));
        return items;
    }

    @Override
    public String getFileContent() {
        return TOC_FILE_HEADER + YamlUtil.objectToYamlString(this, TOC_FILE_NAME);
    }

    @Override
    public String getFileNameWithPath() {
        return outputPath + File.separator + TOC_FILE_NAME;
    }
}
