package org.example.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileOperationService {
    public void createDir(File file) {
        file.mkdir();
    }

    public void deleteDir(File file) throws IOException {
        Files.walk(file.toPath())
                .map(Path::toFile)
                .forEach(File::delete);
        file.delete();
    }
}
