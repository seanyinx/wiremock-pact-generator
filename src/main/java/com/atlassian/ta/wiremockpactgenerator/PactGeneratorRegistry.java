package com.atlassian.ta.wiremockpactgenerator;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactSaver;
import com.google.common.collect.Maps;

import java.util.Map;

final class PactGeneratorRegistry {
    private static Map<String, PactGenerator> instances = Maps.newHashMap();
    private static PactSaver pactSaver = new PactSaver(new LocalFileSystem());

    private PactGeneratorRegistry() {
    }

    static synchronized void saveInteraction(
            final String consumerName,
            final String providerName,
            final PactGeneratorRequest request,
            final PactGeneratorResponse response
    ) {
        final PactGenerator pactGenerator = getInstance(consumerName, providerName);
        pactGenerator.saveInteraction(request, response);
    }

    static synchronized String getPactLocation(final String consumerName, final String providerName) {
        final PactGenerator pactGenerator = getInstance(consumerName, providerName);
        return pactGenerator.getPactLocation();
    }

    private static PactGenerator getInstance(final String consumerName, final String providerName) {
        final String key = String.format("consumer-%s-provider-%s", consumerName, providerName);

        if (!instances.containsKey(key)) {
            instances.put(key, new PactGenerator(consumerName, providerName, pactSaver));
        }

        return instances.get(key);
    }
}
