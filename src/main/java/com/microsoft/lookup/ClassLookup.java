package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.util.ElementUtil;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.apache.commons.lang3.StringUtils;

public class ClassLookup extends BaseLookup<TypeElement> {

    @Override
    protected ExtendedMetadataFileItem buildMetadataFileItem(TypeElement classElement) {
        String packageName = ElementUtil.extractPackageName(classElement);
        String classQName = String.valueOf(classElement.getQualifiedName());
        String classSName = String.valueOf(classElement.getSimpleName());
        String classQNameWithGenericsSupport = String.valueOf(classElement.asType());
        String classSNameWithGenericsSupport = classQNameWithGenericsSupport.replace(packageName.concat("."), "");

        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem();
        result.setUid(classQName);
        result.setId(classSName);
        result.setParent(packageName);
        result.setHref(classQName + ".yml");
        result.setName(classSNameWithGenericsSupport);
        result.setNameWithType(classSNameWithGenericsSupport);
        result.setFullName(classQNameWithGenericsSupport);
        result.setType(ElementUtil.extractType(classElement));
        result.setPackageName(packageName);
        result.setSummary(ElementUtil.extractComment(classElement));
        result.setContent(ElementUtil.extractClassContent(classElement, classSNameWithGenericsSupport));
        result.setSuperclass(ElementUtil.extractSuperclass(classElement));
        result.setTypeParameters(ElementUtil.extractTypeParameters(classElement));
        result.setTocName(classQName.replace(packageName.concat("."), ""));

        return result;
    }
}
