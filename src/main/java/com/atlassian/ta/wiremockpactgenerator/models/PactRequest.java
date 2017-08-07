package com.atlassian.ta.wiremockpactgenerator.models;

import java.util.Map;

public class PactRequest {
    private String method;
    private String path;
    private String query;
    private Map<String, String> headers;
    private String body;

    public PactRequest(String method, String path, String query, Map<String, String> headers, String body){
        this.method = method;
        this.path = path;
        this.query = query;
        this.body = body;
        this.headers = headers;
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}