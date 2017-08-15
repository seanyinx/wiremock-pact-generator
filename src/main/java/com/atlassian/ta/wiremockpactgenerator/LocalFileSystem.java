package com.atlassian.ta.wiremockpactgenerator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFileSystem implements FileSystem {
    public boolean pathExists(final String path) {
        return Paths.get(path).toFile().isDirectory();
    }

    public void saveFile(final String fileName, final String content) throws IOException {
        Files.write(Paths.get(fileName), content.getBytes(Charset.forName("UTF-8")));
    }

    public void createPath(final String path) {
        Paths.get(path).toFile().mkdirs();
    }
}
