package com.atlassian.ta.wiremockpactgenerator.json;

import com.atlassian.ta.wiremockpactgenerator.models.PactHttpBody;
import com.atlassian.ta.wiremockpactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.models.PactResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonInstance {
    private GsonInstance() {

    }

    public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(PactHttpBody.class, new PactHttpBodySerializer())
            .registerTypeAdapter(PactHttpBody.class, new PactHttpBodyDeserializer())
            .registerTypeAdapter(PactRequest.class, new PactRequestSerializer())
            .registerTypeAdapter(PactResponse.class, new PactResponseSerializer())
            .disableHtmlEscaping()
            .serializeNulls()
            .setPrettyPrinting()
            .create();
}
