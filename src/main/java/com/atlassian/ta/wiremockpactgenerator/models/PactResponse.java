package com.atlassian.ta.wiremockpactgenerator.models;

import java.util.HashMap;
import java.util.Map;

public class PactResponse {
    private final int status;
    private final Map<String, String> headers;
    private final PactHttpBody body;

    public PactResponse(final int status, final Map<String, String> headers, final String body) {
        this.status = status;
        this.headers = copyHeaders(headers);
        this.body = new PactHttpBody(body);
    }

    public int getStatus() {
        return status;
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
