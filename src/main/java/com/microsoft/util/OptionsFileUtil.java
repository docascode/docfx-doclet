package com.microsoft.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class OptionsFileUtil {

    public static String[] processOptionsFile(String filename) {
        List<String> jargs = new ArrayList<>();

        String options = readOptionsFromFile(filename);
        StringTokenizer tokens = new StringTokenizer(options);
        while (tokens.hasMoreTokens()) {
            jargs.add(tokens.nextToken());
        }

        return jargs.toArray(new String[0]);
    }

    private static String readOptionsFromFile(String filename) {
        StringBuffer buffer = new StringBuffer();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } catch (IOException ioe) {
            buffer.setLength(0);
            throw new RuntimeException("Error during reading options from file", ioe);
        }

        return buffer.toString();
    }
}
