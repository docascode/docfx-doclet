package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.util.ElementUtil;
import javax.lang.model.element.PackageElement;

public class PackageLookup extends BaseLookup<PackageElement> {

    @Override
    protected ExtendedMetadataFileItem buildMetadataFileItem(PackageElement packageElement) {
        String qName = String.valueOf(packageElement.getQualifiedName());
        String sName = String.valueOf(packageElement.getSimpleName());

        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem();
        result.setUid(qName);
        result.setId(sName);
        result.setHref(qName + ".yml");
        result.setName(qName);
        result.setNameWithType(qName);
        result.setFullName(qName);
        result.setType(ElementUtil.extractType(packageElement));
        result.setSummary(ElementUtil.extractComment(packageElement));
        result.setContent(ElementUtil.extractPackageContent(packageElement));

        return result;
    }
}
