package com.microsoft.lookup;

import com.microsoft.model.MetadataFileItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import java.util.List;

/**
 * Just container to keep cached precalculated values for lookup
 */
class ExtendedMetadataFileItem extends MetadataFileItem {

    private String methodContent;
    private String fieldContent;
    private String constructorContent;
    private List<MethodParameter> parameters;
    private Return returnValue;

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
}
