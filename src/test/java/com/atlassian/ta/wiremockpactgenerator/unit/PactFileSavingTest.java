package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.PactGenerator;
import org.junit.Test;

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

public class PactFileSavingTest extends BasePactGeneratorTest {

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

    private void givenNoPathsExist() {
        givenThePathsExist();
    }

    private void givenThePathsExist(final String... paths) {
        given(fileSystem.pathExists(anyString())).willAnswer(invocation -> {
            final String path = invocation.getArgument(0);
            return Arrays.asList(paths).contains(path);
        });
    }
}
