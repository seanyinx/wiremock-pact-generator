package com.atlassian.ta.wiremockpactgenerator.pactgenerator.models;

import java.util.Collections;
import java.util.List;

public class PactInteraction {
    private final String description;
    private final PactRequest request;
    private final PactResponse response;
    private final List<PactProviderState> providerStates;

    public PactInteraction(final String description, final PactRequest request, final PactResponse response) {
        this(description, request, response, Collections.emptyList());
    }

    private PactInteraction(final String description, final PactRequest request,
            final PactResponse response, final List<PactProviderState> providerStates) {
        this.description = description;
        this.request = request;
        this.response = response;
        this.providerStates = providerStates;
    }

    public PactResponse getResponse() {
        return response;
    }

    public PactRequest getRequest() {
        return request;
    }

    public String getDescription() {
        return description;
    }

    public List<PactProviderState> getProviderStates() {
        return providerStates;
    }

    PactInteraction withProviderStates(final List<PactProviderState> providerStates) {
        return new PactInteraction(description, request, response, providerStates);
    }
}
