package com.heymumford.ftoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Abstract base class for FTOC utility implementations.
 * This class extracts common functionality shared between legacy and refactored implementations,
 * following the "Extract Superclass" refactoring pattern to eliminate code duplication.
 *
 * @see <a href="https://refactoring.com/catalog/extractSuperclass.html">Extract Superclass Refactoring</a>
 */
public abstract class AbstractFtocUtility {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFtocUtility.class);
    protected static final String VERSION = loadVersion();

    /**
     * Load the version from properties file.
     * This is a template method that can be used by all subclasses.
     *
     * @return The version string, or "unknown" if it cannot be loaded
     */
    protected static String loadVersion() {
        Properties properties = new Properties();
        try (InputStream input = AbstractFtocUtility.class.getClassLoader()
                .getResourceAsStream("ftoc-version.properties")) {
            if (input == null) {
                logger.warn("Version properties file not found");
                return "unknown";
            }
            properties.load(input);
            String version = properties.getProperty("version", "unknown");
            logger.debug("Loaded version: {}", version);
            return version;
        } catch (IOException e) {
            logger.error("Error loading version properties: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * Get the current version of the FTOC utility.
     *
     * @return The version string
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Print help/usage information.
     * This is a template method that provides the standard help output.
     */
    protected static void printHelp() {
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
     * Print version information.
     */
    protected static void printVersion() {
        System.out.println("FTOC Utility version " + VERSION);
    }

    /**
     * Initialize the utility with standard logging output.
     * Subclasses should call this during their initialization.
     */
    protected void initialize() {
        logger.info("FTOC utility version {} initialized.", VERSION);

        // Add JVM info for performance monitoring
        Runtime runtime = Runtime.getRuntime();
        logger.debug("JVM information - Max Memory: {} MB, Available Processors: {}",
                runtime.maxMemory() / (1024 * 1024),
                runtime.availableProcessors());
    }
}
