package com.atlassian.ta.wiremockpactgenerator.models;

import java.util.Map;

public class PactResponse {
    private int status;
    private Map<String, String> headers;
    private String body;

    public PactResponse(int status, Map<String, String> headers, String body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public int hashCode() {
        int result = status;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}
