package com.atlassian.ta.wiremockpactgenerator.impl;

import com.atlassian.ta.wiremockpactgenerator.PactSaver;
import com.atlassian.ta.wiremockpactgenerator.Options;
import com.atlassian.ta.wiremockpactgenerator.PactSerializer;

public class PactGeneratorOptions implements Options {
    private String consumerName;
    private String providerName;
    private PactSerializer pactSerializer;
    private PactSaver pactSaver;

    public PactGeneratorOptions(String consumerName, String providerName,
                                PactSaver pactSaver, PactSerializer pactSerializer){
        this.consumerName = consumerName;
        this.providerName = providerName;
        this.pactSerializer = pactSerializer;
        this.pactSaver = pactSaver;
    }

    @Override
    public String consumerName() {
        return consumerName;
    }

    @Override
    public String providerName() {
        return providerName;
    }

    @Override
    public PactSaver pactSaver() {
        return pactSaver;
    }

    @Override
    public PactSerializer pactSerializer() {
        return pactSerializer;
    }
}
