package com.atlassian.ta.wiremockpactgenerator;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRegistry;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorResponse;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WireMockPactGenerator implements RequestListener {
    private final String consumerName;
    private final String providerName;

    public WireMockPactGenerator(final String consumerName, final String providerName) {
        this.consumerName = consumerName;
        this.providerName = providerName;
    }

    @Override
    public void requestReceived(final Request request, final Response response) {
        try {
            saveInteraction(request, response);
        } catch (final RuntimeException exception) {
            System.err.println("WireMock Pact Generator: unexpected error. Forcing system exit.");
            exception.printStackTrace();
            System.exit(1);
        }
    }

    public String getPactLocation() {
        return PactGeneratorRegistry.getPactLocation(consumerName, providerName);
    }

    private void saveInteraction(final Request request, final Response response) {
        final PactGeneratorRequest.Builder requestBuilder = new PactGeneratorRequest.Builder()
                .withMethod(request.getMethod().value())
                .withUrl(request.getUrl())
                .withHeaders(extractHeaders(request.getHeaders()))
                .withBody(request.getBodyAsString());

        final PactGeneratorResponse.Builder responseBuilder = new PactGeneratorResponse.Builder()
                .withStatus(response.getStatus())
                .withHeaders(extractHeaders(response.getHeaders()))
                .withBody(response.getBodyAsString());

        PactGeneratorRegistry.saveInteraction(
                consumerName,
                providerName,
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
}
