package com.microsoft.lookup;

import com.microsoft.lookup.model.ExtendedMetadataFileItem;
import com.microsoft.util.ElementUtil;
import javax.lang.model.element.PackageElement;
import jdk.javadoc.doclet.DocletEnvironment;

public class PackageLookup extends BaseLookup<PackageElement> {

    public PackageLookup(DocletEnvironment environment) {
        super(environment);
    }

    @Override
    protected ExtendedMetadataFileItem buildMetadataFileItem(PackageElement packageElement) {
        String qName = String.valueOf(packageElement.getQualifiedName());
        String sName = String.valueOf(packageElement.getSimpleName());

        ExtendedMetadataFileItem result = new ExtendedMetadataFileItem(qName);
        result.setId(sName);
        result.setHref(qName + ".yml");
        result.setName(qName);
        result.setNameWithType(qName);
        result.setFullName(qName);
        result.setType(determineType(packageElement));
        result.setSummary(determineComment(packageElement));
        result.setContent(determinePackageContent(packageElement));

        return result;
    }

    String determinePackageContent(PackageElement packageElement) {
        return "package " + packageElement.getQualifiedName();
    }
}
