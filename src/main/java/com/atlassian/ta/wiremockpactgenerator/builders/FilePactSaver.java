package com.atlassian.ta.wiremockpactgenerator.builders;

import com.atlassian.ta.wiremockpactgenerator.PactSaver;
import com.atlassian.ta.wiremockpactgenerator.common.FileNames;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilePactSaver implements PactSaver {
    private Path destination;
    private boolean autoSave;

    public FilePactSaver(){
        this(FileNames.getBuildDirectory().resolve("pacts"), true);
    }

    public FilePactSaver(Path destination){
        this(destination, true);
    }

    public FilePactSaver(boolean autoSave){
        this(FileNames.getBuildDirectory().resolve("pacts"), autoSave);
    }

    public FilePactSaver(Path destination, boolean autoSave){
        this.autoSave = autoSave;
        this.destination = destination.toAbsolutePath();
        this.destination.toFile().mkdirs();
    }

    public Path getDestination(){
        return destination;
    }

    @Override
    public boolean autoSave() {
        return autoSave;
    }

    @Override
    public void save(String pactJson, Pact pact) throws IOException {
        String fileName = FileNames.getJsonFileName(pact);
        Files.write(destination.resolve(fileName), pactJson.getBytes(Charset.forName("UTF-8")));
    }
}
