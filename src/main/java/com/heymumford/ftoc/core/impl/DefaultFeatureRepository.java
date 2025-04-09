package com.heymumford.ftoc.core.impl;

import com.heymumford.ftoc.core.FeatureRepository;
import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.parser.FeatureParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of FeatureRepository interface.
 * This class is responsible for finding and loading feature files.
 */
public class DefaultFeatureRepository implements FeatureRepository {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFeatureRepository.class);

    /**
     * Find all feature files in a directory and its subdirectories.
     *
     * @param directoryPath The directory to search in
     * @return A list of feature files
     */
    @Override
    public List<Path> findFeatureFiles(Path directoryPath) {
        logger.debug("Searching for feature files in: {}", directoryPath);
        
        try (Stream<Path> paths = Files.walk(directoryPath)) {
            return paths
                    .filter(path -> path.toString().endsWith(".feature"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error finding feature files: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Load a feature file from the specified path.
     * 
     * @param path The path to the feature file
     * @return The parsed Feature object, or empty if it couldn't be parsed
     */
    @Override
    public Optional<Feature> loadFeature(Path path) {
        try {
            File file = path.toFile();
            if (!file.exists() || !file.isFile() || !file.getName().endsWith(".feature")) {
                logger.warn("Invalid feature file: {}", path);
                return Optional.empty();
            }
            
            logger.debug("Loading feature file: {}", path);
            Feature feature = FeatureParserFactory.getParser(file).parseFeatureFile(file);
            return Optional.of(feature);
        } catch (Exception e) {
            logger.error("Error loading feature file {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Load all feature files from a directory and its subdirectories.
     * 
     * @param directoryPath The directory to search in
     * @return A list of parsed Feature objects
     */
    @Override
    public List<Feature> loadFeatures(Path directoryPath) {
        List<Path> featurePaths = findFeatureFiles(directoryPath);
        return loadFeatures(featurePaths);
    }

    /**
     * Load all feature files from a list of paths.
     * 
     * @param featurePaths Paths to feature files
     * @return A list of parsed Feature objects
     */
    @Override
    public List<Feature> loadFeatures(List<Path> featurePaths) {
        logger.info("Loading {} feature files", featurePaths.size());
        
        List<Feature> features = new ArrayList<>();
        int processedCount = 0;
        
        for (Path path : featurePaths) {
            loadFeature(path).ifPresent(features::add);
            
            // Log progress for larger file sets
            processedCount++;
            if (featurePaths.size() > 20 && processedCount % 10 == 0) {
                logger.debug("Processed {}/{} files ({}%)", 
                        processedCount, featurePaths.size(), 
                        (int)((processedCount / (double)featurePaths.size()) * 100));
            }
        }
        
        logger.info("Successfully loaded {} out of {} feature files", features.size(), featurePaths.size());
        return features;
    }
}