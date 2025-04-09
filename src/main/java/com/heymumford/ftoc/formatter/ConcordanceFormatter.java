package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.formatter.ConcordanceAnalyzer.CoOccurrence;
import com.heymumford.ftoc.formatter.ConcordanceAnalyzer.TagTrend;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Formatter for generating Tag Concordance reports in different formats.
 * Provides detailed statistics about tag usage in feature files.
 */
public class ConcordanceFormatter {
    
    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON,
        JUNIT_XML
    }
    
    /**
     * Generate a tag concordance report in the specified format.
     * 
     * @param tagConcordance Map containing tags and their occurrence counts
     * @param features List of features to analyze for tag statistics
     * @param format The output format
     * @return The formatted tag concordance report as a string
     */
    public String generateConcordanceReport(Map<String, Integer> tagConcordance, 
                                          List<Feature> features,
                                          Format format) {
        // Perform advanced tag analysis
        List<CoOccurrence> coOccurrences = ConcordanceAnalyzer.calculateCoOccurrences(features);
        Map<String, TagTrend> tagTrends = ConcordanceAnalyzer.calculateTagTrends(features, tagConcordance);
        Map<String, Double> tagSignificance = ConcordanceAnalyzer.calculateTagSignificance(features, tagConcordance);
        String visualizationJson = ConcordanceAnalyzer.generateVisualizationJson(coOccurrences, tagConcordance);
        
        switch (format) {
            case PLAIN_TEXT:
                return generatePlainTextReport(tagConcordance, features, coOccurrences, tagTrends, tagSignificance);
            case MARKDOWN:
                return generateMarkdownReport(tagConcordance, features, coOccurrences, tagTrends, tagSignificance);
            case HTML:
                return generateHtmlReport(tagConcordance, features, coOccurrences, tagTrends, tagSignificance, visualizationJson);
            case JSON:
                return generateJsonReport(tagConcordance, features, coOccurrences, tagTrends, tagSignificance, visualizationJson);
            case JUNIT_XML:
                return generateJUnitXmlReport(tagConcordance, features);
            default:
                return generatePlainTextReport(tagConcordance, features, coOccurrences, tagTrends, tagSignificance);
        }
    }
    
    /**
     * Generate a tag concordance report in plain text format.
     */
    private String generatePlainTextReport(Map<String, Integer> tagConcordance, List<Feature> features,
                                          List<CoOccurrence> coOccurrences, Map<String, TagTrend> tagTrends,
                                          Map<String, Double> tagSignificance) {
        StringBuilder report = new StringBuilder();
        int totalTags = tagConcordance.values().stream().mapToInt(Integer::intValue).sum();
        int uniqueTags = tagConcordance.size();
        
        report.append("TAG CONCORDANCE REPORT\n");
        report.append("======================\n\n");
        
        // Summary statistics
        report.append("SUMMARY\n");
        report.append("-------\n");
        report.append(String.format("Total Tags Used: %d\n", totalTags));
        report.append(String.format("Unique Tags: %d\n", uniqueTags));
        report.append(String.format("Features: %d\n", features.size()));
        report.append(String.format("Average Tags Per Feature: %.2f\n\n", 
                features.isEmpty() ? 0 : (double) totalTags / features.size()));
        
        // Tag frequency table
        report.append("TAG FREQUENCY\n");
        report.append("-------------\n");
        report.append(String.format("%-30s %-10s %-10s %-12s %-12s\n", 
                "Tag", "Count", "Percent", "Trend", "Significance"));
        report.append(String.format("%-30s %-10s %-10s %-12s %-12s\n", 
                "------------------------------", "----------", "----------", "------------", "------------"));
        
        DecimalFormat df = new DecimalFormat("0.0%");
        DecimalFormat sigFormat = new DecimalFormat("0.000");
        
        // Sort tags by count (descending)
        List<Map.Entry<String, Integer>> sortedTags = tagConcordance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        for (Map.Entry<String, Integer> entry : sortedTags) {
            String tag = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalTags;
            
            // Get trend and significance for this tag
            String trend = tagTrends.containsKey(tag) ? tagTrends.get(tag).getTrend() : "Unknown";
            double significance = tagSignificance.getOrDefault(tag, 0.0);
            
            report.append(String.format("%-30s %-10d %-10s %-12s %-12s\n", 
                    tag, count, df.format(percentage), trend, sigFormat.format(significance)));
        }
        
        // Tag categories analysis
        report.append("\nTAG CATEGORIES\n");
        report.append("-------------\n");
        
        Map<String, List<String>> categorizedTags = categorizeTagsByPrefix(tagConcordance.keySet());
        
        for (Map.Entry<String, List<String>> category : categorizedTags.entrySet()) {
            report.append(String.format("%s Tags (%d):\n", 
                    category.getKey(), category.getValue().size()));
            
            for (String tag : category.getValue()) {
                report.append(String.format("  %-30s %d\n", tag, tagConcordance.get(tag)));
            }
            report.append("\n");
        }
        
        // Tag co-occurrence metrics
        report.append("TAG CO-OCCURRENCE METRICS\n");
        report.append("------------------------\n");
        
        if (coOccurrences.isEmpty()) {
            report.append("No tag co-occurrences found.\n\n");
        } else {
            report.append(String.format("%-20s %-20s %-10s %-15s\n", 
                    "Tag 1", "Tag 2", "Count", "Coefficient"));
            report.append(String.format("%-20s %-20s %-10s %-15s\n", 
                    "--------------------", "--------------------", "----------", "---------------"));
            
            // Display top 15 co-occurrences by coefficient
            coOccurrences.stream()
                    .limit(15)
                    .forEach(co -> {
                        report.append(String.format("%-20s %-20s %-10d %-15.3f\n", 
                                co.getTag1(), co.getTag2(), co.getCount(), co.getCoefficient()));
                    });
            report.append("\n");
        }
        
        // Tag trend analysis
        report.append("TAG TREND ANALYSIS\n");
        report.append("------------------\n");
        
        if (tagTrends.isEmpty()) {
            report.append("No trend data available.\n\n");
        } else {
            report.append(String.format("%-30s %-10s %-12s %-15s\n", 
                    "Tag", "Count", "Trend", "Growth Rate"));
            report.append(String.format("%-30s %-10s %-12s %-15s\n", 
                    "------------------------------", "----------", "------------", "---------------"));
            
            // Sort trends by growth rate (descending)
            List<TagTrend> sortedTrends = tagTrends.values().stream()
                    .sorted(Comparator.comparing(TagTrend::getGrowthRate).reversed())
                    .collect(Collectors.toList());
            
            for (TagTrend trend : sortedTrends) {
                report.append(String.format("%-30s %-10d %-12s %-15.3f\n", 
                        trend.getTag(), trend.getTotalCount(), trend.getTrend(), trend.getGrowthRate()));
            }
            report.append("\n");
        }
        
        // Low-value tag detection
        report.append("POTENTIALLY LOW-VALUE TAGS\n");
        report.append("-------------------------\n");
        List<String> lowValueTags = detectLowValueTags(tagConcordance, features);
        
        if (lowValueTags.isEmpty()) {
            report.append("No low-value tags detected.\n\n");
        } else {
            for (String tag : lowValueTags) {
                report.append(String.format("%s (Count: %d)\n", tag, tagConcordance.get(tag)));
            }
            report.append("\n");
        }
        
        // Statistically significant tags
        report.append("STATISTICALLY SIGNIFICANT TAGS\n");
        report.append("-----------------------------\n");
        
        // Sort tags by significance (descending)
        List<Map.Entry<String, Double>> significantTags = tagSignificance.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        if (significantTags.isEmpty()) {
            report.append("No significant tags detected.\n\n");
        } else {
            for (Map.Entry<String, Double> entry : significantTags) {
                report.append(String.format("%s (Score: %.3f)\n", 
                        entry.getKey(), entry.getValue()));
            }
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Generate a tag concordance report in markdown format.
     */
    private String generateMarkdownReport(Map<String, Integer> tagConcordance, List<Feature> features,
                                        List<CoOccurrence> coOccurrences, Map<String, TagTrend> tagTrends,
                                        Map<String, Double> tagSignificance) {
        StringBuilder report = new StringBuilder();
        int totalTags = tagConcordance.values().stream().mapToInt(Integer::intValue).sum();
        int uniqueTags = tagConcordance.size();
        
        report.append("# Tag Concordance Report\n\n");
        
        // Summary statistics
        report.append("## Summary\n\n");
        report.append(String.format("- **Total Tags Used:** %d\n", totalTags));
        report.append(String.format("- **Unique Tags:** %d\n", uniqueTags));
        report.append(String.format("- **Features:** %d\n", features.size()));
        report.append(String.format("- **Average Tags Per Feature:** %.2f\n\n", 
                features.isEmpty() ? 0 : (double) totalTags / features.size()));
        
        // Tag frequency table
        report.append("## Tag Frequency\n\n");
        report.append("| Tag | Count | Percent | Trend | Significance |\n");
        report.append("|-----|-------|--------|-------|-------------|\n");
        
        DecimalFormat df = new DecimalFormat("0.0%");
        DecimalFormat sigFormat = new DecimalFormat("0.000");
        
        // Sort tags by count (descending)
        List<Map.Entry<String, Integer>> sortedTags = tagConcordance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        for (Map.Entry<String, Integer> entry : sortedTags) {
            String tag = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalTags;
            
            // Get trend and significance for this tag
            String trend = tagTrends.containsKey(tag) ? tagTrends.get(tag).getTrend() : "Unknown";
            double significance = tagSignificance.getOrDefault(tag, 0.0);
            
            report.append(String.format("| `%s` | %d | %s | %s | %s |\n", 
                    tag, count, df.format(percentage), trend, sigFormat.format(significance)));
        }
        
        // Tag categories analysis
        report.append("\n## Tag Categories\n\n");
        
        Map<String, List<String>> categorizedTags = categorizeTagsByPrefix(tagConcordance.keySet());
        
        for (Map.Entry<String, List<String>> category : categorizedTags.entrySet()) {
            report.append(String.format("### %s Tags (%d)\n\n", 
                    category.getKey(), category.getValue().size()));
            
            for (String tag : category.getValue()) {
                report.append(String.format("- `%s` (%d)\n", tag, tagConcordance.get(tag)));
            }
            report.append("\n");
        }
        
        // Tag co-occurrence metrics
        report.append("## Tag Co-occurrence Metrics\n\n");
        
        if (coOccurrences.isEmpty()) {
            report.append("No tag co-occurrences found.\n\n");
        } else {
            report.append("### Strongest Tag Relationships\n\n");
            report.append("| Tag 1 | Tag 2 | Count | Coefficient |\n");
            report.append("|-------|-------|-------|-------------|\n");
            
            // Display top 15 co-occurrences by coefficient
            coOccurrences.stream()
                    .limit(15)
                    .forEach(co -> {
                        report.append(String.format("| `%s` | `%s` | %d | %.3f |\n", 
                                co.getTag1(), co.getTag2(), co.getCount(), co.getCoefficient()));
                    });
            report.append("\n");
        }
        
        // Tag trend analysis
        report.append("## Tag Trend Analysis\n\n");
        
        if (tagTrends.isEmpty()) {
            report.append("No trend data available.\n\n");
        } else {
            report.append("### Rising Tags\n\n");
            report.append("| Tag | Count | Growth Rate |\n");
            report.append("|-----|-------|------------|\n");
            
            // Sort trends by growth rate (descending) and get rising tags
            List<TagTrend> risingTags = tagTrends.values().stream()
                    .filter(t -> t.getGrowthRate() > 0)
                    .sorted(Comparator.comparing(TagTrend::getGrowthRate).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (risingTags.isEmpty()) {
                report.append("No rising tags detected.\n\n");
            } else {
                for (TagTrend trend : risingTags) {
                    report.append(String.format("| `%s` | %d | %.3f |\n", 
                            trend.getTag(), trend.getTotalCount(), trend.getGrowthRate()));
                }
            }
            
            report.append("\n### Declining Tags\n\n");
            report.append("| Tag | Count | Growth Rate |\n");
            report.append("|-----|-------|------------|\n");
            
            // Sort trends by growth rate (ascending) and get declining tags
            List<TagTrend> decliningTags = tagTrends.values().stream()
                    .filter(t -> t.getGrowthRate() < 0)
                    .sorted(Comparator.comparing(TagTrend::getGrowthRate))
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (decliningTags.isEmpty()) {
                report.append("No declining tags detected.\n\n");
            } else {
                for (TagTrend trend : decliningTags) {
                    report.append(String.format("| `%s` | %d | %.3f |\n", 
                            trend.getTag(), trend.getTotalCount(), trend.getGrowthRate()));
                }
            }
        }
        
        // Low-value tag detection
        report.append("## Potentially Low-Value Tags\n\n");
        List<String> lowValueTags = detectLowValueTags(tagConcordance, features);
        
        if (lowValueTags.isEmpty()) {
            report.append("No low-value tags detected.\n\n");
        } else {
            for (String tag : lowValueTags) {
                report.append(String.format("- `%s` (Count: %d)\n", tag, tagConcordance.get(tag)));
            }
            report.append("\n");
        }
        
        // Statistically significant tags
        report.append("## Statistically Significant Tags\n\n");
        
        // Sort tags by significance (descending)
        List<Map.Entry<String, Double>> significantTags = tagSignificance.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        if (significantTags.isEmpty()) {
            report.append("No significant tags detected.\n\n");
        } else {
            report.append("| Tag | Significance Score |\n");
            report.append("|-----|-------------------|\n");
            for (Map.Entry<String, Double> entry : significantTags) {
                report.append(String.format("| `%s` | %.3f |\n", 
                        entry.getKey(), entry.getValue()));
            }
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Generate a tag concordance report in HTML format.
     */
    private String generateHtmlReport(Map<String, Integer> tagConcordance, List<Feature> features,
                                    List<CoOccurrence> coOccurrences, Map<String, TagTrend> tagTrends,
                                    Map<String, Double> tagSignificance, String visualizationJson) {
        StringBuilder report = new StringBuilder();
        int totalTags = tagConcordance.values().stream().mapToInt(Integer::intValue).sum();
        int uniqueTags = tagConcordance.size();
        
        report.append("<!DOCTYPE html>\n");
        report.append("<html>\n");
        report.append("<head>\n");
        report.append("  <title>Tag Concordance Report</title>\n");
        report.append("  <style>\n");
        report.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
        report.append("    h1 { color: #333; }\n");
        report.append("    h2 { color: #3c7a89; margin-top: 30px; border-bottom: 1px solid #ddd; }\n");
        report.append("    h3 { color: #555; }\n");
        report.append("    table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }\n");
        report.append("    th, td { text-align: left; padding: 8px; border-bottom: 1px solid #ddd; }\n");
        report.append("    th { background-color: #f2f2f2; }\n");
        report.append("    tr:hover { background-color: #f5f5f5; }\n");
        report.append("    .tag { background-color: #e8f4f8; padding: 2px 6px; border-radius: 4px; margin-right: 5px; font-size: 0.8em; }\n");
        report.append("    .low-value { background-color: #f2dede; }\n");
        report.append("    .rising { background-color: #dff0d8; }\n");
        report.append("    .declining { background-color: #f2dede; }\n");
        report.append("    .stable { background-color: #fcf8e3; }\n");
        report.append("    .category { background-color: #dff0d8; }\n");
        report.append("    .summary { background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n");
        report.append("    .progress-bar { background-color: #f2f2f2; border-radius: 4px; height: 20px; margin-top: 5px; }\n");
        report.append("    .progress-value { background-color: #3c7a89; border-radius: 4px; height: 20px; }\n");
        report.append("    .significant { font-weight: bold; }\n");
        report.append("    .visualization { width: 100%; height: 600px; margin-top: 20px; margin-bottom: 20px; }\n");
        report.append("    .tabs { display: flex; overflow: hidden; border: 1px solid #ccc; background-color: #f1f1f1; }\n");
        report.append("    .tab-button { background-color: inherit; float: left; border: none; outline: none; cursor: pointer; padding: 14px 16px; transition: 0.3s; font-size: 17px; }\n");
        report.append("    .tab-button:hover { background-color: #ddd; }\n");
        report.append("    .tab-button.active { background-color: #ccc; }\n");
        report.append("    .tab-content { display: none; padding: 20px; border: 1px solid #ccc; border-top: none; }\n");
        report.append("  </style>\n");
        
        // D3.js for visualization
        report.append("  <script src=\"https://d3js.org/d3.v7.min.js\"></script>\n");
        
        report.append("</head>\n");
        report.append("<body>\n");
        
        report.append("  <h1>Tag Concordance Report</h1>\n");
        
        // Summary statistics
        report.append("  <div class=\"summary\">\n");
        report.append("    <h2>Summary</h2>\n");
        report.append("    <p><strong>Total Tags Used:</strong> ").append(totalTags).append("</p>\n");
        report.append("    <p><strong>Unique Tags:</strong> ").append(uniqueTags).append("</p>\n");
        report.append("    <p><strong>Features:</strong> ").append(features.size()).append("</p>\n");
        report.append("    <p><strong>Average Tags Per Feature:</strong> ")
              .append(String.format("%.2f", features.isEmpty() ? 0 : (double) totalTags / features.size()))
              .append("</p>\n");
        report.append("  </div>\n");
        
        // Tabs for different analyses
        report.append("  <div class=\"tabs\">\n");
        report.append("    <button class=\"tab-button active\" onclick=\"openTab(event, 'frequency')\">Frequency</button>\n");
        report.append("    <button class=\"tab-button\" onclick=\"openTab(event, 'categories')\">Categories</button>\n");
        report.append("    <button class=\"tab-button\" onclick=\"openTab(event, 'cooccurrence')\">Co-occurrence</button>\n");
        report.append("    <button class=\"tab-button\" onclick=\"openTab(event, 'trends')\">Trends</button>\n");
        report.append("    <button class=\"tab-button\" onclick=\"openTab(event, 'visualization')\">Visualization</button>\n");
        report.append("    <button class=\"tab-button\" onclick=\"openTab(event, 'significance')\">Significance</button>\n");
        report.append("  </div>\n");
        
        // Frequency tab
        report.append("  <div id=\"frequency\" class=\"tab-content\" style=\"display: block;\">\n");
        report.append("    <h2>Tag Frequency</h2>\n");
        report.append("    <table>\n");
        report.append("      <tr>\n");
        report.append("        <th>Tag</th>\n");
        report.append("        <th>Count</th>\n");
        report.append("        <th>Percent</th>\n");
        report.append("        <th>Trend</th>\n");
        report.append("        <th>Significance</th>\n");
        report.append("        <th>Distribution</th>\n");
        report.append("      </tr>\n");
        
        DecimalFormat df = new DecimalFormat("0.0%");
        DecimalFormat sigFormat = new DecimalFormat("0.000");
        
        // Sort tags by count (descending)
        List<Map.Entry<String, Integer>> sortedTags = tagConcordance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        for (Map.Entry<String, Integer> entry : sortedTags) {
            String tag = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalTags;
            int barWidth = (int) (percentage * 100);
            
            // Get trend and significance for this tag
            String trend = tagTrends.containsKey(tag) ? tagTrends.get(tag).getTrend() : "Unknown";
            double significance = tagSignificance.getOrDefault(tag, 0.0);
            
            String trendClass = "";
            if (trend.equals("Rising")) {
                trendClass = "rising";
            } else if (trend.equals("Declining")) {
                trendClass = "declining";
            } else if (trend.equals("Stable")) {
                trendClass = "stable";
            }
            
            String sigClass = significance > 0.1 ? "significant" : "";
            
            report.append("      <tr>\n");
            report.append("        <td><span class=\"tag\">").append(tag).append("</span></td>\n");
            report.append("        <td>").append(count).append("</td>\n");
            report.append("        <td>").append(df.format(percentage)).append("</td>\n");
            report.append("        <td><span class=\"").append(trendClass).append("\">").append(trend).append("</span></td>\n");
            report.append("        <td class=\"").append(sigClass).append("\">").append(sigFormat.format(significance)).append("</td>\n");
            report.append("        <td>\n");
            report.append("          <div class=\"progress-bar\">\n");
            report.append("            <div class=\"progress-value\" style=\"width: ").append(barWidth).append("%;\"></div>\n");
            report.append("          </div>\n");
            report.append("        </td>\n");
            report.append("      </tr>\n");
        }
        
        report.append("    </table>\n");
        report.append("  </div>\n");
        
        // Categories tab
        report.append("  <div id=\"categories\" class=\"tab-content\">\n");
        report.append("    <h2>Tag Categories</h2>\n");
        
        Map<String, List<String>> categorizedTags = categorizeTagsByPrefix(tagConcordance.keySet());
        
        for (Map.Entry<String, List<String>> category : categorizedTags.entrySet()) {
            report.append("    <h3>").append(category.getKey()).append(" Tags (")
                  .append(category.getValue().size()).append(")</h3>\n");
            report.append("    <ul>\n");
            
            for (String tag : category.getValue()) {
                // Get trend for this tag
                String trend = tagTrends.containsKey(tag) ? tagTrends.get(tag).getTrend() : "Unknown";
                String trendClass = "";
                if (trend.equals("Rising")) {
                    trendClass = "rising";
                } else if (trend.equals("Declining")) {
                    trendClass = "declining";
                } else if (trend.equals("Stable")) {
                    trendClass = "stable";
                }
                
                report.append("      <li><span class=\"tag category\">").append(tag)
                      .append("</span> (").append(tagConcordance.get(tag)).append(") ")
                      .append("<span class=\"").append(trendClass).append("\">[").append(trend).append("]</span></li>\n");
            }
            
            report.append("    </ul>\n");
        }
        report.append("  </div>\n");
        
        // Co-occurrence tab
        report.append("  <div id=\"cooccurrence\" class=\"tab-content\">\n");
        report.append("    <h2>Tag Co-occurrence Metrics</h2>\n");
        
        if (coOccurrences.isEmpty()) {
            report.append("    <p>No tag co-occurrences found.</p>\n");
        } else {
            report.append("    <table>\n");
            report.append("      <tr>\n");
            report.append("        <th>Tag 1</th>\n");
            report.append("        <th>Tag 2</th>\n");
            report.append("        <th>Count</th>\n");
            report.append("        <th>Coefficient</th>\n");
            report.append("      </tr>\n");
            
            // Display top 20 co-occurrences by coefficient
            coOccurrences.stream()
                    .limit(20)
                    .forEach(co -> {
                        double coefficient = co.getCoefficient();
                        String strengthClass = coefficient > 0.7 ? "rising" : 
                                              (coefficient > 0.4 ? "stable" : "");
                        
                        report.append("      <tr>\n");
                        report.append("        <td><span class=\"tag\">").append(co.getTag1()).append("</span></td>\n");
                        report.append("        <td><span class=\"tag\">").append(co.getTag2()).append("</span></td>\n");
                        report.append("        <td>").append(co.getCount()).append("</td>\n");
                        report.append("        <td class=\"").append(strengthClass).append("\">")
                              .append(String.format("%.3f", coefficient)).append("</td>\n");
                        report.append("      </tr>\n");
                    });
            
            report.append("    </table>\n");
        }
        report.append("  </div>\n");
        
        // Trends tab
        report.append("  <div id=\"trends\" class=\"tab-content\">\n");
        report.append("    <h2>Tag Trend Analysis</h2>\n");
        
        if (tagTrends.isEmpty()) {
            report.append("    <p>No trend data available.</p>\n");
        } else {
            report.append("    <h3>Rising Tags</h3>\n");
            report.append("    <table>\n");
            report.append("      <tr>\n");
            report.append("        <th>Tag</th>\n");
            report.append("        <th>Count</th>\n");
            report.append("        <th>Growth Rate</th>\n");
            report.append("        <th>Associated Tags</th>\n");
            report.append("      </tr>\n");
            
            // Sort trends by growth rate (descending) and get rising tags
            List<TagTrend> risingTags = tagTrends.values().stream()
                    .filter(t -> t.getGrowthRate() > 0)
                    .sorted(Comparator.comparing(TagTrend::getGrowthRate).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (risingTags.isEmpty()) {
                report.append("      <tr><td colspan=\"4\">No rising tags detected.</td></tr>\n");
            } else {
                for (TagTrend trend : risingTags) {
                    report.append("      <tr>\n");
                    report.append("        <td><span class=\"tag rising\">").append(trend.getTag()).append("</span></td>\n");
                    report.append("        <td>").append(trend.getTotalCount()).append("</td>\n");
                    report.append("        <td>").append(String.format("%.3f", trend.getGrowthRate())).append("</td>\n");
                    report.append("        <td>\n");
                    
                    // Get top 3 associated tags
                    trend.getAssociatedTags().entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(3)
                            .forEach(assoc -> {
                                report.append("          <span class=\"tag\">").append(assoc.getKey())
                                      .append(" (").append(assoc.getValue()).append(")</span>\n");
                            });
                    
                    report.append("        </td>\n");
                    report.append("      </tr>\n");
                }
            }
            
            report.append("    </table>\n");
            
            report.append("    <h3>Declining Tags</h3>\n");
            report.append("    <table>\n");
            report.append("      <tr>\n");
            report.append("        <th>Tag</th>\n");
            report.append("        <th>Count</th>\n");
            report.append("        <th>Growth Rate</th>\n");
            report.append("        <th>Associated Tags</th>\n");
            report.append("      </tr>\n");
            
            // Sort trends by growth rate (ascending) and get declining tags
            List<TagTrend> decliningTags = tagTrends.values().stream()
                    .filter(t -> t.getGrowthRate() < 0)
                    .sorted(Comparator.comparing(TagTrend::getGrowthRate))
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (decliningTags.isEmpty()) {
                report.append("      <tr><td colspan=\"4\">No declining tags detected.</td></tr>\n");
            } else {
                for (TagTrend trend : decliningTags) {
                    report.append("      <tr>\n");
                    report.append("        <td><span class=\"tag declining\">").append(trend.getTag()).append("</span></td>\n");
                    report.append("        <td>").append(trend.getTotalCount()).append("</td>\n");
                    report.append("        <td>").append(String.format("%.3f", trend.getGrowthRate())).append("</td>\n");
                    report.append("        <td>\n");
                    
                    // Get top 3 associated tags
                    trend.getAssociatedTags().entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(3)
                            .forEach(assoc -> {
                                report.append("          <span class=\"tag\">").append(assoc.getKey())
                                      .append(" (").append(assoc.getValue()).append(")</span>\n");
                            });
                    
                    report.append("        </td>\n");
                    report.append("      </tr>\n");
                }
            }
            
            report.append("    </table>\n");
        }
        report.append("  </div>\n");
        
        // Visualization tab
        report.append("  <div id=\"visualization\" class=\"tab-content\">\n");
        report.append("    <h2>Tag Relationship Visualization</h2>\n");
        report.append("    <p>This force-directed graph shows the relationships between tags. Stronger connections (higher co-occurrence coefficient) are shown with thicker lines.</p>\n");
        report.append("    <p>Hover over nodes and links to see details. Drag nodes to explore relationships.</p>\n");
        report.append("    <div id=\"tag-graph\" class=\"visualization\"></div>\n");
        
        // Embed D3.js visualization
        report.append("    <script>\n");
        report.append("      // Tag relationship data\n");
        report.append("      const graphData = ").append(visualizationJson).append(";\n\n");
        
        report.append("      // Force-directed graph visualization\n");
        report.append("      function createForceGraph() {\n");
        report.append("        const width = document.getElementById('tag-graph').clientWidth;\n");
        report.append("        const height = 600;\n");
        report.append("        \n");
        report.append("        // Color scale for tag groups\n");
        report.append("        const color = d3.scaleOrdinal(d3.schemeCategory10);\n");
        report.append("        \n");
        report.append("        // Node size scale based on count\n");
        report.append("        const nodeSize = d3.scaleLinear()\n");
        report.append("          .domain([0, d3.max(graphData.nodes, d => d.count)])\n");
        report.append("          .range([5, 25]);\n");
        report.append("        \n");
        report.append("        // Link width scale based on strength\n");
        report.append("        const linkWidth = d3.scaleLinear()\n");
        report.append("          .domain([0, d3.max(graphData.links, d => d.strength)])\n");
        report.append("          .range([1, 10]);\n");
        report.append("        \n");
        report.append("        // Create SVG\n");
        report.append("        const svg = d3.select('#tag-graph')\n");
        report.append("          .append('svg')\n");
        report.append("          .attr('width', width)\n");
        report.append("          .attr('height', height);\n");
        report.append("        \n");
        report.append("        // Create tooltip\n");
        report.append("        const tooltip = d3.select('body')\n");
        report.append("          .append('div')\n");
        report.append("          .style('position', 'absolute')\n");
        report.append("          .style('background', '#f9f9f9')\n");
        report.append("          .style('padding', '5px')\n");
        report.append("          .style('border', '1px solid #ccc')\n");
        report.append("          .style('border-radius', '5px')\n");
        report.append("          .style('pointer-events', 'none')\n");
        report.append("          .style('opacity', 0);\n");
        report.append("        \n");
        report.append("        // Create simulation\n");
        report.append("        const simulation = d3.forceSimulation(graphData.nodes)\n");
        report.append("          .force('link', d3.forceLink(graphData.links).id(d => d.id).distance(100))\n");
        report.append("          .force('charge', d3.forceManyBody().strength(-300))\n");
        report.append("          .force('center', d3.forceCenter(width / 2, height / 2))\n");
        report.append("          .force('collide', d3.forceCollide().radius(d => nodeSize(d.count) + 10));\n");
        report.append("        \n");
        report.append("        // Create links\n");
        report.append("        const link = svg.append('g')\n");
        report.append("          .selectAll('line')\n");
        report.append("          .data(graphData.links)\n");
        report.append("          .enter().append('line')\n");
        report.append("          .attr('stroke-width', d => linkWidth(d.strength))\n");
        report.append("          .attr('stroke', '#999')\n");
        report.append("          .attr('stroke-opacity', 0.6)\n");
        report.append("          .on('mouseover', function(event, d) {\n");
        report.append("            tooltip.transition().duration(200).style('opacity', .9);\n");
        report.append("            tooltip.html(`<strong>${d.source.id} & ${d.target.id}</strong><br/>Coefficient: ${d.strength.toFixed(3)}<br/>Count: ${d.value}`)\n");
        report.append("              .style('left', (event.pageX + 10) + 'px')\n");
        report.append("              .style('top', (event.pageY - 28) + 'px');\n");
        report.append("          })\n");
        report.append("          .on('mouseout', function() {\n");
        report.append("            tooltip.transition().duration(500).style('opacity', 0);\n");
        report.append("          });\n");
        report.append("        \n");
        report.append("        // Create nodes\n");
        report.append("        const node = svg.append('g')\n");
        report.append("          .selectAll('circle')\n");
        report.append("          .data(graphData.nodes)\n");
        report.append("          .enter().append('circle')\n");
        report.append("          .attr('r', d => nodeSize(d.count))\n");
        report.append("          .attr('fill', d => color(d.group))\n");
        report.append("          .attr('stroke', '#fff')\n");
        report.append("          .attr('stroke-width', 1.5)\n");
        report.append("          .on('mouseover', function(event, d) {\n");
        report.append("            tooltip.transition().duration(200).style('opacity', .9);\n");
        report.append("            tooltip.html(`<strong>${d.id}</strong><br/>Count: ${d.count}`)\n");
        report.append("              .style('left', (event.pageX + 10) + 'px')\n");
        report.append("              .style('top', (event.pageY - 28) + 'px');\n");
        report.append("          })\n");
        report.append("          .on('mouseout', function() {\n");
        report.append("            tooltip.transition().duration(500).style('opacity', 0);\n");
        report.append("          })\n");
        report.append("          .call(d3.drag()\n");
        report.append("            .on('start', dragstarted)\n");
        report.append("            .on('drag', dragged)\n");
        report.append("            .on('end', dragended));\n");
        report.append("        \n");
        report.append("        // Add labels\n");
        report.append("        const label = svg.append('g')\n");
        report.append("          .selectAll('text')\n");
        report.append("          .data(graphData.nodes)\n");
        report.append("          .enter().append('text')\n");
        report.append("          .text(d => d.id)\n");
        report.append("          .attr('font-size', 10)\n");
        report.append("          .attr('dx', 12)\n");
        report.append("          .attr('dy', '.35em');\n");
        report.append("        \n");
        report.append("        // Update positions on tick\n");
        report.append("        simulation.on('tick', () => {\n");
        report.append("          link\n");
        report.append("            .attr('x1', d => d.source.x)\n");
        report.append("            .attr('y1', d => d.source.y)\n");
        report.append("            .attr('x2', d => d.target.x)\n");
        report.append("            .attr('y2', d => d.target.y);\n");
        report.append("          \n");
        report.append("          node\n");
        report.append("            .attr('cx', d => d.x = Math.max(nodeSize(d.count), Math.min(width - nodeSize(d.count), d.x)))\n");
        report.append("            .attr('cy', d => d.y = Math.max(nodeSize(d.count), Math.min(height - nodeSize(d.count), d.y)));\n");
        report.append("          \n");
        report.append("          label\n");
        report.append("            .attr('x', d => d.x)\n");
        report.append("            .attr('y', d => d.y);\n");
        report.append("        });\n");
        report.append("        \n");
        report.append("        // Drag functions\n");
        report.append("        function dragstarted(event, d) {\n");
        report.append("          if (!event.active) simulation.alphaTarget(0.3).restart();\n");
        report.append("          d.fx = d.x;\n");
        report.append("          d.fy = d.y;\n");
        report.append("        }\n");
        report.append("        \n");
        report.append("        function dragged(event, d) {\n");
        report.append("          d.fx = event.x;\n");
        report.append("          d.fy = event.y;\n");
        report.append("        }\n");
        report.append("        \n");
        report.append("        function dragended(event, d) {\n");
        report.append("          if (!event.active) simulation.alphaTarget(0);\n");
        report.append("          d.fx = null;\n");
        report.append("          d.fy = null;\n");
        report.append("        }\n");
        report.append("      }\n");
        report.append("      \n");
        report.append("      // Create visualization when tab is opened\n");
        report.append("      document.querySelector('button[onclick=\"openTab(event, \\'visualization\\')\"]')\n");
        report.append("        .addEventListener('click', function() {\n");
        report.append("          setTimeout(() => {\n");
        report.append("            if (document.getElementById('tag-graph').innerHTML === '') {\n");
        report.append("              createForceGraph();\n");
        report.append("            }\n");
        report.append("          }, 100);\n");
        report.append("        });\n");
        report.append("    </script>\n");
        report.append("  </div>\n");
        
        // Significance tab
        report.append("  <div id=\"significance\" class=\"tab-content\">\n");
        report.append("    <h2>Tag Significance Analysis</h2>\n");
        
        // Low-value tag detection
        report.append("    <h3>Potentially Low-Value Tags</h3>\n");
        List<String> lowValueTags = detectLowValueTags(tagConcordance, features);
        
        if (lowValueTags.isEmpty()) {
            report.append("    <p>No low-value tags detected.</p>\n");
        } else {
            report.append("    <ul>\n");
            for (String tag : lowValueTags) {
                report.append("      <li><span class=\"tag low-value\">").append(tag)
                      .append("</span> (Count: ").append(tagConcordance.get(tag)).append(")</li>\n");
            }
            report.append("    </ul>\n");
        }
        
        // Statistically significant tags
        report.append("    <h3>Statistically Significant Tags</h3>\n");
        
        // Sort tags by significance (descending)
        List<Map.Entry<String, Double>> significantTags = tagSignificance.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(15)
                .collect(Collectors.toList());
        
        if (significantTags.isEmpty()) {
            report.append("    <p>No significant tags detected.</p>\n");
        } else {
            report.append("    <table>\n");
            report.append("      <tr>\n");
            report.append("        <th>Tag</th>\n");
            report.append("        <th>Count</th>\n");
            report.append("        <th>Significance Score</th>\n");
            report.append("        <th>Trend</th>\n");
            report.append("      </tr>\n");
            
            for (Map.Entry<String, Double> entry : significantTags) {
                String tag = entry.getKey();
                double score = entry.getValue();
                
                // Get count and trend for this tag
                int count = tagConcordance.getOrDefault(tag, 0);
                String trend = tagTrends.containsKey(tag) ? tagTrends.get(tag).getTrend() : "Unknown";
                String trendClass = "";
                if (trend.equals("Rising")) {
                    trendClass = "rising";
                } else if (trend.equals("Declining")) {
                    trendClass = "declining";
                } else if (trend.equals("Stable")) {
                    trendClass = "stable";
                }
                
                report.append("      <tr>\n");
                report.append("        <td><span class=\"tag\">").append(tag).append("</span></td>\n");
                report.append("        <td>").append(count).append("</td>\n");
                report.append("        <td class=\"significant\">").append(String.format("%.3f", score)).append("</td>\n");
                report.append("        <td><span class=\"").append(trendClass).append("\">").append(trend).append("</span></td>\n");
                report.append("      </tr>\n");
            }
            
            report.append("    </table>\n");
        }
        report.append("  </div>\n");
        
        // Tab navigation script
        report.append("  <script>\n");
        report.append("    function openTab(evt, tabName) {\n");
        report.append("      var i, tabcontent, tablinks;\n");
        report.append("      tabcontent = document.getElementsByClassName(\"tab-content\");\n");
        report.append("      for (i = 0; i < tabcontent.length; i++) {\n");
        report.append("        tabcontent[i].style.display = \"none\";\n");
        report.append("      }\n");
        report.append("      tablinks = document.getElementsByClassName(\"tab-button\");\n");
        report.append("      for (i = 0; i < tablinks.length; i++) {\n");
        report.append("        tablinks[i].className = tablinks[i].className.replace(\" active\", \"\");\n");
        report.append("      }\n");
        report.append("      document.getElementById(tabName).style.display = \"block\";\n");
        report.append("      evt.currentTarget.className += \" active\";\n");
        report.append("    }\n");
        report.append("  </script>\n");
        
        report.append("</body>\n");
        report.append("</html>");
        
        return report.toString();
    }
    
    /**
     * Generate a tag concordance report in JSON format.
     */
    private String generateJsonReport(Map<String, Integer> tagConcordance, List<Feature> features,
                                    List<CoOccurrence> coOccurrences, Map<String, TagTrend> tagTrends,
                                    Map<String, Double> tagSignificance, String visualizationJson) {
        StringBuilder json = new StringBuilder();
        int totalTags = tagConcordance.values().stream().mapToInt(Integer::intValue).sum();
        int uniqueTags = tagConcordance.size();
        
        json.append("{\n");
        json.append("  \"tagConcordanceReport\": {\n");
        
        // Summary statistics
        json.append("    \"summary\": {\n");
        json.append("      \"totalTagsUsed\": ").append(totalTags).append(",\n");
        json.append("      \"uniqueTags\": ").append(uniqueTags).append(",\n");
        json.append("      \"features\": ").append(features.size()).append(",\n");
        json.append("      \"averageTagsPerFeature\": ")
            .append(String.format("%.2f", features.isEmpty() ? 0 : (double) totalTags / features.size()))
            .append("\n");
        json.append("    },\n");
        
        // Tag frequency with trend and significance data
        json.append("    \"tagFrequency\": [\n");
        
        // Sort tags by count (descending)
        List<Map.Entry<String, Integer>> sortedTags = tagConcordance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        for (int i = 0; i < sortedTags.size(); i++) {
            Map.Entry<String, Integer> entry = sortedTags.get(i);
            String tag = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalTags;
            
            // Get trend and significance for this tag
            String trend = tagTrends.containsKey(tag) ? tagTrends.get(tag).getTrend() : "Unknown";
            double significance = tagSignificance.getOrDefault(tag, 0.0);
            double growthRate = tagTrends.containsKey(tag) ? tagTrends.get(tag).getGrowthRate() : 0.0;
            
            json.append("      {\n");
            json.append("        \"tag\": \"").append(escapeJson(tag)).append("\",\n");
            json.append("        \"count\": ").append(count).append(",\n");
            json.append("        \"percentage\": ").append(String.format("%.4f", percentage)).append(",\n");
            json.append("        \"trend\": \"").append(escapeJson(trend)).append("\",\n");
            json.append("        \"growthRate\": ").append(String.format("%.4f", growthRate)).append(",\n");
            json.append("        \"significance\": ").append(String.format("%.4f", significance)).append("\n");
            json.append("      }");
            
            if (i < sortedTags.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("    ],\n");
        
        // Tag co-occurrence metrics
        json.append("    \"coOccurrences\": [\n");
        
        for (int i = 0; i < Math.min(coOccurrences.size(), 20); i++) {
            CoOccurrence co = coOccurrences.get(i);
            
            json.append("      {\n");
            json.append("        \"tag1\": \"").append(escapeJson(co.getTag1())).append("\",\n");
            json.append("        \"tag2\": \"").append(escapeJson(co.getTag2())).append("\",\n");
            json.append("        \"count\": ").append(co.getCount()).append(",\n");
            json.append("        \"coefficient\": ").append(String.format("%.4f", co.getCoefficient())).append("\n");
            json.append("      }");
            
            if (i < Math.min(coOccurrences.size(), 20) - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("    ],\n");
        
        // Tag trends
        json.append("    \"tagTrends\": {\n");
        json.append("      \"rising\": [\n");
        
        // Get rising tags
        List<TagTrend> risingTags = tagTrends.values().stream()
                .filter(t -> t.getGrowthRate() > 0)
                .sorted(Comparator.comparing(TagTrend::getGrowthRate).reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        for (int i = 0; i < risingTags.size(); i++) {
            TagTrend trend = risingTags.get(i);
            
            json.append("        {\n");
            json.append("          \"tag\": \"").append(escapeJson(trend.getTag())).append("\",\n");
            json.append("          \"count\": ").append(trend.getTotalCount()).append(",\n");
            json.append("          \"growthRate\": ").append(String.format("%.4f", trend.getGrowthRate())).append(",\n");
            json.append("          \"scenarioCount\": ").append(trend.getScenarioCount()).append(",\n");
            json.append("          \"featureCount\": ").append(trend.getFeatureCount()).append(",\n");
            json.append("          \"associatedTags\": [");
            
            // Get top 3 associated tags
            List<Map.Entry<String, Integer>> associatedTags = trend.getAssociatedTags().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());
            
            for (int j = 0; j < associatedTags.size(); j++) {
                Map.Entry<String, Integer> assoc = associatedTags.get(j);
                json.append("\n            {\n");
                json.append("              \"tag\": \"").append(escapeJson(assoc.getKey())).append("\",\n");
                json.append("              \"count\": ").append(assoc.getValue()).append("\n");
                json.append("            }");
                
                if (j < associatedTags.size() - 1) {
                    json.append(",");
                }
            }
            
            json.append("\n          ]\n");
            json.append("        }");
            
            if (i < risingTags.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("      ],\n");
        json.append("      \"declining\": [\n");
        
        // Get declining tags
        List<TagTrend> decliningTags = tagTrends.values().stream()
                .filter(t -> t.getGrowthRate() < 0)
                .sorted(Comparator.comparing(TagTrend::getGrowthRate))
                .limit(10)
                .collect(Collectors.toList());
        
        for (int i = 0; i < decliningTags.size(); i++) {
            TagTrend trend = decliningTags.get(i);
            
            json.append("        {\n");
            json.append("          \"tag\": \"").append(escapeJson(trend.getTag())).append("\",\n");
            json.append("          \"count\": ").append(trend.getTotalCount()).append(",\n");
            json.append("          \"growthRate\": ").append(String.format("%.4f", trend.getGrowthRate())).append(",\n");
            json.append("          \"scenarioCount\": ").append(trend.getScenarioCount()).append(",\n");
            json.append("          \"featureCount\": ").append(trend.getFeatureCount()).append(",\n");
            json.append("          \"associatedTags\": [");
            
            // Get top 3 associated tags
            List<Map.Entry<String, Integer>> associatedTags = trend.getAssociatedTags().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());
            
            for (int j = 0; j < associatedTags.size(); j++) {
                Map.Entry<String, Integer> assoc = associatedTags.get(j);
                json.append("\n            {\n");
                json.append("              \"tag\": \"").append(escapeJson(assoc.getKey())).append("\",\n");
                json.append("              \"count\": ").append(assoc.getValue()).append("\n");
                json.append("            }");
                
                if (j < associatedTags.size() - 1) {
                    json.append(",");
                }
            }
            
            json.append("\n          ]\n");
            json.append("        }");
            
            if (i < decliningTags.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("      ]\n");
        json.append("    },\n");
        
        // Tag categories
        json.append("    \"tagCategories\": {\n");
        
        Map<String, List<String>> categorizedTags = categorizeTagsByPrefix(tagConcordance.keySet());
        
        int catIndex = 0;
        for (Map.Entry<String, List<String>> category : categorizedTags.entrySet()) {
            json.append("      \"").append(escapeJson(category.getKey())).append("\": [\n");
            
            List<String> tags = category.getValue();
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                String trend = tagTrends.containsKey(tag) ? tagTrends.get(tag).getTrend() : "Unknown";
                
                json.append("        {\n");
                json.append("          \"tag\": \"").append(escapeJson(tag)).append("\",\n");
                json.append("          \"count\": ").append(tagConcordance.get(tag)).append(",\n");
                json.append("          \"trend\": \"").append(trend).append("\"\n");
                json.append("        }");
                
                if (i < tags.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("      ]");
            
            if (catIndex < categorizedTags.size() - 1) {
                json.append(",");
            }
            json.append("\n");
            
            catIndex++;
        }
        
        json.append("    },\n");
        
        // Low-value tags
        json.append("    \"lowValueTags\": [\n");
        
        List<String> lowValueTags = detectLowValueTags(tagConcordance, features);
        
        for (int i = 0; i < lowValueTags.size(); i++) {
            String tag = lowValueTags.get(i);
            
            json.append("      {\n");
            json.append("        \"tag\": \"").append(escapeJson(tag)).append("\",\n");
            json.append("        \"count\": ").append(tagConcordance.get(tag)).append("\n");
            json.append("      }");
            
            if (i < lowValueTags.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("    ],\n");
        
        // Significant tags
        json.append("    \"significantTags\": [\n");
        
        // Sort tags by significance (descending)
        List<Map.Entry<String, Double>> significantTags = tagSignificance.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(15)
                .collect(Collectors.toList());
        
        for (int i = 0; i < significantTags.size(); i++) {
            Map.Entry<String, Double> entry = significantTags.get(i);
            String tag = entry.getKey();
            double score = entry.getValue();
            
            json.append("      {\n");
            json.append("        \"tag\": \"").append(escapeJson(tag)).append("\",\n");
            json.append("        \"count\": ").append(tagConcordance.getOrDefault(tag, 0)).append(",\n");
            json.append("        \"significance\": ").append(String.format("%.4f", score)).append("\n");
            json.append("      }");
            
            if (i < significantTags.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("    ],\n");
        
        // Visualization data (D3.js compatible)
        json.append("    \"visualization\": ").append(visualizationJson).append("\n");
        
        json.append("  }\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Categorize tags by their prefix (e.g., @P0, @P1 would be in the "P" category).
     */
    private Map<String, List<String>> categorizeTagsByPrefix(Set<String> tags) {
        Map<String, List<String>> categories = new TreeMap<>();
        
        // Define common tag categories
        categories.put("Priority", new ArrayList<>());
        categories.put("Feature Type", new ArrayList<>());
        categories.put("Status", new ArrayList<>());
        categories.put("Other", new ArrayList<>());
        
        // Priority tags
        List<String> priorityPatterns = Arrays.asList("@P0", "@P1", "@P2", "@P3", "@P4", 
                "@Critical", "@High", "@Medium", "@Low");
        
        // Feature type tags
        List<String> featureTypePatterns = Arrays.asList("@UI", "@API", "@Backend", "@Frontend", 
                "@Integration", "@Unit", "@Performance", "@Security", "@Regression", "@Smoke", 
                "@E2E", "@CRUD");
        
        // Status tags
        List<String> statusPatterns = Arrays.asList("@WIP", "@Ready", "@Review", "@Flaky", 
                "@Deprecated", "@Legacy", "@Todo", "@Debug");
        
        for (String tag : tags) {
            if (priorityPatterns.stream().anyMatch(tag::startsWith)) {
                categories.get("Priority").add(tag);
            } else if (featureTypePatterns.stream().anyMatch(tag::equals)) {
                categories.get("Feature Type").add(tag);
            } else if (statusPatterns.stream().anyMatch(tag::equals)) {
                categories.get("Status").add(tag);
            } else {
                // Find prefix (characters after @ and before any digits or other separators)
                String prefix = "Other";
                if (tag.startsWith("@")) {
                    String tagWithoutAt = tag.substring(1);
                    int prefixEnd = 0;
                    
                    while (prefixEnd < tagWithoutAt.length() && 
                           Character.isLetter(tagWithoutAt.charAt(prefixEnd))) {
                        prefixEnd++;
                    }
                    
                    if (prefixEnd > 0) {
                        prefix = tagWithoutAt.substring(0, prefixEnd);
                    }
                }
                
                if (!categories.containsKey(prefix)) {
                    categories.put(prefix, new ArrayList<>());
                }
                
                categories.get(prefix).add(tag);
            }
        }
        
        // Remove empty categories
        categories.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        // Sort tags within each category
        for (List<String> tagList : categories.values()) {
            Collections.sort(tagList);
        }
        
        return categories;
    }
    
    /**
     * Detect potentially low-value tags based on several heuristics.
     */
    private List<String> detectLowValueTags(Map<String, Integer> tagConcordance, List<Feature> features) {
        List<String> lowValueTags = new ArrayList<>();
        int totalFeatures = features.size();
        
        // Define known low-value tag patterns
        List<String> lowValuePatterns = Arrays.asList(
                "@test", "@tests", "@feature", "@scenario", "@cucumber", "@gherkin",
                "@debug", "@temp", "@temporary", "@todo", "@fixme", "@workaround"
        );
        
        for (Map.Entry<String, Integer> entry : tagConcordance.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();
            
            // Check for known low-value patterns
            if (lowValuePatterns.contains(tag.toLowerCase())) {
                lowValueTags.add(tag);
                continue;
            }
            
            // Tags that appear on almost every feature (>90%) might be too generic
            if (totalFeatures > 5 && count >= totalFeatures * 0.9) {
                lowValueTags.add(tag);
                continue;
            }
            
            // Tags that only appear once might be typos or too specific
            if (count == 1 && totalFeatures > 3) {
                lowValueTags.add(tag);
            }
        }
        
        Collections.sort(lowValueTags);
        return lowValueTags;
    }
    
    /**
     * Find common tag combinations across scenarios.
     */
    private Map<Set<String>, Integer> findTagCombinations(List<Feature> features) {
        Map<Set<String>, Integer> combinations = new HashMap<>();
        
        for (Feature feature : features) {
            // Include feature tags
            Set<String> featureTags = new HashSet<>(feature.getTags());
            
            // Process each scenario
            for (var scenario : feature.getScenarios()) {
                // Create a set with both feature and scenario tags
                Set<String> combinedTags = new HashSet<>(featureTags);
                combinedTags.addAll(scenario.getTags());
                
                // Only count combinations of 2 or more tags
                if (combinedTags.size() >= 2) {
                    combinations.merge(combinedTags, 1, Integer::sum);
                }
            }
        }
        
        return combinations;
    }
    
    /**
     * Format a set of tags for display.
     */
    private String formatTagSet(Set<String> tags) {
        StringBuilder sb = new StringBuilder();
        
        List<String> tagList = new ArrayList<>(tags);
        Collections.sort(tagList);
        
        for (int i = 0; i < tagList.size(); i++) {
            sb.append("`").append(tagList.get(i)).append("`");
            
            if (i < tagList.size() - 1) {
                sb.append(", ");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generate a tag concordance report in JUnit XML format.
     */
    private String generateJUnitXmlReport(Map<String, Integer> tagConcordance, List<Feature> features) {
        JUnitFormatter formatter = new JUnitFormatter();
        return formatter.generateConcordanceReport(tagConcordance, features);
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