package com.atlassian.ta.wiremockpactgenerator.support;

import com.atlassian.ta.wiremockpactgenerator.Config;
import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorFactory;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorResponse;

public class InteractionBuilder {
    private final Config.Builder configBuilder;
    private final PactGeneratorRequest request;
    private final PactGeneratorResponse response;

    public InteractionBuilder(final FileSystem fileSystem, final IdGenerator idGenerator) {
        this(
                new Config.Builder()
                    .withConsumerName("default-consumer-name")
                    .withProviderName("default-provider-name")
                    .withFileSystem(fileSystem)
                    .withIdGenerator(idGenerator),
                new PactGeneratorRequest.Builder()
                        .withMethod("GET")
                        .withUrl("/path")
                        .build(),
                new PactGeneratorResponse.Builder()
                        .withStatus(200)
                        .build()
        );
    }

    private InteractionBuilder(final Config.Builder configBuilder,
                               final PactGeneratorRequest request, final PactGeneratorResponse response) {
        this.configBuilder = configBuilder;
        this.request = request;
        this.response = response;
    }

    public InteractionBuilder withConsumer(final String consumer) {
        return new InteractionBuilder(configBuilder.withConsumerName(consumer), request, response);
    }

    public InteractionBuilder withProvider(final String provider) {
        return new InteractionBuilder(configBuilder.withProviderName(provider), request, response);
    }

    public InteractionBuilder withRequest(final PactGeneratorRequest request) {
        return new InteractionBuilder(configBuilder, request, response);
    }

    public InteractionBuilder withResponse(final PactGeneratorResponse response) {
        return new InteractionBuilder(configBuilder, request, response);
    }

    public String getPactLocation() {
        return PactGeneratorFactory
                .createPactGenerator(configBuilder.build())
                .getPactLocation();
    }

    public void perform() {
        PactGeneratorFactory
                .createPactGenerator(configBuilder.build())
                .saveInteraction(request, response);
    }
}
