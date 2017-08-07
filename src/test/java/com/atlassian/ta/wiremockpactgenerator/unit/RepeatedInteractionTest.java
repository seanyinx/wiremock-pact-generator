package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.builders.OptionsBuilder;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import org.junit.Test;
import com.atlassian.ta.wiremockpactgenerator.utils.MockPactSaver;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class RepeatedInteractionTest extends AbstractPactGeneratorTestCase {

    @Test
    public void testDifferentRequestsSameResponse() throws UnirestException {
        MockPactSaver mockPactSaver = new MockPactSaver();

        PactGenerator pactGenerator = new PactGenerator(new OptionsBuilder()
                .withConsumerName("consumer A")
                .withProviderName("service x")
                .withPactSaver(mockPactSaver)
                .build());

        testPactGenerator(pactGenerator);

        wireMockServer.addStubMapping(
                post(urlPathEqualTo("/path/resource/"))
                .withHeader("content-type", containing("text/plain"))
                .withRequestBody(containing("the request"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("content-type", "text/plain")
                        .withBody("the response")
                )
                .build());
        wireMockServer.addStubMapping(
                patch(urlPathEqualTo("/path/resource/"))
                .withHeader("content-type", containing("text/plain"))
                .withRequestBody(containing("the request"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("content-type", "text/plain")
                        .withBody("the response")
                )
                .build());

        HttpRequest baseRequest = Unirest
                .post(baseURI + "/path/resource/")
                .header("CONTENT-TYPE", "text/plain")
                .header("X-Header1", "A")
                .body("this is the request number 1").getHttpRequest();

        HttpRequest differentBody = Unirest
                .post(baseURI + "/path/resource/")
                .header("CONTENT-TYPE", "text/plain")
                .header("X-Header1", "A")
                .body("this is the request number 2").getHttpRequest();

        HttpRequest differentHeaderValue = Unirest
                .post(baseURI + "/path/resource/")
                .header("CONTENT-TYPE", "text/plain")
                .header("X-Header1", "B")
                .body("this is the request number 1").getHttpRequest();

        HttpRequest additionalHeader = Unirest
                .post(baseURI + "/path/resource/")
                .header("CONTENT-TYPE", "text/plain")
                .header("X-Header1", "A")
                .header("X-Header2", "Z")
                .body("this is the request number 1").getHttpRequest();

        HttpRequest missingHeader = Unirest
                .post(baseURI + "/path/resource/")
                .header("CONTENT-TYPE", "text/plain")
                .body("this is the request number 1").getHttpRequest();

        HttpRequest withQueryString = Unirest
                .post(baseURI + "/path/resource/")
                .queryString("arg1", "value1")
                .header("CONTENT-TYPE", "text/plain")
                .header("X-Header1", "A")
                .body("this is the request number 1").getHttpRequest();

        HttpRequest differentMethod = Unirest
                .patch(baseURI + "/path/resource/")
                .header("CONTENT-TYPE", "text/plain")
                .header("X-Header1", "A")
                .body("this is the request number 1").getHttpRequest();

        HttpRequest[] requests = {
                baseRequest, differentBody, differentHeaderValue, additionalHeader,
                missingHeader, withQueryString, differentMethod
        };

        int expectedInteractionCount = 0;
        for (HttpRequest request : requests){
            // make each request twice
            request.asString();
            request.asString();
            expectedInteractionCount++;
            assertThat(mockPactSaver.callCount(), equalTo(expectedInteractionCount));
            assertThat(mockPactSaver.lastPact().getInteractions().size(), equalTo(expectedInteractionCount));
        }
    }

    @Test
    public void testRequestHeaderNamesAreNormalized() throws UnirestException {
        MockPactSaver mockPactSaver = new MockPactSaver();

        PactGenerator pactGenerator = new PactGenerator(new OptionsBuilder()
                .withConsumerName("consumer A")
                .withProviderName("service x")
                .withPactSaver(mockPactSaver)
                .build());

        testPactGenerator(pactGenerator);

        wireMockServer.addStubMapping(
                post(urlPathEqualTo("/path/resource/"))
                        .withHeader("content-type", containing("text/plain"))
                        .withRequestBody(containing("the request"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("content-type", "text/plain")
                                .withBody("the response")
                        )
                        .build());

        HttpRequest request = Unirest
                .post(baseURI + "/path/resource/")
                .header("CONTENT-TYPE", "text/plain")
                .header("X-Header1", "A")
                .body("this is the request number 1").getHttpRequest();
        request.asString();

        request = Unirest
                .post(baseURI + "/path/resource/")
                .header("x-hEADeR1", "A")
                .header("CoNTeNt-tyPe", "text/plain")
                .body("this is the request number 1").getHttpRequest();
        request.asString();

        assertThat(mockPactSaver.callCount(), equalTo(1));
        assertThat(mockPactSaver.lastPact().getInteractions().size(), equalTo(1));
    }

    @Test
    public void testSameRequestDifferentResponses() throws UnirestException {
        MockPactSaver mockPactSaver = new MockPactSaver();

        wireMockServer.addStubMapping(
                get(urlPathEqualTo("/path/resource/"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("X-Header", "123")
                                .withBody("the response")
                        )
                        .build());

        PactGenerator pactGenerator = new PactGenerator(new OptionsBuilder()
                .withConsumerName("consumer A")
                .withProviderName("service x")
                .withPactSaver(mockPactSaver)
                .build());

        testPactGenerator(pactGenerator);

        int expectedInteractions = 0;

        GetRequest request = Unirest.get(baseURI + "/path/resource/");

        request.asString();
        request.asString();
        assertThat(mockPactSaver.lastPact().getInteractions().size(), equalTo(++expectedInteractions));

        responseTransformer.reset().changeBodyTo("the new response");
        request.asString();
        request.asString();
        assertThat(mockPactSaver.lastPact().getInteractions().size(), equalTo(++expectedInteractions));

        responseTransformer.reset().removeHeader("X-Header");
        request.asString();
        request.asString();
        assertThat(mockPactSaver.lastPact().getInteractions().size(), equalTo(++expectedInteractions));

        responseTransformer.reset().addHeader("X-NewHeader", "abc");
        request.asString();
        request.asString();
        assertThat(mockPactSaver.lastPact().getInteractions().size(), equalTo(++expectedInteractions));

        responseTransformer.reset().addHeader("X-Header", "124");
        request.asString();
        request.asString();
        assertThat(mockPactSaver.lastPact().getInteractions().size(), equalTo(++expectedInteractions));

        responseTransformer.reset().changeStatusTo(201);
        request.asString();
        request.asString();
        assertThat(mockPactSaver.lastPact().getInteractions().size(), equalTo(++expectedInteractions));
    }
}
