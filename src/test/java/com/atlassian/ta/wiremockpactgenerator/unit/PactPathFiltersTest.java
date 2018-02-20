package com.atlassian.ta.wiremockpactgenerator.unit;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.unit.support.PactGeneratorInvocation;
import com.atlassian.ta.wiremockpactgenerator.unit.support.PactFileSpy;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PactPathFiltersTest {
    @Mock
    private FileSystem fileSystem;

    @Mock
    private IdGenerator idGenerator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PactGeneratorInvocation pactGeneratorInvocation;
    private PactFileSpy pactFileSpy;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        pactGeneratorInvocation = new PactGeneratorInvocation(fileSystem, idGenerator);
        pactFileSpy = new PactFileSpy(fileSystem);
    }

    @Test
    public void shouldNotSaveInteraction_whenRequestPathNotInWhitelist() {
        pactGeneratorInvocation
                .withWhitelist("/match/me/.*")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/wont/match/")
                                .build())
                .invokeProcess();

        pactFileSpy.verifyNoInteractionsSaved();
    }

    @Test
    public void shouldImplicitlyMatchThePathFromTheBeginning() {
        pactGeneratorInvocation
                .withWhitelist("/path/")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/dont/match/this/path/")
                                .build())
                .invokeProcess();

        pactFileSpy.verifyNoInteractionsSaved();
    }

    @Test
    public void shouldImplicitlyMatchThePathToTheEnd() {
        pactGeneratorInvocation
                .withWhitelist("/path/")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/path/wont/get/matched")
                                .build())
                .invokeProcess();

        pactFileSpy.verifyNoInteractionsSaved();
    }

    @Test
    public void shouldSaveInteraction_whenRequestPathMatchesWhitelistItem() {
        pactGeneratorInvocation
                .withWhitelist("/match/me/.*")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/match/me/please")
                                .build())
                .invokeProcess();
        assertThat(pactFileSpy.interactionCount(), is(1));
    }

    @Test
    public void shouldSaveInteraction_whenRequestMatchesAtLeastOneWhiteListItem() {
        pactGeneratorInvocation
                .withWhitelist(
                        "/wont/match/path/.*",
                        "/should/match/path/.*")
                .withRequest(
                    aDefaultRequest()
                            .withUrl("/should/match/path/in/this/test")
                            .build())
                .invokeProcess();
        assertThat(pactFileSpy.interactionCount(), is(1));
    }

    @Test
    public void shouldNotConsiderTheQueryStringInTheWhitelistMatching() {
        pactGeneratorInvocation
                .withWhitelist(".*/ends-with/path")
                .withRequest(
                        aDefaultRequest()
                            .withUrl("/rest/ends-with/path?with=args")
                            .build())
                .invokeProcess();
        assertThat(pactFileSpy.interactionCount(), is(1));
    }

    @Test
    public void shouldNotIncludeTheUriFragmentInTheWhitelistMatching() {
        pactGeneratorInvocation
                .withWhitelist(".*/ends-with/path")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/rest/ends-with/path#fragment")
                                .build())
                .invokeProcess();
        assertThat(pactFileSpy.interactionCount(), is(1));
    }

    @Test
    public void shouldSaveTheInteractionOnce_whenRequestMatchesMultipleItems() {
        pactGeneratorInvocation
                .withWhitelist("/starts-with/.*")
                .withWhitelist(".*/ends-with/")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/starts-with/and/ends-with/")
                                .build())
                .invokeProcess();
        assertThat(pactFileSpy.interactionCount(), is(1));
    }

    @Test
    public void shouldFailIfWhitelistPatternIsInvalid() {
        expectAWireMockPactGeneratorException(
                equalTo("Invalid regex pattern in request path whitelist"),
                instanceOf(RuntimeException.class)
        );

        pactGeneratorInvocation.withWhitelist("*/invalid").invokeProcess();
    }

    @Test
    public void shouldNotSaveTheInteraction_whenRequestPathInBlacklist() {
        pactGeneratorInvocation
                .withBlacklist("/blacklisted/.*")
                .withRequest(
                        aDefaultRequest()
                            .withUrl("/blacklisted/path")
                            .build())
                .invokeProcess();
        pactFileSpy.verifyNoInteractionsSaved();
    }

    @Test
    public void shouldSaveTheInteraction_whenRequestPathNotInBlacklist() {
        pactGeneratorInvocation
                .withBlacklist("/blacklisted/.*")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/not/blacklisted/path")
                                .build())
                .invokeProcess();
        assertThat(pactFileSpy.interactionCount(), is(1));
    }

    @Test
    public void shouldNotSaveTheInteraction_whenRequestPathMatchesAtLeastOneItemInBlacklist() {
        pactGeneratorInvocation
                .withBlacklist("/blacklisted/.*", ".*/forbidden/.*")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/a/forbidden/path")
                                .build())
                .invokeProcess();
        pactFileSpy.verifyNoInteractionsSaved();
    }

    @Test
    public void shouldNotSaveTheInteraction_whenRequestPathMatchesBothWhitelistAndBlacklist() {
        pactGeneratorInvocation
                .withBlacklist("/some/path/.*")
                .withWhitelist("/some/path/.*")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/some/path/resource")
                                .build())
                .invokeProcess();
        pactFileSpy.verifyNoInteractionsSaved();
    }

    @Test
    public void shouldSaveTheInteraction_whenRequestPathMatchesWhitelistButNotBlacklist() {
        pactGeneratorInvocation
                .withWhitelist("/accept/all/.*")
                .withBlacklist("/accept/all/but/this/.*")
                .withRequest(
                        aDefaultRequest()
                                .withUrl("/accept/all/please")
                                .build())
                .invokeProcess();
        assertThat(pactFileSpy.interactionCount(), is(1));
    }

    @Test
    public void shouldFailIfBlacklistPatternIsInvalid() {
        expectAWireMockPactGeneratorException(
                equalTo("Invalid regex pattern in request path blacklist"),
                instanceOf(RuntimeException.class)
        );

        pactGeneratorInvocation.withBlacklist("*/invalid").invokeProcess();
    }

    private PactGeneratorRequest.Builder aDefaultRequest() {
        return new PactGeneratorRequest.Builder()
                .withMethod("GET")
                .withUrl("/path");
    }

    private void expectAWireMockPactGeneratorException(final Matcher<String> message, final Matcher<Throwable> cause) {
        expectedException.expect(WireMockPactGeneratorException.class);
        expectedException.expectMessage(message);
        expectedException.expectCause(cause);
    }
}
