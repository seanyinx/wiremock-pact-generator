package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.WiremockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactSaver;
import com.atlassian.ta.wiremockpactgenerator.support.InteractionBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PactFileSavingTest {

    @Mock
    private FileSystem fileSystem;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private InteractionBuilder interactionBuilder;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        interactionBuilder = new InteractionBuilder(fileSystem);
    }

    @Test
    public void shouldKnowWhereTheLocationOfThePactFileWillBe() {
        final PactGenerator pactGenerator = whenPactGeneratorIsCreated("aConsumer", "aProvider");

        assertThat(pactGenerator.getPactLocation(), equalTo("target/pacts/aConsumer-aProvider-pact.json"));
    }

    @Test
    public void shouldSaveAPactFileInTheTargetDirectory_WhenItExists() throws Throwable {
        givenThePathsExist("target");

        interactionBuilder
                .withConsumer("consumerName")
                .withProvider("providerName")
                .perform();

        verify(fileSystem).saveFile(eq("target/pacts/consumerName-providerName-pact.json"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheBuildDirectory_WhenItExists() throws Throwable {
        givenThePathsExist("build");

        interactionBuilder
                .withConsumer("consumer")
                .withProvider("provider")
                .perform();

        verify(fileSystem).saveFile(eq("build/pacts/consumer-provider-pact.json"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheTargetDirectory_WhenBothBuildAndTargetExists() throws Throwable {
        givenThePathsExist("target", "build");

        interactionBuilder
                .withConsumer("consumerName")
                .withProvider("providerName")
                .perform();

        verify(fileSystem).saveFile(eq("target/pacts/consumerName-providerName-pact.json"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheTargetDirectory_WhenNoOutputDirectoriesExist() throws Throwable {
        givenNoPathsExist();

        interactionBuilder
                .withConsumer("consumerName")
                .withProvider("providerName")
                .perform();

        verify(fileSystem).saveFile(eq("target/pacts/consumerName-providerName-pact.json"), anyString());
    }

    @Test
    public void shouldNormalizeConsumerInFileName_WhenItHasSpecialUnicodeSequences() throws Throwable {
        interactionBuilder
                .withConsumer("el.Consumidor./Más.Importante☃")
                .withProvider("providerName")
                .perform();

        verify(fileSystem).saveFile(
                eq("target/pacts/elConsumidorMasImportante-providerName-pact.json"), anyString());
    }

    @Test
    public void shouldNormalizeProviderInFileName_WhenItHasSpecialUnicodeSequences() throws Throwable {
        interactionBuilder
                .withConsumer("consumerName")
                .withProvider("☃proveedorEspañol?*/")
                .perform();

        verify(fileSystem).saveFile(
                eq("target/pacts/consumerName-proveedorEspanol-pact.json"), anyString());
    }

    @Test
    public void shouldCreateThePactDirectory_WhenTheOutputDirectoryDoesNotExist() {
        givenNoPathsExist();

        interactionBuilder.perform();

        verify(fileSystem).createPath("target/pacts");
    }

    @Test
    public void shouldCreateThePactDirectory_WhenItDoesNotExist() {
        givenThePathsExist("target");

        interactionBuilder.perform();

        verify(fileSystem).createPath("target/pacts");
    }

    @Test
    public void shouldNotCreateThePactDirectory_WhenItAlreadyExists() {
        givenThePathsExist("target", "target/pacts");

        interactionBuilder.perform();

        verify(fileSystem, never()).createPath("target/pacts");
    }

    @Test
    public void shouldWriteThePactContentToTheFile() throws Throwable {
        interactionBuilder.perform();

        verify(fileSystem).saveFile(anyString(), contains("interactions"));
    }

    @Test
    public void shouldNotDoHTMLEcaping_whenPactContainsSymbolsLikeGreaterThan() throws Throwable {
        interactionBuilder
                .withConsumer("The<Consumer>")
                .perform();

        verify(fileSystem).saveFile(anyString(), contains("The<Consumer>"));
    }

    @Test
    public void shouldGenerate2SpaceIndentedPrettyJson() throws Throwable {
        interactionBuilder.perform();
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

        interactionBuilder
                .withConsumer("consumerName")
                .withProvider("providerName")
                .perform();
    }

    private void givenNoPathsExist() {
        givenThePathsExist();
    }

    private void givenThePathsExist(final String... paths) {
        given(fileSystem.pathExists(anyString())).willAnswer(invocation -> {
            final String path = invocation.getArgument(0);
            return Arrays.asList(paths).contains(path);
        });
    }

    private PactGenerator whenPactGeneratorIsCreated(final String consumerName, final String providerName) {
        final PactSaver pactSaver = new PactSaver(fileSystem);
        return new PactGenerator(consumerName, providerName, pactSaver);
    }

    private void expectAWiremockPactGeneratorException(final String message, final Throwable cause) {
        expectedException.expect(WiremockPactGeneratorException.class);
        expectedException.expectMessage(message);
        expectedException.expectCause(equalTo(cause));
    }
}
