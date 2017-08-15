package com.atlassian.ta.wiremockpactgenerator.models;

import com.google.common.collect.Lists;

import java.util.List;

public class Pact {
    private final PactCollaborator consumer;
    private final PactCollaborator provider;
    private final List<PactInteraction> interactions;

    public List<PactInteraction> getInteractions() {
        return Lists.newArrayList(interactions);
    }

    public Pact(final String consumerName, final String providerName) {
        this.consumer = new PactCollaborator(consumerName);
        this.provider = new PactCollaborator(providerName);
        this.interactions = Lists.newArrayList();
    }

    public void addInteraction(final PactInteraction pactInteraction) {
        interactions.add(pactInteraction);
    }

    public PactCollaborator getConsumer() {
        return consumer;
    }

    public PactCollaborator getProvider() {
        return provider;
    }
}
