package com.microsoft.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.text.StringEscapeUtils;

@JsonPropertyOrder({"uid", "id", "parent", "children", "href", "langs", "isExternal", "name", "nameWithType",
        "fullName", "overload", "overridden", "type", "package", "summary", "syntax", "inheritance", "implements", "exceptions",
        "spec.java", "inheritedMembers"})
public class MetadataFileItem implements Comparable<MetadataFileItem> {

    private final String uid;
    private String id;
    private String parent;
    private List<String> children = new ArrayList<>();
    private String href;
    private String[] langs;
    private String name;
    private String nameWithType;
    private String fullName;
    private String overload;
    private String overridden;
    private String type;
    @JsonProperty("package")
    private String packageName;
    private String summary;
    private Syntax syntax;
    private List<String> inheritance;
    @JsonProperty("implements")
    private List<String> interfaces;
    private List<ExceptionItem> exceptions;
    private boolean isExternal;
    @JsonProperty("spec.java")
    private List<SpecViewModel> specForJava = new ArrayList<>();
    @JsonProperty("inheritedMembers")
    private List<String> inheritedMethods = new ArrayList<>();

    @Override
    public int compareTo(MetadataFileItem item) {
        return this.getUid().compareTo(item.getUid());
    }

    public MetadataFileItem(String[] langs, String uid) {
        this(uid);
        this.langs = langs;
    }

    public MetadataFileItem(String uid) {
        this.uid = uid;
    }

    public MetadataFileItem(String uid, String name, boolean isExternal) {
        this(uid);
        this.name = name;
        this.nameWithType = name;
        this.fullName = uid;
        this.isExternal = isExternal;
    }

    public MetadataFileItem(String uid, List<SpecViewModel> specs) {
        this(uid);
        this.specForJava = specs;
    }

    public String getUid() {
        return uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<String> getChildren() {
        Collections.sort(children);
        return children;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String[] getLangs() {
        return langs;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getOverload() {
        return overload;
    }

    public void setOverload(String overload) {
        this.overload = handleGenericForOverLoad(overload);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = StringEscapeUtils.unescapeJava(summary);
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public void setSyntax(Syntax syntax) {
        this.syntax = syntax;
    }

    public List<String> getInheritance() {
        return inheritance;
    }

    public void setInheritance(List<String> superclass) {
        this.inheritance = (superclass == null) ? null : superclass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInheritedMethods(List<String> inheritedMethods) {
        this.inheritedMethods = (inheritedMethods == null) ? null : inheritedMethods;
    }

    public List<String> getInheritedMethods() {
        return inheritedMethods;
    }

    public List<SpecViewModel> getSpecForJava() {
        return specForJava;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public List<ExceptionItem> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<ExceptionItem> exceptions) {
        this.exceptions = exceptions;
    }

    public void setContent(String content) {
        if (syntax == null) {
            syntax = new Syntax();
        }
        syntax.setContent(content);
    }

    public void setTypeParameters(List<TypeParameter> typeParameters) {
        if (syntax == null) {
            syntax = new Syntax();
        }
        syntax.setTypeParameters(typeParameters);
    }

    public void setParameters(List<MethodParameter> parameters) {
        if (syntax == null) {
            syntax = new Syntax();
        }
        syntax.setParameters(parameters);
    }

    public void setReturn(Return returnValue) {
        if (syntax == null) {
            syntax = new Syntax();
        }
        syntax.setReturnValue(returnValue);
    }

    public void setOverridden(String overridden) {
        this.overridden = overridden;
    }

    public String getOverridden() {
        return overridden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetadataFileItem that = (MetadataFileItem) o;

        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    public Boolean getIsExternal() {
        return isExternal ? true : null;
    }

    public String handleGenericForOverLoad(String value) {
        return RegExUtils.removeAll(value, "<\\w+(,\\s*\\w+)*>");
    }
}
