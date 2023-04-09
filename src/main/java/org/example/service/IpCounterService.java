package org.example.service;

import org.example.model.Row;

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
            row.reorderRow();
            if (row.getRow() != -1) {
                queue.offer(row);
            }
        }
        return count;
    }
}
