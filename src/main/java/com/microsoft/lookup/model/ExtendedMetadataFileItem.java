package com.microsoft.lookup.model;

import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.microsoft.model.TypeParameter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Just container to keep cached precalculated values for lookup
 */
public class ExtendedMetadataFileItem extends MetadataFileItem {

    private String methodContent;
    private String fieldContent;
    private String constructorContent;
    private List<MethodParameter> parameters;
    private Return returnValue;
    private String content;
    private List<TypeParameter> typeParameters;
    private List<String> superclass;
    private String tocName;
    private Set<MetadataFileItem> references = new LinkedHashSet<>();
    private Integer nestedLevel;

    public ExtendedMetadataFileItem(String uid) {
        super(uid);
    }

    public String getMethodContent() {
        return methodContent;
    }

    public void setMethodContent(String methodContent) {
        this.methodContent = methodContent;
    }

    public String getFieldContent() {
        return fieldContent;
    }

    public void setFieldContent(String fieldContent) {
        this.fieldContent = fieldContent;
    }

    public String getConstructorContent() {
        return constructorContent;
    }

    public void setConstructorContent(String constructorContent) {
        this.constructorContent = constructorContent;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public Integer getNestedLevel() {return nestedLevel;}

    public void setNestedLevel(Integer level) {this.nestedLevel = level;}

    @Override
    public void setParameters(List<MethodParameter> parameters) {
        this.parameters = parameters;
    }

    public Return getReturn() {
        return returnValue;
    }

    @Override
    public void setReturn(Return returnValue) {
        this.returnValue = returnValue;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    public List<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    @Override
    public void setTypeParameters(List<TypeParameter> typeParameters) {
        this.typeParameters = typeParameters;
    }

    public List<String> getSuperclass() {
        return superclass;
    }

    public void setSuperclass(List<String>  superclass) {
        this.superclass = superclass;
    }

    public void setTocName(String tocName) {
        this.tocName = tocName;
    }

    public String getTocName() {
        return tocName;
    }

    public void addReferences(Set<MetadataFileItem> references) {
        this.references.addAll(references);
    }

    public Set<MetadataFileItem> getReferences() {
        return references;
    }
}
