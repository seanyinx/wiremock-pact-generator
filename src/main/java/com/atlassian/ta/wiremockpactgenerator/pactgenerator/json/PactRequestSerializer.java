package com.atlassian.ta.wiremockpactgenerator.pactgenerator.json;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class PactRequestSerializer implements JsonSerializer<PactRequest> {

    @Override
    public JsonElement serialize(final PactRequest request, final Type type, final JsonSerializationContext context) {
        final JsonObject jsonRequest = new JsonObject();
        jsonRequest.addProperty("method", request.getMethod());
        jsonRequest.addProperty("path", request.getPath());

        if (request.getQuery() != null) {
            jsonRequest.add("query", context.serialize(request.getQuery()));
        }

        if (request.getHeaders() != null) {
            jsonRequest.add("headers", context.serialize(request.getHeaders()));
        }

        if (request.getBody().getValue() != null) {
            jsonRequest.add("body", context.serialize(request.getBody()));
        }

        return jsonRequest;
    }
}
