package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.Config;
import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorFactory;
import com.atlassian.ta.wiremockpactgenerator.support.InteractionBuilder;
import org.hamcrest.Matcher;
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
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PactFileSavingTest {

    @Mock
    private FileSystem fileSystem;

    @Mock
    private IdGenerator idGenerator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private InteractionBuilder interactionBuilder;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        interactionBuilder = new InteractionBuilder(fileSystem, idGenerator);
    }

    @Test
    public void shouldKnowWhereTheLocationOfThePactFileWillBe() {
        givenThePathsExist("target", "target/pacts");
        givenGeneratedId("123");
        final PactGenerator pactGenerator = whenPactGeneratorIsCreated("aConsumer", "aProvider");

        assertThat(pactGenerator.getPactLocation(), equalTo("target/pacts/aConsumer-aProvider-123-pact.json"));
    }

    @Test
    public void shouldGenerateANewFileNameForEveryPactGeneratorInstance() {
        givenGeneratedId("123");
        final PactGenerator pactGenerator1 = whenPactGeneratorIsCreated("aConsumer", "aProvider");
        givenGeneratedId("456");
        final PactGenerator pactGenerator2 = whenPactGeneratorIsCreated("aConsumer", "aProvider");

        assertThat(pactGenerator1.getPactLocation(), equalTo("target/pacts/aConsumer-aProvider-123-pact.json"));
        assertThat(pactGenerator2.getPactLocation(), equalTo("target/pacts/aConsumer-aProvider-456-pact.json"));
    }

    @Test
    public void shouldSaveAPactFileInTheTargetDirectory_WhenItExists() throws Throwable {
        givenThePathsExist("target");

        interactionBuilder.perform();

        verify(fileSystem).saveFile(startsWith("target/pacts/"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheBuildDirectory_WhenItExists() throws Throwable {
        givenThePathsExist("build");

        interactionBuilder.perform();

        verify(fileSystem).saveFile(startsWith("build/pacts/"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheTargetDirectory_WhenBothBuildAndTargetExists() throws Throwable {
        givenThePathsExist("target", "build");

        interactionBuilder.perform();

        verify(fileSystem).saveFile(startsWith("target/pacts/"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheTargetDirectory_WhenNoOutputDirectoriesExist() throws Throwable {
        givenNoPathsExist();

        interactionBuilder.perform();

        verify(fileSystem).saveFile(startsWith("target/pacts/"), anyString());
    }

    @Test
    public void shouldNormalizeConsumerInFileName_WhenItHasSpecialUnicodeSequences() throws Throwable {
        interactionBuilder
                .withConsumer("el.Consumidor./Más.Importante☃")
                .perform();

        verify(fileSystem).saveFile(
                contains("/elConsumidorMasImportante-"), anyString());
    }

    @Test
    public void shouldNormalizeProviderInFileName_WhenItHasSpecialUnicodeSequences() throws Throwable {
        interactionBuilder
                .withProvider("☃proveedorEspañol?*/")
                .perform();

        verify(fileSystem).saveFile(
                contains("-proveedorEspanol-"), anyString());
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
    public void shouldNotDoHTMLEscaping_whenPactContainsSymbolsLikeGreaterThan() throws Throwable {
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
    public void shouldThrowWireMockPactGeneratorException_WhenFileCantBeSaved() throws Throwable {
        final Throwable cause = new RuntimeException("oops");

        final String pactFile = interactionBuilder.getPactLocation();

        expectAWireMockPactGeneratorException(
                equalTo(String.format("Unable to save file '%s'", pactFile)),
                cause
        );

        doThrow(cause).when(fileSystem).saveFile(anyString(), anyString());

        interactionBuilder.perform();
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

    private void givenGeneratedId(final String id) {
        given(idGenerator.generate()).willAnswer(invocation -> id);
    }

    private PactGenerator whenPactGeneratorIsCreated(final String consumerName, final String providerName) {
        return PactGeneratorFactory.createPactGenerator(
                new Config.Builder()
                    .withConsumerName(consumerName)
                    .withProviderName(providerName)
                    .withFileSystem(fileSystem)
                    .withIdGenerator(idGenerator)
                    .build()
        );
    }

    private void expectAWireMockPactGeneratorException(final Matcher<String> message, final Throwable cause) {
        expectedException.expect(WireMockPactGeneratorException.class);
        expectedException.expectMessage(message);
        expectedException.expectCause(equalTo(cause));
    }
}
