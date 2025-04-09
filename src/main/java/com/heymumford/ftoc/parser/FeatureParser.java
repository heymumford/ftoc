package com.heymumford.ftoc.parser;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parser for Cucumber feature files.
 */
public class FeatureParser {
    private static final Logger logger = LoggerFactory.getLogger(FeatureParser.class);
    
    // Regex patterns
    private static final Pattern FEATURE_PATTERN = Pattern.compile("^\\s*Feature:(.*)$");
    private static final Pattern BACKGROUND_PATTERN = Pattern.compile("^\\s*Background:(.*)$");
    private static final Pattern SCENARIO_PATTERN = Pattern.compile("^\\s*Scenario:(.*)$");
    private static final Pattern SCENARIO_OUTLINE_PATTERN = Pattern.compile("^\\s*Scenario Outline:(.*)$");
    private static final Pattern RULE_PATTERN = Pattern.compile("^\\s*Rule:(.*)$");
    private static final Pattern TAG_PATTERN = Pattern.compile("^\\s*(@[\\w-]+)(.*)$");
    private static final Pattern STEP_PATTERN = Pattern.compile("^\\s*(Given|When|Then|And|But)\\s+(.*)$");
    private static final Pattern EXAMPLES_PATTERN = Pattern.compile("^\\s*Examples:(.*)$");
    private static final Pattern TABLE_ROW_PATTERN = Pattern.compile("^\\s*\\|(.*)\\|\\s*$");
    private static final Pattern KARATE_PATTERN = Pattern.compile("^\\s*\\*\\s+(.+)$");
    private static final Pattern KARATE_MARKER_PATTERN = Pattern.compile("#(string|number|boolean|array|object|regex)");
    
    /**
     * Parse a feature file by path.
     * 
     * @param filePath The path to the feature file
     * @return A Feature object containing all parsed information
     */
    public Feature parseFeatureFile(String filePath) {
        return parseFeatureFile(new File(filePath));
    }
    
    /**
     * Check if a file appears to contain Karate-specific syntax
     * 
     * @param file The file to check
     * @return true if the file contains Karate syntax, false otherwise
     */
    protected boolean isKarateFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            boolean hasKarateSyntax = false;
            
            // Check first 50 non-empty lines for Karate syntax
            while ((line = reader.readLine()) != null && lineCount < 50) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                lineCount++;
                
                // Check for * steps (Karate-specific)
                if (KARATE_PATTERN.matcher(line).matches()) {
                    return true;
                }
                
                // Check for Karate schema markers
                if (KARATE_MARKER_PATTERN.matcher(line).find()) {
                    return true;
                }
                
