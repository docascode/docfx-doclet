package com.microsoft.util;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import org.junit.Test;

public class OptionsFileUtilTest {

    private final String PARAMS_DIR = "src/test/resources/test-doclet-params.txt";

    @Test
    public void processOptionsFile() {
        String[] strings = OptionsFileUtil.processOptionsFile(PARAMS_DIR);

        assertThat("Wrong result", Arrays.asList(strings), hasItems(
            "-doclet", "com.microsoft.doclet.DocFxDoclet",
            "-sourcepath", "./src/test/java",
            "-outputpath", "./target/test-out",
            "-encoding", "UTF-8",
            "-excludepackages",
            "com\\.microsoft\\.samples\\.someexcludedpack.*:com\\.microsoft\\.samples\\.someunexistingpackage",
            "-excludeclasses",
            "com\\.microsoft\\.samples\\.subpackage\\.SomeExcluded.*:com\\.microsoft\\.samples\\.subpackage\\.SomeUnexistingClass",
            "-subpackages", "com.microsoft.samples"));
    }
}
