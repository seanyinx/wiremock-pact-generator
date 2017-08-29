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
        private final int status;
        private final String body;
        private final Map<String, List<String>> headers;

        public Builder() {
            this(0, null, null);
        }

        private Builder(final int status, final String body, final Map<String, List<String>> headers) {
            this.status = status;
            this.body = body;
            this.headers = Headers.cloneHeaders(headers);
        }

        public Builder withStatus(final int status) {
            return new Builder(status, body, headers);
        }

        public Builder withBody(final String body) {
            return new Builder(status, body, headers);
        }

        public Builder withHeaders(final Map<String, List<String>> headers) {
            return new Builder(status, body, headers);
        }

        public PactGeneratorResponse build() {
            return new PactGeneratorResponse(status, headers, body);
        }
    }
}
