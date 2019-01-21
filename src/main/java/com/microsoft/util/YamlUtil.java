package com.microsoft.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public class YamlUtil {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
        .disable(Feature.WRITE_DOC_START_MARKER)
        .disable(Feature.SPLIT_LINES)
    )
        .setSerializationInclusion(Include.NON_NULL)
        .setSerializationInclusion(Include.NON_EMPTY);

    public static String objectToYamlString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Could not serialize object to yaml string", jpe);
        }
    }
}
