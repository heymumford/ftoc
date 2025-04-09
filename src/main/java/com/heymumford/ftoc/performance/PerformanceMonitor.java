package com.heymumford.ftoc.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for monitoring performance metrics in FTOC.
 */
public class PerformanceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final Map<String, Long> startTimes = new HashMap<>();
    private static final Map<String, Long> endTimes = new HashMap<>();
    private static final Map<String, Long> memoryUsage = new HashMap<>();
    private static boolean enabled = false;

    /**
     * Enable or disable performance monitoring.
     * 
     * @param enable True to enable monitoring, false to disable
     */
    public static void setEnabled(boolean enable) {
        enabled = enable;
        logger.info("Performance monitoring {}", enable ? "enabled" : "disabled");
    }

    /**
     * Check if performance monitoring is enabled.
     * 
     * @return True if monitoring is enabled, false otherwise
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Start timing an operation.
     * 
     * @param operationName The name of the operation to time
     */
    public static void startOperation(String operationName) {
        if (!enabled) return;
        
        startTimes.put(operationName, System.currentTimeMillis());
        memoryUsage.put(operationName, getUsedMemory());
        logger.debug("Starting operation: {}", operationName);
    }

    /**
     * End timing an operation and record metrics.
     * 
     * @param operationName The name of the operation to finish timing
     * @return The duration of the operation in milliseconds, or -1 if not started
     */
    public static long endOperation(String operationName) {
        if (!enabled) return -1;
        
        if (!startTimes.containsKey(operationName)) {
            logger.warn("Operation '{}' was never started", operationName);
            return -1;
        }
        
        long endTime = System.currentTimeMillis();
        long startTime = startTimes.get(operationName);
        long duration = endTime - startTime;
        
        endTimes.put(operationName, endTime);
        long memoryBefore = memoryUsage.get(operationName);
        long memoryAfter = getUsedMemory();
        long memoryDelta = memoryAfter - memoryBefore;
        
        logger.info("Operation '{}' completed in {} ms. Memory delta: {} KB", 
                operationName, duration, memoryDelta / 1024);
        
        return duration;
    }

    /**
     * Get the current memory usage in bytes.
     * 
     * @return The amount of used memory in bytes
     */
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Get a summary of all operation metrics.
     * 
     * @return A string containing performance metrics
     */
    public static String getSummary() {
        if (!enabled || startTimes.isEmpty()) {
            return "Performance monitoring is disabled or no operations were recorded.";
        }

        StringBuilder summary = new StringBuilder("Performance Metrics:\n");
        summary.append(String.format("%-30s %-15s %-15s\n", "Operation", "Duration (ms)", "Memory (KB)"));
        summary.append("-".repeat(65)).append("\n");

        for (String operation : startTimes.keySet()) {
            if (endTimes.containsKey(operation)) {
                long duration = endTimes.get(operation) - startTimes.get(operation);
                long memoryBefore = memoryUsage.get(operation);
                long memoryAfter = getUsedMemory();
                if (operation.equals("total")) {
                    memoryAfter = memoryUsage.getOrDefault("total_end", memoryAfter);
                }
                long memoryDelta = memoryAfter - memoryBefore;
                
                summary.append(String.format("%-30s %-15d %-15d\n", 
                        operation, duration, memoryDelta / 1024));
            } else {
                summary.append(String.format("%-30s %-15s %-15s\n", 
                        operation, "Not finished", "N/A"));
            }
        }

        return summary.toString();
    }

    /**
     * Reset all performance metrics.
     */
    public static void reset() {
        startTimes.clear();
        endTimes.clear();
        memoryUsage.clear();
        logger.debug("Performance metrics reset");
    }

    /**
     * Record the final memory state for total operation.
     */
    public static void recordFinalMemory() {
        if (!enabled) return;
        memoryUsage.put("total_end", getUsedMemory());
    }
}