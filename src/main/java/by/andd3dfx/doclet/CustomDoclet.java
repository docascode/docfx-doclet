package by.andd3dfx.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import java.text.MessageFormat;

/**
 * Doclet used for just printing class names to console
 *
 * based on: https://www.javaworld.com/article/2076153/java-se/code-generation-using-javadoc.html
 */
public class CustomDoclet {

    private static final MessageFormat METHODINFO =
        new MessageFormat("Method: name = {0}, comment = {1}, return type = {2};");
    private static final MessageFormat FIELDINFO =
        new MessageFormat("Field: name = {0}, comment = {1}, type = {2};");

    private static final String OUTPUTPATH_PARAM_NAME = "-outputpath";

    public static boolean start(RootDoc root) {
        String outputDirectory = readOutputPath(root.options());
        out("Output directory is: " + outputDirectory);

        for (ClassDoc classDoc : root.classes()) {
            out("\nClass: " + classDoc.name());

            for (MethodDoc methodDoc : classDoc.methods()) {
                out(METHODINFO.format(new Object[]{methodDoc.name(), methodDoc.commentText(), methodDoc.returnType()}));
            }

            for (FieldDoc fieldDoc : classDoc.fields()) {
                Object[] field_info = {fieldDoc.name(), fieldDoc.commentText(), fieldDoc.type()};
                out(FIELDINFO.format(field_info));

                for (Tag tag : fieldDoc.tags()) {
                    out("\tField Tag Name= " + tag.name());
                    out("\tField Tag Value = " + tag.text());
                }
            }
        }

        // No error processing done, simply return true.
        return true;
    }

    public static int optionLength(String option) {
        if (option.equals(OUTPUTPATH_PARAM_NAME)) {
            return 2;
        }
        return 0;
    }

    /*
     * Add validations when needed
     */
    public static boolean validOptions(String options[][], DocErrorReporter reporter) {
        return true;
    }

    /*
     * Change language version in case of Javadoc API upgrade
     */
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    private static String readOutputPath(String[][] options) {
        for (String[] opt : options) {
            if (opt[0].equals(OUTPUTPATH_PARAM_NAME)) {
                return opt[1];
            }
        }
        return ".";
    }

    private static void out(String msg) {
        System.out.println(msg);
    }
}
