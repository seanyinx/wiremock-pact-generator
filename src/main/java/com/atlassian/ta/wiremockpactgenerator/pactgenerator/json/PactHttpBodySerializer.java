package com.atlassian.ta.wiremockpactgenerator.pactgenerator.json;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactHttpBody;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

public abstract class PactHttpBodySerializer implements JsonSerializer<PactHttpBody> {
    private final JsonParser parser = new JsonParser();

    @Override
    public JsonElement serialize(final PactHttpBody body,
                                 final Type type,
                                 final JsonSerializationContext jsonSerializationContext) {
        final String bodyValue = body.getValue();

        if (bodyValue == null) {
            return JsonNull.INSTANCE;
        }

        if (shouldSerializeAsJson(bodyValue)) {
            return parser.parse(bodyValue);
        }

        return new JsonPrimitive(bodyValue);
    }

    protected abstract boolean shouldSerializeElement(final JsonElement element);

    private boolean shouldSerializeAsJson(final String s) {
        try {
            final JsonElement element = parser.parse(s);
            return shouldSerializeElement(element);
        } catch (final JsonSyntaxException ex) {
            return false;
        }
    }
}
