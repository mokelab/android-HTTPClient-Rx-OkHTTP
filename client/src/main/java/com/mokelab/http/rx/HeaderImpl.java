package com.mokelab.http.rx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Headers;

/**
 * Implementation
 */
public class HeaderImpl extends HashMap<String, String> implements Header {
    private Map<String, List<String>> multipleMap = new HashMap<>();

    public HeaderImpl() {
        super();
    }

    HeaderImpl(Headers originalHeaders) {
        if (originalHeaders == null) { return; }

        this.multipleMap = originalHeaders.toMultimap();
        Set<Entry<String, List<String>>> entries = this.multipleMap.entrySet();
        for (Entry<String, List<String>> entry : entries) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (values.size() == 0) { continue; }
            this.put(key, values.get(0));
        }

    }

    @Override
    public List<String> getMultiple(String key) {
        return multipleMap.get(key);
    }
}
