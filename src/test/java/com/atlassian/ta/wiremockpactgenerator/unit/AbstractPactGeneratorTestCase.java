package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.After;
import org.junit.Before;
import com.atlassian.ta.wiremockpactgenerator.utils.MockResponseTransformer;
import com.atlassian.ta.wiremockpactgenerator.utils.Network;
import com.atlassian.ta.wiremockpactgenerator.utils.RequestListenerProxy;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public abstract class AbstractPactGeneratorTestCase {
    private int PORT = Network.findFreePort();
    String baseURI = String.format("http://localhost:%d", PORT);
    WireMockServer wireMockServer;
    MockResponseTransformer responseTransformer = new MockResponseTransformer();
    private RequestListenerProxy requestListenerProxy;


    @Before
    public void setUp(){
        requestListenerProxy = new RequestListenerProxy();
        wireMockServer = new WireMockServer(wireMockConfig().port(PORT).extensions(responseTransformer));
        wireMockServer.addMockServiceRequestListener(requestListenerProxy);
        wireMockServer.start();
    }

    @After
    public void tearDown(){
        wireMockServer.stop();
    }

    void testPactGenerator(PactGenerator pactGenerator){
        requestListenerProxy.delegateTo(pactGenerator);
    }
}
