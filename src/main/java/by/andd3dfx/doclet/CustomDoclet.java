package by.andd3dfx.doclet;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * Based on example from https://docs.oracle.com/en/java/javase/11/docs/api/jdk.javadoc/jdk/javadoc/doclet/package-summary.html
 */
public class CustomDoclet implements Doclet {

    private static final String OUTPUTPATH_PARAM_NAME = "-outputpath";
    private Reporter reporter;

    @Override
    public void init(Locale locale, Reporter reporter) {
        reporter.print(Kind.NOTE, "Doclet using locale: " + locale);
        this.reporter = reporter;
    }

    public void printElement(DocTrees trees, Element e) {
        DocCommentTree docCommentTree = trees.getDocCommentTree(e);
        if (docCommentTree != null) {
            out("Element (" + e.getKind() + ": " + e + ") has the following comments:");
            out("Entire body: " + docCommentTree.getFullBody());
            out("Block tags: " + docCommentTree.getBlockTags());
        }
    }

    @Override
    public boolean run(DocletEnvironment docEnv) {
        reporter.print(Kind.NOTE, "outputPath: " + outputPath);

        // get the DocTrees utility class to access document comments
        DocTrees docTrees = docEnv.getDocTrees();

        for (TypeElement t : ElementFilter.typesIn(docEnv.getIncludedElements())) {
            out(t.getKind() + ":" + t);
            for (Element e : t.getEnclosedElements()) {
                printElement(docTrees, e);
            }
            out("");
        }
        return true;
    }

    @Override
    public String getName() {
        return "Custom Doclet";
    }

    private String outputPath;

    @Override
    public Set<? extends Option> getSupportedOptions() {
        Option[] options = {
            new Option() {
                private final List<String> someOption = Arrays.asList(OUTPUTPATH_PARAM_NAME);

                @Override
                public int getArgumentCount() {
                    return 1;
                }

                @Override
                public String getDescription() {
                    return "output path";
                }

                @Override
                public Kind getKind() {
                    return Kind.STANDARD;
                }

                @Override
                public List<String> getNames() {
                    return someOption;
                }

                @Override
                public String getParameters() {
                    return "path";
                }

                @Override
                public boolean process(String option, List<String> arguments) {
                    outputPath = arguments.get(0);
                    return true;
                }
            }
        };
        return new HashSet<>(Arrays.asList(options));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // support the latest release
        return SourceVersion.latest();
    }

    private static void out(String msg) {
        System.out.println(msg);
    }
}
