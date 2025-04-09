package com.heymumford.ftoc.benchmark;

import com.heymumford.ftoc.FtocUtility;
import com.heymumford.ftoc.performance.PerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A benchmark test suite for FTOC to measure performance across different repository sizes and configurations.
 * This class handles running benchmarks, measuring performance metrics, and reporting results.
 */
public class BenchmarkRunner {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);

    // Repository size categories
    public enum RepositorySize {
        SMALL(10, "Small repository (10 feature files)"),
        MEDIUM(50, "Medium repository (50 feature files)"),
        LARGE(200, "Large repository (200 feature files)"),
        VERY_LARGE(500, "Very large repository (500 feature files)");

        private final int fileCount;
        private final String description;

        RepositorySize(int fileCount, String description) {
            this.fileCount = fileCount;
            this.description = description;
        }

        public int getFileCount() {
            return fileCount;
        }

        public String getDescription() {
            return description;
        }
    }

    // Test scenarios to run
    public enum BenchmarkScenario {
        BASIC_TOC("Basic table of contents generation"),
        CONCORDANCE("Concordance report generation"),
        TAG_QUALITY("Tag quality analysis"),
        ANTI_PATTERNS("Anti-pattern detection"),
        ALL_REPORTS("All reports combined");

        private final String description;

        BenchmarkScenario(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final Path benchmarkTempDir;
    private final Map<String, BenchmarkResult> results = new HashMap<>();
    private boolean cleanupTempFiles = true;

    /**
     * Create a new benchmark runner.
     */
    public BenchmarkRunner() {
        try {
            // Create a temporary directory for benchmark files
            this.benchmarkTempDir = Files.createTempDirectory("ftoc-benchmark-");
            logger.info("Created benchmark temp directory: {}", benchmarkTempDir);
        } catch (IOException e) {
            logger.error("Failed to create temp directory: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize benchmark runner", e);
        }
    }

    /**
     * Set whether to clean up temporary files after benchmarks complete.
     * 
     * @param cleanup If true, temp files will be removed (default); if false, they will be kept
     * @return This benchmark runner (for method chaining)
     */
    public BenchmarkRunner setCleanupTempFiles(boolean cleanup) {
        this.cleanupTempFiles = cleanup;
        return this;
    }

    /**
     * Run all benchmark scenarios on the specified repository size.
     * 
     * @param size The repository size to benchmark
     * @return This benchmark runner (for method chaining)
     */
    public BenchmarkRunner runAllScenarios(RepositorySize size) {
        for (BenchmarkScenario scenario : BenchmarkScenario.values()) {
            runBenchmark(size, scenario);
        }
        return this;
    }

    /**
     * Run a specific benchmark scenario on a repository of the specified size.
     * 
     * @param size The repository size to benchmark
     * @param scenario The benchmark scenario to run
     * @return This benchmark runner (for method chaining)
     */
    public BenchmarkRunner runBenchmark(RepositorySize size, BenchmarkScenario scenario) {
        return runBenchmark(size, scenario, true, true);
    }

    /**
     * Run a specific benchmark scenario with customizable parallel and performance options.
     * 
     * @param size The repository size to benchmark
     * @param scenario The benchmark scenario to run
     * @param useParallel Whether to enable parallel processing
     * @param enablePerformanceMonitoring Whether to enable performance monitoring
     * @return This benchmark runner (for method chaining)
     */
    public BenchmarkRunner runBenchmark(
            RepositorySize size, 
            BenchmarkScenario scenario, 
            boolean useParallel,
            boolean enablePerformanceMonitoring) {
        
        String testId = size.name() + "_" + scenario.name() + "_" + (useParallel ? "PARALLEL" : "SEQUENTIAL");
        logger.info("Running benchmark: {} - {}", testId, scenario.getDescription());
        
        try {
            // Create test directory for this benchmark
            Path testDir = benchmarkTempDir.resolve(testId);
            Files.createDirectories(testDir);
            
            // Generate test feature files
            generateFeatureFiles(testDir, size.getFileCount());
            
            // Run the benchmark with appropriate configuration
            long startTime = System.nanoTime();
            
            FtocUtility ftoc = new FtocUtility();
            ftoc.initialize();
            
            // Configure the utility based on the scenario
            ftoc.setPerformanceMonitoring(enablePerformanceMonitoring);
            
            switch (scenario) {
                case BASIC_TOC:
                    // Default configuration is fine
                    break;
                case CONCORDANCE:
                    // Only generate concordance report
                    ftoc.processDirectory(testDir.toString(), true);
                    break;
                case TAG_QUALITY:
                    ftoc.setAnalyzeTagQuality(true);
                    break;
                case ANTI_PATTERNS:
                    ftoc.setDetectAntiPatterns(true);
                    break;
                case ALL_REPORTS:
                    ftoc.setAnalyzeTagQuality(true);
                    ftoc.setDetectAntiPatterns(true);
                    break;
            }
            
            // Process the directory (skipping concordance-only which was handled above)
            if (scenario != BenchmarkScenario.CONCORDANCE) {
                ftoc.processDirectory(testDir.toString());
            }
            
            long endTime = System.nanoTime();
            long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            
            // Create benchmark result
            BenchmarkResult result = new BenchmarkResult(
                    testId,
                    size,
                    scenario,
                    useParallel,
                    durationMillis);
            
            // Add performance metrics if available
            if (enablePerformanceMonitoring) {
                result.setPerformanceReport(PerformanceMonitor.getSummary());
            }
            
            // Store the result
            results.put(testId, result);
            
            logger.info("Benchmark completed: {} - Execution time: {} ms", testId, durationMillis);
            
        } catch (Exception e) {
            logger.error("Benchmark failed: {} - {}", testId, e.getMessage());
        }
        
        return this;
    }
    
    /**
     * Run the same benchmark with both parallel and sequential processing for comparison.
     * 
     * @param size The repository size to benchmark
     * @param scenario The benchmark scenario to run
     * @return This benchmark runner (for method chaining)
     */
    public BenchmarkRunner runComparisonBenchmark(RepositorySize size, BenchmarkScenario scenario) {
        // Run with parallel processing
        runBenchmark(size, scenario, true, true);
        
        // Run with sequential processing
        runBenchmark(size, scenario, false, true);
        
        return this;
    }

    /**
     * Generate test feature files for benchmarking.
     * 
     * @param directory The directory to create files in
     * @param count The number of feature files to generate
     */
    private void generateFeatureFiles(Path directory, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            String fileName = String.format("test_%03d.feature", i);
            Path filePath = directory.resolve(fileName);
            
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                // Generate feature file with randomized content
                writer.write(generateFeatureFileContent(i));
            }
        }
        logger.debug("Generated {} feature files in {}", count, directory);
    }

    /**
     * Generate content for a test feature file with varied complexity.
     * 
     * @param index The index of the file being generated
     * @return A string containing the feature file content
     */
    private String generateFeatureFileContent(int index) {
        StringBuilder content = new StringBuilder();
        
        // Create a feature with tags
        content.append("@test @benchmark");
        
        // Add varied tags to create complexity
        if (index % 4 == 0) content.append(" @p0 @critical");
        else if (index % 4 == 1) content.append(" @p1 @high");
        else if (index % 4 == 2) content.append(" @p2 @medium @api");
        else content.append(" @p3 @low @ui");
        
        // Add some more tags for variety
        if (index % 7 == 0) content.append(" @smoke");
        if (index % 5 == 0) content.append(" @regression");
        if (index % 11 == 0) content.append(" @flaky");
        
        content.append("\nFeature: Test feature ").append(index).append(" for benchmarking\n");
        content.append("  As a tester\n");
        content.append("  I want to benchmark FTOC performance\n");
        content.append("  So that I can optimize it\n\n");
        
        // Create 3-7 scenarios per feature
        int scenarioCount = 3 + (index % 5);
        for (int i = 0; i < scenarioCount; i++) {
            generateScenario(content, index, i);
            content.append("\n");
        }
        
        return content.toString();
    }

    /**
     * Generate a scenario within a feature file.
     * 
     * @param content The StringBuilder to append to
     * @param featureIndex The index of the feature
     * @param scenarioIndex The index of the scenario within the feature
     */
    private void generateScenario(StringBuilder content, int featureIndex, int scenarioIndex) {
        // Add tags to the scenario 
        content.append("  @scenario_").append(scenarioIndex);
        
        // Add varied tags for complexity
        if (scenarioIndex % 3 == 0) content.append(" @ui");
        else if (scenarioIndex % 3 == 1) content.append(" @api");
        else content.append(" @integration");
        
        // Add a unique tracking tag
        content.append(" @id_").append(UUID.randomUUID().toString().substring(0, 8));
        
        // Basic or outline scenario
        if (scenarioIndex % 5 == 0) {
            content.append("\n  Scenario Outline: Parameterized test <param> for benchmark\n");
        } else {
            content.append("\n  Scenario: Test scenario ").append(scenarioIndex).append(" for benchmark\n");
        }
        
        // Add steps
        content.append("    Given I am benchmarking FTOC\n");
        content.append("    When I run test ").append(featureIndex).append("-").append(scenarioIndex).append("\n");
        
        // Add some conditional steps for variety
        if (scenarioIndex % 2 == 0) {
            content.append("    And I set the parallel processing flag\n");
        }
        if (scenarioIndex % 3 == 0) {
            content.append("    And I enable performance monitoring\n");
        }
        
        // Always add a verification step
        content.append("    Then the execution should complete successfully\n");
        
        // Add example table for scenario outlines
        if (scenarioIndex % 5 == 0) {
            content.append("\n    Examples:\n");
            content.append("    | param    |\n");
            content.append("    | small    |\n");
            content.append("    | medium   |\n");
            content.append("    | large    |\n");
        }
    }

    /**
     * Generate a benchmark report with all results.
     * 
     * @return A formatted string containing the benchmark results
     */
    public String generateReport() {
        if (results.isEmpty()) {
            return "No benchmark results available.";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("FTOC BENCHMARK REPORT\n");
        report.append("====================\n\n");
        
        // Group results by repository size
        for (RepositorySize size : RepositorySize.values()) {
            boolean hasSizeResults = false;
            
            for (BenchmarkResult result : results.values()) {
                if (result.getRepositorySize() == size) {
                    hasSizeResults = true;
                    break;
                }
            }
            
            if (!hasSizeResults) {
                continue;
            }
            
            report.append(size.getDescription()).append("\n");
            report.append("-".repeat(size.getDescription().length())).append("\n\n");
            
            // Create a comparison table header
            report.append(String.format("%-20s %-12s %-12s %-12s\n", 
                    "Scenario", "Sequential", "Parallel", "Improvement"));
            report.append("-".repeat(60)).append("\n");
            
            // Add data for each scenario
            for (BenchmarkScenario scenario : BenchmarkScenario.values()) {
                // Find sequential and parallel results
                BenchmarkResult seqResult = findResult(size, scenario, false);
                BenchmarkResult parResult = findResult(size, scenario, true);
                
                if (seqResult == null && parResult == null) {
                    continue;
                }
                
                // Calculate improvement percentage
                String improvement = "N/A";
                if (seqResult != null && parResult != null && seqResult.getDurationMillis() > 0) {
                    double percent = 100.0 * (1 - (double)parResult.getDurationMillis() / seqResult.getDurationMillis());
                    improvement = String.format("%.1f%%", percent);
                }
                
                // Add row to table
                report.append(String.format("%-20s %-12s %-12s %-12s\n",
                        scenario.name(),
                        seqResult != null ? seqResult.getDurationMillis() + "ms" : "N/A",
                        parResult != null ? parResult.getDurationMillis() + "ms" : "N/A",
                        improvement));
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Find a specific benchmark result.
     * 
     * @param size The repository size
     * @param scenario The benchmark scenario
     * @param parallel Whether to find the parallel or sequential result
     * @return The matching benchmark result, or null if not found
     */
    private BenchmarkResult findResult(RepositorySize size, BenchmarkScenario scenario, boolean parallel) {
        String testId = size.name() + "_" + scenario.name() + "_" + (parallel ? "PARALLEL" : "SEQUENTIAL");
        return results.get(testId);
    }

    /**
     * Write the benchmark report to a file.
     * 
     * @param filePath The path to write the report to
     * @throws IOException If an error occurs writing the file
     */
    public void writeReportToFile(String filePath) throws IOException {
        String report = generateReport();
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(report);
        }
        logger.info("Benchmark report written to: {}", filePath);
    }
    
    /**
     * Cleanup temporary files used for benchmarking.
     */
    public void cleanup() {
        if (!cleanupTempFiles) {
            logger.info("Skipping cleanup of temp directory: {}", benchmarkTempDir);
            return;
        }
        
        try {
            Files.walk(benchmarkTempDir)
                .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
                .map(Path::toFile)
                .forEach(File::delete);
            
            logger.info("Cleaned up benchmark temp directory: {}", benchmarkTempDir);
        } catch (IOException e) {
            logger.warn("Failed to clean up temp directory: {}", e.getMessage());
        }
    }

    /**
     * Class to hold benchmark result data.
     */
    public static class BenchmarkResult {
        private final String id;
        private final RepositorySize repositorySize;
        private final BenchmarkScenario scenario;
        private final boolean parallel;
        private final long durationMillis;
        private String performanceReport;
        
        public BenchmarkResult(String id, RepositorySize repositorySize, BenchmarkScenario scenario, 
                              boolean parallel, long durationMillis) {
            this.id = id;
            this.repositorySize = repositorySize;
            this.scenario = scenario;
            this.parallel = parallel;
            this.durationMillis = durationMillis;
        }
        
        public String getId() {
            return id;
        }
        
        public RepositorySize getRepositorySize() {
            return repositorySize;
        }
        
        public BenchmarkScenario getScenario() {
            return scenario;
        }
        
        public boolean isParallel() {
            return parallel;
        }
        
        public long getDurationMillis() {
            return durationMillis;
        }
        
        public String getPerformanceReport() {
            return performanceReport;
        }
        
        public void setPerformanceReport(String performanceReport) {
            this.performanceReport = performanceReport;
        }
    }
    
    /**
     * Main method to run benchmarks directly.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Parse command line args
        boolean runSmall = false;
        boolean runMedium = false;
        boolean runLarge = false;
        boolean runVeryLarge = false;
        String reportFile = "benchmark-report.txt";
        boolean cleanup = true;
        
        if (args.length == 0) {
            runSmall = true;
            runMedium = true;
        }
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--small":
                    runSmall = true;
                    break;
                case "--medium":
                    runMedium = true;
                    break;
                case "--large":
                    runLarge = true;
                    break;
                case "--very-large":
                    runVeryLarge = true;
                    break;
                case "--all":
                    runSmall = true;
                    runMedium = true;
                    runLarge = true;
                    runVeryLarge = true;
                    break;
                case "--report":
                    if (i + 1 < args.length) {
                        reportFile = args[++i];
                    }
                    break;
                case "--no-cleanup":
                    cleanup = false;
                    break;
                case "--help":
                    printUsage();
                    return;
            }
        }
        
        // Create benchmark runner
        BenchmarkRunner runner = new BenchmarkRunner()
                .setCleanupTempFiles(cleanup);
        
        try {
            // Run benchmarks based on selected sizes
            if (runSmall) {
                System.out.println("Running small repository benchmarks...");
                runner.runComparisonBenchmark(RepositorySize.SMALL, BenchmarkScenario.BASIC_TOC);
                runner.runComparisonBenchmark(RepositorySize.SMALL, BenchmarkScenario.CONCORDANCE);
                runner.runComparisonBenchmark(RepositorySize.SMALL, BenchmarkScenario.TAG_QUALITY);
            }
            
            if (runMedium) {
                System.out.println("Running medium repository benchmarks...");
                runner.runComparisonBenchmark(RepositorySize.MEDIUM, BenchmarkScenario.BASIC_TOC);
                runner.runComparisonBenchmark(RepositorySize.MEDIUM, BenchmarkScenario.CONCORDANCE);
                runner.runComparisonBenchmark(RepositorySize.MEDIUM, BenchmarkScenario.TAG_QUALITY);
            }
            
            if (runLarge) {
                System.out.println("Running large repository benchmarks...");
                runner.runComparisonBenchmark(RepositorySize.LARGE, BenchmarkScenario.BASIC_TOC);
                runner.runComparisonBenchmark(RepositorySize.LARGE, BenchmarkScenario.CONCORDANCE);
            }
            
            if (runVeryLarge) {
                System.out.println("Running very large repository benchmarks...");
                runner.runComparisonBenchmark(RepositorySize.VERY_LARGE, BenchmarkScenario.BASIC_TOC);
            }
            
            // Generate and print report
            String report = runner.generateReport();
            System.out.println("\n" + report);
            
            // Write report to file
            runner.writeReportToFile(reportFile);
            System.out.println("Report saved to " + reportFile);
            
            // Cleanup temp files
            runner.cleanup();
            
        } catch (Exception e) {
            System.err.println("Benchmark failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printUsage() {
        System.out.println("FTOC Benchmark Tool");
        System.out.println("Usage: java -cp ftoc.jar com.heymumford.ftoc.benchmark.BenchmarkRunner [options]");
        System.out.println("Options:");
        System.out.println("  --small       Run benchmarks on small repositories (10 files)");
        System.out.println("  --medium      Run benchmarks on medium repositories (50 files)");
        System.out.println("  --large       Run benchmarks on large repositories (200 files)");
        System.out.println("  --very-large  Run benchmarks on very large repositories (500 files)");
        System.out.println("  --all         Run benchmarks on all repository sizes");
        System.out.println("  --report <file> Specify report output file (default: benchmark-report.txt)");
        System.out.println("  --no-cleanup  Do not delete temporary files after benchmark");
        System.out.println("  --help        Display this help message");
        System.out.println("\nDefault: --small --medium");
    }
}