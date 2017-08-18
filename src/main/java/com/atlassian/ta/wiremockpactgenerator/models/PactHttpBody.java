package com.atlassian.ta.wiremockpactgenerator.models;

public class PactHttpBody {

    private final String value;

    public PactHttpBody(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
