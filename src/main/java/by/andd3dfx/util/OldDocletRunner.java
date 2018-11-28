package by.andd3dfx.util;

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

        com.sun.tools.javadoc.Main.execute("javadoc", OptionsFileUtil.processOptionsFile(args[0]));
    }
}
