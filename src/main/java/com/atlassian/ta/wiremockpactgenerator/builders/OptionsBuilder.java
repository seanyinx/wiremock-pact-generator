package com.atlassian.ta.wiremockpactgenerator.builders;


import com.atlassian.ta.wiremockpactgenerator.PactSaver;
import com.atlassian.ta.wiremockpactgenerator.Options;
import com.atlassian.ta.wiremockpactgenerator.PactSerializer;
import com.atlassian.ta.wiremockpactgenerator.impl.PactGeneratorOptions;
import com.atlassian.ta.wiremockpactgenerator.impl.PactJsonSerializer;


public class OptionsBuilder {
    private String consumerName;
    private String providerName;
    private PactSaver pactSaver;
    private PactSerializer pactSerializer;

    public OptionsBuilder withConsumerName(String consumerName){
        this.consumerName = consumerName;
        return this;
    }

    public OptionsBuilder withProviderName(String providerName){
        this.providerName = providerName;
        return this;
    }

    public OptionsBuilder withPactSerializer(PactSerializer pactSerializer){
        this.pactSerializer = pactSerializer;
        return this;
    }

    public OptionsBuilder withPactSaver(PactSaver pactSaver){
        this.pactSaver = pactSaver;
        return this;
    }

    public Options build(){
        if (pactSaver == null) {
           pactSaver = new FilePactSaver();
        }
        if (pactSerializer == null){
            pactSerializer = new PactJsonSerializer();
        }

        return new PactGeneratorOptions(consumerName, providerName, pactSaver, pactSerializer);
    }
}
