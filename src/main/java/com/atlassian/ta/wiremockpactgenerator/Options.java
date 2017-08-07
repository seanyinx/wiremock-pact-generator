package com.atlassian.ta.wiremockpactgenerator;

public interface Options {
    String consumerName();
    String providerName();
    PactSaver pactSaver();
    PactSerializer pactSerializer();
}
