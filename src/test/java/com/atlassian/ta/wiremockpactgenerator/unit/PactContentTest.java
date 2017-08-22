package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.WiremockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.support.HeadersBuilder;
import com.atlassian.ta.wiremockpactgenerator.support.InteractionBuilder;
import com.atlassian.ta.wiremockpactgenerator.support.PactSpy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class PactContentTest {
    /* missing test cases
        - windows and slashes
     */

    @Mock
    private FileSystem fileSystem;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private InteractionBuilder interactionBuilder;
    private PactSpy pactSpy;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        interactionBuilder = new InteractionBuilder(fileSystem);
        pactSpy = new PactSpy(fileSystem);
    }

    @Test
    public void shouldIncludeTheConsumerName() {
        interactionBuilder
                .withConsumer("consumer-name")
                .perform();

        assertThat(pactSpy.consumerName(), equalTo("consumer-name"));
    }

    @Test
    public void shouldIncludeTheProviderName() {
        interactionBuilder
                .withProvider("provider-name")
                .perform();

        assertThat(pactSpy.providerName(), equalTo("provider-name"));
    }

    @Test
    public void shouldFailIfConsumerNameIsNull() throws WiremockPactGeneratorException {
        expectAWiremockPactGeneratorException("Consumer name can't be null or blank");

        interactionBuilder
                .withConsumer(null)
                .perform();
    }

    @Test
    public void shouldFailIfConsumerNameIsBlank() throws WiremockPactGeneratorException {
        expectAWiremockPactGeneratorException("Consumer name can't be null or blank");

        interactionBuilder
                .withConsumer(" ")
                .perform();
    }

    @Test
    public void shouldFailIfProviderNameIsNull() throws WiremockPactGeneratorException {
        expectAWiremockPactGeneratorException("Provider name can't be null or blank");

        interactionBuilder
                .withProvider(null)
                .perform();
    }

    @Test
    public void shouldFailIfProviderNameIsBlank() throws WiremockPactGeneratorException {
        expectAWiremockPactGeneratorException("Provider name can't be null or blank");

        interactionBuilder
                .withProvider(" ")
                .perform();
    }

    @Test
    public void shouldCaptureTheRequestMethod() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withMethod("POST")
                                .build()
                )
                .perform();

        assertThat(pactSpy.firstRequestMethod(), equalTo("POST"));
    }

    @Test
    public void shouldCaptureTheRequestPath() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/some/path")
                                .build()
                )
                .perform();

        assertThat(pactSpy.firstRequestPath(), equalTo("/some/path"));
    }

    @Test
    public void shouldCaptureTheRequestQueryString() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/path?query=string&foo=bar")
                                .build()
                )
                .perform();

        assertThat(pactSpy.firstRequestQuery(), equalTo("query=string&foo=bar"));
    }

    @Test
    public void shouldNotIncludeQueryProperty_whenUrlHasNoQuery() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/path")
                                .build()
                )
                .perform();

        assertThat(pactSpy.jsonPact(), not(containsString("\"query\":")));
    }

    @Test
    public void shouldNotIncludeQueryAsPartOfTheRequestPathInThePactFile() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/path?query=string")
                                .build()
                )
                .perform();

        assertThat(pactSpy.firstRequestPath(), equalTo("/path"));
    }

    @Test
    public void shouldFail_whenResponseStatusIsTooLow() {
        expectAWiremockPactGeneratorException("Response status code is not valid: 99");
        interactionBuilder
                .withResponse(
                        aDefaultResponse()
                                .withStatus(99)
                                .build()
                )
                .perform();
    }

    @Test
    public void shouldFail_whenResponseStatusIsTooHigh() {
        expectAWiremockPactGeneratorException("Response status code is not valid: 600");
        interactionBuilder
                .withResponse(
                        aDefaultResponse()
                                .withStatus(600)
                                .build()
                )
                .perform();
    }

    @Test
    public void shouldCaptureRequestHeaders() {
        interactionBuilder
            .withRequest(
                aDefaultRequest()
                    .withHeaders(
                        new HeadersBuilder()
                            .withHeader("accept", "application/json")
                            .withHeader("x-header", "value")
                            .build()
                    )
                    .build()
            )
            .perform();

        final Map<String, String> requestHeaders = pactSpy.firstRequestHeaders();
        assertThat(requestHeaders.values(), hasSize(2));
        assertThat(requestHeaders.get("accept"), equalTo("application/json"));
        assertThat(requestHeaders.get("x-header"), equalTo("value"));
    }

    @Test
    public void shouldNormalizeRequestHeaderNames() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(singleHeader("x-HeaDer-NAMe", "some value"))
                                .build()
                )
                .perform();

        final Map<String, String> requestHeaders = pactSpy.firstRequestHeaders();
        assertThat(requestHeaders.values(), hasSize(1));
        assertThat(requestHeaders.get("x-header-name"), equalTo("some value"));
    }

    @Test
    public void shouldCombineMultipleRequestHeadersValues() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(singleHeader("x-header", "value 1", "value 2", "value 3"))
                                .build()
                )
                .perform();

        final Map<String, String> requestHeaders = pactSpy.firstRequestHeaders();
        assertThat(requestHeaders.values(), hasSize(1));
        assertThat(requestHeaders.get("x-header"), equalTo("value 1, value 2, value 3"));
    }

    @Test
    public void shouldNotSetRequestOrResponseHeadersProperty_WhenThereAreNoHeaders() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(null)
                                .build())
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(null)
                                .build()
                )
                .perform();

        assertThat(pactSpy.jsonPact(), not(containsString("\"headers\":")));
    }

    @Test
    public void shouldCaptureTheRequestBody() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withBody("the request body")
                                .build()
                )
                .perform();

        assertThat(pactSpy.firstRequestBody(), equalTo("the request body"));
    }

    @Test
    public void shouldNotSetRequestOrResponseBodyProperty_WhenBodyIsEmpty() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withBody("")
                                .build()
                )
                .withResponse(
                        aDefaultResponse()
                                .withBody("")
                                .build()
                )
                .perform();

        assertThat(pactSpy.jsonPact(), not(containsString("\"body\":")));
    }

    @Test
    public void shouldCaptureTheResponseStatusCode() {
        interactionBuilder
                .withResponse(
                        aDefaultResponse()
                                .withStatus(202)
                                .build()
                )
                .perform();

        assertThat(pactSpy.firstResponseStatus(), equalTo(202));
    }

    @Test
    public void shouldCaptureResponseHeaders() {
        interactionBuilder
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(
                                        new HeadersBuilder()
                                                .withHeader("content-type", "application/json")
                                                .withHeader("x-header", "value")
                                                .build()
                                )
                                .build()
                )
                .perform();

        final Map<String, String> responseHeaders = pactSpy.firstResponseHeaders();
        assertThat(responseHeaders.values(), hasSize(2));
        assertThat(responseHeaders.get("content-type"), equalTo("application/json"));
        assertThat(responseHeaders.get("x-header"), equalTo("value"));
    }

    @Test
    public void shouldNormalizeResponseHeaderNames() {
        interactionBuilder
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(singleHeader("x-HeaDer-NAMe", "some value"))
                                .build()
                )
                .perform();

        final Map<String, String> responseHeaders = pactSpy.firstResponseHeaders();
        assertThat(responseHeaders.values(), hasSize(1));
        assertThat(responseHeaders.get("x-header-name"), equalTo("some value"));
    }

    @Test
    public void shouldCombineMultipleResponseHeadersValues() {
        interactionBuilder
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(singleHeader("x-header", "value 1", "value 2", "value 3"))
                                .build()
                )
                .perform();

        final Map<String, String> responseHeaders = pactSpy.firstResponseHeaders();
        assertThat(responseHeaders.values(), hasSize(1));
        assertThat(responseHeaders.get("x-header"), equalTo("value 1, value 2, value 3"));
    }

    @Test
    public void shouldCaptureTheResponseBody() {
        interactionBuilder
                .withResponse(
                        aDefaultResponse()
                                .withBody("the response body")
                                .build()
                )
                .perform();

        assertThat(pactSpy.firstResponseBody(), equalTo("the response body"));
    }

    @Test
    public void shouldIncludeTheInteractionDescription() {
        interactionBuilder
                .withRequest(
                        aDefaultRequest()
                                .withMethod("GET")
                                .withUrl("/path")
                                .build()
                )
                .withResponse(
                        aDefaultResponse()
                                .withStatus(202)
                                .build()
                )
                .perform();

        assertThat(pactSpy.firstInteractionDescription(), equalTo("GET /path -> 202"));
    }

    @Test
    public void shouldNormalizeTheRequestMethodAsUpperCase() {
        interactionBuilder.withRequest(
                aDefaultRequest()
                        .withMethod("get")
                        .build()
        )
                .perform();

        assertThat(pactSpy.firstRequestMethod(), equalTo("GET"));
    }

    private Map<String, List<String>> singleHeader(final String name, final String... values) {
        return new HeadersBuilder()
                .withHeader(name, values)
                .build();
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

    private void expectAWiremockPactGeneratorException(final String message) {
        expectedException.expect(WiremockPactGeneratorException.class);
        expectedException.expectMessage(message);
    }
}
