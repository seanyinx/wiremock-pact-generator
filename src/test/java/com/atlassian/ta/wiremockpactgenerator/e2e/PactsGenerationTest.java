package com.atlassian.ta.wiremockpactgenerator.e2e;

import au.com.dius.pact.model.Interaction;
import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactSpecVersion;
import au.com.dius.pact.provider.junit.loader.PactFolderLoader;
import au.com.dius.pact.provider.junit.loader.PactLoader;
import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.builders.FilePactSaver;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.atlassian.ta.wiremockpactgenerator.utils.Network;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class PactsGenerationTest {
    private WireMockServer wireMockServer;
    private static final int PORT = Network.findFreePort();
    private static final String baseURI = String.format("http://localhost:%d", PORT);
    private static final File pactsDirectory = new FilePactSaver().getDestination().toFile();

    @Before
    public void setUp(){
        removePacts();
        wireMockServer = new WireMockServer(wireMockConfig().port(PORT));
        wireMockServer.start();
    }

    @After
    public void tearDown(){
        wireMockServer.stop();
    }

    @Test
    public void testSimpleSingleInteraction() throws UnirestException{
        // Given
        PactGenerator generator = new PactGenerator("the-consumer", "the-provider");

        wireMockServer.addMockServiceRequestListener(generator);

        wireMockServer.addStubMapping(get(urlEqualTo("/path/resource/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("the body")).build());

        // When
        Unirest.get(baseURI + "/path/resource/").asString();

        // Then
        List<Pact> generatedPacts = findPactsForProvider("the-provider");

        assertThat(generatedPacts.size(), equalTo(1));
        Pact pact = generatedPacts.get(0);
        assertThat(pact.getConsumer().getName(), equalTo("the-consumer"));
        assertThat(pact.getProvider().getName(), equalTo("the-provider"));

        List<Interaction> interactions = pact.getInteractions();
        assertThat(interactions.size(), equalTo(1));
        Interaction interaction = interactions.get(0);
        assertThat(interaction.getDescription(), equalTo(""));
        assertThat(interaction.getProviderStates().size(), equalTo(0));

        Map<?, ?> v3Interaction = interaction.toMap(PactSpecVersion.V3);
        Map<?, ?> request = (Map) v3Interaction.get("request");
        Map<?, ?> response = (Map) v3Interaction.get("response");
        Map<?, ?> responseHeaders = (Map) response.get("headers");

        assertThat(request.get("method"), equalTo("GET"));
        assertThat(request.get("path"), equalTo("/path/resource/"));
        assertThat(request.get("body"), equalTo(""));

        assertThat(response.get("status"), equalTo(200));
        assertThat(response.get("body"), equalTo("the body"));
        assertThat(responseHeaders.get("content-type"), equalTo("text/plain"));
    }

    private List<Pact> findPactsForProvider(String provider) {
        PactLoader loader = new PactFolderLoader(pactsDirectory);
        try{
            return loader.load(provider);
        }
        catch (IOException error){
            throw new RuntimeException("Could not load generated pacts", error);
        }
    }

    private void removePacts(){
        for(File file : pactsDirectory.listFiles()){
            if (file.isFile() && file.getName().toLowerCase().endsWith(".json")){
                file.delete();
            }
        }
    }
}
