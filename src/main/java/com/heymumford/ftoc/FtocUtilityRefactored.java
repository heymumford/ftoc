package com.heymumford.ftoc;

import com.heymumford.ftoc.config.WarningConfiguration;
import com.heymumford.ftoc.core.FeatureProcessor;
import com.heymumford.ftoc.core.FeatureRepository;
import com.heymumford.ftoc.core.Reporter;
import com.heymumford.ftoc.core.impl.DefaultFeatureProcessor;
import com.heymumford.ftoc.core.impl.DefaultFeatureRepository;
import com.heymumford.ftoc.core.impl.DefaultReporter;
import com.heymumford.ftoc.model.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main utility class for ftoc - Feature Table of Contents Utility.
 * This is a refactored version that uses the new architecture with clear separation of concerns.
 */
public class FtocUtilityRefactored {
    private static final Logger logger = LoggerFactory.getLogger(FtocUtilityRefactored.class);
    private static final String VERSION = loadVersion();
    
    private final FeatureRepository repository;
    private final FeatureProcessor processor;
    private Reporter reporter;
    
    private Reporter.Format outputFormat;
    private List<String> includeTagFilters;
    private List<String> excludeTagFilters;
    private boolean analyzeTagQuality;
    private boolean detectAntiPatterns;
    private boolean enablePerformanceMonitoring;
    private WarningConfiguration warningConfig;
    
    // Plugin system
    private com.heymumford.ftoc.plugin.PluginRegistry pluginRegistry;

    /**
     * Create a new instance with default components.
     */
    public FtocUtilityRefactored() {
        // Initialize plugin registry first
        this.pluginRegistry = new com.heymumford.ftoc.plugin.PluginRegistry();
        this.pluginRegistry.loadPlugins();
        
        // Use plugin-provided components if available, otherwise use defaults
        this.repository = pluginRegistry.getCustomFeatureRepository() != null ? 
                pluginRegistry.getCustomFeatureRepository() : new DefaultFeatureRepository();
                
        this.processor = pluginRegistry.getCustomFeatureProcessor() != null ?
                pluginRegistry.getCustomFeatureProcessor() : new DefaultFeatureProcessor(repository);
                
        this.reporter = pluginRegistry.getCustomReporter() != null ?
                pluginRegistry.getCustomReporter() : new DefaultReporter();
                
        this.outputFormat = Reporter.Format.PLAIN_TEXT;
        this.includeTagFilters = new ArrayList<>();
        this.excludeTagFilters = new ArrayList<>();
        this.analyzeTagQuality = false;
        this.detectAntiPatterns = false;
        this.enablePerformanceMonitoring = false;
        this.warningConfig = new WarningConfiguration();
    }
    
    /**
     * Create a new instance with custom components.
     */
    public FtocUtilityRefactored(FeatureRepository repository, FeatureProcessor processor, Reporter reporter) {
        // Initialize plugin registry
        this.pluginRegistry = new com.heymumford.ftoc.plugin.PluginRegistry();
        this.pluginRegistry.loadPlugins();
        
        // Use explicitly provided components (ignore plugin-provided ones)
        this.repository = repository;
        this.processor = processor;
        this.reporter = reporter;
        this.outputFormat = Reporter.Format.PLAIN_TEXT;
        this.includeTagFilters = new ArrayList<>();
        this.excludeTagFilters = new ArrayList<>();
        this.analyzeTagQuality = false;
        this.detectAntiPatterns = false;
        this.enablePerformanceMonitoring = false;
        this.warningConfig = new WarningConfiguration();
    }

    /**
     * Initialize the utility.
     */
    public void initialize() {
        logger.info("FTOC utility version {} initialized.", VERSION);
        
        // Add JVM info for performance monitoring
        Runtime runtime = Runtime.getRuntime();
        logger.debug("JVM information - Max Memory: {} MB, Available Processors: {}", 
                runtime.maxMemory() / (1024 * 1024),
                runtime.availableProcessors());
        
        // Trigger startup event for plugins
        if (pluginRegistry != null) {
            pluginRegistry.triggerEvent(com.heymumford.ftoc.plugin.PluginEvent.STARTUP);
        }
    }
    
    /**
     * Get the plugin registry.
     * 
     * @return The plugin registry
     */
    public com.heymumford.ftoc.plugin.PluginRegistry getPluginRegistry() {
        return pluginRegistry;
    }
    
    /**
     * Get a summary of all loaded plugins.
     * 
     * @return A summary string of loaded plugins
     */
    public String getPluginSummary() {
        if (pluginRegistry == null) {
            return "Plugin system not initialized";
        }
        return pluginRegistry.getPluginSummary();
    }

