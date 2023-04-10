package org.example.service;

import org.example.factory.ChunkWriterFactory;
import org.example.model.Row;
import org.example.util.ParserUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private final ChunkWriterFactory chunkWriterFactory;

    public ChunkService(int chunkSize,
                        ChunkFileHolder chunkFileHolder,
                        PathHolder pathHolder,
                        ChunkWriterFactory chunkWriterFactory) {
        this.chunkSize = chunkSize;
        this.chunkFileHolder = chunkFileHolder;
        this.pathHolder = pathHolder;
        this.chunkWriterFactory = chunkWriterFactory;
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

    public void convertFileToChunks() throws IOException {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(pathHolder.getInputFile()))) {
            List<Integer> chunk = new ArrayList<>(chunkSize);

            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            byte[] tail = new byte[16];

            int lastLnIndex = 0;
            int lastBufferIndex = 0;
            //в цикле читаем файл и записываем байты в буфер
            while ((bytesRead = reader.read(buffer, 0, buffer.length)) != -1) {
                int start = 0;
                //читаем буффер
                for (int i = 0; i < bytesRead; i++) {
                    //условие начитки байтов начала буфера до первого знака переноса строки
                    if (buffer[i] == '\n' && start == 0) {
                        lastLnIndex = i;
                        byte[] byteLine = finishReadline(tail, buffer, i);
                        processBuffer(byteLine, 0, byteLine.length, chunk);
                        start = i + 1;

                        //условие начитки остального буфера
                    } else if (buffer[i] == '\n') {
                        lastLnIndex = i;
                        int length = i - start;
                        processBuffer(buffer, start, length, chunk);
                        start = i + 1;
                    }
                }
                //случай, если в конце целой пачки есть разорванный IP
                if (lastLnIndex < bytesRead && bytesRead == chunkSize) {
                    //собираем первую половину
                    tail = readTail(buffer, lastLnIndex);
                    lastLnIndex = 0;
                }
                //случай, если остался последний буфер со второй половиной разорванного IP
                if (bytesRead < chunkSize) {
                    //переменная нужна для начитки байтов из последнего буфера
                    lastBufferIndex = bytesRead;
                }
            }

            //После обработки всех буферов начитаем байты последнего разорванного IP в документе
            if (lastLnIndex == 0) {
                byte[] completeLine = finishReadline(tail, buffer, lastBufferIndex);
                processBuffer(completeLine, 0, completeLine.length, chunk);
            }
            if (!chunk.isEmpty()) {
                createSortedChunk(chunk);
            }
        }
    }

    private void processBuffer(byte[] buffer, int start, int length, List<Integer> chunk) throws IOException {
        String line = new String(buffer, start, length, StandardCharsets.UTF_8);
        convertLineAndStore(line, chunk);
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

    private void createSortedChunk(List<Integer> chunk) throws IOException {
        Collections.sort(chunk);
        File sortedChunk = writeChunkFile(chunk);
        chunkFileHolder.add(sortedChunk);
    }

    private File writeChunkFile(List<Integer> chunk) throws IOException {
        File sortedChunk = File.createTempFile("sortedchunk", ".bin", pathHolder.getTempDir());
        try (DataOutputStream writer = chunkWriterFactory.getWriter(sortedChunk)) {

            //на каждое число по 4 байта
            byte[] buffer = new byte[chunk.size() * 4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

            //записываем числа в буфер
            for (Integer integer : chunk) {
                byteBuffer.putInt(integer);
            }

            //записываем весь буфер за раз в файл чанка
            writer.write(buffer);
            writer.close();

            return sortedChunk;
        }
    }

}
