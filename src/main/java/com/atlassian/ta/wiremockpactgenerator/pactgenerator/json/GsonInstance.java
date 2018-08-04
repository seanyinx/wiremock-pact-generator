package com.atlassian.ta.wiremockpactgenerator.pactgenerator.json;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactHttpBody;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonInstance {
    private GsonInstance() {

    }

    public static Gson strictGson = GsonInstance.createBaseGsonBuilder()
            .registerTypeAdapter(PactHttpBody.class, new PactApplicationJsonStrictHttpBodySerializer())
            .create();

    public static Gson nonStrictGson = GsonInstance.createBaseGsonBuilder()
            .registerTypeAdapter(PactHttpBody.class, new PactApplicationJsonNonStrictHttpBodySerializer())
            .create();

    private static GsonBuilder createBaseGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(PactRequest.class, new PactRequestSerializer())
                .registerTypeAdapter(PactResponse.class, new PactResponseSerializer())
                .disableHtmlEscaping()
                .serializeNulls()
                .setPrettyPrinting();
    }
}
