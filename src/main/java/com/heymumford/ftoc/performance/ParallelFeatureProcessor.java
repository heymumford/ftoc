package com.heymumford.ftoc.performance;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.parser.FeatureParser;
import com.heymumford.ftoc.parser.FeatureParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Process feature files in parallel for improved performance with large repositories.
 */
public class ParallelFeatureProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ParallelFeatureProcessor.class);
    
    // Default thread pool properties
    private static final int DEFAULT_THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    private static final int MAX_THREAD_COUNT = 16;
    private static final long DEFAULT_TIMEOUT_SECONDS = 300; // 5 minutes
    
    private final ExecutorService executorService;
    private final int threadCount;
    private final long timeoutSeconds;
    private final ProgressTracker progressTracker;
    
    /**
     * Create a new parallel feature processor with default settings.
     */
    public ParallelFeatureProcessor() {
        this(DEFAULT_THREAD_COUNT, DEFAULT_TIMEOUT_SECONDS);
    }
    
    /**
     * Create a new parallel feature processor with custom settings.
     *
     * @param threadCount The number of threads to use
     * @param timeoutSeconds The timeout in seconds for processing operations
     */
    public ParallelFeatureProcessor(int threadCount, long timeoutSeconds) {
        this.threadCount = Math.min(Math.max(1, threadCount), MAX_THREAD_COUNT);
        this.timeoutSeconds = timeoutSeconds;
        this.executorService = Executors.newFixedThreadPool(this.threadCount);
        this.progressTracker = new ProgressTracker();
        
        logger.info("Initialized parallel processor with {} threads and {} second timeout", 
                this.threadCount, this.timeoutSeconds);
    }
    
    /**
     * Process a list of feature files in parallel.
     *
     * @param featureFiles The list of feature files to process
     * @param onProgressUpdate Function to call with progress updates (0-100)
     * @return A list of parsed Feature objects
     * @throws ExecutionException If an error occurs during processing
     * @throws InterruptedException If the operation is interrupted
     * @throws TimeoutException If the operation times out
     */
    public List<Feature> processFeatureFiles(List<File> featureFiles, Consumer<Integer> onProgressUpdate) 
            throws ExecutionException, InterruptedException, TimeoutException {
        
        if (featureFiles.isEmpty()) {
            logger.warn("No feature files to process");
            return Collections.emptyList();
        }
        
        logger.info("Processing {} feature files in parallel with {} threads", 
                featureFiles.size(), threadCount);
        
        List<Feature> results = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();
        
        // Reset and configure progress tracker
        progressTracker.reset(featureFiles.size());
        progressTracker.setProgressCallback(onProgressUpdate);
        
        PerformanceMonitor.startOperation("parallel_feature_processing");
        
        // Submit tasks to the executor service
        for (File file : featureFiles) {
            futures.add(executorService.submit(() -> {
                try {
                    FeatureParser parser = FeatureParserFactory.getParser(file);
                    Feature feature = parser.parseFeatureFile(file);
                    results.add(feature);
                    progressTracker.incrementProgress();
                } catch (Exception e) {
                    logger.error("Error processing feature file {}: {}", file.getName(), e.getMessage());
                    throw new RuntimeException(e);
                }
            }));
        }
        
        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                logger.error("Error during parallel processing: {}", e.getMessage());
                cancelAllTasks(futures);
                throw e;
            }
        }
        
        long duration = PerformanceMonitor.endOperation("parallel_feature_processing");
        
        logger.info("Parallel processing completed: {} files processed in {} ms (avg {} ms/file)", 
                results.size(), duration, results.isEmpty() ? 0 : duration / results.size());
        
        return results;
    }
    
    /**
     * Cancel all pending tasks.
     *
     * @param futures The list of futures to cancel
     */
    private void cancelAllTasks(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }
    
    /**
     * Shutdown the executor service.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("Executor service did not terminate in the specified time.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Shutdown interrupted: {}", e.getMessage());
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Utility class to track progress of parallel operations.
     */
    private static class ProgressTracker {
        private final AtomicInteger completedItems = new AtomicInteger(0);
        private int totalItems = 0;
        private Consumer<Integer> progressCallback = null;
        
        /**
         * Reset the progress tracker.
         *
         * @param totalItems The total number of items to process
         */
        public void reset(int totalItems) {
            this.totalItems = totalItems;
            this.completedItems.set(0);
        }
        
        /**
         * Set a callback function to report progress.
         *
         * @param callback The function to call with progress updates
         */
        public void setProgressCallback(Consumer<Integer> callback) {
            this.progressCallback = callback;
        }
        
        /**
         * Increment the completed count and report progress.
         */
        public void incrementProgress() {
            int completed = completedItems.incrementAndGet();
            int percentComplete = (int) ((completed / (double) totalItems) * 100);
            
            if (progressCallback != null) {
                progressCallback.accept(percentComplete);
            }
            
            // Log progress at 25%, 50%, 75%, and 100%
            if (percentComplete % 25 == 0 && completed > 0) {
                logger.info("Processing progress: {}% ({}/{})", 
                        percentComplete, completed, totalItems);
            }
        }
    }
}