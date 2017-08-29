package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactResponse;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class PactGeneratorToPactInteractionTransformer {
    private static final List<String> IGNORE_REQUEST_HEADERS = Collections.singletonList("host");
    private static final List<String> IGNORE_RESPONSE_HEADERS = Collections.emptyList();

    private PactGeneratorToPactInteractionTransformer() {

    }

    public static PactInteraction transform(final PactGeneratorRequest request, final PactGeneratorResponse response) {
        final PactRequest pactRequest = toPactRequest(request);
        final PactResponse pactResponse = toPactResponse(response);
        final String description = getDescription(pactRequest, pactResponse);
        return new PactInteraction(description, pactRequest, pactResponse);
    }

    private static PactRequest toPactRequest(final PactGeneratorRequest request) {
        return new PactRequest(
                request.getMethod().toUpperCase(),
                request.getPath(),
                request.getQuery(),
                getProcessedHeaders(request.getHeaders(), IGNORE_REQUEST_HEADERS),
                normalizeBody(request.getBody()));
    }

    private static PactResponse toPactResponse(final PactGeneratorResponse response) {
        return new PactResponse(
                response.getStatus(),
                getProcessedHeaders(response.getHeaders(), IGNORE_RESPONSE_HEADERS),
                normalizeBody(response.getBody()));
    }

    private static Map<String, String> getProcessedHeaders(final Map<String, List<String>> rawHeaders,
                                                           final List<String> ignoreHeaders) {
        if (rawHeaders == null) {
            return new HashMap<>();
        }

        return rawHeaders.entrySet()
                .stream()
                .map(header -> new AbstractMap.SimpleEntry<>(
                        header.getKey().toLowerCase(Locale.ENGLISH),
                        String.join(", ", header.getValue()))
                )
                .filter(header -> !ignoreHeaders.contains(header.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private static  String normalizeBody(final String body) {
        return body == null || body.isEmpty() ? null : body;
    }

    private static String getDescription(final PactRequest pactRequest, final PactResponse pactResponse) {
        return String.format("%s %s -> %s", pactRequest.getMethod(), pactRequest.getPath(), pactResponse.getStatus());
    }
}
