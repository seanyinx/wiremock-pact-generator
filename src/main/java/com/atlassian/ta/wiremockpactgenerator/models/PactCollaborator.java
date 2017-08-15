package com.atlassian.ta.wiremockpactgenerator.models;

public class PactCollaborator {
    private String name;

    public PactCollaborator(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
