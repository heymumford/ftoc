package com.heymumford.ftoc;

import com.heymumford.ftoc.formatter.TocFormatter;
import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.parser.FeatureParser;
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

/**
 * Main utility class for ftoc - Feature Table of Contents Utility
 */
public class FtocUtility {
    private static final Logger logger = LoggerFactory.getLogger(FtocUtility.class);
    private static final String VERSION = loadVersion();
    private final List<File> featureFiles;
    private final Map<String, Integer> tagConcordance;
    private final List<Feature> parsedFeatures;
    private final FeatureParser parser;
    private final TocFormatter formatter;
    private TocFormatter.Format outputFormat;

    public FtocUtility() {
        this.featureFiles = new ArrayList<>();
        this.tagConcordance = new HashMap<>();
        this.parsedFeatures = new ArrayList<>();
        this.parser = new FeatureParser();
        this.formatter = new TocFormatter();
        this.outputFormat = TocFormatter.Format.PLAIN_TEXT; // Default format
    }

    public void initialize() {
        logger.info("FTOC utility version {} initialized.", VERSION);
    }

    public void setOutputFormat(TocFormatter.Format format) {
        this.outputFormat = format;
        logger.debug("Output format set to: {}", format);
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
            
            // Parse all feature files
            for (File file : featureFiles) {
                Feature feature = parser.parseFeatureFile(file);
                parsedFeatures.add(feature);
                
                // Update tag concordance from parsed feature
                updateTagConcordanceFromFeature(feature);
            }
            
            // Generate reports
            generateConcordanceReport();
            generateTableOfContents();
            
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

    private void updateTagConcordanceFromFeature(Feature feature) {
        // Add feature tags to concordance
        for (String tag : feature.getTags()) {
            tagConcordance.merge(tag, 1, Integer::sum);
        }
        
        // Add scenario tags to concordance
        feature.getScenarios().forEach(scenario -> {
            for (String tag : scenario.getTags()) {
                tagConcordance.merge(tag, 1, Integer::sum);
            }
        });
        
        logger.debug("Updated tag concordance for feature: {}", feature.getName());
    }

    private void generateConcordanceReport() {
        logger.info("Generating tag concordance report...");
        tagConcordance.forEach((tag, count) -> logger.info("Tag: {}, Count: {}", tag, count));
        logger.info("Concordance report generated successfully.");
    }
    
    private void generateTableOfContents() {
        if (parsedFeatures.isEmpty()) {
            logger.warn("No features to include in table of contents.");
            return;
        }
        
        logger.info("Generating table of contents...");
        String toc = formatter.generateToc(parsedFeatures, outputFormat);
        System.out.println("\n" + toc);
        logger.info("Table of contents generated successfully.");
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
        System.out.println("Usage: ftoc [-d <directory>] [-f <format>] [--version | -v] [--help]");
        System.out.println("Options:");
        System.out.println("  -d <directory>      Specify the directory to analyze (default: current directory)");
        System.out.println("  -f <format>         Specify output format (text, md, html, json) (default: text)");
        System.out.println("  --version, -v       Display version information");
        System.out.println("  --help              Display this help message");
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
        TocFormatter.Format format = TocFormatter.Format.PLAIN_TEXT;
        
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i]) && i + 1 < args.length) {
                directoryPath = args[i + 1];
                i++; // Skip the next argument
            } else if ("-f".equals(args[i]) && i + 1 < args.length) {
                String formatStr = args[i + 1].toLowerCase();
                if ("md".equals(formatStr) || "markdown".equals(formatStr)) {
                    format = TocFormatter.Format.MARKDOWN;
                } else if ("html".equals(formatStr)) {
                    format = TocFormatter.Format.HTML;
                } else if ("json".equals(formatStr)) {
                    format = TocFormatter.Format.JSON;
                } else {
                    format = TocFormatter.Format.PLAIN_TEXT;
                }
                i++; // Skip the next argument
            }
        }

        FtocUtility ftoc = new FtocUtility();
        ftoc.initialize();
        ftoc.setOutputFormat(format);
        ftoc.processDirectory(directoryPath);
    }
}