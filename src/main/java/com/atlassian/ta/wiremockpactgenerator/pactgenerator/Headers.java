package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Headers {
    static Map<String, List<String>> cloneHeaders(final Map<String, List<String>> headers) {
        if (headers == null) {
            return null;
        }

        return headers
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new ArrayList<>(entry.getValue()))
            );
    }

    private Headers() {

    }
}
