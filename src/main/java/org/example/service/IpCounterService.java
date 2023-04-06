package org.example.service;

import org.example.model.Row;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Queue;

public class IpCounterService {
    public long count(Queue<Row> queue) throws IOException {
        int prevIpValue = -1;
        long count = 0;
        while (!queue.isEmpty()) {
            Row row = queue.poll();
            if (row.getRow() != prevIpValue) {
                count++;
            }
            prevIpValue = row.getRow();
            DataInputStream rowInputStream = row.getDataInputStream();
            if (rowInputStream.available() > 0) {
                int nextRow = rowInputStream.readInt();
                queue.offer(new Row(nextRow, row.getDataInputStream()));
            } else {
                row.getDataInputStream().close();
            }

        }
        return count;
    }
}
