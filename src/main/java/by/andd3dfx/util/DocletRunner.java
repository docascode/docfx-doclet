package by.andd3dfx.util;

import javax.tools.ToolProvider;

/**
 * To use runner just pass as commandline param to main method:
 * - name of file with doclet name amd params
 *
 * For example: java DocletRunner src\main\resources\doclet-params.txt
 */
public class DocletRunner {

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java DocletRunner <doclet-params-filename>");
            return;
        }
        if (!(new java.io.File(args[0])).isFile()) {
            System.err.println(String.format("File '%s' not exists", args[0]));
            return;
        }

        ToolProvider.getSystemDocumentationTool().run(null, null, null, OptionsFileUtil.processOptionsFile(args[0]));
    }
}
