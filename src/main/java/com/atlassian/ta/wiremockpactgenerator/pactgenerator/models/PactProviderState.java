package com.atlassian.ta.wiremockpactgenerator.pactgenerator.models;

public class PactProviderState {
    private String name;

    PactProviderState(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
