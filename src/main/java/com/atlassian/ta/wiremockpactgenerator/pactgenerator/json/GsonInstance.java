package com.atlassian.ta.wiremockpactgenerator.pactgenerator.json;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactHttpBody;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonInstance {
    private GsonInstance() {

    }

    public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(PactHttpBody.class, new PactHttpBodySerializer())
            .registerTypeAdapter(PactRequest.class, new PactRequestSerializer())
            .registerTypeAdapter(PactResponse.class, new PactResponseSerializer())
            .disableHtmlEscaping()
            .serializeNulls()
            .setPrettyPrinting()
            .create();
}
