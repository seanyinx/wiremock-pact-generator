package com.atlassian.ta.wiremockpactgenerator.pactgenerator.models;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;

public class Pact {
    private final PactCollaborator consumer;
    private final PactCollaborator provider;
    private final List<PactInteraction> interactions;

    public List<PactInteraction> getInteractions() {
        return new ArrayList<>(interactions);
    }

    public Pact(final String consumerName, final String providerName) {
        this.consumer = new PactCollaborator(consumerName);
        this.provider = new PactCollaborator(providerName);
        this.interactions = new ArrayList<>();
    }

    public void addInteraction(final PactInteraction pactInteraction) {
        final String providerState = pactInteraction.getDescription();

        interactions.add(pactInteraction.withProviderStates(
                singletonList(new PactProviderState(providerState))));
    }

    public PactCollaborator getConsumer() {
        return consumer;
    }

    public PactCollaborator getProvider() {
        return provider;
    }
}
