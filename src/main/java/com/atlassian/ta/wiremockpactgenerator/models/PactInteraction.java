package com.atlassian.ta.wiremockpactgenerator.models;

public class PactInteraction {
    private PactRequest request;
    private PactResponse response;
    private String description;

    public PactInteraction(String description, PactRequest request, PactResponse response){
        this.description = description;
        this.request = request;
        this.response = response;
    }

    @Override
    public int hashCode() {
        int result = request.hashCode();
        result = 31 * result + response.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }
}
