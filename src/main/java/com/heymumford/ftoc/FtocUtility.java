package com.heymumford.ftoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FtocUtility {
    private static final Logger logger = LoggerFactory.getLogger(FtocUtility.class);
    private static final String VERSION = loadVersion();
    private final List<File> featureFiles;
    private final Map<String, Integer> tagConcordance;

    public FtocUtility() {
        this.featureFiles = new ArrayList<>();
        this.tagConcordance = new HashMap<>();
    }

    public void initialize() {
        logger.info("FTOC utility version {} initialized.", VERSION);
    }

    public void processDirectory(String directoryPath) {
        try {
            Path directory = Paths.get(directoryPath);
            if (Files.notExists(directory) || !Files.isDirectory(directory)) {
                logger.error("Invalid directory: {}", directoryPath);
                return;
            }

            logger.info("Running FTOC utility on directory: {}", directoryPath);
            List<File> foundFeatureFiles = findFeatureFiles(directory);
            if (foundFeatureFiles.isEmpty()) {
                logger.warn("No feature files found in directory: {}", directoryPath);
                return;
            }

            featureFiles.addAll(foundFeatureFiles);
            featureFiles.forEach(this::processFeatureFile);
            generateConcordanceReport();
            logger.info("FTOC utility finished successfully.");
        } catch (IOException e) {
            logger.error("Error while running FTOC utility: {}", e.getMessage());
        }
    }

    private List<File> findFeatureFiles(Path directory) throws IOException {
        logger.debug("Searching for feature files in: {}", directory);
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
            Files.lines(file.toPath()).forEach(line -> {
                line = line.trim();
                if (line.startsWith("Scenario") || line.startsWith("Scenario Outline")) {
                    logger.info("Found scenario in {}: {}", file.getName(), line);
                } else if (line.startsWith("@")) {
                    updateTagConcordance(line);
                }
            });
        } catch (IOException e) {
            logger.error("Error processing feature file {}: {}", file.getName(), e.getMessage());
        }
    }

    private void updateTagConcordance(String tagLine) {
        Arrays.stream(tagLine.split("\\s+"))
                .forEach(tag -> tagConcordance.merge(tag, 1, Integer::sum));
        logger.debug("Updated tag concordance: {}", tagLine);
    }

    private void generateConcordanceReport() {
        logger.info("Generating tag concordance report...");
        tagConcordance.forEach((tag, count) -> logger.info("Tag: {}, Count: {}", tag, count));
        logger.info("Concordance report generated successfully.");
    }

    private static String loadVersion() {
        Properties properties = new Properties();
        try (InputStream input = FtocUtility.class.getClassLoader().getResourceAsStream("ftoc-version.properties")) {
            if (input == null) {
                return "unknown";
            }
            properties.load(input);
            return properties.getProperty("version", "unknown");
        } catch (IOException e) {
            return "unknown";
        }
    }
    private static void printHelp() {
        System.out.println("FTOC Utility version " + VERSION);
        System.out.println("Usage: ftoc [-d <directory>] [--version | -v] [--help]");
        System.out.println("Options:");
        System.out.println("  -d <directory>    Specify the directory to analyze (default: current directory)");
        System.out.println("  --version, -v     Display version information");
        System.out.println("  --help            Display this help message");
    }

    public static void main(String[] args) {
        if (args.length == 0 || Arrays.asList(args).contains("--help")) {
            printHelp();
            return;
        }

        if (Arrays.asList(args).contains("--version") || Arrays.asList(args).contains("-v")) {
            System.out.println("FTOC Utility version " + VERSION);
            return;
        }

        String directoryPath = ".";
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i]) && i + 1 < args.length) {
                directoryPath = args[i + 1];
                break;
            }
        }

        FtocUtility ftoc = new FtocUtility();
        ftoc.initialize();
        ftoc.processDirectory(directoryPath);
    }
}