package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.util.List;
import java.util.Map;

public class PactGeneratorResponse {
    private final int status;
    private final String body;
    private final Map<String, List<String>> headers;

    private PactGeneratorResponse(final int status, final Map<String, List<String>> headers, final String body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public static class Builder {
        private int status;
        private String body;
        private Map<String, List<String>> headers;

        public Builder() {
            headers = null;
            body = null;
        }

        public Builder withStatus(final int status) {
            this.status = status;
            return this;
        }

        public Builder withBody(final String body) {
            this.body = body;
            return this;
        }

        public Builder withHeaders(final Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public PactGeneratorResponse build() {
            return new PactGeneratorResponse(status, headers, body);
        }
    }
}
