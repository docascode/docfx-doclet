package com.microsoft.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class MetadataFileItem {

    private String uid;
    private String id;
    private String parent;
    private List<String> children = new ArrayList<>();
    private String href;
    private String name;
    private String nameWithType;
    private String fullName;
    private String overload;
    private String type;
    private String packageName;
    private String summary;
    private String content;
    private List<TypeParameter> typeParameters = new ArrayList<>();
    private List<TypeParameter> parameters = new ArrayList<>();
    private String returnType;
    private String returnDescription;
    private String superclass;
    private List<TypeParameter> exceptions = new ArrayList<>();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
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
        this.overload = overload;
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
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(List<TypeParameter> typeParameters) {
        this.typeParameters = typeParameters;
    }

    public List<TypeParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<TypeParameter> parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public void setReturnDescription(String returnDescription) {
        this.returnDescription = returnDescription;
    }

    public String getSuperclass() {
        return superclass;
    }

    public void setSuperclass(String superclass) {
        this.superclass = superclass;
    }

    public List<TypeParameter> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<TypeParameter> exceptions) {
        this.exceptions = exceptions;
    }

    public String toItemString() {
        String result = "- uid: " + uid + "\n"
            + "  id: " + id + "\n";
        if (parent != null) {
            result += "  parent: " + parent + "\n";
        }
        if (!children.isEmpty()) {
            result += "  children:\n";
            for (String child : children) {
                result += "  - " + child + "\n";
            }
        }
        result += "  href: " + href + "\n"
            + "  langs:\n"
            + "  - java\n"
            + "  name: " + name + "\n"
            + "  nameWithType: " + nameWithType + "\n"
            + "  fullName: " + fullName + "\n";

        if (StringUtils.isNotEmpty(overload)) {
            result += "  overload: " + overload + "\n";
        }

        result += "  type: " + type + "\n";
        if (StringUtils.isNotEmpty(packageName)) {
            result += "  package: " + packageName + "\n";
        }
        result += "  summary: " + summary + "\n"
            + "  syntax:\n"
            + "    content: " + content + "\n";
        if (!typeParameters.isEmpty()) {
            result += "    typeParameters:\n";
            for (TypeParameter typeParameter : typeParameters) {
                result += "    - id: " + typeParameter.getId() + "\n"
                    + "      type: " + typeParameter.getType() + "\n";
            }
        }
        if (!parameters.isEmpty()) {
            result += "    parameters:\n";
            for (TypeParameter parameter : parameters) {
                result += "    - id: " + parameter.getId() + "\n"
                    + "      type: " + parameter.getType() + "\n"
                    + "      description: " + parameter.getDescription() + "\n";
            }
        }
        if (StringUtils.isNotEmpty(returnType)) {
            result += "    return:\n"
                + "      type: " + returnType + "\n";
            if (returnDescription != null) {
                result += "      description: " + returnDescription + "\n";
            }
        }

        if (StringUtils.isNotEmpty(superclass)) {
            result += "  inheritance:\n"
                + "  - " + superclass + "\n";
        }

        if (!exceptions.isEmpty()) {
            result += "  exceptions:\n";
            for (TypeParameter exception : exceptions) {
                result += "  - type: " + exception.getType() + "\n"
                    + "    description: " + exception.getDescription() + "\n";
            }
        }
        return result;
    }

    public String toReferenceString() {
        String result = "- uid: " + uid + "\n"
            + "  parent: " + parent + "\n"
            + "  href: " + href + "\n"
            + "  name: " + name + "\n"
            + "  nameWithType: " + nameWithType + "\n"
            + "  fullName: " + fullName + "\n"
            + "  type: " + type + "\n"
            + "  summary: " + summary + "\n"
            + "  syntax:\n"
            + "    content: " + content + "\n";

        if (typeParameters.isEmpty()) {
            return result;
        }

        result += "    typeParameters:\n";
        for (TypeParameter typeParameter : typeParameters) {
            result += "    - id: " + typeParameter.getId() + "\n"
                + "      type: " + typeParameter.getType() + "\n";
        }
        return result;
    }
}
