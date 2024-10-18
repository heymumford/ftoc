package com.heymumford.ftoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FtocUtility {
    private static final Logger logger = LoggerFactory.getLogger(FtocUtility.class);
    private List<File> featureFiles;
    private final Map<String, Integer> tagConcordance;

    public FtocUtility() {
        this.featureFiles = new ArrayList<>();
        this.tagConcordance = new HashMap<>();
    }

    public void initialize() {
        logger.info("FTOC utility initialized.");
    }

    public void run(String directoryPath) {
        try {
            Path directory = Paths.get(directoryPath);
            if (Files.notExists(directory) || !Files.isDirectory(directory)) {
                logger.error("Invalid directory: {}", directoryPath);
                return;
            }

            logger.info("Running FTOC utility on directory: {}", directoryPath);
            // Traverse directory and gather feature files
            featureFiles = findFeatureFiles(directory);
            if (featureFiles.isEmpty()) {
                logger.warn("No feature files found in directory: {}", directoryPath);
                return;
            }

            // Process each feature file to generate TOC and concordance
            featureFiles.forEach(this::processFeatureFile);
            generateConcordanceReport();
            logger.info("FTOC utility finished successfully.");
        } catch (IOException e) {
            logger.error("Error while running FTOC utility: {}", e.getMessage());
        }
    }

    private List<File> findFeatureFiles(Path directory) throws IOException {
        logger.debug("Searching for feature files in: {}", directory.toString());
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(path -> path.toString().endsWith(".feature"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }

    private void processFeatureFile(File file) {
        logger.debug("Processing feature file: {}", file.getName());
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                if (line.trim().startsWith("Scenario") || line.trim().startsWith("Scenario Outline")) {
                    logger.info("Found scenario in {}: {}", file.getName(), line.trim());
                }
                // Extract and count tags
                if (line.trim().startsWith("@")) {
                    updateTagConcordance(line.trim());
                }
            }
        } catch (IOException e) {
            logger.error("Error processing feature file {}: {}", file.getName(), e.getMessage());
        }
    }

    private void updateTagConcordance(String tagLine) {
        String[] tags = tagLine.split("\\s+");
        for (String tag : tags) {
            tagConcordance.put(tag, tagConcordance.getOrDefault(tag, 0) + 1);
        }
        logger.debug("Updated tag concordance: {}", tagLine);
    }

    private void generateConcordanceReport() {
        logger.info("Generating tag concordance report...");
        tagConcordance.forEach((tag, count) -> logger.info("Tag: {}, Count: {}", tag, count));
        logger.info("Concordance report generated successfully.");
    }

    public boolean isTocGenerated() {
        return !featureFiles.isEmpty();
    }

    public boolean isConcordanceGenerated() {
        return !tagConcordance.isEmpty();
    }

    public static void main(String[] args) {
        FtocUtility ftoc = new FtocUtility();
        ftoc.initialize();
        if (args.length > 0) {
            ftoc.run(args[0]); // Pass the directory as an argument
        } else {
            Logger logger = LoggerFactory.getLogger(FtocUtility.class);
            logger.error("No directory specified. Please provide a directory path.");
        }
    }
}

