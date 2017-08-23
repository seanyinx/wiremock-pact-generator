package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.Config;

public final class PactGeneratorFactory {

    private PactGeneratorFactory() {

    }

    public static PactGenerator createPactGenerator(final Config config) {
        final PactSaver pactSaver = new PactSaver(config.getFileSystem(), config.getIdGenerator());
        return new PactGenerator(config.getConsumerName(), config.getProviderName(), pactSaver);
    }
}
