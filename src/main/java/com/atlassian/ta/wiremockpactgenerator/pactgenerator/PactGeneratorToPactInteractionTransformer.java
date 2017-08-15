package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.models.PactResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PactGeneratorToPactInteractionTransformer {
    private PactGeneratorToPactInteractionTransformer() {

    }

    public static PactInteraction transform(final PactGeneratorRequest request, final PactGeneratorResponse response) {
        final PactRequest pactRequest = toPactRequest(request);
        final PactResponse pactResponse = toPactResponse(response);
        final String description = getDescription(pactRequest, pactResponse);
        return new PactInteraction(description, pactRequest, pactResponse);
    }

    private static PactRequest toPactRequest(final PactGeneratorRequest request) {
        final String[] pathAndQuery = splitPathAndQuery(request.getUrl());
        final String method = request.getMethod().toUpperCase();
        final Map<String, String> headers = getProcessedHeaders(request.getHeaders());

        return new PactRequest(method, pathAndQuery[0], pathAndQuery[1], headers, normalizeBody(request.getBody()));
    }

    private static PactResponse toPactResponse(final PactGeneratorResponse response) {
        return new PactResponse(
                response.getStatus(),
                getProcessedHeaders(response.getHeaders()),
                normalizeBody(response.getBody()));
    }

    private static String[] splitPathAndQuery(final String url) {
        final int queryLocation = url.indexOf('?');

        if (queryLocation != -1) {
            return new String[] {url.substring(0, queryLocation), url.substring(queryLocation + 1)};
        }

        return new String[] {url, null};
    }

    private static Map<String, String> getProcessedHeaders(final Map<String, List<String>> rawHeaders) {
        final Map<String, String> headers = new HashMap<>();

        if (rawHeaders != null) {
            for (Map.Entry<String, List<String>> header : rawHeaders.entrySet()) {
                final String headerName = header.getKey().toLowerCase(Locale.ENGLISH);
                final String headerValue = String.join(", ", header.getValue());
                headers.put(headerName, headerValue);
            }
        }

        return headers;
    }

    private static  String normalizeBody(final String body) {
        return body == null || body.isEmpty() ? null : body;
    }

    private static String getDescription(final PactRequest pactRequest, final PactResponse pactResponse) {
        return String.format("%s %s -> %s", pactRequest.getMethod(), pactRequest.getPath(), pactResponse.getStatus());
    }
}
