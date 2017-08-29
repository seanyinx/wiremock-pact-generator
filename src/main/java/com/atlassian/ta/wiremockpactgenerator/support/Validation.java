package com.atlassian.ta.wiremockpactgenerator.support;

import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;

public class Validation {

    private Validation() {

    }

    public static <T> T withWireMockPactGeneratorExceptionWrapper(final Work<T> work, final String errorMessage) {
        try {
            return work.doWork();
        } catch (final RuntimeException reason) {
            throw new WireMockPactGeneratorException(errorMessage, reason);
        }
    }

    public static String notNullNorBlank(final String value, final String description) {
        if (value == null || value.trim().length() == 0) {
            throw new WireMockPactGeneratorException(String.format("%s can't be null nor blank", description));
        }
        return value;
    }

    public interface Work<T> {
        T doWork();
    }
}
