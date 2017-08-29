package com.atlassian.ta.wiremockpactgenerator;

import com.atlassian.ta.wiremockpactgenerator.support.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WireMockPactGeneratorUserOptions {
    private final String consumerName;
    private final String providerName;
    private final List<Pattern> requestPathWhitelist;

    public WireMockPactGeneratorUserOptions(final String consumerName,
                                            final String providerName,
                                            final List<String> requestPathWhitelist) {
        this.consumerName = Validation.notNullNorBlank(consumerName, "consumer name");
        this.providerName = Validation.notNullNorBlank(providerName, "provider name");
        this.requestPathWhitelist = Validation.withWireMockPactGeneratorExceptionWrapper(
            () -> requestPathWhitelist
                    .stream()
                    .map(Pattern::compile)
                    .collect(Collectors.toList()),
            "Invalid regex pattern in request path whitelist"
        );
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public List<Pattern> getRequestPathWhitelist() {
        return new ArrayList<>(requestPathWhitelist);
    }
}
