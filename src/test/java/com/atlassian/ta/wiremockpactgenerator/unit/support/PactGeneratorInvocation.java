package com.atlassian.ta.wiremockpactgenerator.unit.support;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGenerator;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorRequest;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorResponse;
import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorUserOptions;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.PactGeneratorInteraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PactGeneratorInvocation {
    private final String consumerName;
    private final String providerName;
    private final List<String> requestPathWhitelist;
    private final List<String> requestPathBlacklist;
    private final boolean strictApplicationJson;
    private final List<String> requestHeaderWhitelist;
    private final List<String> responseHeaderWhitelist;
    private final FileSystem fileSystem;
    private final IdGenerator idGenerator;
    private final PactGeneratorRequest request;
    private final PactGeneratorResponse response;

    public PactGeneratorInvocation(final FileSystem fileSystem, final IdGenerator idGenerator) {
        this(
                "default-consumer-name",
                "default-provider-name",
                new ArrayList<>(),
                new ArrayList<>(),
                true,
                new ArrayList<>(),
                new ArrayList<>(),
                fileSystem,
                idGenerator,
                new PactGeneratorRequest.Builder()
                        .withMethod("GET")
                        .withUrl("/path")
                        .build(),
                new PactGeneratorResponse.Builder()
                        .withStatus(200)
                        .build()
        );
    }

    private PactGeneratorInvocation(final String consumerName,
                                    final String providerName,
                                    final List<String> requestPathWhitelist,
                                    final List<String> requestPathBlacklist,
                                    final boolean strictApplicationJson,
                                    final List<String> requestHeaderWhitelist,
                                    final List<String> responseHeaderWhitelist,
                                    final FileSystem fileSystem,
                                    final IdGenerator idGenerator,
                                    final PactGeneratorRequest request,
                                    final PactGeneratorResponse response) {
        this.consumerName = consumerName;
        this.providerName = providerName;
        this.requestPathWhitelist = new ArrayList<>(requestPathWhitelist);
        this.requestPathBlacklist = new ArrayList<>(requestPathBlacklist);
        this.strictApplicationJson = strictApplicationJson;
        this.requestHeaderWhitelist = requestHeaderWhitelist;
        this.responseHeaderWhitelist = responseHeaderWhitelist;
        this.fileSystem = fileSystem;
        this.idGenerator = idGenerator;
        this.request = request;
        this.response = response;
    }

    public PactGeneratorInvocation withConsumer(final String consumerName) {
        return new PactGeneratorInvocation(consumerName, providerName, requestPathWhitelist, requestPathBlacklist,
                strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist,
                                           fileSystem, idGenerator, request, response);
    }

    public PactGeneratorInvocation withProvider(final String providerName) {
        return new PactGeneratorInvocation(consumerName, providerName, requestPathWhitelist, requestPathBlacklist,
                strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist,
                                           fileSystem, idGenerator, request, response);
    }

    public PactGeneratorInvocation withRequest(final PactGeneratorRequest request) {
        return new PactGeneratorInvocation(consumerName, providerName, requestPathWhitelist, requestPathBlacklist,
                strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist, fileSystem, idGenerator, request, response);
    }

    public PactGeneratorInvocation withResponse(final PactGeneratorResponse response) {
        return new PactGeneratorInvocation(consumerName, providerName, requestPathWhitelist, requestPathBlacklist,
                strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist, fileSystem, idGenerator, request, response);
    }

    public PactGeneratorInvocation withWhitelist(final String... regexPatterns) {
        final List<String> newRequestPathWhitelist = extendListWithItems(requestPathWhitelist, regexPatterns);
        return new PactGeneratorInvocation(consumerName, providerName, newRequestPathWhitelist, requestPathBlacklist,
                strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist, fileSystem, idGenerator, request, response);
    }

    public PactGeneratorInvocation withBlacklist(final String... regexPatterns) {
        final List<String> newRequestPathBlacklist = extendListWithItems(requestPathBlacklist, regexPatterns);
        return new PactGeneratorInvocation(consumerName, providerName, requestPathWhitelist, newRequestPathBlacklist,
                strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist, fileSystem, idGenerator, request, response);
    }

    public PactGeneratorInvocation withStrictApplicationJson(final boolean strictApplicationJson) {
        return new PactGeneratorInvocation(consumerName, providerName, requestPathWhitelist, requestPathBlacklist,
                strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist, fileSystem, idGenerator, request, response);
    }

    public PactGeneratorInvocation withRequestHeaderWhitelist(final String... httpHeaders) {
        final List<String> newRequestHeaderWhitelist = extendListWithItems(responseHeaderWhitelist, httpHeaders);
        return new PactGeneratorInvocation(consumerName, providerName, requestPathWhitelist, requestPathBlacklist,
                                           strictApplicationJson, newRequestHeaderWhitelist, responseHeaderWhitelist, fileSystem, idGenerator, request, response);
    }

    public PactGeneratorInvocation withResponseHeaderWhitelist(final String... httpHeaders) {
        final List<String> newResponseHeaderWhitelist = extendListWithItems(responseHeaderWhitelist, httpHeaders);
        return new PactGeneratorInvocation(consumerName, providerName, requestPathWhitelist, requestPathBlacklist,
                                           strictApplicationJson, requestHeaderWhitelist, newResponseHeaderWhitelist, fileSystem, idGenerator, request, response);
    }

    public void invokeProcess() {
        final PactGenerator pactGenerator = createPactGenerator();
        final WireMockPactGeneratorUserOptions userOptions = new WireMockPactGeneratorUserOptions(
                consumerName, providerName, requestPathWhitelist, requestPathBlacklist,
                strictApplicationJson, requestHeaderWhitelist, responseHeaderWhitelist);

        final PactGeneratorInteraction interaction = new PactGeneratorInteraction(
                request,
                response,
                userOptions.getInteractionFilter(),
                userOptions.isStrictApplicationJson(),
                userOptions.getContentFilter()
        );
        pactGenerator.process(interaction);
    }

    public String invokeGetPactLocation() {
        return createPactGenerator().getPactLocation();
    }

    private PactGenerator createPactGenerator() {
        return new PactGenerator(consumerName, providerName, fileSystem, idGenerator);
    }

    private <T> List<T> extendListWithItems(final List<T> original, final T[] items) {
        final List<T> copyOfOriginal = new ArrayList<>(original);
        copyOfOriginal.addAll(Arrays.asList(items));
        return copyOfOriginal;
    }
}
