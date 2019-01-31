package com.microsoft.build;

import java.util.Map;

public class LookupContext {

    private final Map<String, String> globalLookup;
    private final Map<String, String> localLookup;

    public LookupContext(Map<String, String> globalLookup, Map<String, String> localLookup) {
        this.globalLookup = globalLookup;
        this.localLookup = localLookup;
    }

    public String resolve(String key) {
        if (localLookup.containsKey(key)) {
            return localLookup.get(key);
        }
        return globalLookup.get(key);
    }

    public String getOwnerUid() {
        return localLookup.keySet().iterator().next();
    }

    public boolean containsKey(String key) {
        return localLookup.containsKey(key) || globalLookup.containsKey(key);
    }
}