                // Check for common Karate keywords
                if (line.contains("method GET") || 
                    line.contains("method POST") || 
                    line.contains("status ") ||
                    line.contains("match ") ||
                    line.contains("url ")) {
                    return true;
                }
            }
            
            // Also check for Karate-related tags
            if (file.getName().toLowerCase().contains("karate") || 
                file.getPath().toLowerCase().contains("karate")) {
                return true;
            }
            
            return false;
        } catch (IOException e) {
            logger.error("Error checking for Karate syntax: {}", file.getName(), e);
            return false;
        }
    }
    
    /**
     * Parse a feature file and return a Feature object.
     * 
     * @param file The feature file to parse
     * @return A Feature object containing all parsed information
     */
    public Feature parseFeatureFile(File file) {
        Feature feature = new Feature(file.getAbsolutePath());
        List<String> currentTags = new ArrayList<>();
        Scenario currentScenario = null;
        Scenario.Example currentExample = null;
        boolean inExamplesTable = false;
        boolean isFirstExampleRow = true;
        List<String> exampleHeaders = null;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Handle comments
                if (line.trim().startsWith("#")) {
                    continue;
                }
                
                // Handle tags
                if (line.trim().startsWith("@")) {
                    currentTags = parseTagLine(line);
                    continue;
                }
                
                // Handle Feature
                Matcher featureMatcher = FEATURE_PATTERN.matcher(line);
                if (featureMatcher.matches()) {
                    feature.setName(featureMatcher.group(1).trim());
                    // Add tags to feature
                    currentTags.forEach(feature::addTag);
                    currentTags.clear();
                    continue;
                }
                
                // Handle Background
                Matcher backgroundMatcher = BACKGROUND_PATTERN.matcher(line);
                if (backgroundMatcher.matches()) {
                    currentScenario = new Scenario(backgroundMatcher.group(1).trim(), "Background", lineNumber);
                    feature.addScenario(currentScenario);
                    currentTags.clear(); // Background doesn't have tags
                    continue;
                }
                
                // Handle Scenario
                Matcher scenarioMatcher = SCENARIO_PATTERN.matcher(line);
                if (scenarioMatcher.matches()) {
                    currentScenario = new Scenario(scenarioMatcher.group(1).trim(), "Scenario", lineNumber);
                    feature.addScenario(currentScenario);
                    // Add tags to scenario
                    currentTags.forEach(currentScenario::addTag);
                    currentTags.clear();
                    continue;
                }
                
                // Handle Scenario Outline
                Matcher outlineMatcher = SCENARIO_OUTLINE_PATTERN.matcher(line);
                if (outlineMatcher.matches()) {
                    currentScenario = new Scenario(outlineMatcher.group(1).trim(), "Scenario Outline", lineNumber);
                    feature.addScenario(currentScenario);
                    // Add tags to scenario
                    currentTags.forEach(currentScenario::addTag);
                    currentTags.clear();
                    continue;
                }
                
                // Handle Rule
                Matcher ruleMatcher = RULE_PATTERN.matcher(line);
                if (ruleMatcher.matches()) {
                    // For simplicity, we'll treat Rules as a special type of scenario
                    // In a more complex implementation, you might want a separate Rule class
                    currentScenario = new Scenario(ruleMatcher.group(1).trim(), "Rule", lineNumber);
                    feature.addScenario(currentScenario);
                    // Add tags to rule
                    currentTags.forEach(currentScenario::addTag);
                    currentTags.clear();
                    continue;
                }
                
                // Handle Steps (Given, When, Then, And, But)
                if (currentScenario != null) {
                    Matcher stepMatcher = STEP_PATTERN.matcher(line);
                    if (stepMatcher.matches()) {
                        String step = line.trim();
                        currentScenario.addStep(step);
                        continue;
                    }
                }
                
                // Handle Examples
                Matcher examplesMatcher = EXAMPLES_PATTERN.matcher(line);
                if (examplesMatcher.matches() && currentScenario != null && currentScenario.isOutline()) {
                    String exampleName = examplesMatcher.group(1).trim();
                    currentExample = new Scenario.Example(exampleName);
                    currentScenario.addExample(currentExample);
                    inExamplesTable = true;
                    isFirstExampleRow = true;
                    continue;
                }
                
                // Handle Example table rows
                if (inExamplesTable && currentExample != null) {
                    Matcher tableRowMatcher = TABLE_ROW_PATTERN.matcher(line);
                    if (tableRowMatcher.matches()) {
                        String rowContent = tableRowMatcher.group(1);
                        List<String> cells = parseTableRow(rowContent);
                        
                        if (isFirstExampleRow) {
                            // This is the header row
                            currentExample.setHeaders(cells);
                            exampleHeaders = cells;
                            isFirstExampleRow = false;
                        } else {
                            // This is a data row
                            currentExample.addRow(cells);
                        }
                        continue;
                    } else {
                        // We've left the examples table
                        inExamplesTable = false;
                        isFirstExampleRow = true;
                        exampleHeaders = null;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error parsing feature file: {}", file.getName(), e);
        }
        
        return feature;
    }
    
    /**
     * Parse a tag line into individual tags.
     */
    private List<String> parseTagLine(String line) {
        List<String> tags = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(line);
        
        while (matcher.find()) {
            tags.add(matcher.group(1));
            line = matcher.group(2);
            matcher = TAG_PATTERN.matcher(line);
        }
        
        return tags;
    }
    
    /**
     * Parse a table row into cells.
     */
    private List<String> parseTableRow(String row) {
        String[] cells = row.split("\\|");
        return Arrays.stream(cells)
                .map(String::trim)
                .filter(cell -> !cell.isEmpty())
                .collect(Collectors.toList());
    }
}