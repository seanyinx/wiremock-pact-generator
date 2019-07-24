package com.atlassian.ta.wiremockpactgenerator;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRegistry;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorResponse;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WireMockPactGenerator implements RequestListener {
    private final WireMockPactGeneratorUserOptions userOptions;

    public static Builder builder(final String consumerName, final String providerName) {
        return new Builder(consumerName, providerName);
    }

    private WireMockPactGenerator(final WireMockPactGeneratorUserOptions userOptions) {
        this.userOptions = userOptions;
    }

    @Override
    public void requestReceived(final Request request, final Response response) {
        try {
            processInteraction(request, response);
        } catch (final RuntimeException exception) {
            System.err.println("WireMock Pact Generator: unexpected error. Forcing system exit.");
            exception.printStackTrace();
            System.exit(1);
        }
    }

    public String getPactLocation() {
        return PactGeneratorRegistry.getPactLocation(userOptions);
    }

    private void processInteraction(final Request request, final Response response) {
        final PactGeneratorRequest.Builder requestBuilder = new PactGeneratorRequest.Builder()
                .withMethod(request.getMethod().value())
                .withUrl(request.getUrl())
                .withHeaders(extractHeaders(request.getHeaders()))
                .withBody(request.getBodyAsString());

        final PactGeneratorResponse.Builder responseBuilder = new PactGeneratorResponse.Builder()
                .withStatus(response.getStatus())
                .withHeaders(extractHeaders(response.getHeaders()))
                .withBody(response.getBody() == null ? "" : response.getBodyAsString())
                .withIsConfiguredResponse(response.wasConfigured());

        PactGeneratorRegistry.processInteraction(
                userOptions,
                requestBuilder.build(),
                responseBuilder.build()
        );
    }

    private Map<String, List<String>> extractHeaders(final HttpHeaders wireMockHeaders) {
        final Map<String, List<String>> headers = new HashMap<>();

        for (final HttpHeader header : wireMockHeaders.all()) {
            headers.put(header.key(), header.values());
        }
        return headers;
    }

    public static class Builder {
        private final List<String> requestPathWhitelist;
        private final List<String> requestPathBlacklist;
        private final String consumerName;
        private final String providerName;
        private final boolean strictApplicationJson;
        private final List<String> requestHeaderWhitelist;
        private final List<String> responseHeaderWhitelist;

        private Builder(final String consumerName, final String providerName) {
            this(consumerName, providerName, Collections.emptyList(), Collections.emptyList(), true, Collections.emptyList(), Collections.emptyList());
        }

        private Builder(final String consumerName,
                        final String providerName,
                        final List<String> requestPathWhitelist,
                        final List<String> requestPathBlacklist,
                        final boolean strictApplicationJson,
                        final List<String> requestHeaderWhitelist,
                        final List<String> responseHeaderWhitelist
        ) {
            this.consumerName = consumerName;
            this.providerName = providerName;
            this.requestPathWhitelist = requestPathWhitelist;
            this.requestPathBlacklist = requestPathBlacklist;
            this.strictApplicationJson = strictApplicationJson;
            this.requestHeaderWhitelist = requestHeaderWhitelist;
            this.responseHeaderWhitelist = responseHeaderWhitelist;
        }

        public Builder withRequestPathWhitelist(final String... regexPatterns) {
            final List<String> newRequestPathWhitelist = extendListWithItems(requestPathWhitelist, regexPatterns);
            return new Builder(consumerName, providerName, newRequestPathWhitelist, requestPathBlacklist, strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist);
        }

        public Builder withRequestPathBlacklist(final String... regexPatterns) {
            final List<String> newRequestPathBlacklist = extendListWithItems(requestPathBlacklist, regexPatterns);
            return new Builder(consumerName, providerName, requestPathWhitelist, newRequestPathBlacklist, strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist);
        }

        public Builder withStrictApplicationJson(final boolean strictApplicationJson) {
            return new Builder(consumerName, providerName, requestPathWhitelist, requestPathBlacklist, strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist);
        }

        public Builder withRequestHeaderWhitelist(final String... httpHeaders) {
            final List<String> newRequestHeaderWhitelist = extendListWithItems(requestHeaderWhitelist, httpHeaders);
            return new Builder(consumerName, providerName, requestPathWhitelist, requestPathBlacklist, strictApplicationJson, newRequestHeaderWhitelist, responseHeaderWhitelist);
        }

        public Builder withResponseHeaderWhitelist(final String... httpHeaders) {
            final List<String> newResponseHeaderWhitelist = extendListWithItems(responseHeaderWhitelist, httpHeaders);
            return new Builder(consumerName, providerName, requestPathWhitelist, requestPathBlacklist, strictApplicationJson, requestHeaderWhitelist, newResponseHeaderWhitelist);
        }

        public WireMockPactGenerator build() {
            final WireMockPactGeneratorUserOptions userOptions = new WireMockPactGeneratorUserOptions(
                    consumerName, providerName, requestPathWhitelist, requestPathBlacklist, strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist);
            return new WireMockPactGenerator(userOptions);
        }

        private <T> List<T> extendListWithItems(final List<T> original, final T[] items) {
            final List<T> copyOfOriginal = new ArrayList<>(original);
            copyOfOriginal.addAll(Arrays.asList(items));
            return copyOfOriginal;
        }
    }
}
