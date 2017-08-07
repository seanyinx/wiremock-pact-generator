package com.atlassian.ta.wiremockpactgenerator.models;

import java.util.*;

public class Pact {
    private String consumer;
    private String provider;
    private Map<Integer, PactInteraction> interactions;
    private UUID id;

    public Pact(String consumer, String provider){
        this.consumer = consumer;
        this.provider = provider;
        interactions = new HashMap<>();
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public synchronized boolean addInteraction(PactInteraction interaction){
        int hash = interaction.hashCode();
        if(interactions.containsKey(hash)){
            return false;
        }
        interactions.put(hash, interaction);
        return true;
    }

    public String getConsumer() {
        return consumer;
    }

    public String getProvider() {
        return provider;
    }

    public List<PactInteraction> getInteractions() {
        return new ArrayList<>(interactions.values());
    }
}