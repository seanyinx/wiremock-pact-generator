package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

public class PactGeneratorInteraction {
    private final PactGeneratorResponse response;
    private final PactGeneratorRequest request;
    private final InteractionFilter interactionFilter;

    public PactGeneratorInteraction(
            final PactGeneratorRequest request,
            final PactGeneratorResponse response,
            final InteractionFilter interactionFilter
    ) {
        this.response = response;
        this.request = request;
        this.interactionFilter = interactionFilter;
    }

    public PactGeneratorResponse getResponse() {
        return response;
    }

    public PactGeneratorRequest getRequest() {
        return request;
    }

    InteractionFilter getInteractionFilter() {
        return interactionFilter;
    }
}
