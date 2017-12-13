package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.util.List;
import java.util.Map;

public class PactGeneratorResponse {
    private final int status;
    private final String body;
    private final Map<String, List<String>> headers;
    private final boolean isConfiguredResponse;

    private PactGeneratorResponse(final int status, final Map<String, List<String>> headers, final String body,
                                  final boolean isConfiguredResponse) {
        this.status = status;
        this.headers = headers;
        this.body = body;
        this.isConfiguredResponse = isConfiguredResponse;
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

    public boolean isConfigured() {
        return this.isConfiguredResponse;
    }

    public static class Builder {
        private final int status;
        private final String body;
        private final Map<String, List<String>> headers;
        private final boolean isConfiguredResponse;

        public Builder() {
            this(0, null, null, false);
        }

        private Builder(final int status, final String body, final Map<String, List<String>> headers,
                        final boolean isConfiguredResponse) {
            this.status = status;
            this.body = body;
            this.headers = Headers.cloneHeaders(headers);
            this.isConfiguredResponse = isConfiguredResponse;
        }

        public Builder withStatus(final int status) {
            return new Builder(status, body, headers, isConfiguredResponse);
        }

        public Builder withBody(final String body) {
            return new Builder(status, body, headers, isConfiguredResponse);
        }

        public Builder withHeaders(final Map<String, List<String>> headers) {
            return new Builder(status, body, headers, isConfiguredResponse);
        }

        public Builder withIsConfiguredResponse(final boolean isConfiguredResponse) {
            return new Builder(status, body, headers, isConfiguredResponse);
        }

        public PactGeneratorResponse build() {
            return new PactGeneratorResponse(status, headers, body, isConfiguredResponse);
        }
    }
}
