package by.andd3dfx.doclet;

import by.andd3dfx.model.TocFile;
import by.andd3dfx.model.TocItem;
import by.andd3dfx.tmp.YmlFilesBuilder;
import by.andd3dfx.tmp.YmlFilesBuilderImpl;
import by.andd3dfx.util.FileUtil;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.apache.commons.lang3.StringUtils;

public class CustomDoclet implements Doclet {

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

        return buildTocAndYmlFiles(environment, outputPath, new YmlFilesBuilderImpl());
    }

    boolean buildTocAndYmlFiles(DocletEnvironment environment, String outputPath, YmlFilesBuilder ymlFilesBuilder) {
        TocFile resultTocFile = new TocFile();
        for (PackageElement packageElement : ElementFilter.packagesIn(environment.getIncludedElements())) {
            String packageQName = String.valueOf(packageElement.getQualifiedName());
            String packageYmlFileName = packageQName + ".yml";
            ymlFilesBuilder.buildPackageYmlFile(packageElement, outputPath + File.separator + packageYmlFileName);

            TocItem packageTocItem = new TocItem.Builder()
                .setUid(packageQName)
                .setName(packageQName)
                .setHref(packageYmlFileName)
                .build();

            buildFilesForInnerClasses("", packageElement, ymlFilesBuilder, packageTocItem.getItems());

            resultTocFile.getItems().add(packageTocItem);
        }
        FileUtil.dumpToFile(String.valueOf(resultTocFile), outputPath + File.separator + "toc.yml");
        return true;
    }

    void buildFilesForInnerClasses(String namePrefix, Element element, YmlFilesBuilder ymlFilesBuilder, List<TocItem> listToAddItems) {
        for (TypeElement typeElement : ElementFilter.typesIn(element.getEnclosedElements())) {
            String classQName = String.valueOf(typeElement.getQualifiedName());
            String classSimpleName = String.format("%s%s%s",
                namePrefix,
                StringUtils.isEmpty(namePrefix) ? "" : ".",
                String.valueOf(typeElement.getSimpleName()));
            String classYmlFileName = classQName + ".yml";
            ymlFilesBuilder.buildClassYmlFile(typeElement, outputPath + File.separator + classYmlFileName);

            TocItem classTocItem = new TocItem.Builder()
                .setUid(classQName)
                .setName(classSimpleName)
                .setHref(classYmlFileName)
                .build();
            listToAddItems.add(classTocItem);

            buildFilesForInnerClasses(classSimpleName, typeElement, ymlFilesBuilder, listToAddItems);
        }
    }

    @Override
    public String getName() {
        return "CustomDoclet";
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
