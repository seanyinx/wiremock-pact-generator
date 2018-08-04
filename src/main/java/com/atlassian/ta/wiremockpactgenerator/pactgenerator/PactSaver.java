package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.WireMockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.json.GsonInstance;
import com.atlassian.ta.wiremockpactgenerator.pactgenerator.models.Pact;

import java.text.Normalizer;
import java.util.regex.Pattern;

class PactSaver {
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w-]");

    private final FileSystem fileSystem;
    private final String uuid;

    PactSaver(final FileSystem fileSystem, final IdGenerator idGenerator) {
        this.fileSystem = fileSystem;
        this.uuid = idGenerator.generate();
    }

    String getPactFileLocation(final Pact pact) {
        return String.format("%s/%s", getPactsPath(), getPactFileName(pact, uuid));
    }

    void savePactFile(final Pact pact, final boolean strictApplicationJson) {
        final String pactPath = getPactsPath();
        ensurePathExists(pactPath);
        final String pactFileLocation = getPactFileLocation(pact);
        final String pactJson = serializePact(pact, strictApplicationJson);
        saveFile(pactFileLocation, pactJson);
    }

    private String serializePact(final Pact pact, final boolean strictApplicationJson) {
        return strictApplicationJson ? GsonInstance.strictGson.toJson(pact) : GsonInstance.nonStrictGson.toJson(pact);
    }

    private void saveFile(final String pactFileLocation, final String pactJson) {
        try {
            fileSystem.saveFile(pactFileLocation, pactJson);
        } catch (final Exception e) {
            throw new WireMockPactGeneratorException(String.format("Unable to save file '%s'", pactFileLocation), e);
        }
    }

    private void ensurePathExists(final String pactPath) {
        if (!fileSystem.pathExists(pactPath)) {
            fileSystem.createPath(pactPath);
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

    private String getPactFileName(final Pact pact, final String guid) {
        final String sanitizedConsumer = sanitize(pact.getConsumer().getName());
        final String sanitizedProvider = sanitize(pact.getProvider().getName());

        return String.format("%s-%s-%s-pact.json", sanitizedConsumer, sanitizedProvider, guid);
    }

    private String sanitize(String s) {
        s =  Normalizer.normalize(s, Normalizer.Form.NFD);
        return NON_ALPHANUMERIC.matcher(s).replaceAll("");
    }
}
