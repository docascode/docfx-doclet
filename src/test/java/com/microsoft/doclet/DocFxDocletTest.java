package com.microsoft.doclet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.microsoft.doclet.DocFxDoclet.CustomOption;
import com.microsoft.doclet.DocFxDoclet.FakeOptionForCompatibilityWithStandardDoclet;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.SourceVersion;
import jdk.javadoc.doclet.Doclet.Option.Kind;
import org.junit.Before;
import org.junit.Test;

public class DocFxDocletTest {

    private DocFxDoclet doclet;

    @Before
    public void setup() {
        doclet = new DocFxDoclet();
    }

    @Test
    public void getSupportedSourceVersion() {
        assertThat("Wrong version used", doclet.getSupportedSourceVersion(), is(SourceVersion.latest()));
    }

    @Test
    public void getDocletName() {
        assertThat("Wrong doclet name", doclet.getName(), is("DocFxDoclet"));
    }

    @Test
    public void testCustomOptionCreation() {
        String description = "Some desc";
        List<String> names = Arrays.asList("name 1", "name 2");
        String params = "Some params";

        CustomOption option = new CustomOption(description, names, params) {
            @Override
            public boolean process(String option, List<String> arguments) {
                return false;
            }
        };

        assertThat("Wrong args count", option.getArgumentCount(), is(1));
        assertThat("Wrong description", option.getDescription(), is(description));
        assertThat("Wrong kind", option.getKind(), is(Kind.STANDARD));
        assertThat("Wrong names", option.getNames(), is(names));
        assertThat("Wrong params", option.getParameters(), is(params));
    }

    @Test
    public void testFakeOptionCreation() {
        FakeOptionForCompatibilityWithStandardDoclet option =
            new FakeOptionForCompatibilityWithStandardDoclet("Some description", "title");

        assertThat("Wrong args count", option.getArgumentCount(), is(1));
        assertThat("Wrong description", option.getDescription(), is("Some description"));
        assertThat("Wrong kind", option.getKind(), is(Kind.STANDARD));
        assertThat("Wrong names", option.getNames(), is(Arrays.asList("title")));
        assertThat("Wrong params", option.getParameters(), is("none"));
    }
}
