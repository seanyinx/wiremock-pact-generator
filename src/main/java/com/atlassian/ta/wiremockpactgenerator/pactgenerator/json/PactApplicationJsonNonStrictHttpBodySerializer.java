package com.atlassian.ta.wiremockpactgenerator.pactgenerator.json;

import com.google.gson.JsonElement;

public class PactApplicationJsonNonStrictHttpBodySerializer extends PactHttpBodySerializer {
    @Override
    protected boolean shouldSerializeElement(final JsonElement element) {
        return true;
    }
}
