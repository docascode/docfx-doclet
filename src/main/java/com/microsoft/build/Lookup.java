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

    public Lookup(List<MetadataFile> packageMetadataFiles, List<MetadataFile> classMetadataFiles) {
        consume(packageMetadataFiles);
        consume(classMetadataFiles);
    }

    public LookupContext buildContext(MetadataFile metadataFile) {
        Map<String, String> localLookup = localLookupByFileName.get(metadataFile.getFileName());
        return new LookupContext(globalLookup, localLookup);
    }

    private void consume(List<MetadataFile> metadataFiles) {
        metadataFiles.forEach(file -> {
            /**
             * It's important to use LinkedHashMap here, to put item related with owner class on first place.
             * Logic of {@link YmlFilesBuilder#resolveUidByLookup} based on this for case when @link starts from '#'
             */
            Map<String, String> map = new LinkedHashMap<>();
            file.getItems().forEach(item -> {
                map.put(RegExUtils.removeAll(item.getNameWithType(), "<.*?>"), item.getUid());
                map.put(item.getUid(), item.getUid());
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
