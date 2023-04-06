package org.example.model;

import java.io.DataInputStream;

public class Row implements Comparable<Row>{
    private int row;
    private DataInputStream dataInputStream;

    public Row(int row, DataInputStream dataInputStream) {
        this.row = row;
        this.dataInputStream = dataInputStream;
    }

    @Override
    public int compareTo(Row otherRow) {
        return Integer.compare(row, otherRow.getRow());
    }

    public int getRow() {
        return row;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }
}
