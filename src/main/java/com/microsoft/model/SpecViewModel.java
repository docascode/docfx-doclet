package com.microsoft.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"uid", "name", "fullName", "isExternal"})
public class SpecViewModel {

    private String uid;
    private String name;
    private String fullName;
    private boolean isExternal;

    public SpecViewModel(String uid, String fullName) {
        this.uid = uid;
        this.name = getShortName(fullName);
        this.fullName = fullName;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    String getShortName(String fullName) {

        StringBuilder singleValue = new StringBuilder();
        Optional.ofNullable(fullName).ifPresent(
                Param -> {
                    List<String> strList = new ArrayList<>();
                    strList = Arrays.asList(StringUtils.split(Param, "."));
                    Collections.reverse(strList);
                    singleValue.append(strList.get(0));
                }
        );
        return singleValue.toString();
    }
}
