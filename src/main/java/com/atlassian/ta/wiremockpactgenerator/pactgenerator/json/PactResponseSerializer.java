package com.atlassian.ta.wiremockpactgenerator.pactgenerator.json;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class PactResponseSerializer implements JsonSerializer<PactResponse> {

    @Override
    public JsonElement serialize(final PactResponse response, final Type type, final JsonSerializationContext context) {
        final JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", response.getStatus());

        if (response.getHeaders() != null) {
            jsonResponse.add("headers", context.serialize(response.getHeaders()));
        }

        if (response.getBody().getValue() != null) {
            jsonResponse.add("body", context.serialize(response.getBody()));
        }

        return jsonResponse;
    }
}
