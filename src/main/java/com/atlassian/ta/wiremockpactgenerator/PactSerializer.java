package com.atlassian.ta.wiremockpactgenerator;


import com.atlassian.ta.wiremockpactgenerator.models.Pact;

public interface PactSerializer {
    String toJson(Pact pact);
}
