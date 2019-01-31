package com.microsoft.build;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class LookupContextTest {

    private LookupContext lookupContext;
    private Map<String, String> globalLookup = new HashMap<>();
    private Map<String, String> localLookup = new LinkedHashMap<>();
    private String[] localKeys = {"local key 1", "local key 2"};
    private String[] localValues = {"local value 1", "local value 2"};
    private String globalKey = "global key";
    private String globalValue = "global value";
    private String unknownKey = "unknown key";

    @Before
    public void setUp() {
        localLookup.put(localKeys[0], localValues[0]);
        localLookup.put(localKeys[1], localValues[1]);
        globalLookup.put(globalKey, globalValue);
        globalLookup.putAll(localLookup);
        lookupContext = new LookupContext(globalLookup, localLookup);
    }

    @Test
    public void resolve() {
        assertThat("Wrong value for global key", lookupContext.resolve(globalKey), is(globalValue));
        assertThat("Wrong value for local key 1", lookupContext.resolve(localKeys[0]), is(localValues[0]));
        assertThat("Wrong value for local key 2", lookupContext.resolve(localKeys[1]), is(localValues[1]));
        assertThat("Wrong value for unknown key", lookupContext.resolve(unknownKey), is(nullValue()));
    }

    @Test
    public void getOwnerUid() {
        assertThat("Wrong ownerUid", lookupContext.getOwnerUid(), is(localKeys[0]));
    }

    @Test
    public void containsKey() {
        assertThat("Wrong value for global key", lookupContext.containsKey(globalKey), is(true));
        assertThat("Wrong value for local key 1", lookupContext.containsKey(localKeys[0]), is(true));
        assertThat("Wrong value for local key 2", lookupContext.containsKey(localKeys[1]), is(true));
        assertThat("Wrong value for unknown key", lookupContext.containsKey(unknownKey), is(false));
    }
}
