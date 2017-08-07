package com.atlassian.ta.wiremockpactgenerator.utils;


import com.atlassian.ta.wiremockpactgenerator.PactSaver;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;

import java.io.IOException;

public class MockPactSaver implements PactSaver{
    private boolean autoSave;
    private int callCount;
    private String lastJson;
    private Pact lastPact;

    public MockPactSaver(boolean autoSave){
        this.autoSave = autoSave;
        callCount = 0;
    }

    public MockPactSaver(){
        this(true);
    }

    public int callCount(){
        return callCount;
    }

    public String lastJson(){
        return lastJson;
    }

    public Pact lastPact(){
        return lastPact;
    }

    @Override
    public boolean autoSave() {
        return autoSave;
    }

    @Override
    public void save(String pactJson, Pact pact) throws IOException {
        callCount++;
        lastJson = pactJson;
        lastPact = pact;
    }
}
