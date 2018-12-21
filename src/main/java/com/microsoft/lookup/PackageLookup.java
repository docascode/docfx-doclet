package com.microsoft.lookup;

import com.microsoft.util.ElementUtil;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.PackageElement;

public class PackageLookup {

    private static Map<PackageElement, ExtendedMetadataFileItem> map = new HashMap<>();

    public static String extractUid(PackageElement key) {
        return resolve(key).getUid();
    }

    public static String extractId(PackageElement key) {
        return resolve(key).getId();
    }

    public static String extractHref(PackageElement key) {
        return resolve(key).getHref();
    }

    public static String extractName(PackageElement key) {
        return resolve(key).getName();
    }

    public static String extractNameWithType(PackageElement key) {
        return resolve(key).getNameWithType();
    }

    public static String extractFullName(PackageElement key) {
        return resolve(key).getFullName();
    }

    public static String extractType(PackageElement key) {
        return resolve(key).getType();
    }

    public static String extractSummary(PackageElement key) {
        return resolve(key).getSummary();
    }

    public static String extractContent(PackageElement key) {
        return resolve(key).getContent();
    }

    private static ExtendedMetadataFileItem resolve(PackageElement key) {
        ExtendedMetadataFileItem value = map.get(key);
        if (value == null) {
            value = buildMetadataFileItem(key);
            map.put(key, value);
        }
        return value;
    }

    private static ExtendedMetadataFileItem buildMetadataFileItem(PackageElement packageElement) {
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
