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
     */
    private String generateHtmlToc(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        // Get filtered features for pagination preparation
        List<Feature> filteredFeatures = getFilteredFeatures(features, includeTags, excludeTags);
        
        // Count total scenarios for pagination
        int totalScenarios = 0;
        for (Feature feature : filteredFeatures) {
            List<Scenario> scenarios = feature.getFilteredScenarios(includeTags, excludeTags)
                .stream()
                .filter(s -> !s.isBackground())
                .collect(Collectors.toList());
            totalScenarios += scenarios.size();
        }
        
        // Determine if pagination is needed (more than DEFAULT_SCENARIOS_PER_PAGE scenarios)
        boolean needsPagination = totalScenarios > DEFAULT_SCENARIOS_PER_PAGE;
        
        StringBuilder toc = new StringBuilder();
        toc.append("<!DOCTYPE html>\n");
        toc.append("<html>\n");
        toc.append("<head>\n");
        toc.append("  <title>Feature Table of Contents</title>\n");
        toc.append("  <meta charset=\"UTF-8\">\n");
        toc.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        toc.append("  <style>\n");
        toc.append("    body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }\n");
        toc.append("    h1 { color: #333; margin-bottom: 1em; }\n");
        toc.append("    h2 { color: #3c7a89; margin-top: 30px; border-bottom: 1px solid #ddd; padding-bottom: 5px; }\n");
        toc.append("    h3 { color: #555; margin-left: 20px; margin-top: 20px; }\n");
        toc.append("    .tag { background-color: #e8f4f8; padding: 2px 6px; border-radius: 4px; margin-right: 5px; font-size: 0.8em; }\n");
        toc.append("    .tag.include { background-color: #dff0d8; }\n");
        toc.append("    .tag.exclude { background-color: #f2dede; }\n");
        toc.append("    .file { color: #777; font-style: italic; }\n");
        toc.append("    .feature { margin-bottom: 40px; }\n");
        toc.append("    .scenario { margin-left: 20px; margin-bottom: 20px; padding: 10px; border-left: 3px solid #e8e8e8; }\n");
        toc.append("    .examples { margin-left: 40px; }\n");
        toc.append("    .filters { background-color: #f5f5f5; padding: 10px; border-radius: 5px; margin-bottom: 20px; }\n");
        toc.append("    .no-matches { color: #a94442; background-color: #f2dede; padding: 15px; border-radius: 4px; }\n");
        toc.append("    .collapsible { cursor: pointer; padding: 10px; background-color: #f1f1f1; width: 100%; text-align: left; outline: none; }\n");
        toc.append("    .active, .collapsible:hover { background-color: #ccc; }\n");
        toc.append("    .content { padding: 0 18px; max-height: 0; overflow: hidden; transition: max-height 0.2s ease-out; }\n");
        toc.append("    .toc-nav { position: fixed; top: 10px; right: 10px; width: 250px; background: #f8f9fa; padding: 10px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); max-height: 90vh; overflow-y: auto; }\n");
        toc.append("    .toc-nav ul { padding-left: 20px; }\n");
        toc.append("    .toc-nav li { margin-bottom: 5px; }\n");
        toc.append("    .main-content { margin-right: 280px; }\n");
        toc.append("    .steps { font-family: monospace; white-space: pre-wrap; background-color: #f8f8f8; padding: 10px; }\n");
        toc.append("    .scenario-outline { border-left-color: #3c7a89; }\n");
        toc.append("    .pagination { display: flex; justify-content: center; margin: 20px 0; }\n");
        toc.append("    .pagination button { margin: 0 5px; padding: 5px 10px; cursor: pointer; }\n");
        toc.append("    .pagination .current { font-weight: bold; background-color: #3c7a89; color: white; }\n");
        toc.append("    .page { display: none; }\n");
        toc.append("    .page.active { display: block; }\n");
        toc.append("    table { border-collapse: collapse; width: 100%; margin: 10px 0; }\n");
        toc.append("    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        toc.append("    tr:nth-child(even) { background-color: #f2f2f2; }\n");
        toc.append("    th { background-color: #f1f1f1; }\n");
        toc.append("  </style>\n");
        toc.append("</head>\n");
        toc.append("<body>\n");
        toc.append("  <div class=\"toc-nav\">\n");
        toc.append("    <h3>Navigation</h3>\n");
        toc.append("    <ul>\n");
        
        // Add navigation links
        for (Feature feature : filteredFeatures) {
            String featureId = sanitizeForId(feature.getName());
            toc.append("      <li><a href=\"#").append(featureId).append("\">").append(feature.getName()).append("</a></li>\n");
        }
        
        toc.append("    </ul>\n");
        toc.append("  </div>\n");
        
        toc.append("  <div class=\"main-content\">\n");
        toc.append("    <h1>Feature Table of Contents</h1>\n");
        
        // Add tag filter information if any filters are applied
        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            toc.append("    <div class=\"filters\">\n");
            toc.append("      <h2>Filters Applied</h2>\n");
            
            if (!includeTags.isEmpty()) {
                toc.append("      <p><strong>Include tags:</strong></p>\n");
                toc.append("      <p>\n");
                for (String tag : includeTags) {
                    toc.append("        <span class=\"tag include\">").append(tag).append("</span>\n");
                }
                toc.append("      </p>\n");
            }
            
            if (!excludeTags.isEmpty()) {
                toc.append("      <p><strong>Exclude tags:</strong></p>\n");
                toc.append("      <p>\n");
                for (String tag : excludeTags) {
                    toc.append("        <span class=\"tag exclude\">").append(tag).append("</span>\n");
                }
                toc.append("      </p>\n");
            }
            
            toc.append("    </div>\n");
        }
        
        // Track if any features have scenarios that match the filters
        boolean hasMatchingScenarios = false;
        
        // Setup pagination if needed
        if (needsPagination) {
            toc.append("    <div class=\"pagination\" id=\"pagination-top\">\n");
            toc.append("      <!-- Pagination controls will be inserted here by JavaScript -->\n");
            toc.append("    </div>\n");
        }
        
        // If pagination is needed, create a container for all pages
        if (needsPagination) {
            toc.append("    <div id=\"pages-container\">\n");
        }
        
        // Track current page and scenarios on the current page
        int currentPage = 1;
        int scenariosOnCurrentPage = 0;
        
        // Start the first page
        if (needsPagination) {
            toc.append("      <div class=\"page active\" id=\"page-1\">\n");
        }
        
        for (Feature feature : features) {
            // Get scenarios filtered by tags
            List<Scenario> filteredScenarios = feature.getFilteredScenarios(includeTags, excludeTags);
            
            // Skip features with no matching scenarios
            if (filteredScenarios.isEmpty() || 
                    filteredScenarios.stream().allMatch(Scenario::isBackground)) {
                continue;
            }
            
            hasMatchingScenarios = true;
            
            // If pagination is needed and we've reached the limit, start a new page
            if (needsPagination && scenariosOnCurrentPage >= DEFAULT_SCENARIOS_PER_PAGE) {
                toc.append("      </div>\n"); // Close current page
                currentPage++;
                scenariosOnCurrentPage = 0;
                toc.append("      <div class=\"page\" id=\"page-").append(currentPage).append("\">\n");
            }
            
            // Feature ID for navigation
            String featureId = sanitizeForId(feature.getName());
            
            toc.append("      <div class=\"feature\" id=\"").append(featureId).append("\">\n");
            toc.append("        <h2>").append(feature.getName()).append("</h2>\n");
            toc.append("        <p class=\"file\">File: ").append(feature.getFilename()).append("</p>\n");
            
            if (!feature.getTags().isEmpty()) {
                toc.append("        <p>\n");
                for (String tag : feature.getTags()) {
                    String tagClass = "tag";
                    if (includeTags.contains(tag)) {
                        tagClass += " include";
                    } else if (excludeTags.contains(tag)) {
                        tagClass += " exclude";
                    }
                    toc.append("          <span class=\"").append(tagClass).append("\">").append(tag).append("</span>\n");
                }
                toc.append("        </p>\n");
            }
            
            // Make feature description collapsible if it exists
            if (feature.getDescription() != null && !feature.getDescription().isEmpty()) {
                toc.append("        <button type=\"button\" class=\"collapsible\">Description</button>\n");
                toc.append("        <div class=\"content\">\n");
                toc.append("          <p>").append(feature.getDescription().replace("\n", "<br/>")).append("</p>\n");
                toc.append("        </div>\n");
            }
            
            // Add Karate-specific information if present
            if (feature.hasMetadata("hasKarateSyntax") && "true".equals(feature.getMetadata("hasKarateSyntax"))) {
                toc.append("        <button type=\"button\" class=\"collapsible\">Karate API Test Details</button>\n");
                toc.append("        <div class=\"content\">\n");
                toc.append("          <ul>\n");
                
                if ("true".equals(feature.getMetadata("hasApiCalls"))) {
                    toc.append("            <li>Contains API calls</li>\n");
                }
                
                if ("true".equals(feature.getMetadata("hasJsonSchema"))) {
                    toc.append("            <li>Contains JSON schema validation</li>\n");
                }
                
                if ("true".equals(feature.getMetadata("hasJsonMatching"))) {
                    toc.append("            <li>Contains JSON matching</li>\n");
                }
                
                if ("true".equals(feature.getMetadata("hasEmbeddedJavaScript"))) {
                    toc.append("            <li>Contains embedded JavaScript</li>\n");
                }
                
                if ("true".equals(feature.getMetadata("hasApiOperations"))) {
                    toc.append("            <li>Contains API operations (GET, POST, etc.)</li>\n");
                }
                
                toc.append("          </ul>\n");
                toc.append("        </div>\n");
            }
            
            // Scenarios section with collapsible container
            toc.append("        <button type=\"button\" class=\"collapsible\">Scenarios (")
               .append(filteredScenarios.stream().filter(s -> !s.isBackground()).count())
               .append(")</button>\n");
            toc.append("        <div class=\"content\" style=\"max-height: none;\">\n"); // Initially expanded
            
            for (Scenario scenario : filteredScenarios) {
                // Skip background scenarios in TOC
                if (scenario.isBackground()) {
                    continue;
                }
                
                // Count scenario for pagination
                scenariosOnCurrentPage++;
                
                // Determine CSS class based on scenario type
                String scenarioClass = scenario.isOutline() ? "scenario scenario-outline" : "scenario";
                
                // Scenario ID for navigation
                String scenarioId = sanitizeForId(feature.getName() + "-" + scenario.getName());
                
                toc.append("          <div class=\"").append(scenarioClass).append("\" id=\"").append(scenarioId).append("\">\n");
                String prefix = scenario.isOutline() ? "Scenario Outline: " : "Scenario: ";
                toc.append("            <h3>").append(prefix).append(scenario.getName()).append("</h3>\n");
                
                if (!scenario.getTags().isEmpty()) {
                    toc.append("            <p>\n");
                    for (String tag : scenario.getTags()) {
                        String tagClass = "tag";
                        if (includeTags.contains(tag)) {
                            tagClass += " include";
                        } else if (excludeTags.contains(tag)) {
                            tagClass += " exclude";
                        }
                        toc.append("              <span class=\"").append(tagClass).append("\">").append(tag).append("</span>\n");
                    }
                    toc.append("            </p>\n");
                }
                
                // Add scenario description if it exists
                if (scenario.getDescription() != null && !scenario.getDescription().isEmpty()) {
                    toc.append("            <div class=\"description\">\n");
                    toc.append("              <p>").append(scenario.getDescription().replace("\n", "<br/>")).append("</p>\n");
                    toc.append("            </div>\n");
                }
                
                // Add scenario steps
                if (!scenario.getSteps().isEmpty()) {
                    toc.append("            <button type=\"button\" class=\"collapsible\">Steps</button>\n");
                    toc.append("            <div class=\"content\">\n");
                    toc.append("              <div class=\"steps\">\n");
                    
                    for (String step : scenario.getSteps()) {
                        toc.append(step).append("\n");
                    }
                    
                    toc.append("              </div>\n");
                    toc.append("            </div>\n");
                }
                
                // For scenario outlines, show examples
                if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                    toc.append("            <div class=\"examples\">\n");
                    toc.append("              <button type=\"button\" class=\"collapsible\">Examples (")
                       .append(scenario.getExamples().stream().mapToInt(e -> e.getRows().size()).sum())
                       .append(" variations)</button>\n");
                    toc.append("              <div class=\"content\">\n");
                    
                    for (Scenario.Example example : scenario.getExamples()) {
                        toc.append("                <div class=\"example\">\n");
                        
                        if (!example.getName().isEmpty()) {
                            toc.append("                  <h4>").append(example.getName()).append("</h4>\n");
                        }
                        
                        // Display example data in a table if headers are available
                        if (!example.getHeaders().isEmpty() && !example.getRows().isEmpty()) {
                            toc.append("                  <table>\n");
                            
                            // Table header
                            toc.append("                    <tr>\n");
                            for (String header : example.getHeaders()) {
                                toc.append("                      <th>").append(header).append("</th>\n");
                            }
                            toc.append("                    </tr>\n");
                            
                            // Table rows (up to 10, then a "show more" button)
                            int rowsToShow = Math.min(example.getRows().size(), 10);
                            for (int i = 0; i < rowsToShow; i++) {
                                toc.append("                    <tr>\n");
                                for (String cell : example.getRows().get(i)) {
                                    toc.append("                      <td>").append(cell).append("</td>\n");
                                }
                                toc.append("                    </tr>\n");
                            }
                            
                            toc.append("                  </table>\n");
                            
                            // If there are more rows, show a button to expand
                            if (example.getRows().size() > 10) {
                                toc.append("                  <button type=\"button\" class=\"collapsible\">Show ")
                                   .append(example.getRows().size() - 10).append(" more rows</button>\n");
                                toc.append("                  <div class=\"content\">\n");
                                
                                toc.append("                    <table>\n");
                                // Table header
                                toc.append("                      <tr>\n");
                                for (String header : example.getHeaders()) {
                                    toc.append("                        <th>").append(header).append("</th>\n");
                                }
                                toc.append("                      </tr>\n");
                                
                                // Remaining rows
                                for (int i = 10; i < example.getRows().size(); i++) {
                                    toc.append("                      <tr>\n");
                                    for (String cell : example.getRows().get(i)) {
                                        toc.append("                        <td>").append(cell).append("</td>\n");
                                    }
                                    toc.append("                      </tr>\n");
                                }
                                
                                toc.append("                    </table>\n");
                                toc.append("                  </div>\n");
                            }
                        } else {
                            toc.append("                  <p>No example data available</p>\n");
                        }
                        
                        toc.append("                </div>\n");
                    }
                    
                    toc.append("              </div>\n");
                    toc.append("            </div>\n");
                }
                
                toc.append("          </div>\n");
            }
            
            toc.append("        </div>\n"); // Close scenarios content
            toc.append("      </div>\n"); // Close feature div
        }
        
        // Close the current page and pages container if pagination is enabled
        if (needsPagination) {
            toc.append("      </div>\n"); // Close current page
            toc.append("    </div>\n"); // Close pages container
        }
        
        // If no scenarios match the filters, add a message
        if (!hasMatchingScenarios) {
            toc.append("    <div class=\"no-matches\">\n");
            toc.append("      <h2>No Matching Scenarios</h2>\n");
            toc.append("      <p>No scenarios match the specified tag filters.</p>\n");
            toc.append("    </div>\n");
        }
        
        // Add bottom pagination controls if pagination is needed
        if (needsPagination) {
            toc.append("    <div class=\"pagination\" id=\"pagination-bottom\">\n");
            toc.append("      <!-- Pagination controls will be inserted here by JavaScript -->\n");
            toc.append("    </div>\n");
        }
        
        toc.append("  </div>\n"); // Close main-content
        
        // Add JavaScript for collapsible sections and pagination
        toc.append("<script>\n");
        
        // Collapsible sections
        toc.append("document.addEventListener('DOMContentLoaded', function() {\n");
        toc.append("  var coll = document.getElementsByClassName('collapsible');\n");
        toc.append("  for (var i = 0; i < coll.length; i++) {\n");
        toc.append("    coll[i].addEventListener('click', function() {\n");
        toc.append("      this.classList.toggle('active');\n");
        toc.append("      var content = this.nextElementSibling;\n");
        toc.append("      if (content.style.maxHeight && content.style.maxHeight !== 'none') {\n");
        toc.append("        content.style.maxHeight = null;\n");
        toc.append("      } else {\n");
        toc.append("        content.style.maxHeight = content.scrollHeight + 'px';\n");
        toc.append("      }\n");
        toc.append("    });\n");
        toc.append("  }\n");
        
        // Setup pagination if needed
        if (needsPagination) {
            toc.append("  // Pagination setup\n");
            toc.append("  var totalPages = ").append(currentPage).append(";\n");
            toc.append("  var currentPageNum = 1;\n");
            
            toc.append("  function setupPagination() {\n");
            toc.append("    var paginationTop = document.getElementById('pagination-top');\n");
            toc.append("    var paginationBottom = document.getElementById('pagination-bottom');\n");
            toc.append("    paginationTop.innerHTML = '';\n");
            toc.append("    paginationBottom.innerHTML = '';\n");
            
            toc.append("    // Previous button\n");
            toc.append("    var prevButton = document.createElement('button');\n");
            toc.append("    prevButton.innerHTML = '&laquo; Previous';\n");
            toc.append("    prevButton.disabled = currentPageNum === 1;\n");
            toc.append("    prevButton.addEventListener('click', function() { changePage(currentPageNum - 1); });\n");
            toc.append("    paginationTop.appendChild(prevButton.cloneNode(true));\n");
            toc.append("    paginationBottom.appendChild(prevButton);\n");
            
            toc.append("    // Page buttons\n");
            toc.append("    for (var i = 1; i <= totalPages; i++) {\n");
            toc.append("      var pageButton = document.createElement('button');\n");
            toc.append("      pageButton.textContent = i;\n");
            toc.append("      pageButton.classList.toggle('current', i === currentPageNum);\n");
            toc.append("      pageButton.dataset.page = i;\n");
            toc.append("      pageButton.addEventListener('click', function() { changePage(parseInt(this.dataset.page)); });\n");
            toc.append("      paginationTop.appendChild(pageButton.cloneNode(true));\n");
            toc.append("      paginationBottom.appendChild(pageButton);\n");
            toc.append("    }\n");
            
            toc.append("    // Next button\n");
            toc.append("    var nextButton = document.createElement('button');\n");
            toc.append("    nextButton.innerHTML = 'Next &raquo;';\n");
            toc.append("    nextButton.disabled = currentPageNum === totalPages;\n");
            toc.append("    nextButton.addEventListener('click', function() { changePage(currentPageNum + 1); });\n");
            toc.append("    paginationTop.appendChild(nextButton.cloneNode(true));\n");
            toc.append("    paginationBottom.appendChild(nextButton);\n");
            toc.append("  }\n");
            
            toc.append("  function changePage(pageNum) {\n");
            toc.append("    if (pageNum < 1 || pageNum > totalPages) return;\n");
            
            toc.append("    // Hide all pages\n");
            toc.append("    var pages = document.getElementsByClassName('page');\n");
            toc.append("    for (var i = 0; i < pages.length; i++) {\n");
            toc.append("      pages[i].classList.remove('active');\n");
            toc.append("    }\n");
            
            toc.append("    // Show the selected page\n");
            toc.append("    document.getElementById('page-' + pageNum).classList.add('active');\n");
            toc.append("    currentPageNum = pageNum;\n");
            
            toc.append("    // Update pagination controls\n");
            toc.append("    setupPagination();\n");
            
            toc.append("    // Scroll to top\n");
            toc.append("    window.scrollTo(0, 0);\n");
            toc.append("  }\n");
            
            toc.append("  // Initialize pagination\n");
            toc.append("  setupPagination();\n");
        }
        
        toc.append("});\n");
        toc.append("</script>\n");
        
        toc.append("</body>\n");
        toc.append("</html>");
        
        return toc.toString();
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