package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ContentFilter {
    private static final String WIREMOCK_MATCHED_STUB_ID_HEADER = "matched-stub-id";
    private static final String WIREMOCK_MATCHED_STUB_NAME_HEADER = "matched-stub-name";

    private static final List<String> IGNORE_REQUEST_HEADERS = Collections.singletonList("host");
    private static final List<String> IGNORE_RESPONSE_HEADERS =
            Arrays.asList(WIREMOCK_MATCHED_STUB_ID_HEADER, WIREMOCK_MATCHED_STUB_NAME_HEADER);
    private final List<String> requestHeaderWhitelist;
    private final List<String> responseHeaderWhitelist;

    public ContentFilter(
            final List<String> requestHeaderWhitelist,
            final List<String> responseHeaderWhitelist
    ) {
        this.requestHeaderWhitelist = requestHeaderWhitelist;
        this.responseHeaderWhitelist = responseHeaderWhitelist;
    }

    public boolean isRequestHeaderWhitelisted(final String headerName) {
        if (requestHeaderWhitelist.isEmpty()) {
            return !IGNORE_REQUEST_HEADERS.contains(headerName);
        }
        return headerMatchesAnyCaseInsensitiveKeys(headerName, requestHeaderWhitelist);
    }

    public boolean isResponseHeaderWhitelisted(final String headerName) {
        if (responseHeaderWhitelist.isEmpty()) {
            return !IGNORE_RESPONSE_HEADERS.contains(headerName);
        }
        return headerMatchesAnyCaseInsensitiveKeys(headerName, responseHeaderWhitelist);
    }

    private static boolean headerMatchesAnyCaseInsensitiveKeys(final String headerName, final List<String> headerWhitelist) {
        return headerWhitelist.stream().anyMatch(key -> key.equalsIgnoreCase(headerName));
    }
}
