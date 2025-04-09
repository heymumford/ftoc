package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;

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
        JSON
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
        switch (format) {
            case PLAIN_TEXT:
                return generatePlainTextReport(tagConcordance, features);
            case MARKDOWN:
                return generateMarkdownReport(tagConcordance, features);
            case HTML:
                return generateHtmlReport(tagConcordance, features);
            case JSON:
                return generateJsonReport(tagConcordance, features);
            default:
                return generatePlainTextReport(tagConcordance, features);
        }
    }
    
    /**
     * Generate a tag concordance report in plain text format.
     */
    private String generatePlainTextReport(Map<String, Integer> tagConcordance, List<Feature> features) {
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
        report.append(String.format("%-30s %-10s %-10s\n", "Tag", "Count", "Percent"));
        report.append(String.format("%-30s %-10s %-10s\n", 
                "------------------------------", "----------", "----------"));
        
        DecimalFormat df = new DecimalFormat("0.0%");
        
        // Sort tags by count (descending)
        List<Map.Entry<String, Integer>> sortedTags = tagConcordance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        for (Map.Entry<String, Integer> entry : sortedTags) {
            String tag = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalTags;
            
            report.append(String.format("%-30s %-10d %-10s\n", 
                    tag, count, df.format(percentage)));
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
        
        // Tag co-occurrence information (simplified version)
        report.append("COMMON TAG COMBINATIONS\n");
        report.append("----------------------\n");
        Map<Set<String>, Integer> tagCombinations = findTagCombinations(features);
        
        if (tagCombinations.isEmpty()) {
            report.append("No common tag combinations found.\n");
        } else {
            // Display top 10 tag combinations
            tagCombinations.entrySet().stream()
                    .sorted(Map.Entry.<Set<String>, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        report.append(String.format("%s (%d occurrences)\n", 
                                entry.getKey(), entry.getValue()));
                    });
        }
        
        return report.toString();
    }
    
    /**
     * Generate a tag concordance report in markdown format.
     */
    private String generateMarkdownReport(Map<String, Integer> tagConcordance, List<Feature> features) {
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
        report.append("| Tag | Count | Percent |\n");
        report.append("|-----|-------|--------|\n");
        
        DecimalFormat df = new DecimalFormat("0.0%");
        
        // Sort tags by count (descending)
        List<Map.Entry<String, Integer>> sortedTags = tagConcordance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        for (Map.Entry<String, Integer> entry : sortedTags) {
            String tag = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalTags;
            
            report.append(String.format("| `%s` | %d | %s |\n", 
                    tag, count, df.format(percentage)));
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
        
        // Tag co-occurrence information (simplified version)
        report.append("## Common Tag Combinations\n\n");
        Map<Set<String>, Integer> tagCombinations = findTagCombinations(features);
        
        if (tagCombinations.isEmpty()) {
            report.append("No common tag combinations found.\n");
        } else {
            // Display top 10 tag combinations
            tagCombinations.entrySet().stream()
                    .sorted(Map.Entry.<Set<String>, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        report.append(String.format("- %s (%d occurrences)\n", 
                                formatTagSet(entry.getKey()), entry.getValue()));
                    });
        }
        
        return report.toString();
    }
    
    /**
     * Generate a tag concordance report in HTML format.
     */
    private String generateHtmlReport(Map<String, Integer> tagConcordance, List<Feature> features) {
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
        report.append("    .category { background-color: #dff0d8; }\n");
        report.append("    .summary { background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n");
        report.append("    .progress-bar { background-color: #f2f2f2; border-radius: 4px; height: 20px; margin-top: 5px; }\n");
        report.append("    .progress-value { background-color: #3c7a89; border-radius: 4px; height: 20px; }\n");
        report.append("  </style>\n");
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
        
        // Tag frequency table
        report.append("  <h2>Tag Frequency</h2>\n");
        report.append("  <table>\n");
        report.append("    <tr>\n");
        report.append("      <th>Tag</th>\n");
        report.append("      <th>Count</th>\n");
        report.append("      <th>Percent</th>\n");
        report.append("      <th>Distribution</th>\n");
        report.append("    </tr>\n");
        
        DecimalFormat df = new DecimalFormat("0.0%");
        
        // Sort tags by count (descending)
        List<Map.Entry<String, Integer>> sortedTags = tagConcordance.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        for (Map.Entry<String, Integer> entry : sortedTags) {
            String tag = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalTags;
            int barWidth = (int) (percentage * 100);
            
            report.append("    <tr>\n");
            report.append("      <td><span class=\"tag\">").append(tag).append("</span></td>\n");
            report.append("      <td>").append(count).append("</td>\n");
            report.append("      <td>").append(df.format(percentage)).append("</td>\n");
            report.append("      <td>\n");
            report.append("        <div class=\"progress-bar\">\n");
            report.append("          <div class=\"progress-value\" style=\"width: ").append(barWidth).append("%;\"></div>\n");
            report.append("        </div>\n");
            report.append("      </td>\n");
            report.append("    </tr>\n");
        }
        
        report.append("  </table>\n");
        
        // Tag categories analysis
        report.append("  <h2>Tag Categories</h2>\n");
        
        Map<String, List<String>> categorizedTags = categorizeTagsByPrefix(tagConcordance.keySet());
        
        for (Map.Entry<String, List<String>> category : categorizedTags.entrySet()) {
            report.append("  <h3>").append(category.getKey()).append(" Tags (")
                  .append(category.getValue().size()).append(")</h3>\n");
            report.append("  <ul>\n");
            
            for (String tag : category.getValue()) {
                report.append("    <li><span class=\"tag category\">").append(tag)
                      .append("</span> (").append(tagConcordance.get(tag)).append(")</li>\n");
            }
            
            report.append("  </ul>\n");
        }
        
        // Low-value tag detection
        report.append("  <h2>Potentially Low-Value Tags</h2>\n");
        List<String> lowValueTags = detectLowValueTags(tagConcordance, features);
        
        if (lowValueTags.isEmpty()) {
            report.append("  <p>No low-value tags detected.</p>\n");
        } else {
            report.append("  <ul>\n");
            for (String tag : lowValueTags) {
                report.append("    <li><span class=\"tag low-value\">").append(tag)
                      .append("</span> (Count: ").append(tagConcordance.get(tag)).append(")</li>\n");
            }
            report.append("  </ul>\n");
        }
        
        // Tag co-occurrence information (simplified version)
        report.append("  <h2>Common Tag Combinations</h2>\n");
        Map<Set<String>, Integer> tagCombinations = findTagCombinations(features);
        
        if (tagCombinations.isEmpty()) {
            report.append("  <p>No common tag combinations found.</p>\n");
        } else {
            report.append("  <table>\n");
            report.append("    <tr>\n");
            report.append("      <th>Combination</th>\n");
            report.append("      <th>Occurrences</th>\n");
            report.append("    </tr>\n");
            
            // Display top 10 tag combinations
            tagCombinations.entrySet().stream()
                    .sorted(Map.Entry.<Set<String>, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        report.append("    <tr>\n");
                        report.append("      <td>\n");
                        
                        for (String tag : entry.getKey()) {
                            report.append("        <span class=\"tag\">").append(tag).append("</span>\n");
                        }
                        
                        report.append("      </td>\n");
                        report.append("      <td>").append(entry.getValue()).append("</td>\n");
                        report.append("    </tr>\n");
                    });
            
            report.append("  </table>\n");
        }
        
        report.append("</body>\n");
        report.append("</html>");
        
        return report.toString();
    }
    
    /**
     * Generate a tag concordance report in JSON format.
     */
    private String generateJsonReport(Map<String, Integer> tagConcordance, List<Feature> features) {
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
        
        // Tag frequency
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
            
            json.append("      {\n");
            json.append("        \"tag\": \"").append(escapeJson(tag)).append("\",\n");
            json.append("        \"count\": ").append(count).append(",\n");
            json.append("        \"percentage\": ").append(String.format("%.4f", percentage)).append("\n");
            json.append("      }");
            
            if (i < sortedTags.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("    ],\n");
        
        // Tag categories
        json.append("    \"tagCategories\": {\n");
        
        Map<String, List<String>> categorizedTags = categorizeTagsByPrefix(tagConcordance.keySet());
        
        int catIndex = 0;
        for (Map.Entry<String, List<String>> category : categorizedTags.entrySet()) {
            json.append("      \"").append(escapeJson(category.getKey())).append("\": [\n");
            
            List<String> tags = category.getValue();
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                
                json.append("        {\n");
                json.append("          \"tag\": \"").append(escapeJson(tag)).append("\",\n");
                json.append("          \"count\": ").append(tagConcordance.get(tag)).append("\n");
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
        
        // Tag combinations
        json.append("    \"tagCombinations\": [\n");
        
        Map<Set<String>, Integer> tagCombinations = findTagCombinations(features);
        
        // Display top 10 tag combinations
        List<Map.Entry<Set<String>, Integer>> topCombinations = tagCombinations.entrySet().stream()
                .sorted(Map.Entry.<Set<String>, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        for (int i = 0; i < topCombinations.size(); i++) {
            Map.Entry<Set<String>, Integer> entry = topCombinations.get(i);
            
            json.append("      {\n");
            json.append("        \"tags\": [");
            
            List<String> tagList = new ArrayList<>(entry.getKey());
            for (int j = 0; j < tagList.size(); j++) {
                json.append("\"").append(escapeJson(tagList.get(j))).append("\"");
                
                if (j < tagList.size() - 1) {
                    json.append(", ");
                }
            }
            
            json.append("],\n");
            json.append("        \"occurrences\": ").append(entry.getValue()).append("\n");
            json.append("      }");
            
            if (i < topCombinations.size() - 1) {
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