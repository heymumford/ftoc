package com.heymumford.ftoc.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Factory to create the appropriate parser for feature files.
 */
public class FeatureParserFactory {
    private static final Logger logger = LoggerFactory.getLogger(FeatureParserFactory.class);
    
    /**
     * Get the appropriate parser for a feature file.
     * 
     * @param filePath The path to the feature file
     * @return The appropriate parser for the file
     */
    public static FeatureParser getParser(String filePath) {
        return getParser(new File(filePath));
    }
    
    /**
     * Get the appropriate parser for a feature file.
     * 
     * @param file The feature file
     * @return The appropriate parser for the file
     */
    public static FeatureParser getParser(File file) {
        // Create a standard parser to check if it's a Karate file
        FeatureParser standardParser = new FeatureParser();
        
        if (standardParser.isKarateFile(file)) {
            logger.info("Detected Karate-style feature file: {}", file.getName());
            return new KarateParser();
        }
        
        return standardParser;
    }
}