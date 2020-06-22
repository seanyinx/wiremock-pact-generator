package com.atlassian.ta.wiremockpactgenerator.pactgenerator.models;

import java.util.Map;

public class PactMetadata {

    private final Map<String, String> pactSpecification;

    PactMetadata(final Map<String, String> pactSpecification) {
        this.pactSpecification = pactSpecification;
    }

    public Map<String, String> getPactSpecification() {
        return pactSpecification;
    }
}
