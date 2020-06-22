package com.atlassian.ta.wiremockpactgenerator.pactgenerator.models;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pact {
    private final PactCollaborator consumer;
    private final PactCollaborator provider;
    private final List<PactInteraction> interactions;
    private final Map<String, String> providerStates;

    public List<PactInteraction> getInteractions() {
        return new ArrayList<>(interactions);
    }

    public Pact(final String consumerName, final String providerName) {
        this.consumer = new PactCollaborator(consumerName);
        this.provider = new PactCollaborator(providerName);
        this.interactions = new ArrayList<>();
        this.providerStates = new HashMap<>();
    }

    public void addInteraction(final PactInteraction pactInteraction) {
        String providerState = providerStates.compute(pactInteraction.getDescription(),
                (k, v) -> v == null ? pactInteraction.getDescription() : v + " | " + 1);

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
