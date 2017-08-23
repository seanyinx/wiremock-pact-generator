package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.IdGenerator;
import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.json.GsonInstance;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;
import com.google.gson.Gson;

import java.text.Normalizer;
import java.util.regex.Pattern;

class PactSaver {
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w-]");

    private final Gson gson = GsonInstance.gson;
    private final FileSystem fileSystem;
    private final String uuid;

    PactSaver(final FileSystem fileSystem, final IdGenerator idGenerator) {
        this.fileSystem = fileSystem;
        this.uuid = idGenerator.generate();
    }

    String getPactLocation(final Pact pact) {
        return String.format("%s/%s", getPactsPath(), getPactFileName(pact, uuid));
    }

    void savePactFile(final Pact pact) {
        final String pactPath = getPactsPath();

        if (!fileSystem.pathExists(pactPath)) {
            fileSystem.createPath(pactPath);
        }

        final String pactJson = gson.toJson(pact);
        final String pactLocation = getPactLocation(pact);

        try {
            fileSystem.saveFile(pactLocation, pactJson);
        } catch (final Exception e) {
            throw new WireMockPactGeneratorException(String.format("Unable to save file '%s'", pactLocation), e);
        }
    }

    private String getPactsPath() {
        return String.format("%s/pacts", getOutputPath());
    }

    private String getOutputPath() {
        if (!fileSystem.pathExists("target") && fileSystem.pathExists("build")) {
            return "build";
        }

        return "target";
    }

    private String sanitize(String s) {
        s =  Normalizer.normalize(s, Normalizer.Form.NFD);
        return NON_ALPHANUMERIC.matcher(s).replaceAll("");
    }

    private String getPactFileName(final Pact pact, final String guid) {
        final String sanitizedConsumer = sanitize(pact.getConsumer().getName());
        final String sanitizedProvider = sanitize(pact.getProvider().getName());

        return String.format("%s-%s-%s-pact.json", sanitizedConsumer, sanitizedProvider, guid);
    }
}
