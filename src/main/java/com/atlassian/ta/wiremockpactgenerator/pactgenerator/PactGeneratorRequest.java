package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

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

    public Map<String, List<String>> getQuery() {
        try {
            final List<NameValuePair> pairs = URLEncodedUtils.parse(new URL("http", "dummyhost", getUrl()).toURI(), UTF_8);
            if (pairs.isEmpty()) {
                return null;
            }

            final Map<String, List<String>> queries = new HashMap<>();
            for (NameValuePair pair : pairs) {
                queries.computeIfAbsent(pair.getName(), k -> new ArrayList<>())
                        .add(pair.getValue());
            }
            return queries;
        } catch (final MalformedURLException | URISyntaxException e) {
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
