package com.atlassian.ta.wiremockpactgenerator.unit.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadersBuilder {

    private final Map<String, List<String>> headers;

    public HeadersBuilder() {
        headers = new HashMap<>();
    }

    private HeadersBuilder(final Map<String, List<String>> initialHeaders) {
        headers = cloneMap(initialHeaders);
    }

    public HeadersBuilder withHeader(final String name, final String ...values) {
        final HeadersBuilder builder = new HeadersBuilder(headers);
        builder.addHeader(name, values);
        return builder;
    }

    public Map<String, List<String>> build() {
        return cloneMap(headers);
    }

    private void addHeader(final String name, final String ...values) {
        headers.put(name, Arrays.asList(values));
    }

    private Map<String, List<String>> cloneMap(final Map<String, List<String>> map) {
        final Map<String, List<String>> copyOfMap = new HashMap<>();
        map.forEach((key, value) -> copyOfMap.put(key, new ArrayList<>(value)));
        return copyOfMap;
    }
}
