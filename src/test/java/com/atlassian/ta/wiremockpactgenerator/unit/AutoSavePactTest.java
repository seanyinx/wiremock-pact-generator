package com.atlassian.ta.wiremockpactgenerator.unit;


import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.builders.OptionsBuilder;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.*;
import com.atlassian.ta.wiremockpactgenerator.utils.MockPactSaver;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class AutoSavePactTest extends AbstractPactGeneratorTestCase {

    @Test
    public void testAutoSaveEnabled() throws UnirestException {
        MockPactSaver mockPactSaver = new MockPactSaver(true);

        PactGenerator pactGenerator = new PactGenerator(new OptionsBuilder()
                .withConsumerName("consumer A")
                .withProviderName("service x")
                .withPactSaver(mockPactSaver)
                .build());

        testPactGenerator(pactGenerator);

        wireMockServer.addStubMapping(get(urlMatching("/path/resource/.*"))
                .willReturn(aResponse().withStatus(200))
                .build());


        assertThat(mockPactSaver.callCount(), equalTo(0));

        Unirest.get(baseURI + "/path/resource/1").asString();

        assertThat(mockPactSaver.callCount(), equalTo(1));

        Unirest.get(baseURI + "/path/resource/2").asString();

        assertThat(mockPactSaver.callCount(), equalTo(2));

        pactGenerator.save();

        assertThat(mockPactSaver.callCount(), equalTo(2));
    }

    @Test
    public void testAutoSaveDisabled() throws UnirestException {
        MockPactSaver mockPactSaver = new MockPactSaver(false);

        PactGenerator pactGenerator = new PactGenerator(new OptionsBuilder()
                .withConsumerName("consumer A")
                .withProviderName("service x")
                .withPactSaver(mockPactSaver)
                .build());

        testPactGenerator(pactGenerator);

        wireMockServer.addStubMapping(get(urlMatching("/path/resource/.*"))
                .willReturn(aResponse().withStatus(200))
                .build());


        assertThat(mockPactSaver.callCount(), equalTo(0));

        Unirest.get(baseURI + "/path/resource/1").asString();

        assertThat(mockPactSaver.callCount(), equalTo(0));

        Unirest.get(baseURI + "/path/resource/2").asString();

        assertThat(mockPactSaver.callCount(), equalTo(0));

        pactGenerator.save();

        assertThat(mockPactSaver.callCount(), equalTo(1));
    }
}
