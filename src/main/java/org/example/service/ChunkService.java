package org.example.service;

import org.example.model.Row;
import org.example.util.ParserUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ChunkService {
    private final BufferedReader reader;
    private final int chunkSize;
    private final ChunkFileHolder chunkFileHolder;
    private final PathHolder pathHolder;

    public ChunkService(BufferedReader reader,
                        int chunkSize,
                        ChunkFileHolder chunkFileHolder,
                        PathHolder pathHolder) {
        this.reader = reader;
        this.chunkSize = chunkSize;
        this.chunkFileHolder = chunkFileHolder;
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
        int cacheSizeForEachChunk = chunkSize / sortedChunks.size();
        for (File chunkFile : sortedChunks) {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(chunkFile));
            if (inputStream.available() > 0) {
                int ipValue = inputStream.readInt();
                Row row = new Row(ipValue, inputStream);
                row.setByteBufferSize(cacheSizeForEachChunk);
                queue.offer(row);
            } else {
                inputStream.close();
            }
        }
        return queue;
    }

    private void createSortedChunk(List<Integer> chunk) throws IOException {
        Collections.sort(chunk);
        File sortedChunk = writeChunkFile(chunk);
        chunkFileHolder.add(sortedChunk);
    }

    private File writeChunkFile(List<Integer> chunk) throws IOException {
        File sortedChunk = File.createTempFile("sortedchunk", ".bin", pathHolder.getTempDir());
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(sortedChunk));
        DataOutputStream writer = new DataOutputStream(outputStream);

        byte[] buffer = new byte[chunk.size() * 4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        for (Integer integer : chunk) {
            byteBuffer.putInt(integer);
        }

        writer.write(buffer);
        writer.close();
        return sortedChunk;
    }

}
