package com.atlassian.ta.wiremockpactgenerator.impl;

import com.atlassian.ta.wiremockpactgenerator.PactSerializer;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;
import com.google.gson.*;

import java.lang.reflect.Type;

public class PactJsonSerializer implements JsonSerializer<Pact>, PactSerializer {


    @Override
    public JsonElement serialize(Pact pact, Type type, JsonSerializationContext context) {
        final JsonObject json = new JsonObject();
        final JsonObject consumer = new JsonObject();
        final JsonObject provider = new JsonObject();

        consumer.addProperty("name", pact.getConsumer());
        provider.addProperty("name", pact.getProvider());

        json.add("consumer", consumer);
        json.add("provider", provider);
        json.add("interactions", context.serialize(pact.getInteractions()));
        return json;
    }

    @Override
    public String toJson(Pact pact) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Pact.class, this);
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(pact);
    }
}
