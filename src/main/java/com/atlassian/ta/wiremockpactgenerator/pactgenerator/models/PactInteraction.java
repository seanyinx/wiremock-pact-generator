package com.atlassian.ta.wiremockpactgenerator.pactgenerator.models;

public class PactInteraction {
    private final String description;
    private final PactRequest request;
    private final PactResponse response;

    public PactInteraction(final String description, final PactRequest request, final PactResponse response) {
        this.description = description;
        this.request = request;
        this.response = response;
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
}
