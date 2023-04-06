package org.example;

import org.example.model.Row;
import org.example.service.ChunkHolder;
import org.example.service.ChunkService;
import org.example.service.IpCounterService;
import org.example.service.PathHolder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        long bigStart = System.currentTimeMillis();
        String inputFileName = "D:\\test_tasks\\logs\\ip.txt";
        String outputFileName = "D:\\test_tasks\\logs\\result.txt";
        long chunkSize = 5_000_000;

        PathHolder pathHolder = new PathHolder(inputFileName, outputFileName);
        BufferedReader reader = new BufferedReader(new FileReader(pathHolder.getInputFile()));
        ChunkHolder chunkHolder = new ChunkHolder();
        ChunkService chunkService = new ChunkService(reader, chunkSize, chunkHolder, pathHolder);
        IpCounterService ipCounter = new IpCounterService();


        LOGGER.info("start reading file");
        long start = System.currentTimeMillis();
        chunkService.convertFileToChunks();
        long end = System.currentTimeMillis();

        LOGGER.info("end reading file" + " " + "process time: " + (end - start));

        LOGGER.info("start creating queue");
        start = System.currentTimeMillis();
        Queue<Row> queue = chunkService.createChunkQueue(chunkHolder.getSortedChunks());
        end = System.currentTimeMillis();
        LOGGER.info("end creating queue" + " " + "process time: " + (end - start));

        LOGGER.info("start counting result");
        start = System.currentTimeMillis();
        long count = ipCounter.count(queue);
        end = System.currentTimeMillis();
        LOGGER.info("end counting result" + " " + "process time: " + (end - start));

        PrintWriter writer = new PrintWriter(new FileWriter(pathHolder.getResultFile()));
        writer.println("Number of unique IP addresses: " + count);
        long bigEnd = System.currentTimeMillis();
        LOGGER.info("Number of unique IP addresses: " + count);
        LOGGER.info( "all process took: " + (bigEnd - bigStart));
        writer.close();
    }

}