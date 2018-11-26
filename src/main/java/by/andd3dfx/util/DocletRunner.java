package by.andd3dfx.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * To use runner just pass two commandline params to main method:
 * - full doclet name and
 * - name of file with doclet params in it
 *
 * For example: java DocletRunner by.andd3dfx.doclet.CustomDoclet src\main\resources\doclet-params.txt
 */
public class DocletRunner {

    public static void main(final String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java DocletRunner <doclet-class-fullname> <doclet-params-filename>");
            return;
        }
        if (!(new java.io.File(args[1])).isFile()) {
            System.err.println(String.format("File '%s' not exists", args[1]));
            return;
        }

        com.sun.tools.javadoc.Main.execute("javadoc", args[0],
            processOptionsFile(args[1]));
    }

    private static String[] processOptionsFile(final String filename) {
        List<String> jargs = new ArrayList<String>() {{
            add("-classpath");
            add(System.getenv("JAVA_HOME") + "\\lib\\tools.jar");
        }};

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
