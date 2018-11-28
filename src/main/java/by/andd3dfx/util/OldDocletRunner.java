package by.andd3dfx.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * To use runner just pass as commandline param path+name of file with doclet name and its params
 *
 * For example: java OldDocletRunner src\main\resources\doclet-params.txt
 *
 * For Java 9+ use another runner
 * @see by.andd3dfx.util.DocletRunner
 */
@Deprecated
public class OldDocletRunner {

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java OldDocletRunner <doclet-params-filename>");
            return;
        }
        if (!(new java.io.File(args[0])).isFile()) {
            System.err.println(String.format("File '%s' not exists", args[0]));
            return;
        }

        com.sun.tools.javadoc.Main.execute("javadoc", processOptionsFile(args[0]));
    }

    private static String[] processOptionsFile(final String filename) {
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
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } catch (final IOException ioe) {
            ioe.printStackTrace();
            buffer.setLength(0);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        return buffer.toString();
    }
}
