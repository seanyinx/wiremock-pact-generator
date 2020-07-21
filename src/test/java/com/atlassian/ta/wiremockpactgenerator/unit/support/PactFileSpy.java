package com.atlassian.ta.wiremockpactgenerator.unit.support;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.FileSystem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PactFileSpy {

    private final FileSystem fileSystem;

    public PactFileSpy(final FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public String consumerName() {
        return getPactAsJson().getAsJsonObject("consumer").get("name").getAsString();
    }

    public String providerName() {
        return getPactAsJson().getAsJsonObject("provider").get("name").getAsString();
    }

    public String firstInteractionDescription() {
        return getInteraction(0).get("description").getAsString();
    }

    public String firstRequestMethod() {
        return firstRequest().get("method").getAsString();
    }

    public String firstRequestPath() {
        return firstRequest().get("path").getAsString();
    }

    public Map<String, List<String>> firstRequestQuery() {
        return getQuery(firstRequest());
    }

    public Map<String, String> firstRequestHeaders() {
        return getHeaders(firstRequest());
    }

    public String firstRequestBody() {
        return firstRequest().get("body").getAsString();
    }

    public JsonElement firstRequestBodyAsJson() {
        return firstRequest().get("body");
    }

    public int firstResponseStatus() {
        return firstResponse().get("status").getAsInt();
    }

    public Map<String, String> firstResponseHeaders() {
        return getHeaders(firstResponse());
    }

    public String firstResponseBody() {
        return firstResponse().get("body").getAsString();
    }

    public JsonElement firstResponseBodyAsJson() {
        return firstResponse().get("body");
    }

    public String firstProviderState() {
        return getInteraction(0)
                .getAsJsonArray("providerStates").get(0)
                .getAsJsonObject()
                .get("name")
                .getAsString();
    }

    public Map<String, String> metadata() {
        return getMetadata(getPactAsJson().getAsJsonObject("metadata"));
    }

    public String jsonPact() {
        final ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        try {
            verify(fileSystem).saveFile(anyString(), jsonCaptor.capture());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return jsonCaptor.getValue();
    }

    public void verifyNoInteractionsSaved() {
        try {
            verify(fileSystem, never()).saveFile(anyString(), anyString());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int interactionCount() {
        return getInteractions().size();
    }

    private JsonObject firstRequest() {
        return getInteraction(0).getAsJsonObject("request");
    }

    private JsonObject firstResponse() {
        return getInteraction(0).getAsJsonObject("response");
    }

    private Map<String, List<String>> getQuery(final JsonObject httpMessage) {
        final Map<String, List<String>> queries = new HashMap<>();

        if (httpMessage.has("query")) {
            httpMessage.getAsJsonObject("query")
                    .entrySet()
                    .forEach(entry -> {
                        final JsonArray jsonArray = entry.getValue().getAsJsonArray();
                        final List<String> values = new ArrayList<>(jsonArray.size());
                        for (JsonElement jsonElement : jsonArray) {
                            values.add(jsonElement.getAsString());
                        }
                        queries.put(entry.getKey(), values);
                    });
        }

        return queries;
    }

    private Map<String, String> getHeaders(final JsonObject httpMessage) {
        final Map<String, String> headers = new HashMap<>();

        if (httpMessage.has("headers")) {
            httpMessage.getAsJsonObject("headers")
                    .entrySet()
                    .forEach(entry -> headers.put(entry.getKey(), entry.getValue().getAsString()));
        }

        return headers;
    }

    private Map<String, String> getMetadata(final JsonObject metadata) {
        final Map<String, String> specifications = new HashMap<>();

        if (metadata.has("pactSpecification")) {
            metadata.getAsJsonObject("pactSpecification")
                    .entrySet()
                    .forEach(entry -> specifications.put(entry.getKey(), entry.getValue().getAsString()));
        }

        return specifications;
    }

    private JsonObject getPactAsJson() {
        return new JsonParser().parse(jsonPact()).getAsJsonObject();
    }

    private JsonArray getInteractions() {
        return getPactAsJson().getAsJsonArray("interactions");
    }

    private JsonObject getInteraction(final int index) {
        return getInteractions().get(index).getAsJsonObject();
    }
}
