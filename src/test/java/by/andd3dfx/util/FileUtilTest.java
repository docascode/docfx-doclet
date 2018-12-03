package by.andd3dfx.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileUtilTest {

    private final String ROOT_DIR = "target/dir1";
    private final String FILE_NAME = ROOT_DIR + "/dir2/out.txt";

    @Before
    public void setup() throws IOException {
        deleteDirectoryStream(ROOT_DIR);
    }

    @After
    public void tearDown() throws IOException {
        deleteDirectoryStream(ROOT_DIR);
    }

    @Test
    public void dumpToFile() throws IOException {
        String content = "Bla-bla content";

        FileUtil.dumpToFile(content, FILE_NAME);

        assertThat("File should be present", Files.exists(Paths.get(FILE_NAME)), is(true));
        assertThat("Invalid file content", Files.readString(Paths.get(FILE_NAME)), is(content));
    }

    void deleteDirectoryStream(String pathString) throws IOException {
        Path path = Paths.get(pathString);
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }
}
