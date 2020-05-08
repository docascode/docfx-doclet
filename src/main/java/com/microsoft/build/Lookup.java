package com.microsoft.build;

import static org.apache.commons.lang3.RegExUtils.removeAll;
import static org.apache.commons.lang3.RegExUtils.replaceAll;

import com.microsoft.model.MetadataFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Lookup {

    private Map<String, String> globalLookup = new HashMap<>();
    private Map<String, Map<String, String>> localLookupByFileName = new HashMap<>();

    private final String UID_PACKAGE_NAME_REGEXP = "^.*?\\.(?=[A-Z].*)";
    private final String PARAM_PACKAGE_NAME_REGEXP = "(?<=[\\( ]).*?(?=[A-Z].*)";
    private final String METHOD_PARAMS_REGEXP = "\\s[^\\s]+?(?=[,)])";

    public Lookup(List<MetadataFile> packageMetadataFiles, List<MetadataFile> classMetadataFiles) {
        consume(packageMetadataFiles);
        consume(classMetadataFiles);
    }

    public LookupContext buildContext(MetadataFile metadataFile) {
        Map<String, String> localLookup = localLookupByFileName.get(metadataFile.getFileNameWithPath());
        return new LookupContext(globalLookup, localLookup);
    }

    /**
     * For each such item from items and references of metadata file:
     * <pre>
     * - uid: "com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String, boolean)"
     *   nameWithType: "Person<T>.setFirstName(String firstName, boolean flag)"
     *   ...
     * </pre>
     * add several key-value pairs to lookup where keys are:
     * <ul>
     * <li>Name with type without generics: <br/>Person.setFirstName(String firstName, boolean flag)</li>
     * <li>Uid as is: <br/>com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String, boolean)</li>
     * <li>Uid with param types without package: <br/>com.microsoft.samples.subpackage.Person.setFirstName(String,
     * boolean)</li>
     * <li>Uid without package: <br/>Person.setFirstName(java.lang.String, boolean)</li>
     * <li>Name with type without generics and param names: <br/>Person.setFirstName(String, boolean)</li>
     * <li>Name with type as is: <br/>Person<T>.setFirstName(String, boolean)</li>
     * </ul>
     * and value equals to uid: <br/>com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String)
     */
    private void consume(List<MetadataFile> metadataFiles) {
        metadataFiles.forEach(file -> {
            /**
             * It's important to use LinkedHashMap here, to put item related with owner class on first place.
             * Logic of {@link YmlFilesBuilder#resolveUidByLookup} based on this for case when @link starts from '#'
             */
            Map<String, String> map = new LinkedHashMap<>();
            Map<String, String> specForJavaMap = new LinkedHashMap<>();

            file.getItems().forEach(item -> {
                String uid = item.getUid();
                String nameWithType = item.getNameWithType();
                String nameWithTypeWithoutGenerics = removeAll(nameWithType, "<.*?>");

                map.put(nameWithTypeWithoutGenerics, uid);    // This item should go first
                map.put(uid, uid);
                map.put(removeAll(uid, PARAM_PACKAGE_NAME_REGEXP), uid);
                map.put(removeAll(uid, UID_PACKAGE_NAME_REGEXP), uid);
                map.put(removeAll(nameWithTypeWithoutGenerics, METHOD_PARAMS_REGEXP), uid);
                map.put(removeAll(nameWithType, METHOD_PARAMS_REGEXP), uid);

                map.put(replaceAll(uid, ",", ", "), uid);
                map.put(replaceAll(removeAll(uid, PARAM_PACKAGE_NAME_REGEXP), ",", ", "), uid);
                map.put(replaceAll(removeAll(uid, UID_PACKAGE_NAME_REGEXP), ",", ", "), uid);
                map.put(replaceAll(removeAll(nameWithTypeWithoutGenerics, METHOD_PARAMS_REGEXP), ", ", ","), uid);
            });

            file.getReferences().forEach(item -> {
                map.put(item.getUid(), item.getUid());

                // complex types are recorded in "specForJava" as arrayList of items, thus it has no "NameWithType"
                // thus we need to get every reference item from specForJava, and add to localLookup
                if (item.getNameWithType() == null || item.getNameWithType().isEmpty()) {
                    item.getSpecForJava().forEach(spec -> {
                        specForJavaMap.put(spec.getName(), spec.getUid());
                        specForJavaMap.put(spec.getFullName(), spec.getUid());
                    });
                } else {
                    map.put(item.getNameWithType(), item.getUid());
                }
            });

            // to avoid conflict, the items from specForJava should only add to localLookup
            globalLookup.putAll(map);
            map.putAll(specForJavaMap);
            localLookupByFileName.put(file.getFileNameWithPath(), map);
        });
    }
}
