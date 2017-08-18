package com.atlassian.ta.wiremockpactgenerator.models;

import java.util.HashMap;
import java.util.Map;

public class PactRequest {
    private final String method;
    private final String path;
    private final String query;
    private final Map<String, String> headers;
    private final PactHttpBody body;

    public PactRequest(final String method, final String path, final String query,
                       final Map<String, String> headers, final String body) {
        this.method = method;
        this.path = path;
        this.query = query;
        this.headers = copyHeaders(headers);
        this.body = new PactHttpBody(body);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, String> getHeaders() {
        return copyHeaders(headers);
    }

    public PactHttpBody getBody() {
        return body;
    }

    private Map<String, String> copyHeaders(final Map<String, String> headers) {
        return headers == null || headers.isEmpty() ? null : new HashMap<>(headers);
    }
}
