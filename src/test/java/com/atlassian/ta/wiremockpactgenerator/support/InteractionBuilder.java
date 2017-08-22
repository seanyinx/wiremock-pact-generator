package com.atlassian.ta.wiremockpactgenerator.support;

import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactSaver;

public class InteractionBuilder {
    private final FileSystem fileSystem;
    private final String consumer;
    private final String provider;
    private final PactGeneratorRequest request;
    private final PactGeneratorResponse response;

    public InteractionBuilder(final FileSystem fileSystem) {
        this(
                fileSystem,
                "default-consumer-name",
                "default-provider-name",
                new PactGeneratorRequest.Builder()
                        .withMethod("GET")
                        .withUrl("/path")
                        .build(),
                new PactGeneratorResponse.Builder()
                        .withStatus(200)
                        .build()
        );
    }

    private InteractionBuilder(final FileSystem fileSystem, final String consumer, final String provider,
                               final PactGeneratorRequest request, final PactGeneratorResponse response) {
        this.fileSystem = fileSystem;
        this.consumer = consumer;
        this.provider = provider;
        this.request = request;
        this.response = response;
    }

    public InteractionBuilder withConsumer(final String consumer) {
        return new InteractionBuilder(fileSystem, consumer, provider, request, response);
    }

    public InteractionBuilder withProvider(final String provider) {
        return new InteractionBuilder(fileSystem, consumer, provider, request, response);
    }

    public InteractionBuilder withRequest(final PactGeneratorRequest request) {
        return new InteractionBuilder(fileSystem, consumer, provider, request, response);
    }

    public InteractionBuilder withResponse(final PactGeneratorResponse response) {
        return new InteractionBuilder(fileSystem, consumer, provider, request, response);
    }

    public void perform() {
        final PactSaver pactSaver = new PactSaver(fileSystem);
        new PactGenerator(consumer, provider, pactSaver)
                .saveInteraction(request, response);
    }
}
