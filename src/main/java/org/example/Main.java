package org.example;

import org.example.factory.ChunkWriterFactory;
import org.example.model.Row;
import org.example.service.ChunkFileHolder;
import org.example.service.ChunkService;
import org.example.service.FileOperationService;
import org.example.service.IpCounterService;
import org.example.service.PathHolder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        long bigStart = System.currentTimeMillis();
        String inputFileName = args[0];

        Path inputPath = Paths.get(inputFileName);
        Path outputPath = inputPath.resolveSibling("result.txt");
        String outputFileName = outputPath.toString();

        int chunkSize = Integer.parseInt(args[1]);

        PathHolder pathHolder = new PathHolder(inputFileName, outputFileName);
        FileOperationService fileOperationService = new FileOperationService();
        fileOperationService.createDir(pathHolder.getTempDir());
        ChunkFileHolder chunkFileHolder = new ChunkFileHolder();
        ChunkWriterFactory chunkWriterFactory = new ChunkWriterFactory();
        ChunkService chunkService = new ChunkService(chunkSize, chunkFileHolder, pathHolder, chunkWriterFactory);
        IpCounterService ipCounter = new IpCounterService();


        LOGGER.info("start reading file");
        long start = System.currentTimeMillis();
        chunkService.convertFileToChunks();
        long end = System.currentTimeMillis();
        LOGGER.info("end reading file");
        LOGGER.info("process time: " + (end - start) + " milliseconds");

        LOGGER.info("start creating queue");
        start = System.currentTimeMillis();
        Queue<Row> queue = chunkService.createChunkQueue(chunkFileHolder.getSortedChunks());
        end = System.currentTimeMillis();
        LOGGER.info("end creating queue");
        LOGGER.info("process time: " + (end - start) + " milliseconds");

        LOGGER.info("start counting result");
        start = System.currentTimeMillis();
        long count = ipCounter.count(queue);
        end = System.currentTimeMillis();
        LOGGER.info("end counting result");
        LOGGER.info("process time: " + (end - start) + " milliseconds");

        PrintWriter writer = new PrintWriter(new FileWriter(pathHolder.getResultFile()));
        writer.println("Number of unique IP addresses: " + count);
        long bigEnd = System.currentTimeMillis();
        LOGGER.info("Number of unique IP addresses: " + count);

        fileOperationService.deleteDir(pathHolder.getTempDir());
        LOGGER.info("all process took: " + (bigEnd - bigStart) + " milliseconds");
        writer.close();
    }

}
