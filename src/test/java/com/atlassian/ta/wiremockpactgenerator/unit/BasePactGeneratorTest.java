package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.WiremockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.json.GsonInstance;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;
import com.atlassian.ta.wiremockpactgenerator.models.PactInteraction;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactSaver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

public abstract class BasePactGeneratorTest {

    @Mock
    FileSystem fileSystem;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    PactGenerator whenPactGeneratorIsCreated(final String consumerName, final String providerName) {
        final PactSaver pactSaver = new PactSaver(fileSystem);
        return new PactGenerator(consumerName, providerName, pactSaver);
    }

    void whenTheInteractionIsInvoked() {
        whenTheInteractionIsInvoked("default-consumer-name", "default-provider-name");
    }

    void whenTheInteractionIsInvoked(final String consumerName, final String providerName) {
        final PactGeneratorRequest request = aDefaultRequest().build();
        whenTheInteractionIsInvoked(consumerName, providerName, request, aDefaultResponse().build());
    }

    void whenTheInteractionIsInvoked(final PactGeneratorRequest request) {
        whenTheInteractionIsInvoked(request, aDefaultResponse().build());
    }

    void whenTheInteractionIsInvoked(final PactGeneratorResponse response) {
        whenTheInteractionIsInvoked(aDefaultRequest().build(), response);
    }

    void whenTheInteractionIsInvoked(final PactGeneratorRequest request, final PactGeneratorResponse response) {
        whenTheInteractionIsInvoked(
                "default-consumer-name",
                "default-provider-name",
                request,
                response);
    }

    void whenTheInteractionIsInvoked(final String consumerName, final String providerName,
                                             final PactGeneratorRequest request, final PactGeneratorResponse response) {
        final PactGenerator pactGenerator = whenPactGeneratorIsCreated(consumerName, providerName);

        pactGenerator.saveInteraction(request, response);
    }

    PactGeneratorRequest.Builder aDefaultRequest() {
        return new PactGeneratorRequest.Builder()
                .withMethod("GET")
                .withUrl("/path");
    }

    PactGeneratorResponse.Builder aDefaultResponse() {
        return new PactGeneratorResponse.Builder()
                .withStatus(200);
    }

    void expectAWiremockPactGeneratorException(final String message) {
        expectedException.expect(WiremockPactGeneratorException.class);
        expectedException.expectMessage(message);
    }

    void expectAWiremockPactGeneratorException(final String message, final Throwable cause) {
        expectAWiremockPactGeneratorException(message);
        expectedException.expectCause(equalTo(cause));
    }

    PactInteraction getFirstSavedInteraction() {
        return getSavedPact().getInteractions().get(0);
    }

    String getRawSavedPact() {
        final ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        try {
            verify(fileSystem).saveFile(anyString(), jsonCaptor.capture());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return jsonCaptor.getValue();
    }

    Pact getSavedPact() {
        return GsonInstance.gson.fromJson(getRawSavedPact(), Pact.class);
    }
}
