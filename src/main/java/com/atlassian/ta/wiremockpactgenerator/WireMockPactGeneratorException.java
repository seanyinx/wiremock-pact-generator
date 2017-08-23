package com.atlassian.ta.wiremockpactgenerator;

public class WireMockPactGeneratorException extends RuntimeException {
    public WireMockPactGeneratorException(final String message) {
        super(message);
    }

    public WireMockPactGeneratorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
