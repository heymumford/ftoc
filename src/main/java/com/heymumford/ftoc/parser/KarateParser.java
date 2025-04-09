package com.heymumford.ftoc.parser;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Karate-specific syntax in feature files.
 * Extends the basic Gherkin parser to handle Karate's unique elements.
 */
public class KarateParser extends FeatureParser {
    private static final Logger logger = LoggerFactory.getLogger(KarateParser.class);
    
    // Patterns for Karate-specific elements
    private static final Pattern KARATE_STAR_STEP = Pattern.compile("^\\s*\\*\\s+(.+)$");
    private static final Pattern KARATE_JSON_PATTERN = Pattern.compile("\\{\\s*['\"\\w]+\\s*:");
    private static final Pattern KARATE_FUNCTION_PATTERN = Pattern.compile("function\\s*\\(");
    private static final Pattern KARATE_MATCH_PATTERN = Pattern.compile("match\\s+\\w+(\\.\\w+)*\\s+");
    private static final Pattern KARATE_METHOD_PATTERN = Pattern.compile("method\\s+(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)");
    private static final Pattern KARATE_STATUS_PATTERN = Pattern.compile("status\\s+\\d+");
    
    // Special Karate markers for schema validation
    private static final List<String> KARATE_MARKERS = Arrays.asList(
        "#string", "#number", "#boolean", "#array", "#object", "#null", 
        "#notnull", "#regex", "#uuid", "#present", "#notpresent"
    );
    
    /**
     * Parse a feature file with Karate-specific syntax
     * 
     * @param filePath The path to the feature file
     * @return A Feature object representing the parsed file
     * @throws com.heymumford.ftoc.exception.ParsingException If there is an error parsing the feature file
     * @throws com.heymumford.ftoc.exception.FileException If the file cannot be found or read
     */
    @Override
    public Feature parseFeatureFile(String filePath) throws com.heymumford.ftoc.exception.ParsingException, com.heymumford.ftoc.exception.FileException {
        // First use the standard Gherkin parser
        Feature feature = super.parseFeatureFile(filePath);
        
        // Enhance with Karate-specific information
        try {
            enhanceWithKarateInfo(feature, filePath);
        } catch (Exception e) {
            // If there's an error while enhancing, log it but don't fail the parsing
            logger.warn("Error enhancing feature with Karate information: {}", e.getMessage());
        }
        
        return feature;
    }
    
    /**
     * Enhance a standard Feature with Karate-specific information
     * 
     * @param feature The feature to enhance
     * @param filePath The path to the original feature file
     * @throws com.heymumford.ftoc.exception.FileException If the file cannot be read
     */
    private void enhanceWithKarateInfo(Feature feature, String filePath) throws com.heymumford.ftoc.exception.FileException {
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null");
        }
        
        // Flag to mark the feature as containing Karate syntax
        boolean hasKarateSyntax = false;
        boolean hasApiCalls = false;
        boolean hasJsonSchema = false;
        boolean hasJsonMatching = false;
        boolean hasEmbeddedJavaScript = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Look for Karate-specific * steps
                Matcher starMatcher = KARATE_STAR_STEP.matcher(line);
                if (starMatcher.matches()) {
                    hasKarateSyntax = true;
                    
                    String stepContent = starMatcher.group(1);
                    
                    // Check for API methods
                    if (KARATE_METHOD_PATTERN.matcher(stepContent).find() || 
                        KARATE_STATUS_PATTERN.matcher(stepContent).find() ||
                        stepContent.contains("path") || stepContent.contains("url")) {
                        hasApiCalls = true;
                    }
                    
                    // Check for JavaScript
                    if (KARATE_FUNCTION_PATTERN.matcher(stepContent).find() || 
                        stepContent.startsWith("def ") ||
                        stepContent.contains("assert ")) {
                        hasEmbeddedJavaScript = true;
                    }
                    
                    // Check for JSON matching
                    if (KARATE_MATCH_PATTERN.matcher(stepContent).find()) {
                        hasJsonMatching = true;
                    }
                }
                
                // Look for JSON schema
                for (String marker : KARATE_MARKERS) {
                    if (line.contains(marker)) {
                        hasJsonSchema = true;
                        hasKarateSyntax = true;
                        break;
                    }
                }
                
                // Look for JSON objects
                if (KARATE_JSON_PATTERN.matcher(line).find()) {
                    hasKarateSyntax = true;
                }
            }
        } catch (IOException e) {
            throw new com.heymumford.ftoc.exception.FileException(
                "Error reading file for Karate enhancements: " + filePath,
                e,
                com.heymumford.ftoc.exception.ErrorCode.FILE_READ_ERROR,
                com.heymumford.ftoc.exception.ExceptionSeverity.WARNING); // Use WARNING level since this is non-critical
        }
        
        // Add Karate metadata to the feature
        if (hasKarateSyntax) {
            feature.addMetadata("hasKarateSyntax", "true");
            
            if (hasApiCalls) {
                feature.addMetadata("hasApiCalls", "true");
            }
            
            if (hasJsonSchema) {
                feature.addMetadata("hasJsonSchema", "true");
            }
            
            if (hasJsonMatching) {
                feature.addMetadata("hasJsonMatching", "true");
            }
            
            if (hasEmbeddedJavaScript) {
                feature.addMetadata("hasEmbeddedJavaScript", "true");
            }
            
            // Also check for API-related tags
            for (Scenario scenario : feature.getScenarios()) {
                List<String> tags = scenario.getTags();
                if (tags.contains("@GET") || tags.contains("@POST") || 
                    tags.contains("@PUT") || tags.contains("@DELETE") ||
                    tags.contains("@API")) {
                    feature.addMetadata("hasApiOperations", "true");
                    break;
                }
            }
        }
    }
}