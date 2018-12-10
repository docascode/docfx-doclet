package com.microsoft.util;

import static com.microsoft.util.FileUtilTest.deleteDirectoryStream;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class DocletRunnerTest {

    private final String PARAMS_DIR = "src/test/resources/test-doclet-params.txt";
    private final String EXPECTED_GENERATED_FILES_DIR = "src/test/resources/expected-generated-files";
    private final String OUTPUT_DIR = "target/test-out";

    @Before
    public void cleanup() throws IOException {
        deleteDirectoryStream(OUTPUT_DIR);
    }

    @Test
    public void testFilesGeneration() throws IOException {
        DocletRunner.main(new String[]{PARAMS_DIR});

        List<Path> expectedFilePaths = Files.list(Path.of(EXPECTED_GENERATED_FILES_DIR)).collect(Collectors.toList());
        List<Path> generatedFilePaths = Files.list(Path.of(OUTPUT_DIR)).collect(Collectors.toList());
        assertThat("Wrong files count", generatedFilePaths.size(), is(expectedFilePaths.size()));

        for (Path expectedFilePath : expectedFilePaths) {
            Path generatedFilePath = Path.of(OUTPUT_DIR, expectedFilePath.getFileName().toString());

            String generatedFileContent = Files.readString(generatedFilePath);
            String expectedFileContent = Files.readString(expectedFilePath);

            assertThat("Wrong file content for file " + generatedFilePath,
                generatedFileContent, is(expectedFileContent));
        }
    }
}
