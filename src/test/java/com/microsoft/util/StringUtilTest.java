package com.microsoft.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class StringUtilTest {

    @Test
    public void replaceUppercaseWithUnderscoreWithLowercase() {
        String result = StringUtil.replaceUppercaseWithUnderscoreWithLowercase("CustomIdentificationCreator");

        assertThat("Wrong result", result, is("_custom_identification_creator"));
    }
}
