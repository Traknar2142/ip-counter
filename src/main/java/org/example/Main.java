package org.example;

import org.example.model.Row;
import org.example.service.ChunkFileHolder;
import org.example.service.ChunkService;
import org.example.service.IpCounterService;
import org.example.service.PathHolder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        long bigStart = System.currentTimeMillis();
        String inputFileName = "F:\\logs\\ip_addresses.txt";
        String outputFileName = "F:\\logs\\result.txt";
        int chunkSize = 10_000_000;

        PathHolder pathHolder = new PathHolder(inputFileName, outputFileName);
        ChunkFileHolder chunkFileHolder = new ChunkFileHolder();
        ChunkService chunkService = new ChunkService(chunkSize, chunkFileHolder, pathHolder);
        IpCounterService ipCounter = new IpCounterService();


        LOGGER.info("start reading file");
        long start = System.currentTimeMillis();
        chunkService.convertFileToChunks();
        long end = System.currentTimeMillis();
        LOGGER.info("end reading file");
        LOGGER.info("process time: " + (end - start));

        LOGGER.info("start creating queue");
        start = System.currentTimeMillis();
        Queue<Row> queue = chunkService.createChunkQueue(chunkFileHolder.getSortedChunks());
        end = System.currentTimeMillis();
        LOGGER.info("end creating queue");
        LOGGER.info("process time: " + (end - start));

        LOGGER.info("start counting result");
        start = System.currentTimeMillis();
        long count = ipCounter.count(queue);
        end = System.currentTimeMillis();
        LOGGER.info("end counting result");
        LOGGER.info("process time: " + (end - start));

        PrintWriter writer = new PrintWriter(new FileWriter(pathHolder.getResultFile()));
        writer.println("Number of unique IP addresses: " + count);
        long bigEnd = System.currentTimeMillis();
        LOGGER.info("Number of unique IP addresses: " + count);
        LOGGER.info("all process took: " + (bigEnd - bigStart));
        writer.close();
    }

}
