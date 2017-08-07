package com.atlassian.ta.wiremockpactgenerator;


import com.atlassian.ta.wiremockpactgenerator.models.Pact;

import java.io.IOException;

public interface PactSaver {
    boolean autoSave();
    void save(String pactJson, Pact pact) throws IOException;
}
