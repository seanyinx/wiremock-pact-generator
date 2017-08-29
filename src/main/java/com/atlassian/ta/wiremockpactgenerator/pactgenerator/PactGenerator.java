package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.Pact;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactInteraction;

public class PactGenerator {
    private final PactSaver pactSaver;
    private final Pact pact;

    public PactGenerator(
            final String consumerName,
            final String providerName,
            final FileSystem fileSystem,
            final IdGenerator idGenerator
    ) {
        this.pactSaver = new PactSaver(fileSystem, idGenerator);
        this.pact = new Pact(consumerName, providerName);
    }

    public void process(final PactGeneratorInteraction interaction) {
        final PactGeneratorRequest request = interaction.getRequest();
        final PactGeneratorResponse response = interaction.getResponse();

        if (!InteractionFilter.isRequestWhitelisted(interaction.getRequestPathWhitelist(), request)) {
            return;
        }

        PactGeneratorValidation.validateResponse(response);
        final PactInteraction pactInteraction = PactGeneratorToPactInteractionTransformer.transform(request, response);
        pact.addInteraction(pactInteraction);
        pactSaver.savePactFile(pact);
    }

    public String getPactLocation() {
        return this.pactSaver.getPactFileLocation(pact);
    }
}
