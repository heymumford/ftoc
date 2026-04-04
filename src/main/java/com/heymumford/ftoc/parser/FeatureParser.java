/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.parser;

import com.heymumford.ftoc.exception.ErrorCode;
import com.heymumford.ftoc.exception.FtocException;
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

public class FeatureParser {
    private static final Logger logger = LoggerFactory.getLogger(FeatureParser.class);

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

    public Feature parseFeatureFile(String filePath) throws FtocException {
        if (filePath == null || filePath.isEmpty()) {
            throw new FtocException("Feature file path cannot be null or empty", ErrorCode.PARSE_ERROR);
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FtocException("Feature file not found: " + filePath, ErrorCode.FILE_NOT_FOUND);
        }
        if (!file.canRead()) {
            throw new FtocException("Feature file cannot be read (check permissions): " + filePath, ErrorCode.FILE_READ_ERROR);
        }
        return parseFeatureFile(file);
    }

    protected boolean isKarateFile(File file) throws FtocException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new FtocException("File not found: " + file.getAbsolutePath(), ErrorCode.FILE_NOT_FOUND);
        }
        if (!file.canRead()) {
            throw new FtocException("File cannot be read (check permissions): " + file.getAbsolutePath(), ErrorCode.FILE_READ_ERROR);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 50) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                lineCount++;
                if (KARATE_PATTERN.matcher(line).matches()) {
                    return true;
                }
                if (KARATE_MARKER_PATTERN.matcher(line).find()) {
                    return true;
                }
                if (line.contains("method GET") || line.contains("method POST") || line.contains("status ") || line.contains("match ") || line.contains("url ")) {
                    return true;
                }
            }
            if (file.getName().toLowerCase().contains("karate") || file.getPath().toLowerCase().contains("karate")) {
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new FtocException("Error checking for Karate syntax: " + file.getName(), e, ErrorCode.FILE_READ_ERROR);
        }
    }

    public Feature parseFeatureFile(File file) throws FtocException {
        if (file == null) {
            throw new FtocException("Feature file cannot be null", ErrorCode.PARSE_ERROR);
        }
        if (!file.exists()) {
            throw new FtocException("Feature file not found: " + file.getAbsolutePath(), ErrorCode.FILE_NOT_FOUND);
        }
        if (!file.canRead()) {
            throw new FtocException("Feature file cannot be read (check permissions): " + file.getAbsolutePath(), ErrorCode.FILE_READ_ERROR);
        }
        Feature feature = new Feature(file.getAbsolutePath());
        List<String> currentTags = new ArrayList<>();
        Scenario currentScenario = null;
        Scenario.Example currentExample = null;
        boolean inExamplesTable = false;
        boolean isFirstExampleRow = true;
        boolean featureKeywordFound = false;
        List<String> exampleHeaders = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) { continue; }
                if (line.trim().startsWith("#")) { continue; }
                if (line.trim().startsWith("@")) { currentTags.addAll(parseTagLine(line)); continue; }
                Matcher featureMatcher = FEATURE_PATTERN.matcher(line);
                if (featureMatcher.matches()) {
                    featureKeywordFound = true;
                    feature.setName(featureMatcher.group(1).trim());
                    currentTags.forEach(feature::addTag);
                    currentTags.clear();
                    continue;
                }
                Matcher backgroundMatcher = BACKGROUND_PATTERN.matcher(line);
                if (backgroundMatcher.matches()) {
                    currentScenario = new Scenario(backgroundMatcher.group(1).trim(), "Background", lineNumber);
                    feature.addScenario(currentScenario);
                    currentTags.clear();
                    continue;
                }
                Matcher scenarioMatcher = SCENARIO_PATTERN.matcher(line);
                if (scenarioMatcher.matches()) {
                    currentScenario = new Scenario(scenarioMatcher.group(1).trim(), "Scenario", lineNumber);
                    feature.addScenario(currentScenario);
                    currentTags.forEach(currentScenario::addTag);
                    currentTags.clear();
                    continue;
                }
                Matcher outlineMatcher = SCENARIO_OUTLINE_PATTERN.matcher(line);
                if (outlineMatcher.matches()) {
                    currentScenario = new Scenario(outlineMatcher.group(1).trim(), "Scenario Outline", lineNumber);
                    feature.addScenario(currentScenario);
                    currentTags.forEach(currentScenario::addTag);
                    currentTags.clear();
                    continue;
                }
                Matcher ruleMatcher = RULE_PATTERN.matcher(line);
                if (ruleMatcher.matches()) {
                    currentScenario = new Scenario(ruleMatcher.group(1).trim(), "Rule", lineNumber);
                    feature.addScenario(currentScenario);
                    currentTags.forEach(currentScenario::addTag);
                    currentTags.clear();
                    continue;
                }
                if (currentScenario != null) {
                    Matcher stepMatcher = STEP_PATTERN.matcher(line);
                    if (stepMatcher.matches()) {
                        currentScenario.addStep(line.trim());
                        continue;
                    }
                }
                Matcher examplesMatcher = EXAMPLES_PATTERN.matcher(line);
                if (examplesMatcher.matches() && currentScenario != null && currentScenario.isOutline()) {
                    currentExample = new Scenario.Example(examplesMatcher.group(1).trim());
                    currentScenario.addExample(currentExample);
                    inExamplesTable = true;
                    isFirstExampleRow = true;
                    continue;
                }
                if (inExamplesTable && currentExample != null) {
                    Matcher tableRowMatcher = TABLE_ROW_PATTERN.matcher(line);
                    if (tableRowMatcher.matches()) {
                        List<String> cells = parseTableRow(tableRowMatcher.group(1));
                        if (isFirstExampleRow) {
                            currentExample.setHeaders(cells);
                            exampleHeaders = cells;
                            isFirstExampleRow = false;
                        } else {
                            currentExample.addRow(cells);
                        }
                        continue;
                    } else {
                        inExamplesTable = false;
                        isFirstExampleRow = true;
                        exampleHeaders = null;
                    }
                }
            }
            if (!featureKeywordFound) {
                throw new FtocException("Invalid feature file: No 'Feature:' definition found in " + file.getName(), ErrorCode.INVALID_GHERKIN);
            }
            if (feature.getScenarios().isEmpty()) {
                logger.warn("Feature file {} contains no scenarios", file.getName());
            }
        } catch (IOException e) {
            throw new FtocException("Error reading feature file: " + file.getName(), e, ErrorCode.FILE_READ_ERROR);
        }
        return feature;
    }

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

    private List<String> parseTableRow(String row) {
        String[] cells = row.split("\\|");
        return Arrays.stream(cells).map(String::trim).filter(cell -> !cell.isEmpty()).collect(Collectors.toList());
    }
}
