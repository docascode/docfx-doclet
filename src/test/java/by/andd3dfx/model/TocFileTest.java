package by.andd3dfx.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TocFileTest {

    @Test
    public void testToString() {
        TocFile tocFile = buildTocFile("Some uid", "Some name", "Some href", Arrays.asList(
            buildTocFile("inner uid 1", "inner name 1", "inner href 1", null),
            buildTocFile("inner uid 2", "inner name 2", "inner href 2",
                Arrays.asList(buildTocFile("inner uid 3", "inner name 3", "inner href 3", null))))
        );

        String string = tocFile.toString();
        assertThat("Wrong string", string, is(""
            + "- uid: Some uid\n"
            + "  name: Some name\n"
            + "  href: Some href\n"
            + "  items: \n"
            + "  - uid: inner uid 1\n"
            + "    name: inner name 1\n"
            + "    href: inner href 1\n"
            + "  - uid: inner uid 2\n"
            + "    name: inner name 2\n"
            + "    href: inner href 2\n"
            + "    items: \n"
            + "    - uid: inner uid 3\n"
            + "      name: inner name 3\n"
            + "      href: inner href 3\n"));
    }

    private TocFile buildTocFile(String uid, String name, String href, List<TocFile> items) {
        TocFile result = new TocFile();
        result.setUid(uid);
        result.setName(name);
        result.setHref(href);
        result.setItems(items);
        return result;
    }
}