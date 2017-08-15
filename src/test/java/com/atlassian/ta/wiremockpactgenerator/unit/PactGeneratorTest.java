package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.WiremockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;
import com.atlassian.ta.wiremockpactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.models.PactResponse;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactSaver;
import com.atlassian.ta.wiremockpactgenerator.support.HeadersBuilder;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doThrow;

public class PactGeneratorTest {
    /* missing test cases
        - add tests for factory instance management
        - windows and slashes
     */

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FileSystem fileSystem;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldKnowWhereTheLocationOfThePactFileWillBe() {
        final PactGenerator pactGenerator = whenPactGeneratorIsCreated("aConsumer", "aProvider");

        assertThat(pactGenerator.getPactLocation(), equalTo("target/pacts/aConsumer-aProvider-pact.json"));
    }

    @Test
    public void shouldSaveAPactFileInTheTargetDirectory_WhenItExists() throws Throwable {
        givenThePathsExist("target");

        whenTheInteractionIsInvoked("consumerName", "providerName");

        verify(fileSystem).saveFile(eq("target/pacts/consumerName-providerName-pact.json"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheBuildDirectory_WhenItExists() throws Throwable {
        givenThePathsExist("build");

        whenTheInteractionIsInvoked("consumer", "provider");

        verify(fileSystem).saveFile(eq("build/pacts/consumer-provider-pact.json"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheTargetDirectory_WhenBothBuildAndTargetExists() throws Throwable {
        givenThePathsExist("target", "build");

        whenTheInteractionIsInvoked("consumerName", "providerName");

        verify(fileSystem).saveFile(eq("target/pacts/consumerName-providerName-pact.json"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheTargetDirectory_WhenNoOutputDirectoriesExist() throws Throwable {
        givenNoPathsExist();

        whenTheInteractionIsInvoked("consumerName", "providerName");

        verify(fileSystem).saveFile(eq("target/pacts/consumerName-providerName-pact.json"), anyString());
    }

    @Test
    public void shouldNormalizeConsumerInFileName_WhenItHasSpecialUnicodeSequences() throws Throwable {
        whenTheInteractionIsInvoked("el.Consumidor./Más.Importante☃", "providerName");

        verify(fileSystem).saveFile(
                eq("target/pacts/elConsumidorMasImportante-providerName-pact.json"), anyString());
    }

    @Test
    public void shouldNormalizeProviderInFileName_WhenItHasSpecialUnicodeSequences() throws Throwable {
        whenTheInteractionIsInvoked("consumerName", "☃proveedorEspañol?*/");

        verify(fileSystem).saveFile(
                eq("target/pacts/consumerName-proveedorEspanol-pact.json"), anyString());
    }

    @Test
    public void shouldCreateThePactDirectory_WhenTheOutputDirectoryDoesNotExist() {
        givenNoPathsExist();

        whenTheInteractionIsInvoked();

        verify(fileSystem).createPath("target/pacts");
    }

    @Test
    public void shouldCreateThePactDirectory_WhenItDoesNotExist() {
        givenThePathsExist("target");

        whenTheInteractionIsInvoked();

        verify(fileSystem).createPath("target/pacts");
    }

    @Test
    public void shouldNotCreateThePactDirectory_WhenItAlreadyExists() {
        givenThePathsExist("target", "target/pacts");

        whenTheInteractionIsInvoked();

        verify(fileSystem, never()).createPath("target/pacts");
    }

    @Test
    public void shouldWriteThePactContentToTheFile() throws Throwable {
        whenTheInteractionIsInvoked();

        verify(fileSystem).saveFile(anyString(), contains("interactions"));
    }

    @Test
    public void shouldNotDoHTMLEcaping_whenPactContainsSymbolsLikeGreaterThan() throws Throwable {
        whenTheInteractionIsInvoked("The<Consumer>", "provider");

        verify(fileSystem).saveFile(anyString(), contains("The<Consumer>"));
    }

    @Test
    public void shouldGenerate2SpaceIndentedPrettyJson() throws Throwable {
        whenTheInteractionIsInvoked();
        verify(fileSystem).saveFile(anyString(), contains("  \"interactions\": [\n    {"));
    }

    @Test
    public void shouldThrowWiremockPactGeneratorException_WhenFileCantBeSaved() throws Throwable {
        final Throwable cause = new RuntimeException("oops");

        expectAWiremockPactGeneratorException(
                "Unable to save file 'target/pacts/consumerName-providerName-pact.json'",
                cause
        );

        doThrow(cause).when(fileSystem).saveFile(anyString(), anyString());

        whenTheInteractionIsInvoked("consumerName", "providerName");
    }

    @Test
    public void shouldIncludeTheConsumerName() {
        whenTheInteractionIsInvoked("consumer-name", "provider-name");

        assertThat(getSavedPact().getConsumer().getName(), equalTo("consumer-name"));
    }

    @Test
    public void shouldIncludeTheProviderName() {
        whenTheInteractionIsInvoked("consumer-name", "provider-name");

        assertThat(getSavedPact().getProvider().getName(), equalTo("provider-name"));
    }

    @Test
    public void shouldFailIfConsumerNameIsNull() throws WiremockPactGeneratorException {
        expectAWiremockPactGeneratorException("Consumer name can't be null or blank");

        whenPactGeneratorIsCreated(null, "provider-name");
    }

    @Test
    public void shouldFailIfConsumerNameIsBlank() throws WiremockPactGeneratorException {
        expectAWiremockPactGeneratorException("Consumer name can't be null or blank");

        whenPactGeneratorIsCreated(" ", "provider-name");
    }

    @Test
    public void shouldFailIfProviderNameIsNull() throws WiremockPactGeneratorException {
        expectAWiremockPactGeneratorException("Provider name can't be null or blank");

        whenPactGeneratorIsCreated("consumer-name", null);
    }

    @Test
    public void shouldFailIfProviderNameIsBlank() throws WiremockPactGeneratorException {
        expectAWiremockPactGeneratorException("Provider name can't be null or blank");

        whenPactGeneratorIsCreated("consumer-name", "   ");
    }

    @Test
    public void shouldCaptureTheRequestMethod() {
        final PactGeneratorRequest request = aDefaultRequest()
                .withMethod("POST")
                .build();

        whenTheInteractionIsInvoked(request);

        assertThat(getFirstSavedInteraction().getRequest().getMethod(), equalTo("POST"));
    }

    @Test
    public void shouldCaptureTheRequestPath() {
        final PactGeneratorRequest request = aDefaultRequest()
                .withUrl("/some/path")
                .build();

        whenTheInteractionIsInvoked(request);

        assertThat(getFirstSavedInteraction().getRequest().getPath(), equalTo("/some/path"));
    }

    @Test
    public void shouldCaptureTheRequestQueryString() {
        final PactGeneratorRequest request = aDefaultRequest()
                .withUrl("/path?query=string&foo=bar")
                .build();

        whenTheInteractionIsInvoked(request);

        assertThat(getFirstSavedInteraction().getRequest().getQuery(), equalTo("query=string&foo=bar"));
    }

    @Test
    public void shouldSetQueryToNull_whenUrlHasNoQuery() {
        final PactGeneratorRequest request = aDefaultRequest()
                .withUrl("/path")
                .build();

        whenTheInteractionIsInvoked(request);

        assertThat(getFirstSavedInteraction().getRequest().getQuery(), is(nullValue()));
    }

    @Test
    public void shouldNotIncludeQueryAsPartOfTheRequestPathInThePactFile() {
        final PactGeneratorRequest request = aDefaultRequest()
                .withUrl("/path?query=string")
                .build();

        whenTheInteractionIsInvoked(request);

        assertThat(getFirstSavedInteraction().getRequest().getPath(), equalTo("/path"));
    }

    @Test
    public void shouldFail_whenResponseStatusIsTooLow() {
        expectAWiremockPactGeneratorException("Response status code is not valid: 99");
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withStatus(99)
                        .build()
        );
    }

    @Test
    public void shouldFail_whenResponseStatusIsTooHigh() {
        expectAWiremockPactGeneratorException("Response status code is not valid: 600");
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withStatus(600)
                        .build()
        );
    }

    @Test
    public void shouldCaptureRequestHeaders() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withHeaders(
                                new HeadersBuilder()
                                        .withHeader("accept", "application/json")
                                        .withHeader("x-header", "value")
                                        .build()
                        )
                        .build()
        );

        final Map<String, String> requestHeaders = getFirstSavedInteraction().getRequest().getHeaders();
        assertThat(requestHeaders.size(), is(2));
        assertThat(requestHeaders.get("accept"), equalTo("application/json"));
        assertThat(requestHeaders.get("x-header"), equalTo("value"));
    }

    @Test
    public void shouldProvideImmutableHeadersInThePactRequestModel() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withHeaders(singleHeader("accept", "application/json"))
                        .build()
        );
        final PactRequest pactRequest = getFirstSavedInteraction().getRequest();
        pactRequest.getHeaders().put("accept", "text/html");

        assertThat(pactRequest.getHeaders().get("accept"), equalTo("application/json"));
    }

    @Test
    public void shouldNormalizeRequestHeaderNames() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withHeaders(singleHeader("x-HeaDer-NAMe", "some value"))
                        .build()
        );

        final Map<String, String> requestHeaders = getFirstSavedInteraction().getRequest().getHeaders();
        assertThat(requestHeaders.size(), is(1));
        assertThat(requestHeaders.get("x-header-name"), equalTo("some value"));
    }

    @Test
    public void shouldCombineMultipleRequestHeadersValues() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withHeaders(singleHeader("x-header", "value 1", "value 2", "value 3"))
                        .build()
        );

