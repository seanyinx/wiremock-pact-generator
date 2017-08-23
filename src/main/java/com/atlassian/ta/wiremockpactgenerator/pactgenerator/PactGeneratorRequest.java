package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.util.List;
import java.util.Map;

public class PactGeneratorRequest {
    private final String method;
    private final String url;
    private final String body;
    private final Map<String, List<String>> headers;

    private PactGeneratorRequest(
            final String method, final String url, final Map<String, List<String>> headers, final  String body) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public static class Builder {
        private String method;
        private String url;
        private String body;
        private Map<String, List<String>> headers;

        public Builder() {
            method = null;
            url = null;
            body = null;
            headers = null;
        }

        public Builder withMethod(final String method) {
            this.method = method;
            return this;
        }

        public Builder withUrl(final String url) {
            this.url = url;
            return this;
        }

        public Builder withHeaders(final Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withBody(final String body) {
            this.body = body;
            return this;
        }

        public PactGeneratorRequest build() {
            return new PactGeneratorRequest(method, url, headers, body);
        }
    }
}
