package com.heymumford.ftoc.benchmark;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * JUnit test class for running FTOC benchmarks.
 * These tests are disabled by default as they can take a long time to run.
 * Enable them selectively when you want to run benchmarks.
 */
@Disabled("Benchmarks are disabled by default; enable selectively when needed")
public class BenchmarkTest {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);
    private static BenchmarkRunner benchmarkRunner;
    private static final String REPORT_FILE = "target/benchmark-report.txt";

    @BeforeAll
    public static void setup() {
        // Create a benchmark runner that doesn't clean up files until the end
        benchmarkRunner = new BenchmarkRunner()
                .setCleanupTempFiles(false);
    }

    @AfterAll
    public static void teardown() throws IOException {
        if (benchmarkRunner != null) {
            // Generate and save the report
            String report = benchmarkRunner.generateReport();
            logger.info("Benchmark Report:\n{}", report);
            
            // Write report to file
            benchmarkRunner.writeReportToFile(REPORT_FILE);
            logger.info("Benchmark report written to: {}", 
                    Paths.get(REPORT_FILE).toAbsolutePath());
            
            // Clean up temporary files
            benchmarkRunner.cleanup();
        }
    }

    @Test
    @Tag("benchmark")
    @Tag("small")
    public void testSmallRepository() {
        logger.info("Running small repository benchmarks");
        
        // Run all scenarios on small repository
        benchmarkRunner.runAllScenarios(BenchmarkRunner.RepositorySize.SMALL);
    }

    @Test
    @Tag("benchmark")
    @Tag("medium")
    public void testMediumRepository() {
        logger.info("Running medium repository benchmarks");
        
        // Run basic benchmarks on medium repository
        benchmarkRunner.runComparisonBenchmark(
                BenchmarkRunner.RepositorySize.MEDIUM, 
                BenchmarkRunner.BenchmarkScenario.BASIC_TOC);
        
        benchmarkRunner.runComparisonBenchmark(
                BenchmarkRunner.RepositorySize.MEDIUM, 
                BenchmarkRunner.BenchmarkScenario.CONCORDANCE);
        
        benchmarkRunner.runComparisonBenchmark(
                BenchmarkRunner.RepositorySize.MEDIUM, 
                BenchmarkRunner.BenchmarkScenario.TAG_QUALITY);
    }

    @Test
    @Tag("benchmark")
    @Tag("large")
    public void testLargeRepository() {
        logger.info("Running large repository benchmarks");
        
        // Run only basic benchmarks on large repository
        benchmarkRunner.runComparisonBenchmark(
                BenchmarkRunner.RepositorySize.LARGE, 
                BenchmarkRunner.BenchmarkScenario.BASIC_TOC);
        
        benchmarkRunner.runComparisonBenchmark(
                BenchmarkRunner.RepositorySize.LARGE, 
                BenchmarkRunner.BenchmarkScenario.CONCORDANCE);
    }

    @Test
    @Tag("benchmark")
    @Tag("very-large")
    public void testVeryLargeRepository() {
        logger.info("Running very large repository benchmarks");
        
        // Run only basic TOC generation on very large repository
        benchmarkRunner.runComparisonBenchmark(
                BenchmarkRunner.RepositorySize.VERY_LARGE, 
                BenchmarkRunner.BenchmarkScenario.BASIC_TOC);
    }

    @Test
    @Tag("benchmark")
    @Tag("jvm-warmup")
    public void testJvmWarmupEffects() {
        logger.info("Testing JVM warm-up effects");
        
        // Run the same benchmark multiple times to see JVM warm-up effects
        BenchmarkRunner.RepositorySize size = BenchmarkRunner.RepositorySize.SMALL;
        BenchmarkRunner.BenchmarkScenario scenario = BenchmarkRunner.BenchmarkScenario.BASIC_TOC;
        
        // Run 5 times to see the effect of JVM warm-up
        for (int i = 0; i < 5; i++) {
            logger.info("Warm-up run #{}", i+1);
            benchmarkRunner.runBenchmark(size, scenario);
        }
    }

    @Test
    @Tag("benchmark")
    @Tag("memory-usage")
    public void testMemoryUsage() {
        logger.info("Testing memory usage across different configurations");
        
        // Test with sequential processing
        benchmarkRunner.runBenchmark(
                BenchmarkRunner.RepositorySize.MEDIUM, 
                BenchmarkRunner.BenchmarkScenario.ALL_REPORTS,
                false, true);
        
        // Test with parallel processing
        benchmarkRunner.runBenchmark(
                BenchmarkRunner.RepositorySize.MEDIUM, 
                BenchmarkRunner.BenchmarkScenario.ALL_REPORTS,
                true, true);
    }
}