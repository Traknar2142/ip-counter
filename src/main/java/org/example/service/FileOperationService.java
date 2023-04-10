package org.example.service;

import org.example.exception.DirNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileOperationService {
    public void createDir(File file){
        if (file.isDirectory()){
            file.mkdir();
        } else {
            throw new DirNotFoundException("Directory " + file.getAbsolutePath() + " is not found");
        }
    }

    public void deleteDir(File file) throws IOException {
        Files.walk(file.toPath())
                .map(Path::toFile)
                .forEach(File::delete);
        file.delete();
    }
}
