package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.WiremockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.models.PactRequest;
import com.atlassian.ta.wiremockpactgenerator.models.PactResponse;
import com.atlassian.ta.wiremockpactgenerator.support.HeadersBuilder;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class PactContentTest extends BasePactGeneratorTest {
    /* missing test cases
        - add tests for factory instance management
        - windows and slashes
     */

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
    public void shouldNotIncludeQueryProperty_whenUrlHasNoQuery() {
        final PactGeneratorRequest request = aDefaultRequest()
                .withUrl("/path")
                .build();

        whenTheInteractionIsInvoked(request);

        assertThat(getRawSavedPact(), not(containsString("\"query\":")));
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
        assertThat(requestHeaders.values(), hasSize(2));
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
        assertThat(requestHeaders.values(), hasSize(1));
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
        assertThat(requestHeaders.values(), hasSize(1));
        assertThat(requestHeaders.get("x-header"), equalTo("value 1, value 2, value 3"));
    }

    @Test
    public void shouldNotSetRequestOrResponseHeadersProperty_WhenThereAreNoHeaders() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withHeaders(null)
                        .build(),
                aDefaultResponse()
                        .withHeaders(null)
                        .build());

        assertThat(getRawSavedPact(), not(containsString("\"headers\":")));
    }

    @Test
    public void shouldCaptureTheRequestBody() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withBody("the request body")
                        .build()
        );

        assertThat(getFirstSavedInteraction().getRequest().getBody().getValue(), equalTo("the request body"));
    }

    @Test
    public void shouldNotSetRequestOrResponseBodyProperty_WhenBodyIsEmpty() {
        whenTheInteractionIsInvoked(
                aDefaultRequest()
                        .withBody("")
                        .build(),
                aDefaultResponse()
                        .withBody("")
                        .build()
        );

        assertThat(getRawSavedPact(), not(containsString("\"body\":")));
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
        assertThat(responseHeaders.values(), hasSize(2));
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
        assertThat(responseHeaders.values(), hasSize(1));
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
        assertThat(responseHeaders.values(), hasSize(1));
        assertThat(responseHeaders.get("x-header"), equalTo("value 1, value 2, value 3"));
    }

    @Test
    public void shouldCaptureTheResponseBody() {
        whenTheInteractionIsInvoked(
                aDefaultResponse()
                        .withBody("the response body")
                        .build()
        );

        assertThat(getFirstSavedInteraction().getResponse().getBody().getValue(), equalTo("the response body"));
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

    private Map<String, List<String>> singleHeader(final String name, final String ...values) {
        return new HeadersBuilder()
                .withHeader(name, values)
                .build();
    }
}
