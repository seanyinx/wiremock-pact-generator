package com.atlassian.ta.wiremockpactgenerator.e2e;

import com.atlassian.ta.wiremockpactgenerator.WireMockPactGenerator;
import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.e2e.support.PactHttpBodyDeserializer;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.Pact;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactHttpBody;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.PactResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class WireMockPactGeneratorTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldConsolidatePactsAcrossMultipleWireMockInstances() {
        final String consumer = uniqueName("consumer");
        final String provider = uniqueName("provider");
        final WireMockPactGenerator pactGenerator1 = WireMockPactGenerator.builder(consumer, provider).build();
        final WireMockPactGenerator pactGenerator2 = WireMockPactGenerator.builder(consumer, provider).build();

        withWireMock(pactGenerator1, this::givenAStubForAnyPost, this::whenAUniquePostRequest);
        withWireMock(pactGenerator2, this::givenAStubForAnyPost, this::whenAUniquePostRequest);

        assertThat(pactGenerator1.getPactLocation(), equalTo(pactGenerator2.getPactLocation()));

        final Pact pact = loadPact(pactGenerator1.getPactLocation());

        assertThat(pact.getInteractions(), hasSize(2));
    }

    @Test
    public void shouldHonorEachWireMockPactGeneratorRequestPathWhitelistAndBlacklist() {
        final String consumer = uniqueName("consumer");
        final String provider = uniqueName("provider");
        final WireMockPactGenerator pactGeneratorForPath1AndPath2 = WireMockPactGenerator
            .builder(consumer, provider)
            .withRequestPathWhitelist(
                "/matches/path-1/.*",
                "/matches/path-2/.*")
            .withRequestPathBlacklist(
                "/matches/path-3/.*"
            )
            .build();
        final WireMockPactGenerator pactGeneratorForPath3AndPath4 = WireMockPactGenerator
            .builder(consumer, provider)
            .withRequestPathWhitelist(
                "/matches/path-3/.*",
                "/matches/path-4/.*")
            .withRequestPathBlacklist(
                "/matches/path-1/.*"
            )
            .build();

        withWireMock(
            pactGeneratorForPath1AndPath2,
            this::givenAStubForAnyPost,
            wireMockServer -> {
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-1/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-3/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-4/");
            }
        );

        withWireMock(
            pactGeneratorForPath3AndPath4,
            this::givenAStubForAnyPost,
            wireMockServer -> {
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-1/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-2/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-3/");
            }
        );

        final Pact pact = loadPact(pactGeneratorForPath1AndPath2.getPactLocation());
        assertThat(pact.getInteractions(), hasSize(2));
        assertThat(pact.getInteractions().get(0).getRequest().getPath(), startsWith("/matches/path-1/"));
        assertThat(pact.getInteractions().get(1).getRequest().getPath(), startsWith("/matches/path-3/"));
    }

    @Test
    public void shouldCombineRequestPathWhitelists_whenMultipleListsArePassed() {
        final WireMockPactGenerator wireMockPactGenerator = WireMockPactGenerator
            .builder(uniqueName("consumer"), uniqueName("provider"))
            .withRequestPathWhitelist("/matches/path-1/.*")
            .withRequestPathWhitelist("/matches/path-2/.*", "/matches/path-3/.*")
            .build();

        withWireMock(wireMockPactGenerator,
            this::givenAStubForAnyPost,
            wireMockServer -> {
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-1/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-2/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-3/");
            });

        final Pact pact = loadPact(wireMockPactGenerator.getPactLocation());
        assertThat(pact.getInteractions(), hasSize(3));
    }

    @Test
    public void shouldCombineRequestPathBlacklists_whenMultipleListsArePassed() {
        final WireMockPactGenerator wireMockPactGenerator = WireMockPactGenerator
            .builder(uniqueName("consumer"), uniqueName("provider"))
            .withRequestPathBlacklist("/matches/path-1/.*")
            .withRequestPathBlacklist("/matches/path-2/.*", "/matches/path-3/.*")
            .build();

        withWireMock(wireMockPactGenerator,
            this::givenAStubForAnyPost,
            wireMockServer -> {
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-1/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-2/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/matches/path-3/");
                whenAPostRequestToAPathStartingWith(wireMockServer, "/accept/this");
            });

        final Pact pact = loadPact(wireMockPactGenerator.getPactLocation());
        assertThat(pact.getInteractions(), hasSize(1));
        assertThat(pact.getInteractions().get(0).getRequest().getPath(), startsWith("/accept/this"));
    }

    @Test
    public void shouldContainAllTheRequestAndResponseData() {
        final String consumer = uniqueName("consumer");
        final String provider = uniqueName("provider");
        final WireMockPactGenerator pactGenerator = WireMockPactGenerator
            .builder(consumer, provider)
            .build();

        final ResponseDefinitionBuilder responseDefinition = aResponse()
            .withStatus(200)
            .withHeader("content-type", "text/plain")
            .withHeader("x-header", "one")
            .withHeader("x-header", "two")
            .withBody("response body");

        withWireMock(pactGenerator,
            wireMockServer -> givenAStubForAnyPostWithResponse(wireMockServer, responseDefinition),
            wireMockServer -> performRequest(
                Unirest.post(urlForPath(wireMockServer, "/path/resource"))
                    .queryString("foo", "bar")
                    .header("accept", "text/plain")
                    .body("request body")
                    .getHttpRequest()
            )

        );

        final Pact pact = loadPact(pactGenerator.getPactLocation());

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
        assertThat("interaction.response.headers.content-type",
                responseHeaders.get("content-type"), equalTo("text/plain"));
        assertThat("interaction.response.headers.x-header", responseHeaders.get("x-header"), equalTo("one, two"));
    }

    @Test
    public void shouldAllowNonStrictApplicationJsonSerialization() {
        final WireMockPactGenerator pactGenerator = WireMockPactGenerator
                .builder(uniqueName("consumer"), uniqueName("provider"))
                .withStrictApplicationJson(false)
                .build();

        final String capturedBody = getPactGeneratorCapturedBodyFor(pactGenerator, "\"a quoted json string\"");

        assertThat("interaction.request|response.body", capturedBody, equalTo("a quoted json string"));
    }

    @Test
    public void shouldUseStrictApplicationJsonSerializationByDefault() {
        final WireMockPactGenerator pactGenerator = WireMockPactGenerator
                .builder(uniqueName("consumer"), uniqueName("provider"))
                .build();

        final String capturedBody = getPactGeneratorCapturedBodyFor(pactGenerator, "\"a quoted json string\"");

        assertThat("interaction.request|response.body", capturedBody, equalTo("\"a quoted json string\""));
    }

    @Test
    public void shouldAllowExplicitStrictApplicationJsonSerialization() {
        final WireMockPactGenerator pactGenerator = WireMockPactGenerator
                .builder(uniqueName("consumer"), uniqueName("provider"))
                .withStrictApplicationJson(true)
                .build();

        final String capturedBody = getPactGeneratorCapturedBodyFor(pactGenerator, "\"a quoted json string\"");

        assertThat("interaction.request|response.body", capturedBody, equalTo("\"a quoted json string\""));
    }

    @Test
    public void shouldGenerateAnInteractionEvenWhenTheRequestDoesNotMatchAnyWireMockStubbing() {
        final WireMockPactGenerator wireMockPactGenerator = WireMockPactGenerator
                .builder(uniqueName("consumer"), uniqueName("provider")).build();

        withWireMock(wireMockPactGenerator,
            wireMockServer -> { },
            wireMockServer -> performRequest(
                    Unirest.get(urlForPath(wireMockServer, "/some/path")).getHttpRequest()
            )
        );

        final Pact pact = loadPact(wireMockPactGenerator.getPactLocation());
        final PactInteraction interaction = pact.getInteractions().get(0);
        final PactRequest request = interaction.getRequest();
        final PactResponse response = interaction.getResponse();
        assertThat("interaction.description",
                interaction.getDescription(), equalTo("GET /some/path -> 404 [Not configured in WireMock]"));
        assertThat("interaction.request.path", request.getPath(), equalTo("/some/path"));
        assertThat("interaction.response.status", response.getStatus(), equalTo(404));
    }

    @Test
    public void shouldFailRightAway_whenProvidingAnInvalidConsumerName() {
        expectedException.expect(WireMockPactGeneratorException.class);

        WireMockPactGenerator.builder("", "provider").build();
    }

    @Test
    public void shouldFailRightAway_whenProvidingAnInvalidProviderName() {
        expectedException.expect(WireMockPactGeneratorException.class);

        WireMockPactGenerator.builder("consumer", "").build();
    }

    @Test
    public void shouldFailRightAway_whenProvidingInvalidRequestPathWhitelistRegex() {
        expectedException.expect(WireMockPactGeneratorException.class);

        WireMockPactGenerator
            .builder("consumer", "provider")
            .withRequestPathWhitelist(
                "/valid/.*",
                "*/invalid")
            .build();
    }

    @Test
    public void shouldFailRightAway_whenProvidingInvalidRequestPathBlacklistRegex() {
        expectedException.expect(WireMockPactGeneratorException.class);

        WireMockPactGenerator
                .builder("consumer", "provider")
                .withRequestPathBlacklist(
                        "/valid/.*",
                        "*/invalid")
                .build();
    }

    @Test
    public void shouldApplyRequestHeadersWhitelists_whenMultipleListsArePassed() {
        final WireMockPactGenerator wireMockPactGenerator = WireMockPactGenerator
                .builder(uniqueName("consumer"), uniqueName("provider"))
                .withRequestHeaderWhitelist("in-whitelisted-header-1")
                .withRequestHeaderWhitelist("in-whitelisted-header-2", "in-whitelisted-header-3")
                .build();

        withWireMock(wireMockPactGenerator,
            wireMockServer -> givenAStubForAnyPostWithResponse(wireMockServer, aResponse()),
            wireMockServer -> performRequest(
                     Unirest.post(urlForPath(wireMockServer, "/path/resource"))
                            .header("in-whitelisted-header-1", "header-1-val")
                            .header("in-whitelisted-header-2", "header-2-val")
                            .header("in-whitelisted-header-3", "header-3-val")
                            .header("not-in-whitelisted-header-4", "header-4-val")
                            .getHttpRequest()
             )
        );

        final Pact pact = loadPact(wireMockPactGenerator.getPactLocation());
        assertThat(pact.getInteractions(), hasSize(1));
        assertThat(pact.getInteractions().get(0).getRequest().getHeaders().keySet(),
                   equalTo(ImmutableSet.of("in-whitelisted-header-1", "in-whitelisted-header-2", "in-whitelisted-header-3")));
        assertThat(
                new HashSet<>(pact.getInteractions().get(0).getRequest().getHeaders().values()),
                equalTo(ImmutableSet.of("header-1-val", "header-2-val", "header-3-val")));
    }

    @Test
    public void shouldApplyResponseHeadersWhitelists_whenMultipleListsArePassed() {
        final WireMockPactGenerator wireMockPactGenerator = WireMockPactGenerator
                .builder(uniqueName("consumer"), uniqueName("provider"))
                .withResponseHeaderWhitelist("in-whitelisted-header-1")
                .withResponseHeaderWhitelist("in-whitelisted-header-2", "in-whitelisted-header-3")
                .build();

        final ResponseDefinitionBuilder responseDefinition = aResponse()
                .withHeader("in-whitelisted-header-1", "header-1-val")
                .withHeader("in-whitelisted-header-2", "header-2-val")
                .withHeader("in-whitelisted-header-3", "header-3-val")
                .withHeader("not-in-whitelisted-header-4", "header-4-val");

        withWireMock(wireMockPactGenerator,
            wireMockServer -> givenAStubForAnyPostWithResponse(wireMockServer, responseDefinition),
            wireMockServer -> performRequest(
                     Unirest.post(urlForPath(wireMockServer, "/path/resource")).getHttpRequest()
             )
        );

        final Pact pact = loadPact(wireMockPactGenerator.getPactLocation());
        assertThat(pact.getInteractions(), hasSize(1));
        assertThat(pact.getInteractions().get(0).getResponse().getHeaders().keySet(),
                   equalTo(ImmutableSet.of("in-whitelisted-header-1", "in-whitelisted-header-2", "in-whitelisted-header-3")));
        assertThat(new HashSet<>(pact.getInteractions().get(0).getResponse().getHeaders().values()),
                   equalTo(ImmutableSet.of("header-1-val", "header-2-val", "header-3-val")));
    }

    private String uniqueName(final String prefix) {
        final String uuid = UUID.randomUUID().toString();
        return String.format("%s%s", prefix, uuid.substring(uuid.length() - 12));
    }

    private void whenAUniquePostRequest(final WireMockServer wireMockServer) {
        whenAPostRequestToAPathStartingWith(wireMockServer, "/");
    }

    private void whenAPostRequestToAPathStartingWith(final WireMockServer wireMockServer, final String basePath) {
        final String path = basePath + UUID.randomUUID();
        performRequest(
            Unirest.post(urlForPath(wireMockServer, path))
                .queryString("query", "string")
                .header("accept", "anything")
                .body("some content")
                .getHttpRequest()
        );
    }

    private String getPactGeneratorCapturedBodyFor(final WireMockPactGenerator pactGenerator, final String body) {
        final ResponseDefinitionBuilder responseDefinition = aResponse()
                .withBody(body);

        withWireMock(pactGenerator,
            wireMockServer -> givenAStubForAnyPostWithResponse(wireMockServer, responseDefinition),
            wireMockServer -> performRequest(
                Unirest.post(urlForPath(wireMockServer, "/path/resource"))
                    .body(body)
                    .getHttpRequest()
            )
        );

        final Pact pact = loadPact(pactGenerator.getPactLocation());

        final PactInteraction interaction = pact.getInteractions().get(0);
        final PactRequest request = interaction.getRequest();
        final PactResponse response = interaction.getResponse();
        assertThat("request equals response body", request.getBody().getValue(), equalTo(response.getBody().getValue()));
        return request.getBody().getValue();
    }

    private void givenAStubForAnyPost(final WireMockServer wireMockServer) {
        givenAStubForAnyPostWithResponse(wireMockServer, aDefaultResponse());
    }

    private void givenAStubForAnyPostWithResponse(
        final WireMockServer wireMockServer,
        final ResponseDefinitionBuilder responseDefinition
    ) {
        final StubMapping stub = post(urlMatching(".+")).willReturn(responseDefinition).build();
        wireMockServer.addStubMapping(stub);
    }

    private ResponseDefinitionBuilder aDefaultResponse() {
        return aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/plain")
            .withBody("the body");
    }

    private String urlForPath(final WireMockServer wireMockServer, final String path) {
        return String.format("http://localhost:%d%s", wireMockServer.port(), path);
    }

    private void withWireMock(
        final WireMockPactGenerator listener,
        final Consumer<WireMockServer> setupWireMock,
        final Consumer<WireMockServer> makeRequests
    ) {
        final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        try {
            wireMockServer.addMockServiceRequestListener(listener);
            setupWireMock.accept(wireMockServer);
            makeRequests.accept(wireMockServer);
        } finally {
            wireMockServer.stop();
        }
    }

    private void performRequest(final HttpRequest request) {
        try {
            request.asString();
        } catch (final UnirestException e) {
            throw new RuntimeException("Request failed", e);
        }
    }

    private Pact loadPact(final String filename) {
        final JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(filename));
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("Failed to load pact", e);
        }

        validateSchema(filename);

        return new GsonBuilder()
            .registerTypeAdapter(PactHttpBody.class, new PactHttpBodyDeserializer())
            .create()
            .fromJson(reader, Pact.class);
    }

    private void validateSchema(final String filename) {
        try {
            final JsonNode pact = JsonLoader.fromFile(new File(filename));
            final ProcessingReport result = loadPactSchema().validate(pact);
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
