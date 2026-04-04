/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.core;

import com.heymumford.ftoc.model.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processor for analyzing and processing Feature objects.
 */
public class FeatureProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FeatureProcessor.class);
    private final FeatureRepository repository;

    /**
     * Create a new feature processor with the given repository.
     *
     * @param repository The feature repository to use for loading features
     */
    public FeatureProcessor(FeatureRepository repository) {
        this.repository = repository;
    }

    /**
     * Analyze features to extract tag concordance information.
     *
     * @param features List of features to analyze
     * @return Map of tags to occurrence counts
     */
    public Map<String, Integer> generateTagConcordance(List<Feature> features) {
        Map<String, Integer> tagConcordance = new HashMap<>();

        for (Feature feature : features) {
            for (String tag : feature.getTags()) {
                tagConcordance.merge(tag, 1, Integer::sum);
            }

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
    public List<Feature> filterFeaturesByTags(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        if (includeTags.isEmpty() && excludeTags.isEmpty()) {
            return new ArrayList<>(features);
        }

        return features.stream()
                .filter(feature -> {
                    if (!excludeTags.isEmpty()) {
                        boolean hasExcludedTag = feature.getTags().stream()
                                .anyMatch(excludeTags::contains);
                        if (hasExcludedTag) {
                            return false;
                        }
                    }

                    if (includeTags.isEmpty()) {
                        return true;
                    }

                    return feature.getTags().stream()
                            .anyMatch(includeTags::contains);
                })
                .collect(Collectors.toList());
    }

    /**
     * Process features sequentially.
     *
     * @param featurePaths List of feature file paths to process
     * @return List of processed features
     */
    public List<Feature> processFeatures(List<Path> featurePaths) {
        if (featurePaths.isEmpty()) {
            logger.warn("No feature files to process");
            return new ArrayList<>();
        }

        logger.info("Processing {} feature files sequentially", featurePaths.size());
        return repository.loadFeatures(featurePaths);
    }
}
