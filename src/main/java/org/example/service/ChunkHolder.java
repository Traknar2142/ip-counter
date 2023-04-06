package org.example.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChunkHolder {
    private final List<File> sortedChunks;

    public ChunkHolder() {
        this.sortedChunks = new ArrayList<>();
    }

    public void add(File chunk){
        sortedChunks.add(chunk);
    }

    public List<File> getSortedChunks() {
        return sortedChunks;
    }
}
