package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.analyzer.FeatureAntiPatternAnalyzer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Formatter for anti-pattern analysis results in various output formats.
 */
public class AntiPatternFormatter {

    /**
     * Supported output formats for anti-pattern analysis reports.
     */
    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON,
        JUNIT_XML
    }

    /**
     * Generate a formatted anti-pattern report.
     *
     * @param warnings The list of anti-pattern warnings
     * @param format The desired output format
     * @return Formatted anti-pattern report
     */
    public String generateAntiPatternReport(List<FeatureAntiPatternAnalyzer.Warning> warnings, Format format) {
        if (warnings == null || warnings.isEmpty()) {
            return generateEmptyReport(format);
        }

        switch (format) {
            case MARKDOWN:
                return generateMarkdownReport(warnings);
            case HTML:
                return generateHtmlReport(warnings);
            case JSON:
                return generateJsonReport(warnings);
            case JUNIT_XML:
                return generateJUnitXmlReport(warnings);
            case PLAIN_TEXT:
            default:
                return generatePlainTextReport(warnings);
        }
    }

    /**
     * Generate a plain text empty report.
     */
    private String generateEmptyReport(Format format) {
        switch (format) {
            case MARKDOWN:
                return "## Feature Anti-Pattern Report\n\nNo anti-pattern issues detected.";
            case HTML:
                return "<!DOCTYPE html>\n<html>\n<head>\n<title>Feature Anti-Pattern Report</title>\n" +
                       "<style>body {font-family: Arial, sans-serif; margin: 20px;}</style>\n</head>\n" +
                       "<body>\n<h1>Feature Anti-Pattern Report</h1>\n<p>No anti-pattern issues detected.</p>\n" +
                       "</body>\n</html>";
            case JSON:
                return "{\n  \"antiPatternReport\": {\n    \"warnings\": [],\n    \"count\": 0\n  }\n}";
            case JUNIT_XML:
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                       "<testsuite name=\"FTOC Anti-Pattern Analysis\" tests=\"1\" failures=\"0\" errors=\"0\" skipped=\"0\" timestamp=\"" + 
                       java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) + 
                       "\">\n" +
                       "  <properties>\n" +
                       "    <property name=\"analysis_type\" value=\"anti_pattern\"/>\n" +
                       "  </properties>\n" +
                       "  <testcase name=\"anti_pattern_check\" classname=\"com.heymumford.ftoc.AntiPatternAnalysis\">\n" +
                       "    <system-out>No anti-pattern issues were detected.</system-out>\n" +
                       "  </testcase>\n" +
                       "</testsuite>";
            case PLAIN_TEXT:
            default:
                return "FEATURE ANTI-PATTERN REPORT\n============================\n\nNo anti-pattern issues detected.";
        }
    }

    /**
     * Generate a plain text report of anti-pattern warnings.
     */
    private String generatePlainTextReport(List<FeatureAntiPatternAnalyzer.Warning> warnings) {
        StringBuilder report = new StringBuilder();
        report.append("FEATURE ANTI-PATTERN REPORT\n");
        report.append("============================\n\n");
        report.append("Found ").append(warnings.size()).append(" potential anti-pattern issues.\n\n");

        // Group warnings by type
        Map<FeatureAntiPatternAnalyzer.WarningType, List<FeatureAntiPatternAnalyzer.Warning>> warningsByType = 
                groupWarningsByType(warnings);

        // Generate summary section
        report.append("SUMMARY\n");
        report.append("-------\n");

        for (FeatureAntiPatternAnalyzer.WarningType type : FeatureAntiPatternAnalyzer.WarningType.values()) {
            List<FeatureAntiPatternAnalyzer.Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
            if (!typeWarnings.isEmpty()) {
                report.append(String.format("%-30s: %d\n", type.getDescription(), typeWarnings.size()));
            }
        }

        report.append("\n");

        // Generate detailed section for each warning type
        for (Map.Entry<FeatureAntiPatternAnalyzer.WarningType, List<FeatureAntiPatternAnalyzer.Warning>> entry : warningsByType.entrySet()) {
            FeatureAntiPatternAnalyzer.WarningType type = entry.getKey();
            List<FeatureAntiPatternAnalyzer.Warning> typeWarnings = entry.getValue();

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
            for (FeatureAntiPatternAnalyzer.Warning warning : typeWarnings) {
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
     * Generate a Markdown report of anti-pattern warnings.
     */
    private String generateMarkdownReport(List<FeatureAntiPatternAnalyzer.Warning> warnings) {
        StringBuilder report = new StringBuilder();
        report.append("# Feature Anti-Pattern Report\n\n");
        report.append("Found **").append(warnings.size()).append("** potential anti-pattern issues.\n\n");

        // Group warnings by type
        Map<FeatureAntiPatternAnalyzer.WarningType, List<FeatureAntiPatternAnalyzer.Warning>> warningsByType = 
                groupWarningsByType(warnings);

        // Generate summary section
        report.append("## Summary\n\n");

        report.append("| Warning Type | Count |\n");
        report.append("|-------------|-------|\n");

        for (FeatureAntiPatternAnalyzer.WarningType type : FeatureAntiPatternAnalyzer.WarningType.values()) {
            List<FeatureAntiPatternAnalyzer.Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
            if (!typeWarnings.isEmpty()) {
                report.append("| ").append(type.getDescription()).append(" | ")
                      .append(typeWarnings.size()).append(" |\n");
            }
        }

        report.append("\n");

        // Generate detailed section for each warning type
        for (Map.Entry<FeatureAntiPatternAnalyzer.WarningType, List<FeatureAntiPatternAnalyzer.Warning>> entry : warningsByType.entrySet()) {
            FeatureAntiPatternAnalyzer.WarningType type = entry.getKey();
            List<FeatureAntiPatternAnalyzer.Warning> typeWarnings = entry.getValue();

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
            report.append("### Issues\n\n");
            for (FeatureAntiPatternAnalyzer.Warning warning : typeWarnings) {
                report.append("- **").append(warning.getMessage()).append("**");
                if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                    report.append(" (in `").append(warning.getLocation()).append("`)");
                }
                report.append("\n");
            }

            report.append("\n");
        }

        return report.toString();
    }

    /**
     * Generate an HTML report of anti-pattern warnings.
     */
    private String generateHtmlReport(List<FeatureAntiPatternAnalyzer.Warning> warnings) {
        StringBuilder report = new StringBuilder();
        report.append("<!DOCTYPE html>\n<html>\n<head>\n<title>Feature Anti-Pattern Report</title>\n");
        report.append("<style>\n");
        report.append("  body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }\n");
        report.append("  h1 { color: #2c3e50; }\n");
        report.append("  h2 { color: #3498db; margin-top: 30px; border-bottom: 1px solid #eee; }\n");
        report.append("  h3 { color: #2980b9; }\n");
        report.append("  .summary { margin: 20px 0; }\n");
        report.append("  table { border-collapse: collapse; width: 100%; }\n");
        report.append("  th, td { text-align: left; padding: 8px; border-bottom: 1px solid #ddd; }\n");
        report.append("  th { background-color: #f2f2f2; }\n");
        report.append("  tr:hover { background-color: #f5f5f5; }\n");
        report.append("  .warning { margin: 5px 0; }\n");
        report.append("  .location { color: #7f8c8d; font-style: italic; }\n");
        report.append("  .remediation { background-color: #f9f9f9; padding: 10px; border-left: 4px solid #3498db; }\n");
        report.append("</style>\n");
        report.append("</head>\n<body>\n");

        report.append("<h1>Feature Anti-Pattern Report</h1>\n");
        report.append("<p>Found <strong>").append(warnings.size()).append("</strong> potential anti-pattern issues.</p>\n");

        // Group warnings by type
        Map<FeatureAntiPatternAnalyzer.WarningType, List<FeatureAntiPatternAnalyzer.Warning>> warningsByType = 
                groupWarningsByType(warnings);

        // Generate summary section
        report.append("<h2>Summary</h2>\n");
        report.append("<div class=\"summary\">\n");
        report.append("<table>\n");
        report.append("  <tr><th>Warning Type</th><th>Count</th></tr>\n");

        for (FeatureAntiPatternAnalyzer.WarningType type : FeatureAntiPatternAnalyzer.WarningType.values()) {
            List<FeatureAntiPatternAnalyzer.Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
            if (!typeWarnings.isEmpty()) {
                report.append("  <tr><td>").append(type.getDescription()).append("</td><td>")
                      .append(typeWarnings.size()).append("</td></tr>\n");
            }
        }

        report.append("</table>\n");
        report.append("</div>\n");

        // Generate detailed section for each warning type
        for (Map.Entry<FeatureAntiPatternAnalyzer.WarningType, List<FeatureAntiPatternAnalyzer.Warning>> entry : warningsByType.entrySet()) {
            FeatureAntiPatternAnalyzer.WarningType type = entry.getKey();
            List<FeatureAntiPatternAnalyzer.Warning> typeWarnings = entry.getValue();

            report.append("<h2>").append(type.getDescription()).append("</h2>\n");

            // Include a general remediation suggestion for this warning type
            if (!typeWarnings.isEmpty()) {
                report.append("<h3>Remediation</h3>\n");
                report.append("<div class=\"remediation\">\n<ul>\n");
                for (String remedy : typeWarnings.get(0).getRemediation()) {
                    report.append("  <li>").append(remedy).append("</li>\n");
                }
                report.append("</ul>\n</div>\n");
            }

            // List all instances of this warning type
            report.append("<h3>Issues</h3>\n");
            report.append("<ul>\n");
            for (FeatureAntiPatternAnalyzer.Warning warning : typeWarnings) {
                report.append("  <li class=\"warning\"><strong>").append(escapeHtml(warning.getMessage())).append("</strong>");
                if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                    report.append(" <span class=\"location\">(in ").append(escapeHtml(warning.getLocation())).append(")</span>");
                }
                report.append("</li>\n");
            }
            report.append("</ul>\n");
        }

        report.append("</body>\n</html>");
        return report.toString();
    }

    /**
     * Generate a JSON report of anti-pattern warnings.
     */
    private String generateJsonReport(List<FeatureAntiPatternAnalyzer.Warning> warnings) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"antiPatternReport\": {\n");
        json.append("    \"count\": ").append(warnings.size()).append(",\n");
        
        // Group warnings by type
        Map<FeatureAntiPatternAnalyzer.WarningType, List<FeatureAntiPatternAnalyzer.Warning>> warningsByType = 
                groupWarningsByType(warnings);
        
        // Generate summary
        json.append("    \"summary\": [\n");
        boolean firstSummary = true;
        for (FeatureAntiPatternAnalyzer.WarningType type : FeatureAntiPatternAnalyzer.WarningType.values()) {
            List<FeatureAntiPatternAnalyzer.Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
            if (!typeWarnings.isEmpty()) {
                if (!firstSummary) {
                    json.append(",\n");
                }
                json.append("      {\n");
                json.append("        \"type\": \"").append(escapeJson(type.name())).append("\",\n");
                json.append("        \"description\": \"").append(escapeJson(type.getDescription())).append("\",\n");
                json.append("        \"count\": ").append(typeWarnings.size()).append("\n");
                json.append("      }");
                firstSummary = false;
            }
        }
        json.append("\n    ],\n");
        
        // Generate warnings
        json.append("    \"warnings\": [\n");
        boolean firstWarning = true;
        for (FeatureAntiPatternAnalyzer.Warning warning : warnings) {
            if (!firstWarning) {
                json.append(",\n");
            }
            
            json.append("      {\n");
            json.append("        \"type\": \"").append(escapeJson(warning.getType().name())).append("\",\n");
            json.append("        \"description\": \"").append(escapeJson(warning.getType().getDescription())).append("\",\n");
            json.append("        \"message\": \"").append(escapeJson(warning.getMessage())).append("\"");
            
            if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                json.append(",\n        \"location\": \"").append(escapeJson(warning.getLocation())).append("\"");
            }
            
            json.append(",\n        \"remediation\": [\n");
            boolean firstRemedy = true;
            for (String remedy : warning.getRemediation()) {
                if (!firstRemedy) {
                    json.append(",\n");
                }
                json.append("          \"").append(escapeJson(remedy)).append("\"");
                firstRemedy = false;
            }
            json.append("\n        ]\n");
            
            json.append("      }");
            firstWarning = false;
        }
        json.append("\n    ]\n");
        
        json.append("  }\n");
        json.append("}");
        
        return json.toString();
    }

    /**
     * Group warnings by their type.
     */
    private Map<FeatureAntiPatternAnalyzer.WarningType, List<FeatureAntiPatternAnalyzer.Warning>> groupWarningsByType(
            List<FeatureAntiPatternAnalyzer.Warning> warnings) {
        return warnings.stream()
                .collect(Collectors.groupingBy(FeatureAntiPatternAnalyzer.Warning::getType));
    }

    /**
     * Escape special characters in HTML.
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    /**
     * Generate a JUnit XML report of anti-pattern warnings.
     */
    private String generateJUnitXmlReport(List<FeatureAntiPatternAnalyzer.Warning> warnings) {
        JUnitFormatter formatter = new JUnitFormatter();
        return formatter.generateAntiPatternReport(warnings);
    }

    /**
     * Escape special characters in JSON.
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}