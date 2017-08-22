package com.atlassian.ta.wiremockpactgenerator.support;

import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

public class PactSpy {

    private final FileSystem fileSystem;

    public PactSpy(final FileSystem fileSystem) {
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

    public String firstRequestQuery() {
        return firstRequest().get("query").getAsString();
    }

    public Map<String, String> firstRequestHeaders() {
        return parseHeaders(firstRequest().getAsJsonObject("headers"));
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
        return parseHeaders(firstResponse().getAsJsonObject("headers"));
    }

    public String firstResponseBody() {
        return firstResponse().get("body").getAsString();
    }

    public JsonElement firstResponseBodyAsJson() {
        return firstResponse().get("body");
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

    private JsonObject firstRequest() {
        return getInteraction(0).getAsJsonObject("request");
    }

    private JsonObject firstResponse() {
        return getInteraction(0).getAsJsonObject("response");
    }

    private Map<String, String> parseHeaders(final JsonObject jsonHeaders) {
        final Map<String, String> headers = Maps.newHashMap();
        jsonHeaders.entrySet().forEach((entry) -> headers.put(entry.getKey(), entry.getValue().getAsString()));

        return headers;
    }

    private JsonObject getPactAsJson() {
        return new JsonParser().parse(jsonPact()).getAsJsonObject();
    }

    private JsonObject getInteraction(final int index) {
        return getPactAsJson().getAsJsonArray("interactions").get(index).getAsJsonObject();
    }
}
