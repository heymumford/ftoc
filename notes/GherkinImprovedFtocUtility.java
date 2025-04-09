package com.heymumford.ftoc;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.gherkin.GherkinParserBuilder;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GherkinImprovedFtocUtility {
    private static final Logger logger = LoggerFactory.getLogger(GherkinImprovedFtocUtility.class);
    private static final String VERSION = loadVersion();
    private final List<Path> featureFiles;
    private final Map<String, Integer> tagConcordance;
    private final GherkinParser parser;

    public GherkinImprovedFtocUtility() {
        this.featureFiles = new ArrayList<>();
        this.tagConcordance = new HashMap<>();
        this.parser = new GherkinParserBuilder().build();
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
            List<Path> foundFeatureFiles = findFeatureFiles(directory);
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

    private List<Path> findFeatureFiles(Path directory) throws IOException {
        logger.debug("Searching for feature files in: {}", directory);
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(path -> path.toString().endsWith(".feature"))
                    .collect(Collectors.toList());
        }
    }

    private void processFeatureFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        logger.debug("Processing feature file: {}", fileName);
        
        try {
            String content = Files.readString(filePath);
            List<Envelope> envelopes = parser.parse(content).collect(Collectors.toList());
            
            for (Envelope envelope : envelopes) {
                if (envelope.getGherkinDocument() != null) {
                    GherkinDocument document = envelope.getGherkinDocument();
                    if (document.getFeature() != null) {
                        Feature feature = document.getFeature();
                        
                        // Process feature tags
                        if (feature.getTags() != null) {
                            for (Tag tag : feature.getTags()) {
                                updateTagConcordance(tag.getName());
                            }
                        }
                        
                        // Process scenarios and their tags
                        if (feature.getChildren() != null) {
                            feature.getChildren().forEach(featureChild -> {
                                if (featureChild.getScenario() != null) {
                                    Scenario scenario = featureChild.getScenario();
                                    logger.info("Found scenario in {}: {}", fileName, scenario.getName());
                                    
                                    // Process scenario tags
                                    if (scenario.getTags() != null) {
                                        for (Tag tag : scenario.getTags()) {
                                            updateTagConcordance(tag.getName());
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error processing feature file {}: {}", fileName, e.getMessage());
        }
    }

    private void updateTagConcordance(String tag) {
        tagConcordance.merge(tag, 1, Integer::sum);
        logger.debug("Updated tag concordance: {}", tag);
    }

    private void generateConcordanceReport() {
        logger.info("Generating tag concordance report...");
        tagConcordance.forEach((tag, count) -> logger.info("Tag: {}, Count: {}", tag, count));
        logger.info("Concordance report generated successfully.");
    }

    private static String loadVersion() {
        Properties properties = new Properties();
        try (InputStream input = GherkinImprovedFtocUtility.class.getClassLoader().getResourceAsStream("ftoc-version.properties")) {
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

        GherkinImprovedFtocUtility ftoc = new GherkinImprovedFtocUtility();
        ftoc.initialize();
        ftoc.processDirectory(directoryPath);
    }
}