package com.atlassian.ta.wiremockpactgenerator;

public interface FileSystem {
    boolean pathExists(String path);

    void saveFile(String fileName, String content) throws Exception;

    void createPath(String path);
}
