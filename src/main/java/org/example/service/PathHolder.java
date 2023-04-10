package org.example.service;

import java.io.File;

public class PathHolder {
    private final File inputFile;
    private final File resultFile;
    private final File tempDir;

    public PathHolder(String inputFile, String resultFile) {
        this.inputFile = new File(inputFile);
        this.resultFile = new File(resultFile);
        tempDir = new File(this.inputFile.getParent() + "\\tmp");
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getResultFile() {
        return resultFile;
    }

    public File getTempDir() {
        return tempDir;
    }
}
