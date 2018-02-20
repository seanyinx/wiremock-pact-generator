package com.atlassian.ta.wiremockpactgenerator;

import com.atlassian.ta.wiremockpactgenerator.pactgenerator.InteractionFilter;
import com.atlassian.ta.wiremockpactgenerator.support.Validation;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WireMockPactGeneratorUserOptions {
    private final String consumerName;
    private final String providerName;
    private final List<Pattern> requestPathWhitelist;
    private final List<Pattern> requestPathBlacklist;

    public WireMockPactGeneratorUserOptions(final String consumerName,
                                            final String providerName,
                                            final List<String> requestPathWhitelist,
                                            final List<String> requestPathBlacklist) {
        this.consumerName = Validation.notNullNorBlank(consumerName, "consumer name");
        this.providerName = Validation.notNullNorBlank(providerName, "provider name");
        this.requestPathWhitelist = this.loadPatternListOption(
                requestPathWhitelist, "Invalid regex pattern in request path whitelist");
        this.requestPathBlacklist = this.loadPatternListOption(
                requestPathBlacklist, "Invalid regex pattern in request path blacklist");

    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public InteractionFilter getInteractionFilter() {
        return new InteractionFilter(requestPathWhitelist, requestPathBlacklist);
    }

    private List<Pattern> loadPatternListOption(final List<String> patternList, final String errorMessage) {
        return Validation.withWireMockPactGeneratorExceptionWrapper(
            () -> patternList
                .stream()
                .map(Pattern::compile)
                .collect(Collectors.toList()),
            errorMessage
        );
    }
}
