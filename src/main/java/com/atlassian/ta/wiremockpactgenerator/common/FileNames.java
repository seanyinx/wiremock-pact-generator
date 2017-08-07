package com.atlassian.ta.wiremockpactgenerator.common;


import com.atlassian.ta.wiremockpactgenerator.models.Pact;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class FileNames {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static Path getBuildDirectory(){
        File buildDir = new File("./build");
        if (buildDir.exists() && buildDir.isDirectory()){
            return Paths.get(".", "build");
        }
        File targetDir = new File("./target");
        if (targetDir.exists() && targetDir.isDirectory()){
            return Paths.get(".", "target");
        }
        return Paths.get(".");
    }

    public static String getJsonFileName(Pact pact){
        String base = String.format("%s-%s", pact.getConsumer(), pact.getProvider());
        String suffix = pact.getId().toString().replace("-", "");
        String extension = ".json";

        base = sanitize(base);

        if (base.isEmpty()) {
            return suffix + extension;
        }
        return base + "-" + suffix + extension;
    }

    private static String sanitize(String s){
        s = WHITESPACE.matcher(s).replaceAll("-");
        s =  Normalizer.normalize(s, Normalizer.Form.NFD);
        s = NON_ALPHANUMERIC.matcher(s).replaceAll("");

        s = s.replaceAll("^[_]*", "").replaceAll("[_]*$", "");

        s = s.substring(0, Math.min(s.length(), 200));
        return s.toLowerCase(Locale.ENGLISH);
    }
}
