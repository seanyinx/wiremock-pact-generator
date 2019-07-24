package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorUserOptions;

import java.util.HashMap;
import java.util.Map;

public final class PactGeneratorRegistry {
    private static final Map<String, PactGenerator> instances = new HashMap<>();
    private static final FileSystem fileSystem = new LocalFileSystem();
    private static final IdGenerator idGenerator = new UuidGenerator();

    private PactGeneratorRegistry() {
    }

    public static synchronized void processInteraction(
            final WireMockPactGeneratorUserOptions userOptions,
            final PactGeneratorRequest request,
            final PactGeneratorResponse response
    ) {
        final PactGeneratorInteraction interaction = new PactGeneratorInteraction(
                request, response, userOptions.getInteractionFilter(), userOptions.isStrictApplicationJson(),
                userOptions.getContentFilter());
        getInstance(userOptions).process(interaction);
    }

    public static synchronized String getPactLocation(final WireMockPactGeneratorUserOptions userOptions) {
        return getInstance(userOptions).getPactLocation();
    }

    private static PactGenerator getInstance(final WireMockPactGeneratorUserOptions userOptions) {
        final String consumerName = userOptions.getConsumerName();
        final String providerName = userOptions.getProviderName();
        final String key = String.format("consumer-%s-provider-%s", consumerName, providerName);

        if (!instances.containsKey(key)) {
            instances.put(key, new PactGenerator(consumerName, providerName, fileSystem, idGenerator));
        }

        return instances.get(key);
    }
}
