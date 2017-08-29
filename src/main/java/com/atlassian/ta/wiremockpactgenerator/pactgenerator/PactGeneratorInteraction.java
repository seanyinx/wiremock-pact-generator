package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import java.util.List;
import java.util.regex.Pattern;

public class PactGeneratorInteraction {
    private final PactGeneratorResponse response;
    private final PactGeneratorRequest request;
    private final List<Pattern> requestPathWhitelist;

    public PactGeneratorInteraction(
            final PactGeneratorRequest request,
            final PactGeneratorResponse response,
            final List<Pattern> requestPathWhitelist
    ) {
        this.response = response;
        this.request = request;
        this.requestPathWhitelist = requestPathWhitelist;
    }

    public PactGeneratorResponse getResponse() {
        return response;
    }

    public PactGeneratorRequest getRequest() {
        return request;
    }

    List<Pattern> getRequestPathWhitelist() {
        return requestPathWhitelist;
    }
}
