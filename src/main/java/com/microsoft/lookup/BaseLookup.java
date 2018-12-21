package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.model.ExceptionItem;
import com.microsoft.model.MethodParameter;
import com.microsoft.model.Return;
import com.microsoft.model.TypeParameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;

public abstract class BaseLookup<T> {

    protected Map<T, ExtendedMetadataFileItem> map = new HashMap<>();

    protected ExtendedMetadataFileItem resolve(T key) {
        ExtendedMetadataFileItem value = map.get(key);
        if (value == null) {
            value = buildMetadataFileItem(key);
            map.put(key, value);
        }
        return value;
    }

    protected abstract ExtendedMetadataFileItem buildMetadataFileItem(T key);

    public String extractPackageName(T key) {
        return resolve(key).getPackageName();
    }

    public String extractFullName(T key) {
        return resolve(key).getFullName();
    }

    public String extractName(T key) {
        return resolve(key).getName();
    }

    public String extractHref(T key) {
        return resolve(key).getHref();
    }

    public String extractParent(T key) {
        return resolve(key).getParent();
    }

    public String extractId(T key) {
        return resolve(key).getId();
    }

    public String extractUid(T key) {
        return resolve(key).getUid();
    }

    public String extractNameWithType(T key) {
        return resolve(key).getNameWithType();
    }

    public String extractMethodContent(T key) {
        return resolve(key).getMethodContent();
    }

    public String extractFieldContent(T key) {
        return resolve(key).getFieldContent();
    }

    public String extractConstructorContent(T key) {
        return resolve(key).getConstructorContent();
    }

    public String extractOverload(T key) {
        return resolve(key).getOverload();
    }

    public List<MethodParameter> extractParameters(T key) {
        return resolve(key).getParameters();
    }

    public List<ExceptionItem> extractExceptions(T key) {
        return resolve(key).getExceptions();
    }

    public Return extractReturn(T key) {
        return resolve(key).getReturn();
    }

    public String extractSummary(T key) {
        return resolve(key).getSummary();
    }

    public String extractType(T key) {
        return resolve(key).getType();
    }

    public String extractContent(T key) {
        return resolve(key).getContent();
    }

    public List<TypeParameter> extractTypeParameters(T key) {
        return resolve(key).getTypeParameters();
    }

    public String extractSuperclass(T key) {
        return resolve(key).getSuperclassValue();
    }

    public String extractTocName(T key) {
        return resolve(key).getTocName();
    }
}
