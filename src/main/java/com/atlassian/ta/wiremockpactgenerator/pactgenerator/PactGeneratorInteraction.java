package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

public class PactGeneratorInteraction {
    private final PactGeneratorResponse response;
    private final PactGeneratorRequest request;
    private final InteractionFilter interactionFilter;
    private final boolean strictApplicationJson;

    public PactGeneratorInteraction(
            final PactGeneratorRequest request,
            final PactGeneratorResponse response,
            final InteractionFilter interactionFilter,
            final boolean strictApplicationJson
    ) {
        this.response = response;
        this.request = request;
        this.interactionFilter = interactionFilter;
        this.strictApplicationJson = strictApplicationJson;
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

    boolean isStrictApplicationJson() {
        return strictApplicationJson;
    }
}
