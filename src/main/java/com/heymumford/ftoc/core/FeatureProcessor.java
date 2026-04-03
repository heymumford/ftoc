package com.heymumford.ftoc.core;

import com.heymumford.ftoc.model.Feature;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Processor interface for analyzing Feature objects.
 * This interface defines operations for analyzing and processing Feature objects.
 */
public interface FeatureProcessor {

    /**
     * Analyze features to extract tag concordance information.
     * 
     * @param features List of features to analyze
     * @return Map of tags to occurrence counts
     */
    Map<String, Integer> generateTagConcordance(List<Feature> features);
    
    /**
     * Filter features based on tag inclusion/exclusion criteria.
     * 
     * @param features List of features to filter
     * @param includeTags Tags to include (features must have at least one)
     * @param excludeTags Tags to exclude (features must have none)
     * @return Filtered list of features
     */
    List<Feature> filterFeaturesByTags(List<Feature> features, List<String> includeTags, List<String> excludeTags);
    
    /**
     * Process feature files sequentially.
     * 
     * @param features List of feature files to process
     * @return List of processed features
     */
    List<Feature> processFeatures(List<Path> features);
}