package com.microsoft.build;

import com.microsoft.model.MetadataFile;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RegExUtils;

public class Lookup {

    private Map<String, String> globalLookup = new HashMap<>();
    private Map<String, Map<String, String>> localLookupByFileName = new HashMap<>();
    private final String METHOD_PARAMS_REGEXP = "\\s[^\\s]+?(?=[,)])";

    public Lookup(List<MetadataFile> packageMetadataFiles, List<MetadataFile> classMetadataFiles) {
        consume(packageMetadataFiles);
        consume(classMetadataFiles);
    }

    public LookupContext buildContext(MetadataFile metadataFile) {
        Map<String, String> localLookup = localLookupByFileName.get(metadataFile.getFileName());
        return new LookupContext(globalLookup, localLookup);
    }

    /**
     * For each such item from items and references of metadata file:
     * <pre>
     * - uid: "com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String)"
     *   nameWithType: "Person<T>.setFirstName(String firstName)"
     *   ...
     * </pre>
     * We add next records to lookup:
     * <ul>
     *     <li>Person.setFirstName(String firstName) -> com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String)</li>
     *     <li>com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String) -> com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String)</li>
     *     <li>Person.setFirstName(String) -> com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String)</li>
     * </ul>
     */
    private void consume(List<MetadataFile> metadataFiles) {
        metadataFiles.forEach(file -> {
            /**
             * It's important to use LinkedHashMap here, to put item related with owner class on first place.
             * Logic of {@link YmlFilesBuilder#resolveUidByLookup} based on this for case when @link starts from '#'
             */
            Map<String, String> map = new LinkedHashMap<>();
            file.getItems().forEach(item -> {
                String uid = item.getUid();

                map.put(RegExUtils.removeAll(item.getNameWithType(), "<.*?>"), uid);
                map.put(uid, uid);
                map.put(RegExUtils.removeAll(item.getNameWithType(), METHOD_PARAMS_REGEXP), uid);
            });
            file.getReferences().forEach(item -> {
                map.put(item.getNameWithType(), item.getUid());
                map.put(item.getUid(), item.getUid());
            });

            localLookupByFileName.put(file.getFileName(), map);
            globalLookup.putAll(map);
        });
    }
}
