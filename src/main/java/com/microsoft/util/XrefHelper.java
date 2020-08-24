package com.microsoft.util;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;

public class XrefHelper {

    public static String generateXrefString(String typeString, XrefOption option) {
        List<String> types = replaceUidAndSplit(typeString);
        StringBuilder xrefBuilder = new StringBuilder();

        Optional.ofNullable(types).ifPresent(
                ref -> types.forEach(
                        uid -> {
                            if (uid.equalsIgnoreCase("<")
                                    || uid.equalsIgnoreCase(">")
                                    || uid.equalsIgnoreCase(",")
                                    || uid.equalsIgnoreCase("[]")
                                    || uid.equalsIgnoreCase("?"))
                                xrefBuilder.append(StringEscapeUtils.escapeHtml4(uid));
                            else if (!"".equals(uid)) {
                                switch (option) {
                                    case DEFAULT : xrefBuilder.append(getXrefStringDefault(uid));
                                        break;
                                    case SHORTNAME: xrefBuilder.append(getXrefStringWithShortName(uid));
                                        break;
                                }
                            }
                        })
        );

        return xrefBuilder.toString() ;
    }

    static List<String> replaceUidAndSplit(String uid) {
        String retValue = RegExUtils.replaceAll(uid, "\\<", "//<//");
        retValue = RegExUtils.replaceAll(retValue, "\\>", "//>//");
        retValue = RegExUtils.replaceAll(retValue, ",", "//,//");
        retValue = RegExUtils.replaceAll(retValue, "\\[\\]", "//[]//");

        return Arrays.asList(StringUtils.split(retValue, "//"));
    }

    static String getShortName(String uid) {

        StringBuilder singleValue = new StringBuilder();
        Optional.ofNullable(uid).ifPresent(
                Param -> {
                    List<String> strList = new ArrayList<>();
                    strList = Arrays.asList(StringUtils.split(Param, "."));
                    singleValue.append(strList.get(strList.size() - 1));
                }
        );
        return singleValue.toString();
    }

    static String getXrefStringDefault(String uid) {
        return String.format("<xref href=\"%s\" data-throw-if-not-resolved=\"False\" />", uid);
    }

    static String getXrefStringWithShortName(String uid) {
        return String.format("<xref href=\"%s?alt=%s&text=%s\" data-throw-if-not-resolved=\"False\" />", uid, uid, getShortName(uid));
    }

    public enum XrefOption {
        DEFAULT,
        SHORTNAME
    }
}