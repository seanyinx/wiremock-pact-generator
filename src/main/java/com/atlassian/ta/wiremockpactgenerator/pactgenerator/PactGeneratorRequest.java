package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.net.MalformedURLException;
import java.net.URL;
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

    public String getPath() {
        try {
            return new URL("http", "dummyhost", getUrl()).getPath();
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    public String getQuery() {
        try {
            return new URL("http", "dummyhost", getUrl()).getQuery();
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public static class Builder {
        private final String method;
        private final String url;
        private final String body;
        private final Map<String, List<String>> headers;

        public Builder() {
            this(null, null, null, null);
        }

        private Builder(final String method,
                        final  String url,
                        final String body,
                        final Map<String, List<String>> headers) {
            this.method = method;
            this.url = url;
            this.body = body;
            this.headers = Headers.cloneHeaders(headers);
        }

        public Builder withMethod(final String method) {
            return new Builder(method, url, body, headers);
        }

        public Builder withUrl(final String url) {
            return new Builder(method, url, body, headers);
        }

        public Builder withHeaders(final Map<String, List<String>> headers) {
            return new Builder(method, url, body, headers);
        }

        public Builder withBody(final String body) {
            return new Builder(method, url, body, headers);
        }

        public PactGeneratorRequest build() {
            return new PactGeneratorRequest(method, url, headers, body);
        }
    }
}
