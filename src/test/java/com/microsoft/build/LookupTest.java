package com.microsoft.build;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.microsoft.model.MetadataFile;
import com.microsoft.model.MetadataFileItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class LookupTest {

    private Lookup lookup;
    private String packageUid = "package uid";
    private String packageNameWithType = "package name with type";
    private String classUid = "com.microsoft.samples.subpackage.Person.setFirstName(java.lang.String)";
    private String classNameWithType = "Person<T>.setFirstName(String firstName)";

    private List<MetadataFile> packageFiles = new ArrayList<>() {{
        MetadataFile packageFile = new MetadataFile("package path", "package name");
        packageFile.getItems().add(buildMetadataFileItem(packageUid, packageNameWithType));
        add(packageFile);
    }};
    private List<MetadataFile> classFiles = new ArrayList<>() {{
        MetadataFile classFile = new MetadataFile("class path", "class name");
        classFile.getItems().add(buildMetadataFileItem(classUid, classNameWithType));
        add(classFile);
    }};

    @Before
    public void setUp() {
        lookup = new Lookup(packageFiles, classFiles);
    }

    @Test
    public void buildContext() {
        LookupContext context = lookup.buildContext(classFiles.get(0));

        String localKey = classNameWithType.replaceAll("<.*>", "");
        String localKeyWithoutParamNames = classNameWithType.replaceAll("\\s[^\\s]+?(?=[,)])", "");
        assertThat("Wrong owner uid", context.getOwnerUid(), is(localKey));

        assertThat("Context should contain local key", context.containsKey(localKey), is(true));
        assertThat("Context shouldn't contain unknown key", context.containsKey("unknown key"), is(false));
        assertThat("Context should contain global key", context.containsKey(packageNameWithType), is(true));
        assertThat("Wrong value for local key", context.resolve(localKey), is(classUid));
        assertThat("Wrong value for global key", context.resolve(packageNameWithType), is(packageUid));

        assertThat("Context should contain local value as a key", context.containsKey(classUid), is(true));
        assertThat("Wrong value for local value as a key", context.resolve(classUid), is(classUid));
        assertThat("Context should contain global value as a key", context.containsKey(packageUid), is(true));
        assertThat("Wrong value for local value as a key", context.resolve(packageUid), is(packageUid));

        assertThat("Context should contain local key without param names",
            context.containsKey(localKeyWithoutParamNames), is(true));
        assertThat("Wrong value for local key without param names",
            context.resolve(localKeyWithoutParamNames), is(classUid));
    }

    private MetadataFileItem buildMetadataFileItem(String uid, String nameWithType) {
        MetadataFileItem result = new MetadataFileItem(uid);
        result.setNameWithType(nameWithType);
        return result;
    }
}
