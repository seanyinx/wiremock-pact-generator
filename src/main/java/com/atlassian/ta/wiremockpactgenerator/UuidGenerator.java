package com.atlassian.ta.wiremockpactgenerator;

import java.util.UUID;

public class UuidGenerator implements IdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