    /**
     * Set the output format for all reports.
     * 
     * @param format The format to use
     */
    public void setOutputFormat(Reporter.Format format) {
        this.outputFormat = format;
        logger.debug("Output format set to: {}", format);
    }
    
    /**
     * Enable or disable tag quality analysis.
     * 
     * @param analyze Whether to enable tag quality analysis
     */
    public void setAnalyzeTagQuality(boolean analyze) {
        this.analyzeTagQuality = analyze;
        logger.debug("Tag quality analysis set to: {}", analyze);
    }
    
    /**
     * Enable or disable anti-pattern detection.
     * 
     * @param detect Whether to enable anti-pattern detection
     */
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
        if (processor instanceof DefaultFeatureProcessor) {
            ((DefaultFeatureProcessor) processor).setPerformanceMonitoringEnabled(enable);
        }
        logger.debug("Performance monitoring set to: {}", enable);
    }
    
    /**
     * Set a custom warning configuration file.
     * This will reload the warning configuration from the specified file.
     * 
     * @param configFilePath Path to the configuration file
     */
    public void setWarningConfigFile(String configFilePath) {
        logger.debug("Loading warning configuration from: {}", configFilePath);
        this.warningConfig = new WarningConfiguration(configFilePath);
        
        if (this.warningConfig.getConfigPath() != null) {
            logger.info("Warning configuration loaded from: {}", this.warningConfig.getConfigPath());
        } else {
            logger.warn("Failed to load warning configuration from: {}. Using defaults.", configFilePath);
        }
        
        // Update reporter with new warning configuration
        if (reporter instanceof DefaultReporter) {
            this.reporter = new DefaultReporter(warningConfig);
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
     * Register a plugin manually (without loading from JAR).
     * Useful for testing or programmatic plugin creation.
     * 
     * @param plugin The plugin to register
     */
    public void registerPlugin(com.heymumford.ftoc.plugin.FtocPlugin plugin) {
        if (pluginRegistry != null) {
            pluginRegistry.registerPlugin(plugin);
        } else {
            logger.error("Cannot register plugin: plugin system not initialized");
        }
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
            
            logger.info("Running FTOC utility on directory: {}", directoryPath);
            
            // Find and load feature files
            List<Path> featurePaths = repository.findFeatureFiles(directory);
            if (featurePaths.isEmpty()) {
                logger.warn("No feature files found in directory: {}", directoryPath);
                return;
            }
            
            // Enable performance monitoring if requested
            if (enablePerformanceMonitoring) {
                com.heymumford.ftoc.performance.PerformanceMonitor.setEnabled(true);
                com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("total");
            }
            
            // Process feature files
            boolean useParallel = enablePerformanceMonitoring && processor.shouldUseParallelProcessing(featurePaths.size());
            List<Feature> features = processor.processFeatures(featurePaths, useParallel);
            
            if (features.isEmpty()) {
                logger.warn("No features were successfully parsed in directory: {}", directoryPath);
                return;
            }
            
            // Notify plugins that features have been loaded
            if (pluginRegistry != null) {
                pluginRegistry.triggerEvent(com.heymumford.ftoc.plugin.PluginEvent.FEATURES_LOADED, features);
            }
            
            // Generate tag concordance
            Map<String, Integer> tagConcordance = processor.generateTagConcordance(features);
            
            // Create report context object for plugin events
            ReportContext reportContext = new ReportContext(
                features, tagConcordance, outputFormat, includeTagFilters, excludeTagFilters,
                analyzeTagQuality, detectAntiPatterns, generateConcordanceOnly
            );
            
            // Notify plugins before report generation
            if (pluginRegistry != null) {
                pluginRegistry.triggerEvent(com.heymumford.ftoc.plugin.PluginEvent.PRE_GENERATE_REPORT, reportContext);
            }
            
            // Generate concordance report
            reporter.generateConcordanceReport(tagConcordance, features, outputFormat);
            
            // Generate tag quality report if requested
            if (analyzeTagQuality) {
                reporter.generateTagQualityReport(features, tagConcordance, outputFormat);
            }
            
            // Generate anti-pattern report if requested
            if (detectAntiPatterns) {
                reporter.generateAntiPatternReport(features, outputFormat);
            }
            
            // Generate TOC only if not in concordance-only mode
            if (!generateConcordanceOnly) {
                reporter.generateTableOfContents(features, outputFormat, includeTagFilters, excludeTagFilters);
            }
            
            // Notify plugins after report generation
            if (pluginRegistry != null) {
                pluginRegistry.triggerEvent(com.heymumford.ftoc.plugin.PluginEvent.POST_GENERATE_REPORT, reportContext);
            }
            
            // Report performance metrics if enabled
            if (enablePerformanceMonitoring) {
                com.heymumford.ftoc.performance.PerformanceMonitor.recordFinalMemory();
                long totalDuration = com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("total");
                logger.info("Total execution time: {} ms", totalDuration);
                
                String performanceSummary = com.heymumford.ftoc.performance.PerformanceMonitor.getSummary();
                logger.info("Performance Summary:\n{}", performanceSummary);
                System.out.println("\nPerformance Summary:\n" + performanceSummary);
            }
            
            logger.info("FTOC utility finished successfully.");
        } catch (Exception e) {
            logger.error("Error while running FTOC utility: {}", e.getMessage());
        }
    }

    /**
     * Load the version from properties.
     */
    private static String loadVersion() {
        Properties properties = new Properties();
        try (InputStream input = FtocUtilityRefactored.class.getClassLoader().getResourceAsStream("ftoc-version.properties")) {
            if (input == null) {
                return "unknown";
            }
            properties.load(input);
            return properties.getProperty("version", "unknown");
        } catch (IOException e) {
            return "unknown";
        }
    }

    /**
     * Print help/usage information.
     */
    private static void printHelp() {
        System.out.println("FTOC Utility version " + VERSION);
        System.out.println("Usage: ftoc [-d <directory>] [-f <format>] [--tags <tags>] [--exclude-tags <tags>] [--concordance] [--analyze-tags] [--detect-anti-patterns] [--format <format>] [--config-file <file>] [--show-config] [--junit-report] [--performance] [--benchmark] [--version | -v] [--help]");
        System.out.println("Options:");
        System.out.println("  -d <directory>      Specify the directory to analyze (default: current directory)");
        System.out.println("  -f <format>         Specify output format (text, md, html, json, junit) (default: text)");
        System.out.println("                      Same as --format");
        System.out.println("  --format <format>   Specify output format for all reports (text, md, html, json, junit)");
        System.out.println("                      This is a shorthand to set all format options at once");
        System.out.println("  --tags <tags>       Include only scenarios with at least one of these tags");
        System.out.println("                      Comma-separated list, e.g. \"@P0,@Smoke\"");
        System.out.println("  --exclude-tags <tags> Exclude scenarios with any of these tags");
        System.out.println("                      Comma-separated list, e.g. \"@Flaky,@Debug\"");
        System.out.println("  --concordance       Generate detailed tag concordance report instead of TOC");
        System.out.println("  --analyze-tags      Perform tag quality analysis and generate warnings report");
        System.out.println("  --detect-anti-patterns");
        System.out.println("                      Detect common anti-patterns in feature files and generate warnings");
        System.out.println("  --junit-report      Output all reports in JUnit XML format (for CI integration)");
        System.out.println("                      This is a shorthand for setting all format options to junit");
        System.out.println("  --config-file <file>");
        System.out.println("                      Specify a custom warning configuration file");
        System.out.println("                      (default: looks for .ftoc/config.yml, .ftoc.yml, etc.)");
        System.out.println("  --show-config       Display the current warning configuration and exit");
        System.out.println("  --performance       Enable performance monitoring and optimizations");
        System.out.println("                      Will use parallel processing for large repositories");
        System.out.println("  --list-plugins      List all loaded plugins and exit");
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

    /**
     * Main entry point for the command-line utility.
     */
    public static void main(String[] args) {
        if (args.length == 0 || Arrays.asList(args).contains("--help")) {
            printHelp();
            return;
        }
        
        if (Arrays.asList(args).contains("--version") || Arrays.asList(args).contains("-v")) {
            System.out.println("FTOC Utility version " + VERSION);
            return;
        }
        
        // Handle benchmark mode separately
        if (Arrays.asList(args).contains("--benchmark")) {
            runBenchmark(args);
            return;
        }
        
        // Handle plugin listing
        if (Arrays.asList(args).contains("--list-plugins")) {
            FtocUtilityRefactored ftoc = new FtocUtilityRefactored();
            ftoc.initialize();
            System.out.println("Loaded Plugins:");
            System.out.println(ftoc.getPluginSummary());
            return;
        }
        
        FtocUtilityRefactored ftoc = new FtocUtilityRefactored();
        ftoc.initialize();
        
        // Allow plugins to pre-process command line arguments
        if (ftoc.getPluginRegistry() != null) {
            ftoc.getPluginRegistry().triggerEvent(
                com.heymumford.ftoc.plugin.PluginEvent.PRE_PARSE_ARGUMENTS, args);
        }
        
        String directory = ".";
        boolean concordanceOnly = false;
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "-d":
                    if (i + 1 < args.length) {
                        directory = args[++i];
                    }
                    break;
                case "-f":
                case "--format":
                    if (i + 1 < args.length) {
                        String format = args[++i].toLowerCase();
                        ftoc.setOutputFormat(parseFormat(format));
                    }
                    break;
                case "--tags":
                    if (i + 1 < args.length) {
                        String tags = args[++i];
                        for (String tag : tags.split(",")) {
                            ftoc.addIncludeTagFilter(tag.trim());
                        }
                    }
                    break;
                case "--exclude-tags":
                    if (i + 1 < args.length) {
                        String tags = args[++i];
                        for (String tag : tags.split(",")) {
                            ftoc.addExcludeTagFilter(tag.trim());
                        }
                    }
                    break;
                case "--concordance":
                    concordanceOnly = true;
                    break;
                case "--analyze-tags":
                    ftoc.setAnalyzeTagQuality(true);
                    break;
                case "--detect-anti-patterns":
                    ftoc.setDetectAntiPatterns(true);
                    break;
                case "--junit-report":
                    ftoc.setOutputFormat(Reporter.Format.JUNIT_XML);
                    break;
                case "--config-file":
                    if (i + 1 < args.length) {
                        ftoc.setWarningConfigFile(args[++i]);
                    }
                    break;
                case "--show-config":
                    System.out.println(ftoc.getWarningConfigSummary());
                    return;
                case "--performance":
                    ftoc.setPerformanceMonitoring(true);
                    break;
            }
        }
        
        // Create command line context for plugins
        CommandLineContext cmdContext = new CommandLineContext(
            directory, concordanceOnly, ftoc.getOutputFormat(),
            ftoc.includeTagFilters, ftoc.excludeTagFilters,
            ftoc.analyzeTagQuality, ftoc.detectAntiPatterns,
            ftoc.enablePerformanceMonitoring
        );
        
        // Allow plugins to post-process command line arguments
        if (ftoc.getPluginRegistry() != null) {
            ftoc.getPluginRegistry().triggerEvent(
                com.heymumford.ftoc.plugin.PluginEvent.POST_PARSE_ARGUMENTS, cmdContext);
        }
        
        // Process the directory
        ftoc.processDirectory(cmdContext.getDirectory(), cmdContext.isConcordanceOnly());
    }
    
    /**
     * Parse format string to Reporter.Format enum.
     */
    private static Reporter.Format parseFormat(String format) {
        switch (format.toLowerCase()) {
            case "text":
            case "plain":
            case "plaintext":
            case "plain_text":
                return Reporter.Format.PLAIN_TEXT;
            case "md":
            case "markdown":
                return Reporter.Format.MARKDOWN;
            case "html":
                return Reporter.Format.HTML;
            case "json":
                return Reporter.Format.JSON;
            case "junit":
            case "xml":
            case "junit_xml":
            case "junitxml":
                return Reporter.Format.JUNIT_XML;
            default:
                logger.warn("Unknown format: {}. Using plain text format.", format);
                return Reporter.Format.PLAIN_TEXT;
        }
    }
    
    /**
     * Run benchmark tests.
     */
    private static void runBenchmark(String[] args) {
        List<String> argsList = Arrays.asList(args);
        
        // Parse benchmark options
        boolean runSmall = argsList.contains("--small") || argsList.contains("--all");
        boolean runMedium = argsList.contains("--medium") || argsList.contains("--all");
        boolean runLarge = argsList.contains("--large") || argsList.contains("--all");
        boolean runVeryLarge = argsList.contains("--very-large") || argsList.contains("--all");
        boolean noCleanup = argsList.contains("--no-cleanup");
        
        // Default to all if none specified
        if (!runSmall && !runMedium && !runLarge && !runVeryLarge) {
            runSmall = runMedium = runLarge = true;
        }
        
        // Get report file if specified
        String reportFile = "benchmark-report.txt";
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--report")) {
                reportFile = args[i + 1];
                break;
            }
        }
        
        // Run benchmarks
        try {
            Class<?> benchmarkClass = Class.forName("com.heymumford.ftoc.benchmark.BenchmarkRunner");
            benchmarkClass.getMethod("runBenchmarks", boolean.class, boolean.class, boolean.class, boolean.class, 
                    boolean.class, String.class).invoke(null, runSmall, runMedium, runLarge, runVeryLarge, 
                    noCleanup, reportFile);
        } catch (Exception e) {
            logger.error("Error running benchmarks: {}", e.getMessage());
            System.err.println("Error: Failed to run benchmarks - " + e.getMessage());
        }
    }
}