package org.example.service;

import org.example.model.Row;
import org.example.util.ParserUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ChunkService {
    private final int chunkSize;
    private final ChunkFileHolder chunkFileHolder;
    private final PathHolder pathHolder;

    public ChunkService(int chunkSize,
                        ChunkFileHolder chunkFileHolder,
                        PathHolder pathHolder) {
        this.chunkSize = chunkSize;
        this.chunkFileHolder = chunkFileHolder;
        this.pathHolder = pathHolder;
    }

    public void convertFileToChunks() throws IOException {
        List<Integer> chunk = new ArrayList<>(chunkSize);

        byte[] buffer = new byte[chunkSize];
        int bytesRead = 0;
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(pathHolder.getInputFile()));
        byte[] tail = new byte[16];

        int lastLnIndex = 0;
        int lastBufferIndex = 0;
        while ((bytesRead = reader.read(buffer, 0, buffer.length)) != -1) {
            int start = 0;
            for (int i = 0; i < bytesRead; i++) {
                if (buffer[i] == '\n' && start == 0) {
                    lastLnIndex = i;
                    byte[] byteLine = finishReadline(tail, buffer, i);
                    String line = new String(byteLine, 0, byteLine.length, StandardCharsets.UTF_8);
                    convertLineAndStore(line, chunk);
                    start = i + 1;

                } else if (buffer[i] == '\n') {
                    lastLnIndex = i;
                    int end = i - start;
                    String line = new String(buffer, start, end, StandardCharsets.UTF_8);
                    convertLineAndStore(line, chunk);
                    start = i + 1;
                }
            }
            //случай, если в конце целой пачки есть разорванный IP
            if (lastLnIndex < bytesRead && bytesRead == chunkSize) {
                //собираем первую половину
                tail = readTail(buffer, lastLnIndex);
                lastLnIndex = 0;
            }
            //случай, если осталась последняя пачка со второй половиной разорванного IP
            if (bytesRead < chunkSize) {
                //переменная нужна для начитки байтов из последней пачки
                lastBufferIndex = bytesRead;
            }
        }
        //После обработки всех пачек начитаем байты последнего разорванного IP в документе
        if (lastLnIndex == 0) {
            byte[] bytes = finishReadline(tail, buffer, lastBufferIndex);
            String line = new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            convertLineAndStore(line, chunk);
        }
        reader.close();
        if (!chunk.isEmpty()) {
            createSortedChunk(chunk);
        }
    }

    private byte[] readTail(byte[] buffer, int startIndex) {
        byte[] tail = new byte[16];
        int tailIndex = 0;
        for (; startIndex < buffer.length; startIndex++) {
            tail[tailIndex] = buffer[startIndex];
            tailIndex++;
        }
        return tail;
    }

    private byte[] finishReadline(byte[] tail, byte[] buffer, int restrictionIndex) {
        int bufferIndex = 0;
        for (int i = 0; i < tail.length; i++) {
            if (tail[i] == 0 && bufferIndex < restrictionIndex) {
                tail[i] = buffer[bufferIndex];
                bufferIndex++;
            }
        }
        return tail;
    }

    private void convertLineAndStore(String line, List<Integer> chunk) throws IOException {
        int byteIp = ParserUtil.toByte(line.trim());
        chunk.add(byteIp);
        if (chunk.size() >= chunkSize) {
            createSortedChunk(chunk);
            chunk.clear();
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
        DataOutputStream writer = getWriter(sortedChunk);

        //на каждое число по 4 байта
        byte[] buffer = new byte[chunk.size() * 4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        //записываем числа в буффер
        for (Integer integer : chunk) {
            byteBuffer.putInt(integer);
        }

        //записываем весь буффер за раз в файл чанка
        writer.write(buffer);
        writer.close();
        return sortedChunk;
    }

    private DataOutputStream getWriter(File sortedChunk) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(sortedChunk));
        return new DataOutputStream(outputStream);
    }

}
