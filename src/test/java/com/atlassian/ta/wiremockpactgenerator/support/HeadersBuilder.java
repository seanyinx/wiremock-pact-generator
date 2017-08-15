package com.atlassian.ta.wiremockpactgenerator.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
        headers.put(name, Lists.newArrayList(values));
    }

    private Map<String, List<String>> cloneMap(final Map<String, List<String>> map) {
        final Map<String, List<String>> copyOfMap = Maps.newHashMap();
        map.forEach((key, value) -> copyOfMap.put(key, Lists.newArrayList(value)));
        return copyOfMap;
    }
}
