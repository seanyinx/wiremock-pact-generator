package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.Config;
import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.LocalFileSystem;
import com.atlassian.ta.wiremockpactgenerator.UuidGenerator;
import com.google.common.collect.Maps;

import java.util.Map;

public final class PactGeneratorRegistry {
    private static final Map<String, PactGenerator> instances = Maps.newHashMap();
    private static final FileSystem fileSystem = new LocalFileSystem();
    private static final IdGenerator idGenerator = new UuidGenerator();

    private PactGeneratorRegistry() {
    }

    public static synchronized void saveInteraction(
            final String consumerName,
            final String providerName,
            final PactGeneratorRequest request,
            final PactGeneratorResponse response
    ) {
        final PactGenerator pactGenerator = getInstance(consumerName, providerName);
        pactGenerator.saveInteraction(request, response);
    }

    public static synchronized String getPactLocation(final String consumerName, final String providerName) {
        final PactGenerator pactGenerator = getInstance(consumerName, providerName);
        return pactGenerator.getPactLocation();
    }

    private static PactGenerator getInstance(final String consumerName, final String providerName) {
        final String key = String.format("consumer-%s-provider-%s", consumerName, providerName);

        if (!instances.containsKey(key)) {
            instances.put(key, PactGeneratorFactory.createPactGenerator(new Config.Builder()
                    .withConsumerName(consumerName)
                    .withProviderName(providerName)
                    .withFileSystem(fileSystem)
                    .withIdGenerator(idGenerator)
                    .build()
            ));
        }

        return instances.get(key);
    }
}
