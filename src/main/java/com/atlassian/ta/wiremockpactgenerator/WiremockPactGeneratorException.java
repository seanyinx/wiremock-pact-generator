package com.atlassian.ta.wiremockpactgenerator;

public class WiremockPactGeneratorException extends RuntimeException {
    public WiremockPactGeneratorException(final String message) {
        super(message);
    }

    public WiremockPactGeneratorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
