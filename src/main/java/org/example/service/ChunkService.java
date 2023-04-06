package org.example.service;

import org.example.model.Row;
import org.example.util.ParserUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ChunkService {
    private final BufferedReader reader;
    private final long chunkSize;
    private final ChunkHolder chunkHolder;
    private final PathHolder pathHolder;

    public ChunkService(BufferedReader reader,
                        long chunkSize,
                        ChunkHolder chunkHolder,
                        PathHolder pathHolder) {
        this.reader = reader;
        this.chunkSize = chunkSize;
        this.chunkHolder = chunkHolder;
        this.pathHolder = pathHolder;
    }

    public void convertFileToChunks() throws IOException {
        String line;
        List<Integer> chunk = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            int byteIp = ParserUtil.toByte(line);
            chunk.add(byteIp);
            if (chunk.size() >= chunkSize) {
                createSortedChunk(chunk);
                chunk.clear();
            }
        }
        reader.close();
        if (!chunk.isEmpty()) {
            createSortedChunk(chunk);
        }
    }

    public Queue<Row> createChunkQueue(List<File> sortedChunks) throws IOException {
        Queue<Row> queue = new PriorityQueue<>();
        for (File chunkFile : sortedChunks) {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(chunkFile));
            if (inputStream.available() > 0) {
                int ipValue = inputStream.readInt();
                queue.offer(new Row(ipValue, inputStream));
            } else {
                inputStream.close();
            }
        }
        return queue;
    }

    private void createSortedChunk(List<Integer> chunk) throws IOException {
        Collections.sort(chunk);
        File sortedChunk = writeChunkFile(chunk);
        chunkHolder.add(sortedChunk);
    }

    private File writeChunkFile(List<Integer> chunk) throws IOException {
        File sortedChunk = File.createTempFile("sortedchunk", ".bin", pathHolder.getTempDir());
        DataOutputStream writer = new DataOutputStream(new FileOutputStream(sortedChunk));
        for (int ipValue : chunk) {
            writer.writeInt(ipValue);
        }
        writer.close();
        return sortedChunk;
    }

}
