package com.microsoft.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    /**
     * Dump string to file. Create required folders when needed
     */
    public static void dumpToFile(String content, String fileName) {

        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, content.getBytes());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
