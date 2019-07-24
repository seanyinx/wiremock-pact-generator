package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactResponse;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PactGeneratorToPactInteractionTransformer {

    private PactGeneratorToPactInteractionTransformer() {

    }

    public static PactInteraction transform(final PactGeneratorRequest request, final PactGeneratorResponse response, final ContentFilter contentFilter) {
        final PactRequest pactRequest = toPactRequest(request, contentFilter);
        final PactResponse pactResponse = toPactResponse(response, contentFilter);
        final String description = getDescription(pactRequest, pactResponse);
        return new PactInteraction(description, pactRequest, pactResponse);
    }

    private static PactRequest toPactRequest(final PactGeneratorRequest request, final ContentFilter contentFilter) {
        return new PactRequest(
                request.getMethod().toUpperCase(),
                request.getPath(),
                request.getQuery(),
                getProcessedHeaders(request.getHeaders(), header -> contentFilter.isRequestHeaderWhitelisted(header.getKey())),
                normalizeBody(request.getBody()));
    }

    private static PactResponse toPactResponse(final PactGeneratorResponse response, final ContentFilter contentFilter) {
        return new PactResponse(
                response.getStatus(),
                getProcessedHeaders(response.getHeaders(), header -> contentFilter.isResponseHeaderWhitelisted(header.getKey())),
                normalizeBody(response.getBody()),
                response.isConfigured());
    }

    private static Map<String, String> getProcessedHeaders(final Map<String, List<String>> rawHeaders,
                                                           final Predicate<AbstractMap.SimpleEntry<String, String>> headerFilter) {
        if (rawHeaders == null) {
            return new HashMap<>();
        }

        return rawHeaders.entrySet()
                .stream()
                .map(header -> new AbstractMap.SimpleEntry<>(
                        header.getKey().toLowerCase(Locale.ENGLISH),
                        String.join(", ", header.getValue()))
                )
                .filter(headerFilter)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private static String normalizeBody(final String body) {
        return body == null || body.isEmpty() ? null : body;
    }

    private static String getDescription(final PactRequest pactRequest, final PactResponse pactResponse) {
        final String notConfiguredWarningOrEmpty = pactResponse.isConfigured() ? "" : " [Not configured in WireMock]";

        return String.format("%s %s -> %s%s", pactRequest.getMethod(),
                pactRequest.getPath(), pactResponse.getStatus(), notConfiguredWarningOrEmpty);
    }
}
