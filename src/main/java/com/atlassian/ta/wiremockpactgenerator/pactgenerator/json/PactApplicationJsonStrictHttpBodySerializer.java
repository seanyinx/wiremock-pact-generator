package com.atlassian.ta.wiremockpactgenerator.pactgenerator.json;

import com.google.gson.JsonElement;

public class PactApplicationJsonStrictHttpBodySerializer extends PactHttpBodySerializer {
    @Override
    protected boolean shouldSerializeElement(final JsonElement element) {
        return element.isJsonObject() || element.isJsonArray();
    }
}
