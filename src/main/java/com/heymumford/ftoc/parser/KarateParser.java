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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KarateParser extends FeatureParser {
    private static final Logger logger = LoggerFactory.getLogger(KarateParser.class);

    private static final Pattern KARATE_STAR_STEP = Pattern.compile("^\\s*\\*\\s+(.+)$");
    private static final Pattern KARATE_JSON_PATTERN = Pattern.compile("\\{\\s*['\"\\w]+\\s*:");
    private static final Pattern KARATE_FUNCTION_PATTERN = Pattern.compile("function\\s*\\(");
    private static final Pattern KARATE_MATCH_PATTERN = Pattern.compile("match\\s+\\w+(\\.\\w+)*\\s+");
    private static final Pattern KARATE_METHOD_PATTERN = Pattern.compile("method\\s+(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)");
    private static final Pattern KARATE_STATUS_PATTERN = Pattern.compile("status\\s+\\d+");
    private static final List<String> KARATE_MARKERS = Arrays.asList("#string", "#number", "#boolean", "#array", "#object", "#null", "#notnull", "#regex", "#uuid", "#present", "#notpresent");

    @Override
    public Feature parseFeatureFile(File file) throws FtocException {
        Feature feature = super.parseFeatureFile(file);
        try {
            enhanceWithKarateInfo(feature, file.getAbsolutePath());
        } catch (Exception e) {
            logger.warn("Error enhancing feature with Karate information: {}", e.getMessage());
        }
        return feature;
    }

    private void enhanceWithKarateInfo(Feature feature, String filePath) throws FtocException {
        if (feature == null) { throw new IllegalArgumentException("Feature cannot be null"); }
        boolean hasKarateSyntax = false;
        boolean hasApiCalls = false;
        boolean hasJsonSchema = false;
        boolean hasJsonMatching = false;
        boolean hasEmbeddedJavaScript = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher starMatcher = KARATE_STAR_STEP.matcher(line);
                if (starMatcher.matches()) {
                    hasKarateSyntax = true;
                    String stepContent = starMatcher.group(1);
                    if (KARATE_METHOD_PATTERN.matcher(stepContent).find() || KARATE_STATUS_PATTERN.matcher(stepContent).find() || stepContent.contains("path") || stepContent.contains("url")) { hasApiCalls = true; }
                    if (KARATE_FUNCTION_PATTERN.matcher(stepContent).find() || stepContent.startsWith("def ") || stepContent.contains("assert ")) { hasEmbeddedJavaScript = true; }
                    if (KARATE_MATCH_PATTERN.matcher(stepContent).find()) { hasJsonMatching = true; }
                }
                for (String marker : KARATE_MARKERS) { if (line.contains(marker)) { hasJsonSchema = true; hasKarateSyntax = true; break; } }
                if (KARATE_JSON_PATTERN.matcher(line).find()) { hasKarateSyntax = true; }
            }
        } catch (IOException e) {
            throw new FtocException("Error reading file for Karate enhancements: " + filePath, e, ErrorCode.FILE_READ_ERROR);
        }
        if (hasKarateSyntax) {
            feature.addMetadata("hasKarateSyntax", "true");
            if (hasApiCalls) { feature.addMetadata("hasApiCalls", "true"); }
            if (hasJsonSchema) { feature.addMetadata("hasJsonSchema", "true"); }
            if (hasJsonMatching) { feature.addMetadata("hasJsonMatching", "true"); }
            if (hasEmbeddedJavaScript) { feature.addMetadata("hasEmbeddedJavaScript", "true"); }
            for (Scenario scenario : feature.getScenarios()) {
                List<String> tags = scenario.getTags();
                if (tags.contains("@GET") || tags.contains("@POST") || tags.contains("@PUT") || tags.contains("@DELETE") || tags.contains("@API")) { feature.addMetadata("hasApiOperations", "true"); break; }
            }
        }
    }
}
