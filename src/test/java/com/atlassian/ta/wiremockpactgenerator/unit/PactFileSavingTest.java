package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.unit.support.PactGeneratorInvocation;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
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

    private PactGeneratorInvocation pactGeneratorInvocation;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        pactGeneratorInvocation = new PactGeneratorInvocation(fileSystem, idGenerator);
    }

    @Test
    public void shouldKnowWhereTheLocationOfThePactFileWillBe() {
        givenThePathsExist("target", "target/pacts");
        givenGeneratedId("123");

        final String actualPactLocation = pactGeneratorInvocation
                .withConsumer("consumer")
                .withProvider("provider")
                .invokeGetPactLocation();

        assertThat(actualPactLocation, equalTo("target/pacts/consumer-provider-123-pact.json"));
    }

    @Test
    public void shouldGenerateANewFileNameForEveryPactGeneratorInstance() {
        givenGeneratedId("123");
        final String actualPactLocation1 = pactGeneratorInvocation
                .withConsumer("consumer")
                .withProvider("provider")
                .invokeGetPactLocation();

        givenGeneratedId("456");
        final String actualPactLocation2 = pactGeneratorInvocation
                .withConsumer("consumer")
                .withProvider("provider")
                .invokeGetPactLocation();

        assertThat(actualPactLocation1, equalTo("target/pacts/consumer-provider-123-pact.json"));
        assertThat(actualPactLocation2, equalTo("target/pacts/consumer-provider-456-pact.json"));
    }

    @Test
    public void shouldSaveAPactFileInTheTargetDirectory_WhenItExists() throws Throwable {
        givenThePathsExist("target");

        pactGeneratorInvocation.invokeProcess();

        verify(fileSystem).saveFile(startsWith("target/pacts/"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheBuildDirectory_WhenItExists() throws Throwable {
        givenThePathsExist("build");

        pactGeneratorInvocation.invokeProcess();

        verify(fileSystem).saveFile(startsWith("build/pacts/"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheTargetDirectory_WhenBothBuildAndTargetExists() throws Throwable {
        givenThePathsExist("target", "build");

        pactGeneratorInvocation.invokeProcess();

        verify(fileSystem).saveFile(startsWith("target/pacts/"), anyString());
    }

    @Test
    public void shouldSaveThePactFileInTheTargetDirectory_WhenNoOutputDirectoriesExist() throws Throwable {
        givenNoPathsExist();

        pactGeneratorInvocation.invokeProcess();

        verify(fileSystem).saveFile(startsWith("target/pacts/"), anyString());
    }

    @Test
    public void shouldNormalizeConsumerInFileName_WhenItHasSpecialUnicodeSequences() throws Throwable {
        pactGeneratorInvocation
                .withConsumer("el.Consumidor./Más.Importante☃")
                .invokeProcess();

        verify(fileSystem).saveFile(
                contains("/elConsumidorMasImportante-"), anyString());
    }

    @Test
    public void shouldNormalizeProviderInFileName_WhenItHasSpecialUnicodeSequences() throws Throwable {
        pactGeneratorInvocation
                .withProvider("☃proveedorEspañol?*/")
                .invokeProcess();

        verify(fileSystem).saveFile(
                contains("-proveedorEspanol-"), anyString());
    }

    @Test
    public void shouldCreateThePactDirectory_WhenTheOutputDirectoryDoesNotExist() {
        givenNoPathsExist();

        pactGeneratorInvocation.invokeProcess();

        verify(fileSystem).createPath("target/pacts");
    }

    @Test
    public void shouldCreateThePactDirectory_WhenItDoesNotExist() {
        givenThePathsExist("target");

        pactGeneratorInvocation.invokeProcess();

        verify(fileSystem).createPath("target/pacts");
    }

    @Test
    public void shouldNotCreateThePactDirectory_WhenItAlreadyExists() {
        givenThePathsExist("target", "target/pacts");

        pactGeneratorInvocation.invokeProcess();

        verify(fileSystem, never()).createPath("target/pacts");
    }

    @Test
    public void shouldWriteThePactContentToTheFile() throws Throwable {
        pactGeneratorInvocation.invokeProcess();

        verify(fileSystem).saveFile(anyString(), contains("interactions"));
    }

    @Test
    public void shouldNotDoHTMLEscaping_whenPactContainsSymbolsLikeGreaterThan() throws Throwable {
        pactGeneratorInvocation
                .withConsumer("The<Consumer>")
                .invokeProcess();

        verify(fileSystem).saveFile(anyString(), contains("The<Consumer>"));
    }

    @Test
    public void shouldGenerate2SpaceIndentedPrettyJson() throws Throwable {
        pactGeneratorInvocation.invokeProcess();
        verify(fileSystem).saveFile(anyString(), contains("  \"interactions\": [\n    {"));
    }

    @Test
    public void shouldThrowWireMockPactGeneratorException_WhenFileCantBeSaved() throws Throwable {
        final Throwable cause = new RuntimeException("oops");

        expectAWireMockPactGeneratorException(
                containsString("Unable to save file"),
                cause
        );

        doThrow(cause).when(fileSystem).saveFile(anyString(), anyString());

        pactGeneratorInvocation.invokeProcess();
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

    private void expectAWireMockPactGeneratorException(final Matcher<String> message, final Throwable cause) {
        expectedException.expect(WireMockPactGeneratorException.class);
        expectedException.expectMessage(message);
        expectedException.expectCause(equalTo(cause));
    }
}
