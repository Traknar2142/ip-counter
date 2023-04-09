package org.example.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathHolder {
    private final File inputFile;
    private final File resultFile;
    private final File tempDir;

    public PathHolder(String inputFile, String resultFile) {
        this.inputFile = new File(inputFile);
        this.resultFile = new File(resultFile);
        tempDir = new File(this.inputFile.getParent() + "\\tmp");
        tempDir.mkdir();
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

    public void deleteTempDir() throws IOException {
        Files.walk(tempDir.toPath())
                .map(Path::toFile)
                .forEach(File::delete);
        tempDir.delete();
    }
}