        final Map<String, String> requestHeaders = getFirstSavedInteraction().getRequest().getHeaders();
        assertThat(requestHeaders.size(), is(1));
        assertThat(requestHeaders.get("x-header"), equalTo("value 1, value 2, value 3"));
    }

    @Test
    public void shouldSetRequestHeadersToNull_WhenThereAreNoHeaders() {
        whenTheInteractionIsInvoked(aDefaultRequest().build());

        assertThat(getFirstSavedInteraction().getRequest().getHeaders(), is(nullValue()));
    }

    @Test
    public void shouldCaptureTheRequestBody() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withBody("the request body")
                        .build()
        );

        assertThat(getFirstSavedInteraction().getRequest().getBody(), equalTo("the request body"));
    }

    @Test
    public void shouldSetRequestBodyToNull_WhenBodyIsEmpty() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withBody("")
                        .build()
        );

        assertThat(getFirstSavedInteraction().getRequest().getBody(), is(nullValue()));
    }

    @Test
    public void shouldCaptureTheResponseStatusCode() {
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withStatus(202)
                        .build()
        );

        assertThat(getFirstSavedInteraction().getResponse().getStatus(), equalTo(202));
    }

    @Test
    public void shouldCaptureResponseHeaders() {
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withHeaders(
                                new HeadersBuilder()
                                        .withHeader("content-type", "application/json")
                                        .withHeader("x-header", "value")
                                        .build()
                        )
                        .build()
        );

        final Map<String, String> responseHeaders = getFirstSavedInteraction().getResponse().getHeaders();
        assertThat(responseHeaders.size(), is(2));
        assertThat(responseHeaders.get("content-type"), equalTo("application/json"));
        assertThat(responseHeaders.get("x-header"), equalTo("value"));
    }

    @Test
    public void shouldProvideImmutableHeadersInThePactResponseModel() {
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withHeaders(singleHeader("content-type", "application/json"))
                        .build()
        );
        final PactResponse pactResponse = getFirstSavedInteraction().getResponse();
        pactResponse.getHeaders().put("content-type", "text/html");

        assertThat(pactResponse.getHeaders().get("content-type"), equalTo("application/json"));
    }

    @Test
    public void shouldNormalizeResponseHeaderNames() {
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withHeaders(singleHeader("x-HeaDer-NAMe", "some value"))
                        .build()
        );

        final Map<String, String> responseHeaders = getFirstSavedInteraction().getResponse().getHeaders();
        assertThat(responseHeaders.size(), is(1));
        assertThat(responseHeaders.get("x-header-name"), equalTo("some value"));
    }

    @Test
    public void shouldCombineMultipleResponseHeadersValues() {
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withHeaders(singleHeader("x-header", "value 1", "value 2", "value 3"))
                        .build()
        );

        final Map<String, String> responseHeaders = getFirstSavedInteraction().getResponse().getHeaders();
        assertThat(responseHeaders.size(), is(1));
        assertThat(responseHeaders.get("x-header"), equalTo("value 1, value 2, value 3"));
    }

    @Test
    public void shouldSetResponseHeadersToNull_WhenThereAreNoHeaders() {
        whenTheInteractionIsInvoked(aDefaultResponse().build());

        assertThat(getFirstSavedInteraction().getResponse().getHeaders(), is(nullValue()));
    }

    @Test
    public void shouldCaptureTheResponseBody() {
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withBody("the response body")
                        .build()
        );

        assertThat(getFirstSavedInteraction().getResponse().getBody(), equalTo("the response body"));
    }

    @Test
    public void shouldSetResponseBodyToNull_WhenBodyIsEmpty() {
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withBody("")
                        .build()
        );

        assertThat(getFirstSavedInteraction().getResponse().getBody(), is(nullValue()));
    }

    @Test
    public void shouldIncludeTheInteractionDescription() {
        final PactGeneratorRequest request = aDefaultRequest()
                .withMethod("GET")
                .withUrl("/path")
                .build();
        final PactGeneratorResponse response = aDefaultResponse()
                .withStatus(202)
                .build();

        whenTheInteractionIsInvoked(request, response);

        assertThat(getFirstSavedInteraction().getDescription(), equalTo("GET /path -> 202"));
    }

    @Test
    public void shouldNormalizeTheRequestMethodAsUpperCase() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withMethod("get")
                        .build()
        );

        assertThat(getFirstSavedInteraction().getRequest().getMethod(), equalTo("GET"));
    }

    private PactInteraction getFirstSavedInteraction() {
        return getSavedPact().getInteractions().get(0);
    }

    private Pact getSavedPact() {
        final ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        try {
            verify(fileSystem).saveFile(anyString(), jsonCaptor.capture());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return new Gson().fromJson(jsonCaptor.getValue(), Pact.class);
    }

    private void givenNoPathsExist() {
        givenThePathsExist();
    }

    private Map<String, List<String>> singleHeader(final String name, final String ...values) {
        return new HeadersBuilder()
                .withHeader(name, values)
                .build();
    }

    private void givenThePathsExist(final String... paths) {
        given(fileSystem.pathExists(anyString())).willAnswer(invocation -> {
            final String path = invocation.getArgument(0);
            return Arrays.asList(paths).contains(path);
        });
    }

    private void whenTheInteractionIsInvoked() {
        whenTheInteractionIsInvoked("default-consumer-name", "default-provider-name");
    }

    private void whenTheInteractionIsInvoked(final String consumerName, final String providerName) {
        final PactGeneratorRequest request = aDefaultRequest().build();
        whenTheInteractionIsInvoked(consumerName, providerName, request, aDefaultResponse().build());
    }

    private void whenTheInteractionIsInvoked(final PactGeneratorRequest request) {
        whenTheInteractionIsInvoked(request, aDefaultResponse().build());
    }

    private PactGeneratorRequest.Builder aDefaultRequest() {
        return new PactGeneratorRequest.Builder()
                .withMethod("GET")
                .withUrl("/path");
    }

    private PactGeneratorResponse.Builder aDefaultResponse() {
        return new PactGeneratorResponse.Builder()
                .withStatus(200);
    }

    private void whenTheInteractionIsInvoked(final PactGeneratorResponse response) {
        whenTheInteractionIsInvoked(aDefaultRequest().build(), response);
    }

    private void whenTheInteractionIsInvoked(final PactGeneratorRequest request, final PactGeneratorResponse response) {
        whenTheInteractionIsInvoked(
                "default-consumer-name",
                "default-provider-name",
                request,
                response);
    }

    private void whenTheInteractionIsInvoked(final String consumerName, final String providerName,
                                             final PactGeneratorRequest request, final PactGeneratorResponse response) {
        final PactGenerator pactGenerator = whenPactGeneratorIsCreated(consumerName, providerName);

        pactGenerator.saveInteraction(request, response);
    }

    private PactGenerator whenPactGeneratorIsCreated(final String consumerName, final String providerName) {
        final PactSaver pactSaver = new PactSaver(fileSystem);
        return new PactGenerator(consumerName, providerName, pactSaver);
    }

    private void expectAWiremockPactGeneratorException(final String message) {
        expectedException.expect(WiremockPactGeneratorException.class);
        expectedException.expectMessage(message);
    }

    private void expectAWiremockPactGeneratorException(final String message, final Throwable cause) {
        expectAWiremockPactGeneratorException(message);
        expectedException.expectCause(equalTo(cause));
    }
}
