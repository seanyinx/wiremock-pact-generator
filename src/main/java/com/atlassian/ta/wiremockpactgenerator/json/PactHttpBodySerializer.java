package com.atlassian.ta.wiremockpactgenerator.json;

import com.atlassian.ta.wiremockpactgenerator.models.PactHttpBody;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

public class PactHttpBodySerializer implements JsonSerializer<PactHttpBody> {
    private final JsonParser parser = new JsonParser();

    @Override
    public JsonElement serialize(final PactHttpBody body,
                                 final Type type,
                                 final JsonSerializationContext jsonSerializationContext) {
        final String bodyValue = body.getValue();

        if (bodyValue == null) {
            return JsonNull.INSTANCE;
        }

        if (isRFC4627jsonText(bodyValue)) {
            return parser.parse(bodyValue);
        }

        return new JsonPrimitive(bodyValue);
    }

    private boolean isRFC4627jsonText(final String s) {
        try {
            final JsonElement element = parser.parse(s);
            return element.isJsonObject() || element.isJsonArray();
        } catch (final JsonSyntaxException ex) {
            return false;
        }
    }
}
