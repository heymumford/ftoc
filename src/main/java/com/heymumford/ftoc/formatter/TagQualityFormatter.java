package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer.Warning;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer.WarningType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Formatter for generating Tag Quality Analysis reports in different formats.
 * Takes warnings from TagQualityAnalyzer and formats them for output.
 */
public class TagQualityFormatter {
    
    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON,
        JUNIT_XML
    }
    
    /**
     * Generate a tag quality report in the specified format.
     * 
     * @param warnings List of warnings from the TagQualityAnalyzer
     * @param format The output format
     * @return The formatted tag quality report as a string
     */
    public String generateTagQualityReport(List<Warning> warnings, Format format) {
        switch (format) {
            case PLAIN_TEXT:
                return generatePlainTextReport(warnings);
            case MARKDOWN:
                return generateMarkdownReport(warnings);
            case HTML:
                return generateHtmlReport(warnings);
            case JSON:
                return generateJsonReport(warnings);
            case JUNIT_XML:
                return generateJUnitXmlReport(warnings);
            default:
                return generatePlainTextReport(warnings);
        }
    }
    
    /**
     * Generate a tag quality report in plain text format.
     */
    private String generatePlainTextReport(List<Warning> warnings) {
        if (warnings.isEmpty()) {
            return "No tag quality issues detected.";
        }
        
        // Group warnings by type
        Map<WarningType, List<Warning>> warningsByType = groupWarningsByType(warnings);
        
        StringBuilder report = new StringBuilder();
        report.append("TAG QUALITY REPORT\n");
        report.append("=================\n\n");
        report.append("Found ").append(warnings.size()).append(" potential tag quality issues.\n\n");
        
        // Generate summary section
        report.append("SUMMARY\n");
        report.append("-------\n");
        
        for (WarningType type : WarningType.values()) {
            List<Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
            if (!typeWarnings.isEmpty()) {
                report.append(String.format("%-25s: %d\n", type.getDescription(), typeWarnings.size()));
            }
        }
        
        report.append("\n");
        
        // Generate detailed section for each warning type
        for (Map.Entry<WarningType, List<Warning>> entry : warningsByType.entrySet()) {
            WarningType type = entry.getKey();
            List<Warning> typeWarnings = entry.getValue();
            
            report.append(type.getDescription().toUpperCase()).append("\n");
            report.append(String.join("", Collections.nCopies(type.getDescription().length(), "-"))).append("\n");
            
            // Include a general remediation suggestion for this warning type
            if (!typeWarnings.isEmpty()) {
                report.append("Remediation:\n");
                for (String remedy : typeWarnings.get(0).getRemediation()) {
                    report.append("- ").append(remedy).append("\n");
                }
                report.append("\n");
            }
            
            // List all instances of this warning type
            for (Warning warning : typeWarnings) {
                report.append("- ").append(warning.getMessage());
                if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                    report.append(" (in ").append(warning.getLocation()).append(")");
                }
                report.append("\n");
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Generate a tag quality report in markdown format.
     */
    private String generateMarkdownReport(List<Warning> warnings) {
        if (warnings.isEmpty()) {
            return "# Tag Quality Report\n\nNo tag quality issues detected.";
        }
        
        // Group warnings by type
        Map<WarningType, List<Warning>> warningsByType = groupWarningsByType(warnings);
        
        StringBuilder report = new StringBuilder();
        report.append("# Tag Quality Report\n\n");
        report.append("Found **").append(warnings.size()).append("** potential tag quality issues.\n\n");
        
        // Generate summary section
        report.append("## Summary\n\n");
        
        for (WarningType type : WarningType.values()) {
            List<Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
            if (!typeWarnings.isEmpty()) {
                report.append("- **").append(type.getDescription()).append("**: ")
                      .append(typeWarnings.size()).append("\n");
            }
        }
        
        report.append("\n");
        
        // Generate detailed section for each warning type
        for (Map.Entry<WarningType, List<Warning>> entry : warningsByType.entrySet()) {
            WarningType type = entry.getKey();
            List<Warning> typeWarnings = entry.getValue();
            
            report.append("## ").append(type.getDescription()).append("\n\n");
            
            // Include a general remediation suggestion for this warning type
            if (!typeWarnings.isEmpty()) {
                report.append("### Remediation\n\n");
                for (String remedy : typeWarnings.get(0).getRemediation()) {
                    report.append("- ").append(remedy).append("\n");
                }
                report.append("\n");
            }
            
            // List all instances of this warning type
            report.append("### Detected Issues\n\n");
            for (Warning warning : typeWarnings) {
                report.append("- **").append(warning.getMessage()).append("**");
                if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                    report.append(" *(in ").append(warning.getLocation()).append(")*");
                }
                report.append("\n");
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Generate a tag quality report in HTML format.
     */
    private String generateHtmlReport(List<Warning> warnings) {
        StringBuilder report = new StringBuilder();
        
        report.append("<!DOCTYPE html>\n");
        report.append("<html>\n");
        report.append("<head>\n");
        report.append("  <title>Tag Quality Report</title>\n");
        report.append("  <style>\n");
        report.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
        report.append("    h1 { color: #333; }\n");
        report.append("    h2 { color: #3c7a89; margin-top: 30px; border-bottom: 1px solid #ddd; }\n");
        report.append("    h3 { color: #555; }\n");
        report.append("    .summary { background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n");
        report.append("    .warning { background-color: #fff9e6; padding: 10px 15px; border-left: 4px solid #ffd966; margin-bottom: 10px; }\n");
        report.append("    .warning-location { color: #666; font-style: italic; }\n");
        report.append("    .remediation { background-color: #e8f4f8; padding: 10px 15px; border-radius: 5px; margin-bottom: 20px; }\n");
        report.append("    .no-warnings { color: #2e7d32; }\n");
        report.append("  </style>\n");
        report.append("</head>\n");
        report.append("<body>\n");
        
        report.append("  <h1>Tag Quality Report</h1>\n");
        
        if (warnings.isEmpty()) {
            report.append("  <p class=\"no-warnings\">No tag quality issues detected.</p>\n");
        } else {
            // Group warnings by type
            Map<WarningType, List<Warning>> warningsByType = groupWarningsByType(warnings);
            
            report.append("  <div class=\"summary\">\n");
            report.append("    <h2>Summary</h2>\n");
            report.append("    <p>Found <strong>").append(warnings.size()).append("</strong> potential tag quality issues.</p>\n");
            
            report.append("    <ul>\n");
            for (WarningType type : WarningType.values()) {
                List<Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
                if (!typeWarnings.isEmpty()) {
                    report.append("      <li><strong>").append(type.getDescription()).append("</strong>: ")
                          .append(typeWarnings.size()).append("</li>\n");
                }
            }
            report.append("    </ul>\n");
            report.append("  </div>\n");
            
            // Generate detailed section for each warning type
            for (Map.Entry<WarningType, List<Warning>> entry : warningsByType.entrySet()) {
                WarningType type = entry.getKey();
                List<Warning> typeWarnings = entry.getValue();
                
                report.append("  <h2>").append(type.getDescription()).append("</h2>\n");
                
                // Include a general remediation suggestion for this warning type
                if (!typeWarnings.isEmpty()) {
                    report.append("  <div class=\"remediation\">\n");
                    report.append("    <h3>Remediation</h3>\n");
                    report.append("    <ul>\n");
                    for (String remedy : typeWarnings.get(0).getRemediation()) {
                        report.append("      <li>").append(remedy).append("</li>\n");
                    }
                    report.append("    </ul>\n");
                    report.append("  </div>\n");
                }
                
                // List all instances of this warning type
                report.append("  <h3>Detected Issues</h3>\n");
                for (Warning warning : typeWarnings) {
                    report.append("  <div class=\"warning\">\n");
                    report.append("    <p><strong>").append(warning.getMessage()).append("</strong></p>\n");
                    if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                        report.append("    <p class=\"warning-location\">In: ").append(warning.getLocation()).append("</p>\n");
                    }
                    report.append("  </div>\n");
                }
            }
        }
        
        report.append("</body>\n");
        report.append("</html>");
        
        return report.toString();
    }
    
    /**
     * Generate a tag quality report in JSON format.
     */
    private String generateJsonReport(List<Warning> warnings) {
        StringBuilder json = new StringBuilder();
        
        json.append("{\n");
        json.append("  \"tagQualityReport\": {\n");
        
        if (warnings.isEmpty()) {
            json.append("    \"totalWarnings\": 0,\n");
            json.append("    \"warningsByType\": {},\n");
            json.append("    \"warnings\": []\n");
        } else {
            // Group warnings by type
            Map<WarningType, List<Warning>> warningsByType = groupWarningsByType(warnings);
            
            json.append("    \"totalWarnings\": ").append(warnings.size()).append(",\n");
            
            // Summary by warning type
            json.append("    \"warningsByType\": {\n");
            
            int typeIndex = 0;
            for (WarningType type : WarningType.values()) {
                List<Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
                if (!typeWarnings.isEmpty()) {
                    json.append("      \"").append(escapeJson(type.name())).append("\": {\n");
                    json.append("        \"description\": \"").append(escapeJson(type.getDescription())).append("\",\n");
                    json.append("        \"count\": ").append(typeWarnings.size()).append(",\n");
                    
                    if (!typeWarnings.isEmpty()) {
                        json.append("        \"remediation\": [\n");
                        List<String> remediation = typeWarnings.get(0).getRemediation();
                        for (int i = 0; i < remediation.size(); i++) {
                            json.append("          \"").append(escapeJson(remediation.get(i))).append("\"");
                            if (i < remediation.size() - 1) {
                                json.append(",");
                            }
                            json.append("\n");
                        }
                        json.append("        ]\n");
                    }
                    
                    json.append("      }");
                    
                    if (typeIndex < warningsByType.size() - 1) {
                        json.append(",");
                    }
                    json.append("\n");
                    
                    typeIndex++;
                }
            }
            
            json.append("    },\n");
            
            // All warnings
            json.append("    \"warnings\": [\n");
            
            for (int i = 0; i < warnings.size(); i++) {
                Warning warning = warnings.get(i);
                
                json.append("      {\n");
                json.append("        \"type\": \"").append(escapeJson(warning.getType().name())).append("\",\n");
                json.append("        \"message\": \"").append(escapeJson(warning.getMessage())).append("\"");
                
                if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                    json.append(",\n        \"location\": \"").append(escapeJson(warning.getLocation())).append("\"");
                }
                
                json.append("\n      }");
                
                if (i < warnings.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("    ]\n");
        }
        
        json.append("  }\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Group warnings by their type.
     */
    private Map<WarningType, List<Warning>> groupWarningsByType(List<Warning> warnings) {
        Map<WarningType, List<Warning>> warningsByType = new HashMap<>();
        for (Warning warning : warnings) {
            warningsByType.computeIfAbsent(warning.getType(), k -> new ArrayList<>()).add(warning);
        }
        return warningsByType;
    }
    
    /**
     * Generate a tag quality report in JUnit XML format.
     */
    private String generateJUnitXmlReport(List<Warning> warnings) {
        JUnitFormatter formatter = new JUnitFormatter();
        return formatter.generateTagQualityReport(warnings);
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