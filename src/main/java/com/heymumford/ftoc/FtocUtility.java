package com.heymumford.ftoc;

import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import com.heymumford.ftoc.formatter.ConcordanceFormatter;
import com.heymumford.ftoc.formatter.TagQualityFormatter;
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
    private final TocFormatter tocFormatter;
    private final ConcordanceFormatter concordanceFormatter;
    private final TagQualityFormatter tagQualityFormatter;
    private TocFormatter.Format outputFormat;
    private ConcordanceFormatter.Format concordanceFormat;
    private TagQualityFormatter.Format tagQualityFormat;
    private final List<String> includeTagFilters;
    private final List<String> excludeTagFilters;
    private boolean analyzeTagQuality;

    public FtocUtility() {
        this.featureFiles = new ArrayList<>();
        this.tagConcordance = new HashMap<>();
        this.parsedFeatures = new ArrayList<>();
        this.parser = new FeatureParser();
        this.tocFormatter = new TocFormatter();
        this.concordanceFormatter = new ConcordanceFormatter();
        this.tagQualityFormatter = new TagQualityFormatter();
        this.outputFormat = TocFormatter.Format.PLAIN_TEXT; // Default format
        this.concordanceFormat = ConcordanceFormatter.Format.PLAIN_TEXT; // Default format
        this.tagQualityFormat = TagQualityFormatter.Format.PLAIN_TEXT; // Default format
        this.includeTagFilters = new ArrayList<>();
        this.excludeTagFilters = new ArrayList<>();
        this.analyzeTagQuality = false;
    }

    public void initialize() {
        logger.info("FTOC utility version {} initialized.", VERSION);
    }

    public void setOutputFormat(TocFormatter.Format format) {
        this.outputFormat = format;
        logger.debug("Output format set to: {}", format);
    }
    
    public void setConcordanceFormat(ConcordanceFormatter.Format format) {
        this.concordanceFormat = format;
        logger.debug("Concordance format set to: {}", format);
    }
    
    public void setTagQualityFormat(TagQualityFormatter.Format format) {
        this.tagQualityFormat = format;
        logger.debug("Tag quality format set to: {}", format);
    }
    
    public void setAnalyzeTagQuality(boolean analyze) {
        this.analyzeTagQuality = analyze;
        logger.debug("Tag quality analysis set to: {}", analyze);
    }
    
    /**
     * Add a tag to include in the filtered output.
     * Scenarios with at least one of the included tags will appear in the TOC.
     * If no include filters are specified, all scenarios will be included (unless excluded).
     * 
     * @param tag The tag to include (e.g., "@P0", "@Smoke")
     */
    public void addIncludeTagFilter(String tag) {
        if (!tag.startsWith("@")) {
            tag = "@" + tag;
        }
        includeTagFilters.add(tag);
        logger.debug("Added include tag filter: {}", tag);
    }
    
    /**
     * Add a tag to exclude from the filtered output.
     * Scenarios with any of the excluded tags will be removed from the TOC.
     * 
     * @param tag The tag to exclude (e.g., "@Flaky", "@Debug")
     */
    public void addExcludeTagFilter(String tag) {
        if (!tag.startsWith("@")) {
            tag = "@" + tag;
        }
        excludeTagFilters.add(tag);
        logger.debug("Added exclude tag filter: {}", tag);
    }
    
    /**
     * Clear all tag filters (both include and exclude).
     */
    public void clearTagFilters() {
        includeTagFilters.clear();
        excludeTagFilters.clear();
        logger.debug("Cleared all tag filters");
    }

    /**
     * Process a directory of feature files and generate reports.
     * 
     * @param directoryPath Path to the directory containing feature files
     */
    public void processDirectory(String directoryPath) {
        processDirectory(directoryPath, false);
    }
    
    /**
     * Process a directory of feature files and generate reports.
     * 
     * @param directoryPath Path to the directory containing feature files
     * @param generateConcordanceOnly If true, only generate the concordance report, not the TOC
     */
    public void processDirectory(String directoryPath, boolean generateConcordanceOnly) {
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
            
            // Generate tag quality report if requested
            if (analyzeTagQuality) {
                generateTagQualityReport();
            }
            
            // Generate TOC only if not in concordance-only mode
            if (!generateConcordanceOnly) {
                generateTableOfContents();
            }
            
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
        
        // Log basic stats to the logger (for backward compatibility)
        tagConcordance.forEach((tag, count) -> logger.info("Tag: {}, Count: {}", tag, count));
        
        // Generate detailed report using the formatter
        String report = concordanceFormatter.generateConcordanceReport(
                tagConcordance, parsedFeatures, concordanceFormat);
        
        // Output the report to the console
        System.out.println("\n" + report);
        
        logger.info("Concordance report generated successfully.");
    }
    
    private void generateTagQualityReport() {
        logger.info("Generating tag quality analysis report...");
        
        if (parsedFeatures.isEmpty()) {
            logger.warn("No features to analyze for tag quality.");
            return;
        }
        
        // Create a tag quality analyzer with the current data
        TagQualityAnalyzer analyzer = new TagQualityAnalyzer(tagConcordance, parsedFeatures);
        
        // Perform the analysis
        List<TagQualityAnalyzer.Warning> warnings = analyzer.analyzeTagQuality();
        
        // Generate a report using the formatter
        String report = tagQualityFormatter.generateTagQualityReport(warnings, tagQualityFormat);
        
        // Output the report to the console
        System.out.println("\n" + report);
        
        logger.info("Tag quality analysis found {} potential issues.", warnings.size());
    }
    
    private void generateTableOfContents() {
        if (parsedFeatures.isEmpty()) {
            logger.warn("No features to include in table of contents.");
            return;
        }
        
        logger.info("Generating table of contents...");
        
        // Apply tag filters if they are set
        String toc;
        if (!includeTagFilters.isEmpty() || !excludeTagFilters.isEmpty()) {
            logger.info("Applying tag filters - include: {}, exclude: {}", 
                    includeTagFilters, excludeTagFilters);
            toc = tocFormatter.generateToc(parsedFeatures, outputFormat, includeTagFilters, excludeTagFilters);
        } else {
            toc = tocFormatter.generateToc(parsedFeatures, outputFormat);
        }
        
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
        System.out.println("Usage: ftoc [-d <directory>] [-f <format>] [--tags <tags>] [--exclude-tags <tags>] [--concordance] [--concordance-format <format>] [--analyze-tags] [--tag-quality-format <format>] [--format <format>] [--version | -v] [--help]");
        System.out.println("Options:");
        System.out.println("  -d <directory>      Specify the directory to analyze (default: current directory)");
        System.out.println("  -f <format>         Specify TOC output format (text, md, html, json) (default: text)");
        System.out.println("                      Same as --format");
        System.out.println("  --format <format>   Specify output format for all reports (text, md, html, json)");
        System.out.println("                      This is a shorthand to set all format options at once");
        System.out.println("  --tags <tags>       Include only scenarios with at least one of these tags");
        System.out.println("                      Comma-separated list, e.g. \"@P0,@Smoke\"");
        System.out.println("  --exclude-tags <tags> Exclude scenarios with any of these tags");
        System.out.println("                      Comma-separated list, e.g. \"@Flaky,@Debug\"");
        System.out.println("  --concordance       Generate detailed tag concordance report instead of TOC");
        System.out.println("  --concordance-format <format>");
        System.out.println("                      Specify concordance output format (text, md, html, json) (default: text)");
        System.out.println("  --analyze-tags      Perform tag quality analysis and generate warnings report");
        System.out.println("  --tag-quality-format <format>");
        System.out.println("                      Specify tag quality report format (text, md, html, json) (default: text)");
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
        TocFormatter.Format tocFormat = TocFormatter.Format.PLAIN_TEXT;
        ConcordanceFormatter.Format concordanceFormat = ConcordanceFormatter.Format.PLAIN_TEXT;
        TagQualityFormatter.Format tagQualityFormat = TagQualityFormatter.Format.PLAIN_TEXT;
        boolean generateConcordanceOnly = false;
        boolean analyzeTagQuality = false;
        
        FtocUtility ftoc = new FtocUtility();
        ftoc.initialize();
        
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i]) && i + 1 < args.length) {
                directoryPath = args[i + 1];
                i++; // Skip the next argument
            } else if (("-f".equals(args[i]) || "--format".equals(args[i])) && i + 1 < args.length) {
                // Parse the format once and apply to all formatters
                String formatStr = args[i + 1].toLowerCase();
                TocFormatter.Format selectedFormat;
                if ("md".equals(formatStr) || "markdown".equals(formatStr)) {
                    selectedFormat = TocFormatter.Format.MARKDOWN;
                } else if ("html".equals(formatStr)) {
                    selectedFormat = TocFormatter.Format.HTML;
                } else if ("json".equals(formatStr)) {
                    selectedFormat = TocFormatter.Format.JSON;
                } else {
                    selectedFormat = TocFormatter.Format.PLAIN_TEXT;
                }
                
                // Apply the format to all formatters
                tocFormat = selectedFormat;
                concordanceFormat = ConcordanceFormatter.Format.valueOf(selectedFormat.name());
                tagQualityFormat = TagQualityFormatter.Format.valueOf(selectedFormat.name());
                
                i++; // Skip the next argument
            } else if ("--concordance-format".equals(args[i]) && i + 1 < args.length) {
                String formatStr = args[i + 1].toLowerCase();
                if ("md".equals(formatStr) || "markdown".equals(formatStr)) {
                    concordanceFormat = ConcordanceFormatter.Format.MARKDOWN;
                } else if ("html".equals(formatStr)) {
                    concordanceFormat = ConcordanceFormatter.Format.HTML;
                } else if ("json".equals(formatStr)) {
                    concordanceFormat = ConcordanceFormatter.Format.JSON;
                } else {
                    concordanceFormat = ConcordanceFormatter.Format.PLAIN_TEXT;
                }
                i++; // Skip the next argument
            } else if ("--concordance".equals(args[i])) {
                generateConcordanceOnly = true;
            } else if ("--analyze-tags".equals(args[i])) {
                analyzeTagQuality = true;
            } else if ("--tag-quality-format".equals(args[i]) && i + 1 < args.length) {
                String formatStr = args[i + 1].toLowerCase();
                if ("md".equals(formatStr) || "markdown".equals(formatStr)) {
                    tagQualityFormat = TagQualityFormatter.Format.MARKDOWN;
                } else if ("html".equals(formatStr)) {
                    tagQualityFormat = TagQualityFormatter.Format.HTML;
                } else if ("json".equals(formatStr)) {
                    tagQualityFormat = TagQualityFormatter.Format.JSON;
                } else {
                    tagQualityFormat = TagQualityFormatter.Format.PLAIN_TEXT;
                }
                i++; // Skip the next argument
            } else if ("--tags".equals(args[i]) && i + 1 < args.length) {
                String[] tags = args[i + 1].split(",");
                for (String tag : tags) {
                    ftoc.addIncludeTagFilter(tag.trim());
                }
                i++; // Skip the next argument
            } else if ("--exclude-tags".equals(args[i]) && i + 1 < args.length) {
                String[] tags = args[i + 1].split(",");
                for (String tag : tags) {
                    ftoc.addExcludeTagFilter(tag.trim());
                }
                i++; // Skip the next argument
            }
        }
        
        ftoc.setOutputFormat(tocFormat);
        ftoc.setConcordanceFormat(concordanceFormat);
        ftoc.setTagQualityFormat(tagQualityFormat);
        ftoc.setAnalyzeTagQuality(analyzeTagQuality);
        
        // Process the directory and generate concordance data
        ftoc.processDirectory(directoryPath, generateConcordanceOnly);
    }
}