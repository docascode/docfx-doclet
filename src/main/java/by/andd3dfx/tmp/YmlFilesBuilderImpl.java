package by.andd3dfx.tmp;

import by.andd3dfx.util.FileUtil;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

public class YmlFilesBuilderImpl implements YmlFilesBuilder {

    @Override
    public void buildPackageYmlFile(PackageElement packageElement, String outputPath) {
        // TODO: Add implementation
        String content = "";

        FileUtil.dumpToFile(content, outputPath);
    }

    @Override
    public void buildClassYmlFile(TypeElement typeElement, String outputPath) {
        // TODO: Add implementation
        String content = "";

        FileUtil.dumpToFile(content, outputPath);
    }
}
