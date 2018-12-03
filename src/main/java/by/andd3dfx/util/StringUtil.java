package by.andd3dfx.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private final static Pattern PATTERN = Pattern.compile("[A-Z]");

    public static String replaceUppercaseWithUnderscoreWithLowercase(String str) {
        Matcher matcher = PATTERN.matcher(str);
        return matcher.replaceAll(matchResult -> "_" + matchResult.group().toLowerCase());
    }
}
