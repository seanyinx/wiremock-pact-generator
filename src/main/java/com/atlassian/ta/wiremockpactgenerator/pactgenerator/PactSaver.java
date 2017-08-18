package com.atlassian.ta.wiremockpactgenerator.pactgenerator;

import com.atlassian.ta.wiremockpactgenerator.FileSystem;
import com.atlassian.ta.wiremockpactgenerator.WiremockPactGeneratorException;
import com.atlassian.ta.wiremockpactgenerator.json.GsonInstance;
import com.atlassian.ta.wiremockpactgenerator.models.Pact;
import com.google.gson.Gson;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class PactSaver {
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w-]");

    private final Gson gson = GsonInstance.gson;
    private final FileSystem fileSystem;

    public PactSaver(final FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public String getPactLocation(final Pact pact) {
        return String.format("%s/%s", getPactsPath(), getPactFileName(pact));
    }

    public void savePactFile(final Pact pact) {
        final String pactPath = getPactsPath();

        if (!fileSystem.pathExists(pactPath)) {
            fileSystem.createPath(pactPath);
        }

        final String pactJson = gson.toJson(pact);
        final String sanitizedConsumer = sanitize(pact.getConsumer().getName());
        final String sanitizedProvider = sanitize(pact.getProvider().getName());

        final String pactFileName = String.format(
                "%s/%s-%s-pact.json", pactPath, sanitizedConsumer, sanitizedProvider
        );
        try {
            fileSystem.saveFile(pactFileName, pactJson);
        } catch (final Exception e) {
            throw new WiremockPactGeneratorException(String.format("Unable to save file '%s'", pactFileName), e);
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

    private String getPactFileName(final Pact pact) {
        final String sanitizedConsumer = sanitize(pact.getConsumer().getName());
        final String sanitizedProvider = sanitize(pact.getProvider().getName());

        return String.format("%s-%s-pact.json", sanitizedConsumer, sanitizedProvider);
    }
}
