package com.heymumford.ftoc.core.impl;

import com.heymumford.ftoc.core.FeatureProcessor;
import com.heymumford.ftoc.core.FeatureRepository;
import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.performance.ParallelFeatureProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of FeatureProcessor interface.
 * This class is responsible for analyzing and processing Feature objects.
 */
public class DefaultFeatureProcessor implements FeatureProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFeatureProcessor.class);
    private static final int PARALLEL_PROCESSING_THRESHOLD = 5;
    
    private final FeatureRepository repository;
    private boolean performanceMonitoringEnabled;
    
    /**
     * Create a new feature processor with the given repository.
     * 
     * @param repository The feature repository to use for loading features
     */
    public DefaultFeatureProcessor(FeatureRepository repository) {
        this.repository = repository;
        this.performanceMonitoringEnabled = false;
    }
    
    /**
     * Enable or disable performance monitoring.
     * This affects whether parallel processing is used.
     * 
     * @param enabled Whether performance monitoring is enabled
     */
    public void setPerformanceMonitoringEnabled(boolean enabled) {
        this.performanceMonitoringEnabled = enabled;
        
        if (enabled) {
            com.heymumford.ftoc.performance.PerformanceMonitor.setEnabled(true);
        }
    }

    /**
     * Analyze features to extract tag concordance information.
     * 
     * @param features List of features to analyze
     * @return Map of tags to occurrence counts
     */
    @Override
    public Map<String, Integer> generateTagConcordance(List<Feature> features) {
        Map<String, Integer> tagConcordance = new HashMap<>();
        
        for (Feature feature : features) {
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
        }
        
        logger.debug("Generated tag concordance with {} unique tags", tagConcordance.size());
        return tagConcordance;
    }

    /**
     * Filter features based on tag inclusion/exclusion criteria.
     * 
     * @param features List of features to filter
     * @param includeTags Tags to include (features must have at least one)
     * @param excludeTags Tags to exclude (features must have none)
     * @return Filtered list of features
     */
    @Override
    public List<Feature> filterFeaturesByTags(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        // If no filters are set, return all features
        if (includeTags.isEmpty() && excludeTags.isEmpty()) {
            return new ArrayList<>(features);
        }
        
        return features.stream()
                .filter(feature -> {
                    // Check exclude tags first - if any match, exclude the feature
                    if (!excludeTags.isEmpty()) {
                        boolean hasExcludedTag = feature.getTags().stream()
                                .anyMatch(excludeTags::contains);
                        if (hasExcludedTag) {
                            return false;
                        }
                    }
                    
                    // If no include tags are specified, include all features (that weren't excluded)
                    if (includeTags.isEmpty()) {
                        return true;
                    }
                    
                    // Include features that have at least one of the include tags
                    return feature.getTags().stream()
                            .anyMatch(includeTags::contains);
                })
                .collect(Collectors.toList());
    }

    /**
     * Process features with parallel execution if appropriate.
     * 
     * @param featurePaths List of feature file paths to process
     * @param useParallel Whether to use parallel processing
     * @return List of processed features
     */
    @Override
    public List<Feature> processFeatures(List<Path> featurePaths, boolean useParallel) {
        if (featurePaths.isEmpty()) {
            logger.warn("No feature files to process");
            return new ArrayList<>();
        }
        
        boolean shouldUseParallel = useParallel && shouldUseParallelProcessing(featurePaths.size());
        
        if (shouldUseParallel) {
            return processInParallel(featurePaths);
        } else {
            return processSequentially(featurePaths);
        }
    }

    /**
     * Check if parallel processing should be used based on number of features.
     * 
     * @param featureCount Number of features to process
     * @return true if parallel processing should be used
     */
    @Override
    public boolean shouldUseParallelProcessing(int featureCount) {
        return performanceMonitoringEnabled && featureCount > PARALLEL_PROCESSING_THRESHOLD;
    }
    
    /**
     * Process feature files in parallel.
     * 
     * @param featurePaths List of feature file paths to process
     * @return List of processed features
     */
    private List<Feature> processInParallel(List<Path> featurePaths) {
        logger.info("Using parallel processing for {} feature files", featurePaths.size());
        
        if (performanceMonitoringEnabled) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("parallel_processing");
        }
        
        List<Feature> processedFeatures = new ArrayList<>();
        
        try {
            List<File> files = featurePaths.stream()
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            
            ParallelFeatureProcessor processor = new ParallelFeatureProcessor();
            
            // Process files in parallel and get results
            processedFeatures.addAll(processor.processFeatureFiles(
                    files, 
                    progress -> logger.debug("Processing progress: {}%", progress)));
            
            // Shutdown the processor
            processor.shutdown();
        } catch (Exception e) {
            logger.error("Error during parallel processing: {}", e.getMessage());
            logger.info("Falling back to sequential processing");
            // Clear and fallback to sequential processing
            processedFeatures.clear();
            processedFeatures.addAll(processSequentially(featurePaths));
        }
        
        if (performanceMonitoringEnabled) {
            long duration = com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("parallel_processing");
            logger.info("Parallel processing completed in {} ms", duration);
        }
        
        return processedFeatures;
    }
    
    /**
     * Process feature files sequentially.
     * 
     * @param featurePaths List of feature file paths to process
     * @return List of processed features
     */
    private List<Feature> processSequentially(List<Path> featurePaths) {
        logger.info("Processing {} feature files sequentially", featurePaths.size());
        
        if (performanceMonitoringEnabled) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("sequential_processing");
        }
        
        List<Feature> features = repository.loadFeatures(featurePaths);
        
        if (performanceMonitoringEnabled) {
            long duration = com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("sequential_processing");
            logger.info("Sequential processing completed in {} ms", duration);
        }
        
        return features;
    }
}