package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;

class PactGeneratorValidation {
    static void validateResponse(final PactGeneratorResponse response) {
        final int status = response.getStatus();
        if (status < 100 || status > 599) {
            throw new WireMockPactGeneratorException(String.format("Response status code is not valid: %d", status));
        }
    }

    private PactGeneratorValidation() {

    }
}
