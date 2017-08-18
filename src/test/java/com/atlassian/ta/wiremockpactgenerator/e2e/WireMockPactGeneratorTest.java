package com.atlassian.ta.wiremockpactgenerator.e2e;

import com.atlassian.ta.wiremockpactgenerator.json.GsonInstance;
import com.atlassian.ta.wiremockpactgenerator.WireMockPactGenerator;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;
import com.atlassian.ta.wiremockpactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.models.PactResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.gson.stream.JsonReader;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class WireMockPactGeneratorTest {
    private static final JsonSchema pactSchema = loadPactSchema();

    private interface Action {
        void perform();
    }

    private interface WireMockContext {
        void execute(WireMockServer wireMockServer);
    }

    @Test
    public void shouldCreateAValidPactFileWhenWireMockProcessesARequest() {
        final WireMockPactGenerator pactGenerator = new WireMockPactGenerator(
                uniqueName("the-consumer"),
                uniqueName("the-provider")
        );

        withWireMock(this::whenInteractionOccurs, pactGenerator);

        validateSchema(pactGenerator.getPactLocation());
    }

    @Test
    public void shouldConsolidatePactsAcrossMultipleWireMockInstances() {
        final String consumer = uniqueName("consumer");
        final String provider = uniqueName("provider");
        final WireMockPactGenerator pactGenerator1 = new WireMockPactGenerator(consumer, provider);
        final WireMockPactGenerator pactGenerator2 = new WireMockPactGenerator(consumer, provider);

        withWireMock(this::whenUniqueInteractionOccurs, pactGenerator1);

        withWireMock(this::whenUniqueInteractionOccurs, pactGenerator2);

        final Pact pact = loadPact(pactGenerator1.getPactLocation());

        assertThat(pact.getInteractions(), hasSize(2));
    }

    @Test
    public void shouldContainAllTheRequestAndResponseData() {
        final String consumer = uniqueName("consumer");
        final String provider = uniqueName("provider");
        final WireMockPactGenerator pactGenerator = new WireMockPactGenerator(consumer, provider);

        withWireMock((WireMockServer wireMockServer) -> {
            final StubMapping stub = post(urlEqualTo("/path/resource?foo=bar"))
                    .willReturn(
                            aResponse()
                                    .withStatus(200)
                                    .withHeader("content-type", "text/plain")
                                    .withHeader("x-header", "one", "two")
                                    .withBody("response body")
                    )
                    .build();

            withWireMockStub(wireMockServer, stub, () -> performRequest(
                    Unirest.post(urlForPath(wireMockServer, "/path/resource"))
                            .queryString("foo", "bar")
                            .header("accept", "text/plain")
                            .body("request body")
                            .getHttpRequest()
            ));
        }, pactGenerator);

        final Pact pact = loadPact(pactGenerator.getPactLocation());

        validateSchema(pactGenerator.getPactLocation());

        assertThat("consumer", pact.getConsumer().getName(), equalTo(consumer));
        assertThat("provider", pact.getProvider().getName(), equalTo(provider));
        assertThat("number of interactions", pact.getInteractions(), hasSize(1));

        final PactInteraction interaction = pact.getInteractions().get(0);
        final PactRequest request = interaction.getRequest();
        final PactResponse response = interaction.getResponse();
        final Map<String, String> requestHeaders = request.getHeaders();
        final Map<String, String> responseHeaders = response.getHeaders();

        assertThat("interaction.description", interaction.getDescription(), equalTo("POST /path/resource -> 200"));
        assertThat("interaction.request.method", request.getMethod(), equalTo("POST"));
        assertThat("interaction.request.path", request.getPath(), equalTo("/path/resource"));
        assertThat("interaction.request.query", request.getQuery(), equalTo("foo=bar"));
        assertThat("interaction.request.body", request.getBody().getValue(), equalTo("request body"));
        assertThat("interaction.request.headers.accept", requestHeaders.get("accept"), equalTo("text/plain"));
        assertThat("interaction.response.status", response.getStatus(), equalTo(200));
        assertThat("interaction.response.body", response.getBody().getValue(), equalTo("response body"));
        assertThat("interaction.response.headers.content-type", responseHeaders.get("content-type"), equalTo("text/plain"));
        assertThat("interaction.response.headers.x-header", responseHeaders.get("x-header"), equalTo("one, two"));
    }

    private String uniqueName(final String prefix) {
        return String.format("%s-%s", prefix, UUID.randomUUID());
    }

    private void whenInteractionOccurs(final WireMockServer server) {
        final StubMapping aGetResponse = get(urlEqualTo("/path/resource/"))
                .willReturn(aDefaultResponse()).build();

        withWireMockStub(server, aGetResponse, () -> performRequest(Unirest.get(urlForPath(server, "/path/resource/"))));
    }

    private void whenUniqueInteractionOccurs(final WireMockServer server) {
        final StubMapping response = post(urlMatching("/path/resource/.+"))
                .willReturn(aDefaultResponse()).build();

        withWireMockStub(server, response, () -> {
            final String path = String.format("/path/resource/%s", UUID.randomUUID());
            performRequest(
                    Unirest.post(urlForPath(server, path))
                            .queryString("query", "string")
                            .header("accept", "anything")
                            .body("some content")
                            .getHttpRequest()
            );
        });
    }

    private ResponseDefinitionBuilder aDefaultResponse() {
        return  aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody("the body");
    }

    private String urlForPath(final WireMockServer wireMockServer, final String path) {
        return String.format("http://localhost:%d%s", wireMockServer.port(), path);
    }

    private Pact loadPact(final String filename) {
        final JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(filename));
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("Failed to load pact", e);
        }
        return GsonInstance.gson.fromJson(reader, Pact.class);
    }

    private void withWireMock(final WireMockContext action) {
        final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        try {
            action.execute(wireMockServer);
        } finally {
            wireMockServer.stop();
        }
    }

    private void withWireMock(final WireMockContext action, final WireMockPactGenerator listener) {
        withWireMock((WireMockServer wireMockServer) -> {
            wireMockServer.addMockServiceRequestListener(listener);
            action.execute(wireMockServer);
        });
    }

    private void withWireMockStub(final WireMockServer wireMockServer, final StubMapping stub, final Action action) {
        wireMockServer.addStubMapping(stub);
        try {
            action.perform();
        } finally {
            wireMockServer.removeStubMapping(stub);
        }
    }

    private void performRequest(final HttpRequest request) {
        try {
            request.asString();
        } catch (final UnirestException e) {
            throw new RuntimeException("Request failed", e);
        }
    }

    private void validateSchema(final String filename) {
        try {
            final JsonNode pact = JsonLoader.fromFile(new File(filename));
            final ProcessingReport result = pactSchema.validate(pact);
            final String failureMessage = result.toString();
            assertThat(failureMessage, result.isSuccess());
        } catch (final IOException e) {
            throw new RuntimeException("Unable load file", e);
        } catch (final ProcessingException e) {
            throw new RuntimeException("Unable to validate schema", e);
        }
    }

    private static JsonSchema loadPactSchema() {
        try {
            return JsonSchemaFactory.byDefault().getJsonSchema("resource:/pactv1.schema.json");
        } catch (final ProcessingException e) {
            throw new RuntimeException("Unable to load pact schema", e);
        }
    }
}
