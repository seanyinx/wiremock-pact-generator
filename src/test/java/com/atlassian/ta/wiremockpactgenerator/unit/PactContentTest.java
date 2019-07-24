package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.unit.support.HeadersBuilder;
import com.atlassian.ta.wiremockpactgenerator.unit.support.PactGeneratorInvocation;
import com.atlassian.ta.wiremockpactgenerator.unit.support.PactFileSpy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasKey;

public class PactContentTest {
    /* missing test cases
        - windows and slashes
     */

    @Mock
    private FileSystem fileSystem;

    @Mock
    private IdGenerator idGenerator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PactGeneratorInvocation pactGeneratorInvocation;
    private PactFileSpy pactFileSpy;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        pactGeneratorInvocation = new PactGeneratorInvocation(fileSystem, idGenerator);
        pactFileSpy = new PactFileSpy(fileSystem);
    }

    @Test
    public void shouldIncludeTheConsumerName() {
        pactGeneratorInvocation
                .withConsumer("consumer-name")
                .invokeProcess();

        assertThat(pactFileSpy.consumerName(), equalTo("consumer-name"));
    }

    @Test
    public void shouldIncludeTheProviderName() {
        pactGeneratorInvocation
                .withProvider("provider-name")
                .invokeProcess();

        assertThat(pactFileSpy.providerName(), equalTo("provider-name"));
    }

    @Test
    public void shouldFailIfConsumerNameIsNull() throws WireMockPactGeneratorException {
        expectAWireMockPactGeneratorException("consumer name can't be null nor blank");

        pactGeneratorInvocation
                .withConsumer(null)
                .invokeProcess();
    }

    @Test
    public void shouldFailIfConsumerNameIsBlank() throws WireMockPactGeneratorException {
        expectAWireMockPactGeneratorException("consumer name can't be null nor blank");

        pactGeneratorInvocation
                .withConsumer(" ")
                .invokeProcess();
    }

    @Test
    public void shouldFailIfProviderNameIsNull() throws WireMockPactGeneratorException {
        expectAWireMockPactGeneratorException("provider name can't be null nor blank");

        pactGeneratorInvocation
                .withProvider(null)
                .invokeProcess();
    }

    @Test
    public void shouldFailIfProviderNameIsBlank() throws WireMockPactGeneratorException {
        expectAWireMockPactGeneratorException("provider name can't be null nor blank");

        pactGeneratorInvocation
                .withProvider(" ")
                .invokeProcess();
    }

    @Test
    public void shouldCaptureTheRequestMethod() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withMethod("POST")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestMethod(), equalTo("POST"));
    }

    @Test
    public void shouldCaptureTheRequestPath() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/some/path")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestPath(), equalTo("/some/path"));
    }

    @Test
    public void shouldNotCaptureTheUriFragment() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/some/path#fragment")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestPath(), equalTo("/some/path"));
    }

    @Test
    public void shouldCaptureTheRequestQueryString() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/path?query=string&foo=bar")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestQuery(), equalTo("query=string&foo=bar"));
    }

    @Test
    public void shouldNotCaptureTheUriFragment_whenThereIsAQueryToo() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/some/path?query=string#fragment")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestPath(), equalTo("/some/path"));
        assertThat(pactFileSpy.firstRequestQuery(), equalTo("query=string"));
    }

    @Test
    public void shouldNotIncludeQueryProperty_whenUrlHasNoQuery() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/path")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.jsonPact(), not(containsString("\"query\":")));
    }

    @Test
    public void shouldNotIncludeQueryAsPartOfTheRequestPathInThePactFile() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/path?query=string")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestPath(), equalTo("/path"));
    }

    @Test
    public void shouldFail_whenResponseStatusIsTooLow() {
        expectAWireMockPactGeneratorException("Response status code is not valid: 99");
        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withStatus(99)
                                .build())
                .invokeProcess();
    }

    @Test
    public void shouldFail_whenResponseStatusIsTooHigh() {
        expectAWireMockPactGeneratorException("Response status code is not valid: 600");

        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withStatus(600)
                                .build())
                .invokeProcess();
    }

    @Test
    public void shouldCaptureRequestHeaders() {
        pactGeneratorInvocation
            .withRequest(
                aDefaultRequest()
                    .withHeaders(
                        new HeadersBuilder()
                            .withHeader("accept", "application/json")
                            .withHeader("x-header", "value")
                            .build())
                    .build())
            .invokeProcess();

        final Map<String, String> requestHeaders = pactFileSpy.firstRequestHeaders();
        assertThat(requestHeaders.values(), hasSize(2));
        assertThat(requestHeaders.get("accept"), equalTo("application/json"));
        assertThat(requestHeaders.get("x-header"), equalTo("value"));
    }

    @Test
    public void shouldNormalizeRequestHeaderNames() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(singleHeader("x-HeaDer-NAMe", "some value"))
                                .build())
                .invokeProcess();

        final Map<String, String> requestHeaders = pactFileSpy.firstRequestHeaders();
        assertThat(requestHeaders.values(), hasSize(1));
        assertThat(requestHeaders.get("x-header-name"), equalTo("some value"));
    }

    @Test
    public void shouldNotCaptureRequestHostHeader() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(
                                        new HeadersBuilder()
                                                .withHeader("host", "localhost:1234")
                                                .withHeader("x-keep-me", "value")
                                                .build())
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestHeaders().keySet(), equalTo(new HashSet<>(Arrays.asList("x-keep-me"))));
    }

    @Test
    public void shouldCaptureRequestHostHeaderIfWhitelisted() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(
                                        new HeadersBuilder()
                                                .withHeader("host", "localhost:1234")
                                                .build())
                                .build())
                .withRequestHeaderWhitelist("host")
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestHeaders().keySet(), equalTo(new HashSet<>(Arrays.asList("host"))));
    }

    @Test
    public void shouldCombineMultipleRequestHeadersValues() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(singleHeader("x-header", "value 1", "value 2", "value 3"))
                                .build())
                .invokeProcess();

        final Map<String, String> requestHeaders = pactFileSpy.firstRequestHeaders();
        assertThat(requestHeaders.values(), hasSize(1));
        assertThat(requestHeaders.get("x-header"), equalTo("value 1, value 2, value 3"));
    }

    @Test
    public void shouldContainOnlyWhitelistedRequestHeadersValues() {
        final Map<String, List<String>> headers = new HeadersBuilder()
                .withHeader("whitelisted-header", "whitelisted-header-val")
                .withHeader("non-whitelisted-header", "non-whitelisted-header-val")
                .build();
        pactGeneratorInvocation
                .withRequestHeaderWhitelist("whitelisted-header")
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(headers)
                                .build())
                .invokeProcess();

        final Map<String, String> requestHeaders = pactFileSpy.firstRequestHeaders();
        assertThat(requestHeaders.values(), hasSize(1));
        assertThat(requestHeaders, hasKey("whitelisted-header"));
    }

    @Test
    public void shouldContainOnlyWhitelistedResponseHeadersValues() {
        final Map<String, List<String>> headers = new HeadersBuilder()
                .withHeader("whitelisted-header", "whitelisted-header-val")
                .withHeader("non-whitelisted-header", "non-whitelisted-header-val")
                .build();
        pactGeneratorInvocation
                .withResponseHeaderWhitelist("whitelisted-header")
                .withRequest(aDefaultRequest().build())
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(headers)
                                .build()
                )
                .invokeProcess();

        final Map<String, String> responseHeaders = pactFileSpy.firstResponseHeaders();
        assertThat(responseHeaders.values(), hasSize(1));
        assertThat(responseHeaders, hasKey("whitelisted-header"));
    }

    @Test
    public void shouldWhitelistResponseHeadersValuesAndIgnoreCase() {
        final Map<String, List<String>> headers = new HeadersBuilder()
                .withHeader("Whitelisted-Request-Header", "whitelisted-request-header-val")
                .withHeader("Whitelisted-Response-Header", "whitelisted-response-header-val")
                .build();
        pactGeneratorInvocation
                .withRequestHeaderWhitelist("whitelisted-request-header")
                .withResponseHeaderWhitelist("whitelisted-response-header")
                .withRequest(aDefaultRequest().withHeaders(headers).build())
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(headers)
                                .build()
                )
                .invokeProcess();

        final Map<String, String> responseHeaders = pactFileSpy.firstResponseHeaders();
        final Map<String, String> requestHeaders = pactFileSpy.firstRequestHeaders();
        assertThat(requestHeaders.values(), hasSize(1));
        assertThat(requestHeaders, hasKey("whitelisted-request-header"));
        assertThat(responseHeaders.values(), hasSize(1));
        assertThat(responseHeaders, hasKey("whitelisted-response-header"));
    }

    @Test
    public void shouldNotSetRequestOrResponseHeadersProperty_WhenThereAreNoHeaders() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withHeaders(null)
                                .build())
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(null)
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.jsonPact(), not(containsString("\"headers\":")));
    }

    @Test
    public void shouldCaptureTheRequestBody() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withBody("the request body")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestBody(), equalTo("the request body"));
    }

    @Test
    public void shouldNotSetRequestOrResponseBodyProperty_WhenBodyIsEmpty() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withBody("")
                                .build())
                .withResponse(
                        aDefaultResponse()
                                .withBody("")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.jsonPact(), not(containsString("\"body\":")));
    }

    @Test
    public void shouldCaptureTheResponseStatusCode() {
        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withStatus(202)
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstResponseStatus(), equalTo(202));
    }

    @Test
    public void shouldCaptureResponseHeaders() {
        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(
                                        new HeadersBuilder()
                                                .withHeader("content-type", "application/json")
                                                .withHeader("x-header", "value")
                                                .build())
                                .build())
                .invokeProcess();

        final Map<String, String> responseHeaders = pactFileSpy.firstResponseHeaders();
        assertThat(responseHeaders.values(), hasSize(2));
        assertThat(responseHeaders.get("content-type"), equalTo("application/json"));
        assertThat(responseHeaders.get("x-header"), equalTo("value"));
    }

    @Test
    public void shouldNormalizeResponseHeaderNames() {
        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(singleHeader("x-HeaDer-NAMe", "some value"))
                                .build())
                .invokeProcess();

        final Map<String, String> responseHeaders = pactFileSpy.firstResponseHeaders();
        assertThat(responseHeaders.values(), hasSize(1));
        assertThat(responseHeaders.get("x-header-name"), equalTo("some value"));
    }

    @Test
    public void shouldCombineMultipleResponseHeadersValues() {
        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(singleHeader("x-header", "value 1", "value 2", "value 3"))
                                .build())
                .invokeProcess();

        final Map<String, String> responseHeaders = pactFileSpy.firstResponseHeaders();
        assertThat(responseHeaders.values(), hasSize(1));
        assertThat(responseHeaders.get("x-header"), equalTo("value 1, value 2, value 3"));
    }

    @Test
    public void shouldExcludeWireMockDebugResponseHeaders() {
        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(
                                        new HeadersBuilder()
                                                .withHeader("content-type", "application/json")
                                                .withHeader("Matched-Stub-Id", "test-stub-id")
                                                .withHeader("Matched-Stub-Name", "Test Stub Name")
                                                .build())
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstResponseHeaders().keySet(), equalTo(Collections.singleton("content-type")));
    }

    @Test
    public void shouldIncludeWireMockDebugResponseHeadersIfWhitelisted() {
        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withHeaders(
                                        new HeadersBuilder()
                                                .withHeader("Matched-Stub-Id", "test-stub-id")
                                                .withHeader("Matched-Stub-Name", "Test Stub Name")
                                                .build())
                                .build())
                .withResponseHeaderWhitelist("Matched-Stub-Id")
                .invokeProcess();

        assertThat(pactFileSpy.firstResponseHeaders().keySet(), equalTo(Collections.singleton("matched-stub-id")));
    }

    @Test
    public void shouldCaptureTheResponseBody() {
        pactGeneratorInvocation
                .withResponse(
                        aDefaultResponse()
                                .withBody("the response body")
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstResponseBody(), equalTo("the response body"));
    }

    @Test
    public void shouldIncludeTheInteractionDescription() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withMethod("GET")
                                .withUrl("/path")
                                .build())
                .withResponse(
                        aDefaultResponse()
                                .withStatus(202)
                                .withIsConfiguredResponse(true)
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstInteractionDescription(), equalTo("GET /path -> 202"));
    }

    @Test
    public void shouldShowInTheInteractionDescriptionIfTheResponseWasNotConfigured() {
        pactGeneratorInvocation
                .withRequest(
                        aDefaultRequest()
                                .withMethod("GET")
                                .withUrl("/path")
                                .build())
                .withResponse(
                        aDefaultResponse()
                                .withStatus(404)
                                .withIsConfiguredResponse(false)
                                .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstInteractionDescription(), equalTo("GET /path -> 404 [Not configured in WireMock]"));
    }

    @Test
    public void shouldNormalizeTheRequestMethodAsUpperCase() {
        pactGeneratorInvocation
                .withRequest(
                    aDefaultRequest()
                            .withMethod("get")
                            .build())
                .invokeProcess();

        assertThat(pactFileSpy.firstRequestMethod(), equalTo("GET"));
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
                .withStatus(200)
                .withIsConfiguredResponse(true);
    }

    private void expectAWireMockPactGeneratorException(final String message) {
        expectedException.expect(WireMockPactGeneratorException.class);
        expectedException.expectMessage(message);
    }
}
