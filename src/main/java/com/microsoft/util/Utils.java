package com.microsoft.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class Utils {

    public static boolean isPackagePrivate(Element e) {
        return !(isPublic(e) || isPrivate(e) || isProtected(e));
    }

    public static boolean isPrivate(Element e) {
        return e.getModifiers().contains(Modifier.PRIVATE);
    }

    public static boolean isProtected(Element e) {
        return e.getModifiers().contains(Modifier.PROTECTED);
    }

    public static boolean isPublic(Element e) {
        return e.getModifiers().contains(Modifier.PUBLIC);
    }

    public static boolean isPrivateOrPackagePrivate(Element e) {
        return isPrivate(e) || isPackagePrivate(e);
    }
}
