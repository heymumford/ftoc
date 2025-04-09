package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example formatter class for FTOC project.
 * This demonstrates the typical pattern for formatters in the project.
 */
public class ExampleFormatter {
    private static final Logger logger = LoggerFactory.getLogger(ExampleFormatter.class);
    
    /**
     * Supported output formats for this formatter.
     */
    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON
    }
    
    /**
     * Generate formatted output for the provided features.
     * 
     * @param features List of features to include in the output
     * @param format The desired output format
     * @return Formatted string representing the features
     */
    public String generate(List<Feature> features, Format format) {
        logger.debug("Generating output in {} format for {} features", format, features.size());
        
        switch (format) {
            case MARKDOWN:
                return generateMarkdown(features);
            case HTML:
                return generateHtml(features);
            case JSON:
                return generateJson(features);
            case PLAIN_TEXT:
            default:
                return generatePlainText(features);
        }
    }
    
    /**
     * Generate plain text output.
     */
    private String generatePlainText(List<Feature> features) {
        StringBuilder sb = new StringBuilder();
        sb.append("FEATURES\n");
        sb.append("========\n\n");
        
        for (Feature feature : features) {
            sb.append(feature.getName()).append("\n");
            sb.append("-".repeat(feature.getName().length())).append("\n");
            
            for (Scenario scenario : feature.getScenarios()) {
                sb.append("  * ").append(scenario.getName()).append("\n");
                
                if (!scenario.getTags().isEmpty()) {
                    sb.append("    Tags: ").append(String.join(", ", scenario.getTags())).append("\n");
                }
                
                sb.append("\n");
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate markdown output.
     */
    private String generateMarkdown(List<Feature> features) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Features\n\n");
        
        for (Feature feature : features) {
            sb.append("## ").append(feature.getName()).append("\n\n");
            
            for (Scenario scenario : feature.getScenarios()) {
                sb.append("### ").append(scenario.getName()).append("\n\n");
                
                if (!scenario.getTags().isEmpty()) {
                    sb.append("*Tags:* ");
                    sb.append(scenario.getTags().stream()
                        .map(tag -> "`" + tag + "`")
                        .collect(Collectors.joining(", ")));
                    sb.append("\n\n");
                }
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate HTML output.
     */
    private String generateHtml(List<Feature> features) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("  <title>Features</title>\n");
        sb.append("  <style>\n");
        sb.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
        sb.append("    h1 { color: #333; }\n");
        sb.append("    h2 { color: #3c7a89; margin-top: 30px; }\n");
        sb.append("    h3 { color: #555; }\n");
        sb.append("    .tag { background-color: #e8f4f8; padding: 3px 7px; border-radius: 4px; margin-right: 5px; font-size: 0.8em; }\n");
        sb.append("  </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        
        sb.append("  <h1>Features</h1>\n");
        
        for (Feature feature : features) {
            sb.append("  <h2>").append(feature.getName()).append("</h2>\n");
            
            for (Scenario scenario : feature.getScenarios()) {
                sb.append("  <h3>").append(scenario.getName()).append("</h3>\n");
                
                if (!scenario.getTags().isEmpty()) {
                    sb.append("  <p>Tags: ");
                    for (String tag : scenario.getTags()) {
                        sb.append("<span class=\"tag\">").append(tag).append("</span> ");
                    }
                    sb.append("</p>\n");
                }
            }
        }
        
        sb.append("</body>\n");
        sb.append("</html>");
        
        return sb.toString();
    }
    
    /**
     * Generate JSON output.
     */
    private String generateJson(List<Feature> features) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"features\": [\n");
        
        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            sb.append("    {\n");
            sb.append("      \"name\": \"").append(escapeJson(feature.getName())).append("\",\n");
            sb.append("      \"file\": \"").append(escapeJson(feature.getFile())).append("\",\n");
            sb.append("      \"scenarios\": [\n");
            
            List<Scenario> scenarios = feature.getScenarios();
            for (int j = 0; j < scenarios.size(); j++) {
                Scenario scenario = scenarios.get(j);
                sb.append("        {\n");
                sb.append("          \"name\": \"").append(escapeJson(scenario.getName())).append("\",\n");
                sb.append("          \"tags\": [");
                
                List<String> tags = scenario.getTags();
                for (int k = 0; k < tags.size(); k++) {
                    sb.append("\"").append(escapeJson(tags.get(k))).append("\"");
                    if (k < tags.size() - 1) {
                        sb.append(", ");
                    }
                }
                
                sb.append("]\n");
                sb.append("        }");
                if (j < scenarios.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            
            sb.append("      ]\n");
            sb.append("    }");
            if (i < features.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        
        sb.append("  ]\n");
        sb.append("}");
        
        return sb.toString();
    }
    
    /**
     * Escape special characters for JSON strings.
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}