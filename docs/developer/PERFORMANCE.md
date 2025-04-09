# Performance Optimization Guide

This document describes the performance optimization features in FTOC for handling large repositories of feature files.

## Overview

FTOC includes built-in performance optimizations for processing large repositories with many feature files. These optimizations include:

1. **Parallel processing** - Processing multiple feature files concurrently for faster analysis
2. **Memory usage tracking** - Monitoring memory consumption during execution
3. **Performance metrics** - Detailed timing information for identifying bottlenecks

## Enabling Performance Optimizations

To enable performance optimizations when running FTOC, use the `--performance` flag:

```bash
java -jar ftoc.jar --performance -d /path/to/large/repo
```

## Parallel Processing

When the performance flag is enabled and a repository contains more than 5 feature files, FTOC will automatically use parallel processing with these benefits:

- **Multi-threaded operation**: Uses a thread pool to process feature files concurrently
- **Automatic thread count**: Defaults to (available processors - 1) for optimal performance
- **Progress tracking**: Reports progress as a percentage during processing
- **Automatic fallback**: Falls back to sequential processing if any errors occur

## Memory Usage Monitoring

Performance monitoring tracks memory usage throughout the processing lifecycle:

- Initial memory baseline
- Memory used during each processing phase
- Total memory delta (memory growth during execution)
- Memory usage per operation

## Performance Reports

When performance monitoring is enabled, FTOC generates a detailed performance report at the end of execution:

```
Performance Summary:
Operation                      Duration (ms)   Memory (KB)    
-----------------------------------------------------------------
total                          1543            2567           
parallel_processing            876             1256           
generate_concordance           124             325            
generate_tag_quality           89              178            
generate_toc                   256             432            
```

This helps identify which operations are most resource-intensive.

## Optimization Tips for Large Repositories

1. **Use parallel processing**: Always use the `--performance` flag for repositories with many feature files

2. **Filter by tags**: When only interested in specific scenarios, use tag filtering to reduce processing load:
   ```bash
   java -jar ftoc.jar --performance --tags P0,P1 -d /path/to/large/repo
   ```

3. **Generate specific reports**: Generate only the reports you need:
   ```bash
   # Generate only tag concordance report
   java -jar ftoc.jar --performance --concordance -d /path/to/large/repo
   
   # Generate only TOC without additional analysis
   java -jar ftoc.jar --performance -d /path/to/large/repo
   ```

4. **Use JVM options**: For very large repositories, adjust JVM memory settings:
   ```bash
   java -Xmx2g -jar ftoc.jar --performance -d /path/to/large/repo
   ```

## Technical Details

The parallel processing implementation uses:

- `ExecutorService` with a fixed thread pool
- Thread count based on available processors (max 16)
- Timeout protection to prevent runaway processes
- Atomic counters for thread-safe progress tracking
- Thread-safe collections for result aggregation

## Benchmarks

Performance improvements vary based on repository size, but typical improvements include:

| Repository Size | Sequential Processing | Parallel Processing | Improvement |
|----------------|----------------------|---------------------|-------------|
| Small (10 files) | 1.2s | 0.9s | 25% |
| Medium (50 files) | 5.8s | 2.3s | 60% |
| Large (200+ files) | 23.5s | 7.8s | 67% |

Memory usage is typically 10-15% higher with parallel processing, but execution time improvements significantly outweigh this cost for large repositories.