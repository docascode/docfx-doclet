package com.microsoft.doclet;

import com.microsoft.tmp.YmlFilesBuilderImpl;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic.Kind;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.apache.commons.lang3.StringUtils;

public class DocFxDoclet implements Doclet {

    private Reporter reporter;

    @Override
    public void init(Locale locale, Reporter reporter) {
        reporter.print(Kind.NOTE, "Doclet using locale: " + locale);
        this.reporter = reporter;
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        if (StringUtils.isBlank(this.outputPath)) {
            reporter.print(Kind.ERROR, "Output path not specified");
            return false;
        }

        reporter.print(Kind.NOTE, "Output path: " + outputPath);

        return (new YmlFilesBuilderImpl(environment, outputPath)).build();
    }

    @Override
    public String getName() {
        return "DocFxDoclet";
    }

    private String outputPath;

    @Override
    public Set<? extends Option> getSupportedOptions() {
        Option[] options = {
            new Option() {
                private final List<String> someOption = Arrays.asList(
                    "-outputpath",
                    "--output-path",
                    "-o"
                );

                @Override
                public int getArgumentCount() {
                    return 1;
                }

                @Override
                public String getDescription() {
                    return "Output path";
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
}
