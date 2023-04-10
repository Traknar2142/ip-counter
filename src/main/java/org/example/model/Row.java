package org.example.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Row implements Comparable<Row> {
    private int row;
    private final DataInputStream dataInputStream;
    private List<Integer> cache;
    private int byteBufferSize;
    private int cacheIndex;

    public Row(int row, DataInputStream dataInputStream) {
        this.row = row;
        this.dataInputStream = dataInputStream;
    }

    public int getRow() {
        return row;
    }

    public int getFromCache() throws IOException {
        if (cache == null || cacheIndex > cache.size() - 1) {
            refillCache();
            cacheIndex = 0;
        }
        Integer row = cache.get(cacheIndex);
        cacheIndex++;
        return row;
    }

    public void reorderRow() throws IOException {
        this.row = getFromCache();
    }

    public void setByteBufferSize(int countOfRow) {
        byteBufferSize = countOfRow * 4;
    }

    private void refillCache() throws IOException {
        try {
            byte[] buffer = new byte[byteBufferSize];
            int bytesRead = dataInputStream.read(buffer);
            if (isFileRead(bytesRead)) {
                dataInputStream.close();
                cache = new ArrayList<>(1);
                cache.add(-1);
            } else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                int cashCapacity = bytesRead / 4;
                List<Integer> intBuffer = new ArrayList<>(cashCapacity);
                for (int i = 0; i < cashCapacity; i++) {
                    int value = byteBuffer.getInt();
                    intBuffer.add(value);
                }
                cache = intBuffer;
            }
        } catch (IOException e){
            dataInputStream.close();
            throw e;
        }
    }

    private boolean isFileRead(int bytesRead) {
        return bytesRead == -1;
    }

    @Override
    public int compareTo(Row otherRow) {
        return Integer.compare(row, otherRow.getRow());
    }
}
