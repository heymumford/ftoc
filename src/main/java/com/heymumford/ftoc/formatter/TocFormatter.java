package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Formatter for generating Table of Contents (TOC) in different formats.
 */
public class TocFormatter {
    
    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON
    }
    
    /**
     * Generate a TOC for a list of features in the specified format.
     * 
     * @param features List of features to include in the TOC
     * @param format The output format
     * @return The formatted TOC as a string
     */
    public String generateToc(List<Feature> features, Format format) {
        switch (format) {
            case PLAIN_TEXT:
                return generatePlainTextToc(features);
            case MARKDOWN:
                return generateMarkdownToc(features);
            case HTML:
                return generateHtmlToc(features);
            case JSON:
                return generateJsonToc(features);
            default:
                return generatePlainTextToc(features);
        }
    }
    
    /**
     * Generate a TOC in plain text format.
     */
    private String generatePlainTextToc(List<Feature> features) {
        StringBuilder toc = new StringBuilder();
        toc.append("TABLE OF CONTENTS\n");
        toc.append("=================\n\n");
        
        for (Feature feature : features) {
            toc.append(feature.getName()).append(" (").append(feature.getFilename()).append(")\n");
            
            if (!feature.getTags().isEmpty()) {
                toc.append("  Tags: ").append(String.join(", ", feature.getTags())).append("\n");
            }
            
            for (Scenario scenario : feature.getScenarios()) {
                // Skip background scenarios in TOC
                if (scenario.isBackground()) {
                    continue;
                }
                
                String prefix = scenario.isOutline() ? "  Scenario Outline: " : "  Scenario: ";
                toc.append(prefix).append(scenario.getName()).append("\n");
                
                if (!scenario.getTags().isEmpty()) {
                    toc.append("    Tags: ").append(String.join(", ", scenario.getTags())).append("\n");
                }
                
                // For scenario outlines, show examples summary
                if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                    int totalExamples = scenario.getExamples().stream()
                            .mapToInt(ex -> ex.getRows().size())
                            .sum();
                    toc.append("    Examples: ").append(totalExamples).append("\n");
                }
            }
            
            toc.append("\n");
        }
        
        return toc.toString();
    }
    
    /**
     * Generate a TOC in Markdown format.
     */
    private String generateMarkdownToc(List<Feature> features) {
        StringBuilder toc = new StringBuilder();
        toc.append("# Table of Contents\n\n");
        
        for (Feature feature : features) {
            toc.append("## ").append(feature.getName()).append("\n");
            toc.append("*File: ").append(feature.getFilename()).append("*\n\n");
            
            if (!feature.getTags().isEmpty()) {
                toc.append("**Tags:** ");
                for (String tag : feature.getTags()) {
                    toc.append("`").append(tag).append("` ");
                }
                toc.append("\n\n");
            }
            
            for (Scenario scenario : feature.getScenarios()) {
                // Skip background scenarios in TOC
                if (scenario.isBackground()) {
                    continue;
                }
                
                String prefix = scenario.isOutline() ? "### Scenario Outline: " : "### Scenario: ";
                toc.append(prefix).append(scenario.getName()).append("\n");
                
                if (!scenario.getTags().isEmpty()) {
                    toc.append("**Tags:** ");
                    for (String tag : scenario.getTags()) {
                        toc.append("`").append(tag).append("` ");
                    }
                    toc.append("\n");
                }
                
                // For scenario outlines, show examples
                if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                    toc.append("**Examples:**\n");
                    
                    for (Scenario.Example example : scenario.getExamples()) {
                        if (!example.getName().isEmpty()) {
                            toc.append("- ").append(example.getName()).append(": ");
                        }
                        toc.append(example.getRows().size()).append(" variations\n");
                    }
                }
                
                toc.append("\n");
            }
        }
        
        return toc.toString();
    }
    
    /**
     * Generate a TOC in HTML format.
     */
    private String generateHtmlToc(List<Feature> features) {
        StringBuilder toc = new StringBuilder();
        toc.append("<!DOCTYPE html>\n");
        toc.append("<html>\n");
        toc.append("<head>\n");
        toc.append("  <title>Feature Table of Contents</title>\n");
        toc.append("  <style>\n");
        toc.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
        toc.append("    h1 { color: #333; }\n");
        toc.append("    h2 { color: #3c7a89; margin-top: 30px; border-bottom: 1px solid #ddd; }\n");
        toc.append("    h3 { color: #555; margin-left: 20px; }\n");
        toc.append("    .tag { background-color: #e8f4f8; padding: 2px 6px; border-radius: 4px; margin-right: 5px; font-size: 0.8em; }\n");
        toc.append("    .file { color: #777; font-style: italic; }\n");
        toc.append("    .feature { margin-bottom: 40px; }\n");
        toc.append("    .scenario { margin-left: 20px; margin-bottom: 20px; }\n");
        toc.append("    .examples { margin-left: 40px; }\n");
        toc.append("  </style>\n");
        toc.append("</head>\n");
        toc.append("<body>\n");
        toc.append("  <h1>Feature Table of Contents</h1>\n");
        
        for (Feature feature : features) {
            toc.append("  <div class=\"feature\">\n");
            toc.append("    <h2>").append(feature.getName()).append("</h2>\n");
            toc.append("    <p class=\"file\">File: ").append(feature.getFilename()).append("</p>\n");
            
            if (!feature.getTags().isEmpty()) {
                toc.append("    <p>\n");
                for (String tag : feature.getTags()) {
                    toc.append("      <span class=\"tag\">").append(tag).append("</span>\n");
                }
                toc.append("    </p>\n");
            }
            
            for (Scenario scenario : feature.getScenarios()) {
                // Skip background scenarios in TOC
                if (scenario.isBackground()) {
                    continue;
                }
                
                toc.append("    <div class=\"scenario\">\n");
                String prefix = scenario.isOutline() ? "Scenario Outline: " : "Scenario: ";
                toc.append("      <h3>").append(prefix).append(scenario.getName()).append("</h3>\n");
                
                if (!scenario.getTags().isEmpty()) {
                    toc.append("      <p>\n");
                    for (String tag : scenario.getTags()) {
                        toc.append("        <span class=\"tag\">").append(tag).append("</span>\n");
                    }
                    toc.append("      </p>\n");
                }
                
                // For scenario outlines, show examples
                if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                    toc.append("      <div class=\"examples\">\n");
                    toc.append("        <p><strong>Examples:</strong></p>\n");
                    toc.append("        <ul>\n");
                    
                    for (Scenario.Example example : scenario.getExamples()) {
                        toc.append("          <li>");
                        if (!example.getName().isEmpty()) {
                            toc.append(example.getName()).append(": ");
                        }
                        toc.append(example.getRows().size()).append(" variations</li>\n");
                    }
                    
                    toc.append("        </ul>\n");
                    toc.append("      </div>\n");
                }
                
                toc.append("    </div>\n");
            }
            
            toc.append("  </div>\n");
        }
        
        toc.append("</body>\n");
        toc.append("</html>");
        
        return toc.toString();
    }
    
    /**
     * Generate a TOC in JSON format.
     */
    private String generateJsonToc(List<Feature> features) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"tableOfContents\": {\n");
        json.append("    \"features\": [\n");
        
        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            json.append("      {\n");
            json.append("        \"name\": \"").append(escapeJson(feature.getName())).append("\",\n");
            json.append("        \"file\": \"").append(escapeJson(feature.getFilename())).append("\",\n");
            
            // Tags
            json.append("        \"tags\": [");
            if (!feature.getTags().isEmpty()) {
                for (int t = 0; t < feature.getTags().size(); t++) {
                    if (t > 0) json.append(", ");
                    json.append("\"").append(escapeJson(feature.getTags().get(t))).append("\"");
                }
            }
            json.append("],\n");
            
            // Scenarios
            json.append("        \"scenarios\": [\n");
            List<Scenario> scenarios = feature.getScenarios().stream()
                    .filter(s -> !s.isBackground())
                    .collect(Collectors.toList());
            
            for (int s = 0; s < scenarios.size(); s++) {
                Scenario scenario = scenarios.get(s);
                json.append("          {\n");
                json.append("            \"type\": \"").append(scenario.getType()).append("\",\n");
                json.append("            \"name\": \"").append(escapeJson(scenario.getName())).append("\",\n");
                
                // Scenario tags
                json.append("            \"tags\": [");
                if (!scenario.getTags().isEmpty()) {
                    for (int t = 0; t < scenario.getTags().size(); t++) {
                        if (t > 0) json.append(", ");
                        json.append("\"").append(escapeJson(scenario.getTags().get(t))).append("\"");
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
                        json.append("                \"count\": ").append(example.getRows().size()).append("\n");
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
            
            if (i < features.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("    ]\n");
        json.append("  }\n");
        json.append("}");
        
        return json.toString();
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