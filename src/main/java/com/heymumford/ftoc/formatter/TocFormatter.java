package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Formatter for generating Table of Contents (TOC) in different formats.
 */
public class TocFormatter {
    
    // Default number of scenarios per page for paginated HTML output
    private static final int DEFAULT_SCENARIOS_PER_PAGE = 20;
    
    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON,
        JUNIT_XML
    }
    
    /**
     * Generate a TOC for a list of features in the specified format.
     * 
     * @param features List of features to include in the TOC
     * @param format The output format
     * @return The formatted TOC as a string
     */
    public String generateToc(List<Feature> features, Format format) {
        return generateToc(features, format, Collections.emptyList(), Collections.emptyList());
    }
    
    /**
     * Generate a TOC for a list of features in the specified format with tag filtering.
     * 
     * @param features List of features to include in the TOC
     * @param format The output format
     * @param includeTags Scenarios must have at least one of these tags to be included (empty list means include all)
     * @param excludeTags Scenarios with any of these tags will be excluded
     * @return The formatted TOC as a string
     */
    public String generateToc(List<Feature> features, Format format, 
                              List<String> includeTags, List<String> excludeTags) {
        switch (format) {
            case PLAIN_TEXT:
                return generatePlainTextToc(features, includeTags, excludeTags);
            case MARKDOWN:
                return generateMarkdownToc(features, includeTags, excludeTags);
            case HTML:
                return generateHtmlToc(features, includeTags, excludeTags);
            case JSON:
                return generateJsonToc(features, includeTags, excludeTags);
            case JUNIT_XML:
                return generateJUnitXmlToc(features, includeTags, excludeTags);
            default:
                return generatePlainTextToc(features, includeTags, excludeTags);
        }
    }
    
    /**
     * Generate a TOC in plain text format.
     */
    private String generatePlainTextToc(List<Feature> features) {
        return generatePlainTextToc(features, Collections.emptyList(), Collections.emptyList());
    }
    
    /**
     * Generate a TOC in plain text format with tag filtering.
     */
    private String generatePlainTextToc(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        StringBuilder toc = new StringBuilder();
        toc.append("TABLE OF CONTENTS\n");
        toc.append("=================\n\n");
        
        // Add tag filter information if any filters are applied
        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            toc.append("FILTERS APPLIED:\n");
            if (!includeTags.isEmpty()) {
                toc.append("  Include tags: ").append(String.join(", ", includeTags)).append("\n");
            }
            if (!excludeTags.isEmpty()) {
                toc.append("  Exclude tags: ").append(String.join(", ", excludeTags)).append("\n");
            }
            toc.append("\n");
        }
        
        // Track if any features have scenarios that match the filters
        boolean hasMatchingScenarios = false;
        
        for (Feature feature : features) {
            // Get scenarios filtered by tags
            List<Scenario> filteredScenarios = feature.getFilteredScenarios(includeTags, excludeTags);
            
            // Skip features with no matching scenarios
            if (filteredScenarios.isEmpty() || 
                    filteredScenarios.stream().allMatch(Scenario::isBackground)) {
                continue;
            }
            
            hasMatchingScenarios = true;
            toc.append(feature.getName()).append(" (").append(feature.getFilename()).append(")\n");
            
            if (!feature.getTags().isEmpty()) {
                toc.append("  Tags: ").append(String.join(", ", feature.getTags())).append("\n");
            }
            
            // Add Karate-specific information if present
            if (feature.hasMetadata("hasKarateSyntax") && "true".equals(feature.getMetadata("hasKarateSyntax"))) {
                toc.append("  [Karate-style Feature File]\n");
                
                if ("true".equals(feature.getMetadata("hasApiCalls"))) {
                    toc.append("  - Contains API calls\n");
                }
                
                if ("true".equals(feature.getMetadata("hasJsonSchema"))) {
                    toc.append("  - Contains JSON schema validation\n");
                }
                
                if ("true".equals(feature.getMetadata("hasJsonMatching"))) {
                    toc.append("  - Contains JSON matching\n");
                }
                
                if ("true".equals(feature.getMetadata("hasEmbeddedJavaScript"))) {
                    toc.append("  - Contains embedded JavaScript\n");
                }
                
                if ("true".equals(feature.getMetadata("hasApiOperations"))) {
                    toc.append("  - Contains API operations (GET, POST, etc.)\n");
                }
            }
            
            // Track scenario nesting level
            int scenarioDepth = 0;
            Scenario lastScenarioOutline = null;
            
            for (Scenario scenario : filteredScenarios) {
                // Skip background scenarios in TOC
                if (scenario.isBackground()) {
                    continue;
                }
                
                // Reset nesting depth for regular scenarios
                if (!scenario.isOutline()) {
                    scenarioDepth = 0;
                    lastScenarioOutline = null;
                }
                
                // Apply indentation based on nesting level
                String indent = "  " + "  ".repeat(scenarioDepth);
                
                String prefix = scenario.isOutline() ? indent + "Scenario Outline: " : indent + "Scenario: ";
                toc.append(prefix).append(scenario.getName()).append("\n");
                
                if (!scenario.getTags().isEmpty()) {
                    toc.append(indent).append("  Tags: ").append(String.join(", ", scenario.getTags())).append("\n");
                }
                
                // For scenario outlines, show examples summary with proper indentation
                if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                    lastScenarioOutline = scenario;
                    scenarioDepth++;
                    
                    int totalExamples = scenario.getExamples().stream()
                            .mapToInt(ex -> ex.getRows().size())
                            .sum();
                    
                    String exampleIndent = "  " + "  ".repeat(scenarioDepth);
                    toc.append(exampleIndent).append("Examples: ").append(totalExamples).append(" total\n");
                    
                    // Add more detail about individual example groups with additional indentation
                    for (Scenario.Example example : scenario.getExamples()) {
                        String exampleName = example.getName().isEmpty() ? "Unnamed" : example.getName();
                        toc.append(exampleIndent).append("  - ").append(exampleName)
                           .append(": ").append(example.getRows().size()).append(" variations\n");
                    }
                }
            }
            
            toc.append("\n");
        }
        
        // If no scenarios match the filters, add a message
        if (!hasMatchingScenarios) {
            toc.append("No scenarios match the specified tag filters.\n");
        }
        
        return toc.toString();
    }
    
    /**
     * Generate a TOC in Markdown format.
     */
    private String generateMarkdownToc(List<Feature> features) {
        return generateMarkdownToc(features, Collections.emptyList(), Collections.emptyList());
    }
    
    /**
     * Generate a TOC in Markdown format with tag filtering.
     */
    private String generateMarkdownToc(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        StringBuilder toc = new StringBuilder();
        toc.append("# Table of Contents\n\n");
        
        // Add table of contents navigation at the top (new feature)
        toc.append("## Contents\n\n");
        
        List<Feature> filteredFeatures = getFilteredFeatures(features, includeTags, excludeTags);
        
        // Table of contents navigation
        if (filteredFeatures.isEmpty()) {
            toc.append("- No matching scenarios\n\n");
        } else {
            for (Feature feature : filteredFeatures) {
                // Create anchor link from feature name
                String anchor = feature.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
                toc.append("- [").append(feature.getName()).append("](#").append(anchor).append(")\n");
                
                // For each feature, add links to scenarios (up to 5, then a "more" indicator)
                List<Scenario> scenarios = feature.getFilteredScenarios(includeTags, excludeTags)
                    .stream()
                    .filter(s -> !s.isBackground())
                    .collect(Collectors.toList());
                
                if (!scenarios.isEmpty()) {
                    for (int i = 0; i < Math.min(scenarios.size(), 5); i++) {
                        String scenarioAnchor = anchor + "-" + 
                            scenarios.get(i).getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
                        toc.append("  - [").append(scenarios.get(i).getName())
                           .append("](#").append(scenarioAnchor).append(")\n");
                    }
                    
                    if (scenarios.size() > 5) {
                        toc.append("  - ... and ").append(scenarios.size() - 5).append(" more\n");
                    }
                }
            }
            toc.append("\n");
        }
        
        // Add tag filter information if any filters are applied
        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            toc.append("## Filters Applied\n\n");
            if (!includeTags.isEmpty()) {
                toc.append("**Include tags:** ");
                for (String tag : includeTags) {
                    toc.append("`").append(tag).append("` ");
                }
                toc.append("\n\n");
            }
            if (!excludeTags.isEmpty()) {
                toc.append("**Exclude tags:** ");
                for (String tag : excludeTags) {
                    toc.append("`").append(tag).append("` ");
                }
                toc.append("\n\n");
            }
        }
        
        // Track if any features have scenarios that match the filters
        boolean hasMatchingScenarios = false;
        
        for (Feature feature : features) {
            // Get scenarios filtered by tags
            List<Scenario> filteredScenarios = feature.getFilteredScenarios(includeTags, excludeTags);
            
            // Skip features with no matching scenarios
            if (filteredScenarios.isEmpty() || 
                    filteredScenarios.stream().allMatch(Scenario::isBackground)) {
                continue;
            }
            
            hasMatchingScenarios = true;
            
            // Create anchor for feature
            String anchor = feature.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
            toc.append("<h2 id=\"").append(anchor).append("\">").append(feature.getName()).append("</h2>\n\n");
            
            toc.append("*File: ").append(feature.getFilename()).append("*\n\n");
            
            if (!feature.getTags().isEmpty()) {
                toc.append("**Tags:** ");
                for (String tag : feature.getTags()) {
                    toc.append("`").append(tag).append("` ");
                }
                toc.append("\n\n");
            }
            
            // Add feature description if available
            if (feature.getDescription() != null && !feature.getDescription().isEmpty()) {
                toc.append("> ").append(feature.getDescription().replace("\n", "\n> ")).append("\n\n");
            }
            
            // Add Karate-specific information if present
            if (feature.hasMetadata("hasKarateSyntax") && "true".equals(feature.getMetadata("hasKarateSyntax"))) {
                toc.append("**Karate API Test File:**\n\n");
                
                toc.append("<details>\n");
                toc.append("<summary>API Test Details</summary>\n\n");
                
                if ("true".equals(feature.getMetadata("hasApiCalls"))) {
                    toc.append("- Contains API calls\n");
                }
                
                if ("true".equals(feature.getMetadata("hasJsonSchema"))) {
                    toc.append("- Contains JSON schema validation\n");
                }
                
                if ("true".equals(feature.getMetadata("hasJsonMatching"))) {
                    toc.append("- Contains JSON matching\n");
                }
                
                if ("true".equals(feature.getMetadata("hasEmbeddedJavaScript"))) {
                    toc.append("- Contains embedded JavaScript\n");
                }
                
                if ("true".equals(feature.getMetadata("hasApiOperations"))) {
                    toc.append("- Contains API operations (GET, POST, etc.)\n");
                }
                
                toc.append("</details>\n\n");
            }
            
            for (Scenario scenario : filteredScenarios) {
                // Skip background scenarios in TOC
                if (scenario.isBackground()) {
                    continue;
                }
                
                // Create anchor for scenario
                String scenarioAnchor = anchor + "-" + 
                    scenario.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
                
                String prefix = scenario.isOutline() ? "### Scenario Outline: " : "### Scenario: ";
                toc.append("<h3 id=\"").append(scenarioAnchor).append("\">")
                   .append(prefix.substring(4)).append(scenario.getName()).append("</h3>\n\n");
                
                if (!scenario.getTags().isEmpty()) {
                    toc.append("**Tags:** ");
                    for (String tag : scenario.getTags()) {
                        toc.append("`").append(tag).append("` ");
                    }
                    toc.append("\n\n");
                }
                
                // Add scenario description if available
                if (scenario.getDescription() != null && !scenario.getDescription().isEmpty()) {
                    toc.append("> ").append(scenario.getDescription().replace("\n", "\n> ")).append("\n\n");
                }
                
                // For scenario outlines, show examples with collapsible details
                if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                    int totalExamples = scenario.getExamples().stream()
                            .mapToInt(ex -> ex.getRows().size())
                            .sum();
                    
                    toc.append("**Examples:** ").append(totalExamples).append(" total variations\n\n");
                    
                    toc.append("<details>\n");
                    toc.append("<summary>Example Details</summary>\n\n");
                    
                    for (Scenario.Example example : scenario.getExamples()) {
                        if (!example.getName().isEmpty()) {
                            toc.append("#### ").append(example.getName()).append("\n\n");
                        } else {
                            toc.append("#### Examples\n\n");
                        }
                        
                        // Only show header and first 3 rows as a sample
                        if (!example.getHeaders().isEmpty()) {
                            toc.append("| ");
                            for (String header : example.getHeaders()) {
                                toc.append(header).append(" | ");
                            }
                            toc.append("\n");
                            
                            toc.append("| ");
                            for (int i = 0; i < example.getHeaders().size(); i++) {
                                toc.append("--- | ");
                            }
                            toc.append("\n");
                            
                            // Show the first 3 rows or fewer if there are fewer rows
                            int rowsToShow = Math.min(example.getRows().size(), 3);
                            for (int i = 0; i < rowsToShow; i++) {
                                toc.append("| ");
                                for (String cell : example.getRows().get(i)) {
                                    toc.append(cell).append(" | ");
                                }
                                toc.append("\n");
                            }
                            
                            // If there are more rows, indicate that
                            if (example.getRows().size() > 3) {
                                toc.append("| ");
                                for (int i = 0; i < example.getHeaders().size(); i++) {
                                    toc.append("... | ");
                                }
                                toc.append(" _(").append(example.getRows().size() - 3).append(" more rows)_\n");
                            }
                            
                            toc.append("\n");
                        }
                    }
                    
                    toc.append("</details>\n\n");
                }
                
                // Add scenario steps as a collapsible section
                if (!scenario.getSteps().isEmpty()) {
                    toc.append("<details>\n");
                    toc.append("<summary>Steps</summary>\n\n");
                    
                    toc.append("```gherkin\n");
                    for (String step : scenario.getSteps()) {
                        toc.append(step).append("\n");
                    }
                    toc.append("```\n");
                    
                    toc.append("</details>\n\n");
                }
            }
            
            // Add a backtion navigation link
            toc.append("[Back to Contents](#contents)\n\n");
            toc.append("---\n\n");
        }
        
        // If no scenarios match the filters, add a message
        if (!hasMatchingScenarios) {
            toc.append("## No Matching Scenarios\n\n");
            toc.append("No scenarios match the specified tag filters.\n");
        }
        
        return toc.toString();
    }
    
    /**
     * Generate a TOC in HTML format.
     */
    private String generateHtmlToc(List<Feature> features) {
        return generateHtmlToc(features, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Generate a TOC in HTML format with tag filtering.
     * Uses builder pattern to compose HTML sections for improved maintainability.
     */
    private String generateHtmlToc(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        List<Feature> filteredFeatures = getFilteredFeatures(features, includeTags, excludeTags);

        // Build HTML sections using focused builders
        String header = new HtmlHeaderBuilder().build();
        String navigation = new HtmlNavigationBuilder().build(filteredFeatures);
        String filterMetadata = new HtmlFilterMetadataBuilder().build(includeTags, excludeTags);

        // Build body content and get pagination info
        HtmlBodyBuilder.HtmlBodyContent bodyContent = new HtmlBodyBuilder().build(features, includeTags, excludeTags);

        // Build footer with pagination support
        String footer = new HtmlFooterBuilder().build(bodyContent.needsPagination, bodyContent.totalPages);

        // Assemble complete HTML document
        StringBuilder html = new StringBuilder();
        html.append(header);
        html.append(navigation);
        html.append("  <div class=\"main-content\">\n");
        html.append(filterMetadata);
        html.append(bodyContent.content);
        html.append(footer);

        return html.toString();
    }
    
    /**
     * Generate a TOC in JSON format.
     */
    private String generateJsonToc(List<Feature> features) {
        return generateJsonToc(features, Collections.emptyList(), Collections.emptyList());
    }
    
    /**
     * Generate a TOC in JSON format with tag filtering.
     */
    private String generateJsonToc(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"tableOfContents\": {\n");
        
        // Add filters information if they are applied
        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            json.append("    \"filters\": {\n");
            
            // Include tags
            json.append("      \"includeTags\": [");
            if (!includeTags.isEmpty()) {
                for (int t = 0; t < includeTags.size(); t++) {
                    if (t > 0) json.append(", ");
                    json.append("\"").append(escapeJson(includeTags.get(t))).append("\"");
                }
            }
            json.append("],\n");
            
            // Exclude tags
            json.append("      \"excludeTags\": [");
            if (!excludeTags.isEmpty()) {
                for (int t = 0; t < excludeTags.size(); t++) {
                    if (t > 0) json.append(", ");
                    json.append("\"").append(escapeJson(excludeTags.get(t))).append("\"");
                }
            }
            json.append("]\n");
            
            json.append("    },\n");
        }
        
        json.append("    \"features\": [\n");
        
        // Collect features that have matching scenarios
        List<Feature> filteredFeatures = getFilteredFeatures(features, includeTags, excludeTags);
        
        for (int i = 0; i < filteredFeatures.size(); i++) {
            Feature feature = filteredFeatures.get(i);
            json.append("      {\n");
            json.append("        \"name\": \"").append(escapeJson(feature.getName())).append("\",\n");
            json.append("        \"file\": \"").append(escapeJson(feature.getFilename())).append("\",\n");
            
            // Feature description
            if (feature.getDescription() != null && !feature.getDescription().isEmpty()) {
                json.append("        \"description\": \"").append(escapeJson(feature.getDescription())).append("\",\n");
            }
            
            // Tags
            json.append("        \"tags\": [");
            if (!feature.getTags().isEmpty()) {
                for (int t = 0; t < feature.getTags().size(); t++) {
                    if (t > 0) json.append(", ");
                    json.append("\"").append(escapeJson(feature.getTags().get(t))).append("\"");
                }
            }
            json.append("],\n");
            
            // Feature metadata
            if (!feature.getMetadata().isEmpty()) {
                json.append("        \"metadata\": {\n");
                int metaCount = 0;
                for (Map.Entry<String, String> entry : feature.getMetadata().entrySet()) {
                    if (metaCount > 0) json.append(",\n");
                    json.append("          \"").append(escapeJson(entry.getKey())).append("\": \"")
                       .append(escapeJson(entry.getValue())).append("\"");
                    metaCount++;
                }
                json.append("\n        },\n");
            }
            
            // Scenarios
            json.append("        \"scenarios\": [\n");
            List<Scenario> scenarios = feature.getFilteredScenarios(includeTags, excludeTags).stream()
                    .filter(s -> !s.isBackground())
                    .collect(Collectors.toList());
            
            for (int s = 0; s < scenarios.size(); s++) {
                Scenario scenario = scenarios.get(s);
                json.append("          {\n");
                json.append("            \"type\": \"").append(scenario.getType()).append("\",\n");
                json.append("            \"name\": \"").append(escapeJson(scenario.getName())).append("\",\n");
                json.append("            \"line\": ").append(scenario.getLineNumber()).append(",\n");
                
                // Scenario description
                if (scenario.getDescription() != null && !scenario.getDescription().isEmpty()) {
                    json.append("            \"description\": \"").append(escapeJson(scenario.getDescription())).append("\",\n");
                }
                
                // Scenario tags
                json.append("            \"tags\": [");
                if (!scenario.getTags().isEmpty()) {
                    for (int t = 0; t < scenario.getTags().size(); t++) {
                        if (t > 0) json.append(", ");
                        json.append("\"").append(escapeJson(scenario.getTags().get(t))).append("\"");
                    }
                }
                json.append("],\n");
                
                // Scenario steps
                json.append("            \"steps\": [");
                if (!scenario.getSteps().isEmpty()) {
                    for (int t = 0; t < scenario.getSteps().size(); t++) {
                        if (t > 0) json.append(", ");
                        json.append("\"").append(escapeJson(scenario.getSteps().get(t))).append("\"");
                    }
                }
                json.append("],\n");
                
                // Examples for scenario outlines
                if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                    json.append("            \"examples\": [\n");
                    
                    for (int e = 0; e < scenario.getExamples().size(); e++) {
                        Scenario.Example example = scenario.getExamples().get(e);
                        json.append("              {\n");
                        json.append("                \"name\": \"").append(escapeJson(example.getName())).append("\",\n");
                        json.append("                \"count\": ").append(example.getRows().size()).append(",\n");
                        
                        // Headers
                        json.append("                \"headers\": [");
                        if (!example.getHeaders().isEmpty()) {
                            for (int h = 0; h < example.getHeaders().size(); h++) {
                                if (h > 0) json.append(", ");
                                json.append("\"").append(escapeJson(example.getHeaders().get(h))).append("\"");
                            }
                        }
                        json.append("],\n");
                        
                        // Rows
                        json.append("                \"rows\": [\n");
                        for (int r = 0; r < example.getRows().size(); r++) {
                            json.append("                  [");
                            List<String> row = example.getRows().get(r);
                            for (int c = 0; c < row.size(); c++) {
                                if (c > 0) json.append(", ");
                                json.append("\"").append(escapeJson(row.get(c))).append("\"");
                            }
                            json.append("]");
                            
                            if (r < example.getRows().size() - 1) {
                                json.append(",");
                            }
                            json.append("\n");
                        }
                        json.append("                ]\n");
                        json.append("              }");
                        
                        if (e < scenario.getExamples().size() - 1) {
                            json.append(",");
                        }
                        json.append("\n");
                    }
                    
                    json.append("            ]\n");
                } else {
                    json.append("            \"examples\": []\n");
                }
                
                json.append("          }");
                
                if (s < scenarios.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("        ]\n");
            json.append("      }");
            
            if (i < filteredFeatures.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("    ],\n");
        
        // Add summary section
        json.append("    \"summary\": {\n");
        json.append("      \"totalFeatures\": ").append(filteredFeatures.size()).append(",\n");
        
        // Count total scenarios
        int totalScenarios = 0;
        int totalOutlines = 0;
        int totalExamples = 0;
        
        for (Feature feature : filteredFeatures) {
            List<Scenario> scenarios = feature.getFilteredScenarios(includeTags, excludeTags)
                .stream()
                .filter(s -> !s.isBackground())
                .collect(Collectors.toList());
            
            totalScenarios += scenarios.size();
            
            for (Scenario scenario : scenarios) {
                if (scenario.isOutline()) {
                    totalOutlines++;
                    for (Scenario.Example example : scenario.getExamples()) {
                        totalExamples += example.getRows().size();
                    }
                }
            }
        }
        
        json.append("      \"totalScenarios\": ").append(totalScenarios).append(",\n");
        json.append("      \"scenarioOutlines\": ").append(totalOutlines).append(",\n");
        json.append("      \"totalExamples\": ").append(totalExamples).append("\n");
        json.append("    }\n");
        
        json.append("  }\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Generate a JUnit XML report for the table of contents.
     */
    private String generateJUnitXmlToc(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        JUnitFormatter formatter = new JUnitFormatter();
        return formatter.generateTocReport(features, includeTags, excludeTags);
    }

    /**
     * Get a filtered list of features that have matching scenarios.
     * 
     * @param features The full list of features
     * @param includeTags Tags to include
     * @param excludeTags Tags to exclude
     * @return A filtered list of features with matching scenarios
     */
    private List<Feature> getFilteredFeatures(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        List<Feature> filteredFeatures = new ArrayList<>();
        
        for (Feature feature : features) {
            List<Scenario> filteredScenarios = feature.getFilteredScenarios(includeTags, excludeTags);
            
            // Skip features with no matching scenarios or only backgrounds
            if (filteredScenarios.isEmpty() || 
                    filteredScenarios.stream().allMatch(Scenario::isBackground)) {
                continue;
            }
            
            filteredFeatures.add(feature);
        }
        
        return filteredFeatures;
    }
    
    /**
     * Sanitize a string to be used as an HTML ID.
     * 
     * @param input The input string
     * @return A sanitized ID-safe string
     */
    private String sanitizeForId(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
    
    /**
     * Escape special characters in JSON strings.
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}