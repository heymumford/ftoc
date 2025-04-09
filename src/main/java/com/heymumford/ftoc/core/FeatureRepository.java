package com.heymumford.ftoc.core;

import com.heymumford.ftoc.model.Feature;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Feature objects.
 * This interface defines operations for finding and accessing Feature objects.
 */
public interface FeatureRepository {

    /**
     * Find all feature files in a directory and its subdirectories.
     *
     * @param directoryPath The directory to search in
     * @return A list of feature files
     */
    List<Path> findFeatureFiles(Path directoryPath);
    
    /**
     * Load a feature file from the specified path.
     * 
     * @param path The path to the feature file
     * @return The parsed Feature object, or empty if it couldn't be parsed
     */
    Optional<Feature> loadFeature(Path path);
    
    /**
     * Load all feature files from a directory and its subdirectories.
     * 
     * @param directoryPath The directory to search in
     * @return A list of parsed Feature objects
     */
    List<Feature> loadFeatures(Path directoryPath);
    
    /**
     * Load all feature files from a list of paths.
     * 
     * @param featurePaths Paths to feature files
     * @return A list of parsed Feature objects
     */
    List<Feature> loadFeatures(List<Path> featurePaths);
}