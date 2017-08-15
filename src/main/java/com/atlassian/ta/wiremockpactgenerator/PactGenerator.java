package com.atlassian.ta.wiremockpactgenerator;

import com.atlassian.ta.wiremockpactgenerator.models.Pact;
import com.atlassian.ta.wiremockpactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorToPactInteractionTransformer;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactSaver;

public class PactGenerator {
    private final Pact pact;
    private final PactSaver pactSaver;

    public PactGenerator(final String consumerName, final String providerName, final PactSaver pactSaver) {
        validateCollaboratorName(consumerName, "Consumer");
        validateCollaboratorName(providerName, "Provider");
        this.pactSaver = pactSaver;
        pact = new Pact(consumerName, providerName);
    }

    public void saveInteraction(final PactGeneratorRequest request, final PactGeneratorResponse response) {
        validatePactResponse(response);

        final PactInteraction pactInteraction = PactGeneratorToPactInteractionTransformer.transform(request, response);
        pact.addInteraction(pactInteraction);
        pactSaver.savePactFile(pact);
    }

    private void validatePactResponse(final PactGeneratorResponse response) {
        final int status = response.getStatus();
        if (status < 100 || status > 599) {
            throw new WiremockPactGeneratorException(String.format("Response status code is not valid: %d", status));
        }
    }

    public String getPactLocation() {
        return pactSaver.getPactLocation(pact);
    }

    private void validateCollaboratorName(final String name, final String collaborator) {
        if (name == null || name.trim().length() == 0) {
            throw new WiremockPactGeneratorException(String.format("%s name can't be null or blank", collaborator));
        }
    }
}
