package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

public class PactGeneratorInteraction {
    private final PactGeneratorResponse response;
    private final PactGeneratorRequest request;
    private final InteractionFilter interactionFilter;
    private final boolean strictApplicationJson;
    private final ContentFilter contentFilter;

    public PactGeneratorInteraction(
            final PactGeneratorRequest request,
            final PactGeneratorResponse response,
            final InteractionFilter interactionFilter,
            final boolean strictApplicationJson,
            final ContentFilter contentFilter
    ) {
        this.response = response;
        this.request = request;
        this.interactionFilter = interactionFilter;
        this.strictApplicationJson = strictApplicationJson;
        this.contentFilter = contentFilter;
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

    ContentFilter getContentFilter() {
        return contentFilter;
    }
}
