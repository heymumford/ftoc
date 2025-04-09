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
     * @throws com.heymumford.ftoc.exception.FileException If the file cannot be found or read
     */
    public static FeatureParser getParser(String filePath) throws com.heymumford.ftoc.exception.FileException {
        if (filePath == null || filePath.isEmpty()) {
            throw new com.heymumford.ftoc.exception.FileException(
                "Feature file path cannot be null or empty",
                com.heymumford.ftoc.exception.ErrorCode.FILE_NOT_FOUND);
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new com.heymumford.ftoc.exception.FileException(
                "Feature file not found: " + filePath,
                com.heymumford.ftoc.exception.ErrorCode.FILE_NOT_FOUND);
        }
        
        return getParser(file);
    }
    
    /**
     * Get the appropriate parser for a feature file.
     * 
     * @param file The feature file
     * @return The appropriate parser for the file
     * @throws com.heymumford.ftoc.exception.FileException If the file cannot be found or read
     */
    public static FeatureParser getParser(File file) throws com.heymumford.ftoc.exception.FileException {
        if (file == null) {
            throw new com.heymumford.ftoc.exception.FileException(
                "Feature file cannot be null",
                com.heymumford.ftoc.exception.ErrorCode.FILE_NOT_FOUND);
        }
        
        if (!file.exists()) {
            throw new com.heymumford.ftoc.exception.FileException(
                "Feature file not found: " + file.getAbsolutePath(),
                com.heymumford.ftoc.exception.ErrorCode.FILE_NOT_FOUND);
        }
        
        if (!file.canRead()) {
            throw new com.heymumford.ftoc.exception.FileException(
                "Feature file cannot be read (check permissions): " + file.getAbsolutePath(),
                com.heymumford.ftoc.exception.ErrorCode.FILE_READ_ERROR);
        }
        
        // Create a standard parser to check if it's a Karate file
        FeatureParser standardParser = new FeatureParser();
        
        try {
            if (standardParser.isKarateFile(file)) {
                logger.info("Detected Karate-style feature file: {}", file.getName());
                return new KarateParser();
            }
        } catch (com.heymumford.ftoc.exception.FileException e) {
            // Log the error but continue with standard parser
            logger.warn("Error detecting Karate syntax (using standard parser): {}", e.getMessage());
        }
        
        return standardParser;
    }
}