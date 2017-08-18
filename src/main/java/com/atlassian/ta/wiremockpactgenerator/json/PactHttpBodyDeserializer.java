package com.atlassian.ta.wiremockpactgenerator.json;

import com.atlassian.ta.wiremockpactgenerator.models.PactHttpBody;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class PactHttpBodyDeserializer implements JsonDeserializer<PactHttpBody> {
    @Override
    public PactHttpBody deserialize(final JsonElement jsonElement,
                                    final Type type,
                                    final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new PactHttpBody(jsonElement.getAsString());
    }
}
