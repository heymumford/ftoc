# FTOC Benchmark Suite

This document describes the benchmarking capabilities in FTOC for measuring and optimizing performance.

## Overview

The FTOC benchmark suite allows you to:

1. Measure performance across different repository sizes
2. Compare parallel vs. sequential processing
3. Analyze performance of different operations
4. Generate detailed performance reports

These benchmarks are useful for development, optimization, and regression testing.

## Running Benchmarks

You can run benchmarks using the `--benchmark` command-line flag along with various options:

```bash
# Run default benchmarks (small and medium repositories)
java -jar ftoc.jar --benchmark

# Run specific repository size benchmarks
java -jar ftoc.jar --benchmark --small --medium

# Run all benchmarks (warning: can take a long time)
java -jar ftoc.jar --benchmark --all

# Save report to a specific file
java -jar ftoc.jar --benchmark --medium --report benchmark-results.txt

# Keep temporary files for inspection
java -jar ftoc.jar --benchmark --small --no-cleanup
```

## Benchmark Repository Sizes

The benchmark suite includes different repository sizes for testing:

| Size | File Count | Description |
|------|------------|-------------|
| Small | 10 | Good for quick tests and JVM warm-up analysis |
| Medium | 50 | Represents typical project size |
| Large | 200 | Tests scalability with larger codebases |
| Very Large | 500 | Stress test for parallel processing |

## Benchmark Scenarios

Each benchmark runs a set of scenarios that test different aspects of FTOC:

1. **Basic TOC** - Simple table of contents generation
2. **Concordance** - Tag concordance report generation
3. **Tag Quality** - Tag quality analysis with warnings
4. **Anti-Patterns** - Feature file anti-pattern detection
5. **All Reports** - Complete analysis with all features enabled

## Sample Report

A benchmark report looks like this:

```
FTOC BENCHMARK REPORT
====================

Small repository (10 feature files)
----------------------------------

Scenario             Sequential    Parallel      Improvement  
------------------------------------------------------------
BASIC_TOC            876ms         712ms         18.7%        
CONCORDANCE          543ms         478ms         12.0%        
TAG_QUALITY          1243ms        978ms         21.3%        
ANTI_PATTERNS        897ms         743ms         17.2%        
ALL_REPORTS          2345ms        1780ms        24.1%        

Medium repository (50 feature files)
-----------------------------------

Scenario             Sequential    Parallel      Improvement  
------------------------------------------------------------
BASIC_TOC            3421ms        1587ms        53.6%        
CONCORDANCE          2876ms        1232ms        57.2%        
TAG_QUALITY          5623ms        2345ms        58.3%        
```

## Running Benchmarks via JUnit

For developers, benchmarks can also be run as JUnit tests:

```java
@Test
@Tag("benchmark")
public void testLargeRepository() {
    BenchmarkRunner benchmarkRunner = new BenchmarkRunner();
    benchmarkRunner.runAllScenarios(BenchmarkRunner.RepositorySize.LARGE);
    String report = benchmarkRunner.generateReport();
    System.out.println(report);
}
```

These tests are disabled by default as they can take a long time to run.

## Implementation Notes

- Benchmarks create temporary feature files with realistic content
- Each file varies in complexity to simulate real-world scenarios
- Files are automatically cleaned up after benchmarks complete
- Both parallel and sequential execution are tested for comparison
- Memory usage is tracked when performance monitoring is enabled