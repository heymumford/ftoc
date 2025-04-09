package com.heymumford.ftoc;

import com.heymumford.ftoc.analyzer.FeatureAntiPatternAnalyzer;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import com.heymumford.ftoc.formatter.AntiPatternFormatter;
import com.heymumford.ftoc.formatter.ConcordanceFormatter;
import com.heymumford.ftoc.formatter.TagQualityFormatter;
import com.heymumford.ftoc.formatter.TocFormatter;
import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.parser.FeatureParser;
import com.heymumford.ftoc.parser.FeatureParserFactory;
import com.heymumford.ftoc.parser.KarateParser;
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
    private FeatureParser parser;
    private final TocFormatter tocFormatter;
    private final ConcordanceFormatter concordanceFormatter;
    private final TagQualityFormatter tagQualityFormatter;
    private final AntiPatternFormatter antiPatternFormatter;
    private TocFormatter.Format outputFormat;
    private ConcordanceFormatter.Format concordanceFormat;
    private TagQualityFormatter.Format tagQualityFormat;
    private AntiPatternFormatter.Format antiPatternFormat;
    private final List<String> includeTagFilters;
    private final List<String> excludeTagFilters;
    private boolean analyzeTagQuality;
    private boolean detectAntiPatterns;
    private boolean enablePerformanceMonitoring;
    private com.heymumford.ftoc.config.WarningConfiguration warningConfig;

    public FtocUtility() {
        this.featureFiles = new ArrayList<>();
        this.tagConcordance = new HashMap<>();
        this.parsedFeatures = new ArrayList<>();
        this.parser = null; // Will be initialized when needed
        this.tocFormatter = new TocFormatter();
        this.concordanceFormatter = new ConcordanceFormatter();
        this.tagQualityFormatter = new TagQualityFormatter();
        this.antiPatternFormatter = new AntiPatternFormatter();
        this.outputFormat = TocFormatter.Format.PLAIN_TEXT; // Default format
        this.concordanceFormat = ConcordanceFormatter.Format.PLAIN_TEXT; // Default format
        this.tagQualityFormat = TagQualityFormatter.Format.PLAIN_TEXT; // Default format
        this.antiPatternFormat = AntiPatternFormatter.Format.PLAIN_TEXT; // Default format
        this.includeTagFilters = new ArrayList<>();
        this.excludeTagFilters = new ArrayList<>();
        this.analyzeTagQuality = false;
        this.detectAntiPatterns = false;
        this.enablePerformanceMonitoring = false;
        this.warningConfig = new com.heymumford.ftoc.config.WarningConfiguration();
    }

    public void initialize() {
        logger.info("FTOC utility version {} initialized.", VERSION);
        
        // Add JVM info for performance monitoring
        Runtime runtime = Runtime.getRuntime();
        logger.debug("JVM information - Max Memory: {} MB, Available Processors: {}", 
                runtime.maxMemory() / (1024 * 1024),
                runtime.availableProcessors());
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
    
    public void setDetectAntiPatterns(boolean detect) {
        this.detectAntiPatterns = detect;
        logger.debug("Anti-pattern detection set to: {}", detect);
    }
    
    /**
     * Enable or disable performance monitoring.
     * 
     * @param enable Whether to enable performance monitoring
     */
    public void setPerformanceMonitoring(boolean enable) {
        this.enablePerformanceMonitoring = enable;
        logger.debug("Performance monitoring set to: {}", enable);
    }
    
    public void setAntiPatternFormat(AntiPatternFormatter.Format format) {
        this.antiPatternFormat = format;
        logger.debug("Anti-pattern format set to: {}", format);
    }
    
    /**
     * Set a custom warning configuration file.
     * This will reload the warning configuration from the specified file.
     * 
     * @param configFilePath Path to the configuration file
     */
    public void setWarningConfigFile(String configFilePath) {
        logger.debug("Loading warning configuration from: {}", configFilePath);
        this.warningConfig = new com.heymumford.ftoc.config.WarningConfiguration(configFilePath);
        
        if (this.warningConfig.getConfigPath() != null) {
            logger.info("Warning configuration loaded from: {}", this.warningConfig.getConfigPath());
        } else {
            logger.warn("Failed to load warning configuration from: {}. Using defaults.", configFilePath);
        }
    }
    
    /**
     * Get a summary of the current warning configuration.
     * 
     * @return A summary string of the warning configuration
     */
    public String getWarningConfigSummary() {
        return warningConfig.getSummary();
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
            
            // Enable performance monitoring if requested
            if (enablePerformanceMonitoring) {
                com.heymumford.ftoc.performance.PerformanceMonitor.setEnabled(true);
                com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("total");
            }
            
            boolean useParallel = com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled();
            
            if (useParallel && featureFiles.size() > 5) {
                // Use parallel processing for larger repositories
                logger.info("Using parallel processing for {} feature files", featureFiles.size());
                com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("parallel_processing");
                
                try {
                    com.heymumford.ftoc.performance.ParallelFeatureProcessor processor = 
                            new com.heymumford.ftoc.performance.ParallelFeatureProcessor();
                    
                    // Process files in parallel and get results
                    parsedFeatures.addAll(processor.processFeatureFiles(
                            featureFiles, 
                            progress -> logger.debug("Processing progress: {}%", progress)));
                    
                    // Shutdown the processor
                    processor.shutdown();
                    
                    // Update tag concordance from all parsed features
                    for (Feature feature : parsedFeatures) {
                        updateTagConcordanceFromFeature(feature);
                    }
                } catch (Exception e) {
                    logger.error("Error during parallel processing: {}", e.getMessage());
                    logger.info("Falling back to sequential processing");
                    // Clear and fallback to sequential processing
                    parsedFeatures.clear();
                    processFeatureFilesSequentially();
                }
            } else {
                // Use sequential processing for smaller repositories
                processFeatureFilesSequentially();
            }
            
            // Generate reports
            generateConcordanceReport();
            
            // Generate tag quality report if requested
            if (analyzeTagQuality) {
                generateTagQualityReport();
            }
            
            // Generate anti-pattern report if requested
            if (detectAntiPatterns) {
                generateAntiPatternReport();
            }
            
            // Generate TOC only if not in concordance-only mode
            if (!generateConcordanceOnly) {
                generateTableOfContents();
            }
            
            // Report performance metrics if enabled
            if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
                com.heymumford.ftoc.performance.PerformanceMonitor.recordFinalMemory();
                long totalDuration = com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("total");
                logger.info("Total execution time: {} ms", totalDuration);
                
                String performanceSummary = com.heymumford.ftoc.performance.PerformanceMonitor.getSummary();
                logger.info("Performance Summary:\n{}", performanceSummary);
                System.out.println("\nPerformance Summary:\n" + performanceSummary);
            }
            
            logger.info("FTOC utility finished successfully.");
        } catch (IOException e) {
            logger.error("Error while running FTOC utility: {}", e.getMessage());
        }
    }

    /**
     * Process feature files sequentially (non-parallel version).
     * This method is used for small file sets or as a fallback if parallel processing fails.
     */
    private void processFeatureFilesSequentially() {
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("sequential_processing");
        }
        
        logger.info("Processing {} feature files sequentially", featureFiles.size());
        
        // Parse all feature files
        int processedCount = 0;
        for (File file : featureFiles) {
            try {
                // Use FeatureParserFactory to get the appropriate parser
                if (parser == null) {
                    logger.debug("Initializing feature parser");
                }
                FeatureParser fileParser = FeatureParserFactory.getParser(file);
                Feature feature = fileParser.parseFeatureFile(file);
                parsedFeatures.add(feature);
                
                // Update tag concordance from parsed feature
                updateTagConcordanceFromFeature(feature);
                
                // Log progress for larger file sets
                processedCount++;
                if (featureFiles.size() > 20 && processedCount % 10 == 0) {
                    logger.debug("Processed {}/{} files ({}%)", 
                            processedCount, featureFiles.size(), 
                            (int)((processedCount / (double)featureFiles.size()) * 100));
                }
                
            } catch (Exception e) {
                logger.error("Error processing file {}: {}", file.getName(), e.getMessage());
            }
        }
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            long duration = com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("sequential_processing");
            logger.info("Sequential processing completed in {} ms", duration);
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
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("generate_concordance");
        }
        
        // Log basic stats to the logger (for backward compatibility)
        tagConcordance.forEach((tag, count) -> logger.info("Tag: {}, Count: {}", tag, count));
        
        // Generate detailed report using the formatter
        String report = concordanceFormatter.generateConcordanceReport(
                tagConcordance, parsedFeatures, concordanceFormat);
        
        // Output the report to the console
        System.out.println("\n" + report);
        
        logger.info("Concordance report generated successfully.");
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("generate_concordance");
        }
    }
    
    private void generateTagQualityReport() {
        logger.info("Generating tag quality analysis report...");
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("generate_tag_quality");
        }
        
        if (parsedFeatures.isEmpty()) {
            logger.warn("No features to analyze for tag quality.");
            return;
        }
        
        // Create a tag quality analyzer with the current data and warning configuration
        TagQualityAnalyzer analyzer = new TagQualityAnalyzer(tagConcordance, parsedFeatures, warningConfig);
        
        // Perform the analysis
        List<TagQualityAnalyzer.Warning> warnings = analyzer.analyzeTagQuality();
        
        // Generate a report using the formatter
        String report = tagQualityFormatter.generateTagQualityReport(warnings, tagQualityFormat);
        
        // Output the report to the console
        System.out.println("\n" + report);
        
        logger.info("Tag quality analysis found {} potential issues.", warnings.size());
        
        if (warningConfig.getConfigPath() != null) {
            logger.debug("Using warning configuration from: {}", warningConfig.getConfigPath());
        }
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("generate_tag_quality");
        }
    }
    
    private void generateAntiPatternReport() {
        logger.info("Generating feature anti-pattern report...");
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("generate_anti_pattern");
        }
        
        if (parsedFeatures.isEmpty()) {
            logger.warn("No features to analyze for anti-patterns.");
            return;
        }
        
        // Create an anti-pattern analyzer with the current features and warning configuration
        FeatureAntiPatternAnalyzer analyzer = new FeatureAntiPatternAnalyzer(parsedFeatures, warningConfig);
        
        // Perform the analysis
        List<FeatureAntiPatternAnalyzer.Warning> warnings = analyzer.analyzeAntiPatterns();
        
        // Generate a report using the formatter
        String report = antiPatternFormatter.generateAntiPatternReport(warnings, antiPatternFormat);
        
        // Output the report to the console
        System.out.println("\n" + report);
        
        logger.info("Anti-pattern analysis found {} potential issues.", warnings.size());
        
        if (warningConfig.getConfigPath() != null) {
            logger.debug("Using warning configuration from: {}", warningConfig.getConfigPath());
        }
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("generate_anti_pattern");
        }
    }
    
    private void generateTableOfContents() {
        if (parsedFeatures.isEmpty()) {
            logger.warn("No features to include in table of contents.");
            return;
        }
        
        logger.info("Generating table of contents...");
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("generate_toc");
        }
        
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
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("generate_toc");
        }
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
        System.out.println("Usage: ftoc [-d <directory>] [-f <format>] [--tags <tags>] [--exclude-tags <tags>] [--concordance] [--concordance-format <format>] [--analyze-tags] [--tag-quality-format <format>] [--detect-anti-patterns] [--anti-pattern-format <format>] [--format <format>] [--config-file <file>] [--show-config] [--junit-report] [--performance] [--benchmark] [--version | -v] [--help]");
        System.out.println("Options:");
        System.out.println("  -d <directory>      Specify the directory to analyze (default: current directory)");
        System.out.println("  -f <format>         Specify TOC output format (text, md, html, json, junit) (default: text)");
        System.out.println("                      Same as --format");
        System.out.println("  --format <format>   Specify output format for all reports (text, md, html, json, junit)");
        System.out.println("                      This is a shorthand to set all format options at once");
        System.out.println("  --tags <tags>       Include only scenarios with at least one of these tags");
        System.out.println("                      Comma-separated list, e.g. \"@P0,@Smoke\"");
        System.out.println("  --exclude-tags <tags> Exclude scenarios with any of these tags");
        System.out.println("                      Comma-separated list, e.g. \"@Flaky,@Debug\"");
        System.out.println("  --concordance       Generate detailed tag concordance report instead of TOC");
        System.out.println("  --concordance-format <format>");
        System.out.println("                      Specify concordance output format (text, md, html, json, junit) (default: text)");
        System.out.println("  --analyze-tags      Perform tag quality analysis and generate warnings report");
        System.out.println("  --tag-quality-format <format>");
        System.out.println("                      Specify tag quality report format (text, md, html, json, junit) (default: text)");
        System.out.println("  --detect-anti-patterns");
        System.out.println("                      Detect common anti-patterns in feature files and generate warnings");
        System.out.println("  --anti-pattern-format <format>");
        System.out.println("                      Specify anti-pattern report format (text, md, html, json, junit) (default: text)");
        System.out.println("  --junit-report      Output all reports in JUnit XML format (for CI integration)");
        System.out.println("                      This is a shorthand for setting all format options to junit");
        System.out.println("  --config-file <file>");
        System.out.println("                      Specify a custom warning configuration file");
        System.out.println("                      (default: looks for .ftoc/config.yml, .ftoc.yml, etc.)");
        System.out.println("  --show-config       Display the current warning configuration and exit");
        System.out.println("  --performance       Enable performance monitoring and optimizations");
        System.out.println("                      Will use parallel processing for large repositories");
        System.out.println("  --benchmark         Run performance benchmarks (see benchmark options below)");
        System.out.println("  --version, -v       Display version information");
        System.out.println("  --help              Display this help message");
        System.out.println("\nBenchmark Options (used with --benchmark):");
        System.out.println("  --small             Run benchmarks on small repositories (10 files)");
        System.out.println("  --medium            Run benchmarks on medium repositories (50 files)");
        System.out.println("  --large             Run benchmarks on large repositories (200 files)");
        System.out.println("  --very-large        Run benchmarks on very large repositories (500 files)");
        System.out.println("  --all               Run benchmarks on all repository sizes");
        System.out.println("  --report <file>     Specify report output file (default: benchmark-report.txt)");
        System.out.println("  --no-cleanup        Do not delete temporary files after benchmark");
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
        
        // Check for benchmark command
        if (Arrays.asList(args).contains("--benchmark")) {
            runBenchmark(args);
            return;
        }
        
        // Process config file early if specified, so it can be used with --show-config
        FtocUtility ftoc = new FtocUtility();
        ftoc.initialize();
        
        // Check for config file option
        for (int i = 0; i < args.length; i++) {
            if ("--config-file".equals(args[i]) && i + 1 < args.length) {
                ftoc.setWarningConfigFile(args[i + 1]);
                break;
            }
        }
        
        // Check if we should just display the config and exit
        if (Arrays.asList(args).contains("--show-config")) {
            System.out.println(ftoc.getWarningConfigSummary());
            return;
        }

        String directoryPath = ".";
        TocFormatter.Format tocFormat = TocFormatter.Format.PLAIN_TEXT;
        ConcordanceFormatter.Format concordanceFormat = ConcordanceFormatter.Format.PLAIN_TEXT;
        TagQualityFormatter.Format tagQualityFormat = TagQualityFormatter.Format.PLAIN_TEXT;
        AntiPatternFormatter.Format antiPatternFormat = AntiPatternFormatter.Format.PLAIN_TEXT;
        boolean generateConcordanceOnly = false;
        boolean analyzeTagQuality = false;
        boolean detectAntiPatterns = false;
        boolean enablePerformance = false;
        
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
                } else if ("junit".equals(formatStr) || "junit-xml".equals(formatStr) || "xml".equals(formatStr)) {
                    selectedFormat = TocFormatter.Format.JUNIT_XML;
                } else {
                    selectedFormat = TocFormatter.Format.PLAIN_TEXT;
                }
                
                // Apply the format to all formatters
                tocFormat = selectedFormat;
                concordanceFormat = ConcordanceFormatter.Format.valueOf(selectedFormat.name());
                tagQualityFormat = TagQualityFormatter.Format.valueOf(selectedFormat.name());
                antiPatternFormat = AntiPatternFormatter.Format.valueOf(selectedFormat.name());
                
                i++; // Skip the next argument
            } else if ("--config-file".equals(args[i]) && i + 1 < args.length) {
                // Already processed above, just skip the argument
                i++; // Skip the next argument
            } else if ("--concordance-format".equals(args[i]) && i + 1 < args.length) {
                String formatStr = args[i + 1].toLowerCase();
                if ("md".equals(formatStr) || "markdown".equals(formatStr)) {
                    concordanceFormat = ConcordanceFormatter.Format.MARKDOWN;
                } else if ("html".equals(formatStr)) {
                    concordanceFormat = ConcordanceFormatter.Format.HTML;
                } else if ("json".equals(formatStr)) {
                    concordanceFormat = ConcordanceFormatter.Format.JSON;
                } else if ("junit".equals(formatStr) || "junit-xml".equals(formatStr) || "xml".equals(formatStr)) {
                    concordanceFormat = ConcordanceFormatter.Format.JUNIT_XML;
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
                } else if ("junit".equals(formatStr) || "junit-xml".equals(formatStr) || "xml".equals(formatStr)) {
                    tagQualityFormat = TagQualityFormatter.Format.JUNIT_XML;
                } else {
                    tagQualityFormat = TagQualityFormatter.Format.PLAIN_TEXT;
                }
                i++; // Skip the next argument
            } else if ("--detect-anti-patterns".equals(args[i])) {
                detectAntiPatterns = true;
            } else if ("--anti-pattern-format".equals(args[i]) && i + 1 < args.length) {
                String formatStr = args[i + 1].toLowerCase();
                if ("md".equals(formatStr) || "markdown".equals(formatStr)) {
                    antiPatternFormat = AntiPatternFormatter.Format.MARKDOWN;
                } else if ("html".equals(formatStr)) {
                    antiPatternFormat = AntiPatternFormatter.Format.HTML;
                } else if ("json".equals(formatStr)) {
                    antiPatternFormat = AntiPatternFormatter.Format.JSON;
                } else if ("junit".equals(formatStr) || "junit-xml".equals(formatStr) || "xml".equals(formatStr)) {
                    antiPatternFormat = AntiPatternFormatter.Format.JUNIT_XML;
                } else {
                    antiPatternFormat = AntiPatternFormatter.Format.PLAIN_TEXT;
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
            } else if ("--junit-report".equals(args[i])) {
                // Set all formatters to JUnit XML format
                tocFormat = TocFormatter.Format.JUNIT_XML;
                concordanceFormat = ConcordanceFormatter.Format.JUNIT_XML;
                tagQualityFormat = TagQualityFormatter.Format.JUNIT_XML;
                antiPatternFormat = AntiPatternFormatter.Format.JUNIT_XML;
            } else if ("--performance".equals(args[i])) {
                enablePerformance = true;
            }
        }
        
        ftoc.setOutputFormat(tocFormat);
        ftoc.setConcordanceFormat(concordanceFormat);
        ftoc.setTagQualityFormat(tagQualityFormat);
        ftoc.setAntiPatternFormat(antiPatternFormat);
        ftoc.setAnalyzeTagQuality(analyzeTagQuality);
        ftoc.setDetectAntiPatterns(detectAntiPatterns);
        ftoc.setPerformanceMonitoring(enablePerformance);
        
        // Process the directory and generate concordance data
        ftoc.processDirectory(directoryPath, generateConcordanceOnly);
    }
    
    /**
     * Run benchmark tests.
     * 
     * @param args Command line arguments for benchmarking
     */
    private static void runBenchmark(String[] args) {
        System.out.println("Running FTOC performance benchmarks...");
        
        // Delegate to the BenchmarkRunner main method
        try {
            Class<?> benchmarkClass = Class.forName("com.heymumford.ftoc.benchmark.BenchmarkRunner");
            java.lang.reflect.Method mainMethod = benchmarkClass.getMethod("main", String[].class);
            
            // Filter out the --benchmark arg
            String[] benchmarkArgs = Arrays.stream(args)
                .filter(arg -> !"--benchmark".equals(arg))
                .toArray(String[]::new);
            
            // Invoke the benchmark runner
            mainMethod.invoke(null, (Object) benchmarkArgs);
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Benchmark classes not available in this build.");
            System.err.println("This feature may only be available in development builds.");
        } catch (NoSuchMethodException | IllegalAccessException e) {
            System.err.println("Error accessing benchmark functionality: " + e.getMessage());
        } catch (java.lang.reflect.InvocationTargetException e) {
            System.err.println("Error during benchmark execution: " + e.getCause().getMessage());
            e.getCause().printStackTrace();
        }
    }
}