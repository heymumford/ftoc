package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.analyzer.FeatureAntiPatternAnalyzer;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Formatter for generating JUnit XML reports for various analysis results.
 * This provides compatibility with CI/CD systems that support JUnit XML format.
 */
public class JUnitFormatter {

    /**
     * Generate a JUnit XML report for tag quality analysis.
     * Each warning is represented as a test case failure, and the entire
     * report is wrapped in a test suite.
     *
     * @param warnings List of tag quality warnings
     * @return JUnit XML report as a string
     */
    public String generateTagQualityReport(List<TagQualityAnalyzer.Warning> warnings) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        int failureCount = warnings.size();
        int totalTests = failureCount + 1; // Add one successful test when there are no issues
        
        xml.append("<testsuite name=\"FTOC Tag Quality Analysis\" ");
        xml.append("tests=\"").append(totalTests).append("\" ");
        xml.append("failures=\"").append(failureCount).append("\" ");
        xml.append("errors=\"0\" ");
        xml.append("skipped=\"0\" ");
        xml.append("timestamp=\"").append(getCurrentTimestamp()).append("\">\n");
        
        // Add a property section for metadata
        xml.append("  <properties>\n");
        xml.append("    <property name=\"analysis_type\" value=\"tag_quality\"/>\n");
        xml.append("  </properties>\n");
        
        // If there are no warnings, add a successful test
        if (warnings.isEmpty()) {
            xml.append("  <testcase name=\"tag_quality_check\" classname=\"com.heymumford.ftoc.TagQualityAnalysis\">\n");
            xml.append("    <system-out>All tag quality checks passed.</system-out>\n");
            xml.append("  </testcase>\n");
        } else {
            // Group warnings by type for better organization
            Map<String, List<TagQualityAnalyzer.Warning>> warningsByType = 
                warnings.stream().collect(Collectors.groupingBy(w -> w.getType().name()));
            
            for (Map.Entry<String, List<TagQualityAnalyzer.Warning>> entry : warningsByType.entrySet()) {
                String warningType = entry.getKey();
                List<TagQualityAnalyzer.Warning> typeWarnings = entry.getValue();
                
                xml.append("  <testcase name=\"tag_quality_check_").append(warningType.toLowerCase()).append("\" ");
                xml.append("classname=\"com.heymumford.ftoc.TagQualityAnalysis\">\n");
                
                xml.append("    <failure type=\"").append(warningType).append("\" ");
                xml.append("message=\"Found ").append(typeWarnings.size()).append(" ").append(warningType).append(" issues\">");
                
                // Add the warnings as the failure message content
                xml.append(escapeXml(generateWarningDetails(typeWarnings)));
                
                xml.append("</failure>\n");
                xml.append("  </testcase>\n");
            }
        }
        
        xml.append("</testsuite>");
        return xml.toString();
    }
    
    /**
     * Generate a JUnit XML report for anti-pattern analysis.
     * Each anti-pattern type is represented as a test case failure, and the entire
     * report is wrapped in a test suite.
     *
     * @param warnings List of anti-pattern warnings
     * @return JUnit XML report as a string
     */
    public String generateAntiPatternReport(List<FeatureAntiPatternAnalyzer.Warning> warnings) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        int failureCount = warnings.size() > 0 ? 1 : 0;  // One failure if there are any warnings
        int totalTests = 1; // We have one test that passes or fails
        
        xml.append("<testsuite name=\"FTOC Anti-Pattern Analysis\" ");
        xml.append("tests=\"").append(totalTests).append("\" ");
        xml.append("failures=\"").append(failureCount).append("\" ");
        xml.append("errors=\"0\" ");
        xml.append("skipped=\"0\" ");
        xml.append("timestamp=\"").append(getCurrentTimestamp()).append("\">\n");
        
        // Add a property section for metadata
        xml.append("  <properties>\n");
        xml.append("    <property name=\"analysis_type\" value=\"anti_pattern\"/>\n");
        xml.append("  </properties>\n");
        
        // Single test case for anti-pattern analysis
        xml.append("  <testcase name=\"anti_pattern_check\" classname=\"com.heymumford.ftoc.AntiPatternAnalysis\">\n");
        
        // If there are warnings, mark as failure
        if (!warnings.isEmpty()) {
            xml.append("    <failure type=\"AntiPatternDetected\" ");
            xml.append("message=\"Found ").append(warnings.size()).append(" anti-pattern issues\">");
            
            // Add the warnings as the failure message content
            xml.append(escapeXml(generateAntiPatternDetails(warnings)));
            
            xml.append("</failure>\n");
        } else {
            xml.append("    <system-out>No anti-pattern issues were detected.</system-out>\n");
        }
        
        xml.append("  </testcase>\n");
        xml.append("</testsuite>");
        return xml.toString();
    }
    
    /**
     * Generate a JUnit XML report for tag concordance.
     * This doesn't really fit JUnit's model of tests, but we create a report with
     * informational test cases for compatibility with CI systems.
     *
     * @param tagConcordance Map of tags to their occurrence counts
     * @param features List of features analyzed
     * @return JUnit XML report as a string
     */
    public String generateConcordanceReport(Map<String, Integer> tagConcordance, List<Feature> features) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        int totalTests = 1;  // One informational test case
        
        xml.append("<testsuite name=\"FTOC Tag Concordance\" ");
        xml.append("tests=\"").append(totalTests).append("\" ");
        xml.append("failures=\"0\" ");
        xml.append("errors=\"0\" ");
        xml.append("skipped=\"0\" ");
        xml.append("timestamp=\"").append(getCurrentTimestamp()).append("\">\n");
        
        // Add a property section for metadata
        xml.append("  <properties>\n");
        xml.append("    <property name=\"analysis_type\" value=\"tag_concordance\"/>\n");
        xml.append("    <property name=\"features_analyzed\" value=\"").append(features.size()).append("\"/>\n");
        xml.append("    <property name=\"unique_tags\" value=\"").append(tagConcordance.size()).append("\"/>\n");
        xml.append("  </properties>\n");
        
        // Single test case for concordance info
        xml.append("  <testcase name=\"tag_concordance_info\" classname=\"com.heymumford.ftoc.TagConcordance\">\n");
        xml.append("    <system-out>");
        
        // Add tag concordance information to system-out
        for (Map.Entry<String, Integer> entry : tagConcordance.entrySet()) {
            xml.append(escapeXml("Tag: " + entry.getKey() + ", Count: " + entry.getValue() + "\n"));
        }
        
        xml.append("</system-out>\n");
        xml.append("  </testcase>\n");
        xml.append("</testsuite>");
        return xml.toString();
    }
    
    /**
     * Generate a JUnit XML report for the table of contents.
     * Similar to concordance, this doesn't really represent tests, but we create
     * an informational report for CI system compatibility.
     *
     * @param features List of features to include
     * @param includeTags Tags to include
     * @param excludeTags Tags to exclude
     * @return JUnit XML report as a string
     */
    public String generateTocReport(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        int totalFeatures = features.size();
        int totalScenarios = features.stream()
                .mapToInt(f -> (int) f.getFilteredScenarios(includeTags, excludeTags).stream()
                        .filter(s -> !s.isBackground())
                        .count())
                .sum();
        
        xml.append("<testsuite name=\"FTOC Table of Contents\" ");
        xml.append("tests=\"").append(totalFeatures).append("\" ");
        xml.append("failures=\"0\" ");
        xml.append("errors=\"0\" ");
        xml.append("skipped=\"0\" ");
        xml.append("timestamp=\"").append(getCurrentTimestamp()).append("\">\n");
        
        // Add a property section for metadata
        xml.append("  <properties>\n");
        xml.append("    <property name=\"analysis_type\" value=\"table_of_contents\"/>\n");
        xml.append("    <property name=\"total_features\" value=\"").append(totalFeatures).append("\"/>\n");
        xml.append("    <property name=\"total_scenarios\" value=\"").append(totalScenarios).append("\"/>\n");
        
        if (!includeTags.isEmpty()) {
            xml.append("    <property name=\"include_tags\" value=\"")
               .append(String.join(", ", includeTags))
               .append("\"/>\n");
        }
        
        if (!excludeTags.isEmpty()) {
            xml.append("    <property name=\"exclude_tags\" value=\"")
               .append(String.join(", ", excludeTags))
               .append("\"/>\n");
        }
        
        xml.append("  </properties>\n");
        
        // Create a test case for each feature
        for (Feature feature : features) {
            // Get scenarios filtered by tags
            List<Scenario> filteredScenarios = feature.getFilteredScenarios(includeTags, excludeTags);
            
            // Skip features with no matching scenarios or only backgrounds
            if (filteredScenarios.isEmpty() || 
                    filteredScenarios.stream().allMatch(Scenario::isBackground)) {
                continue;
            }
            
            String featureName = feature.getName().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
            
            xml.append("  <testcase name=\"feature_").append(featureName).append("\" ");
            xml.append("classname=\"com.heymumford.ftoc.TableOfContents\">\n");
            xml.append("    <system-out>");
            
            // Add feature and scenario information to system-out
            xml.append(escapeXml("Feature: " + feature.getName() + "\n"));
            xml.append(escapeXml("File: " + feature.getFilename() + "\n"));
            
            if (!feature.getTags().isEmpty()) {
                xml.append(escapeXml("Tags: " + String.join(", ", feature.getTags()) + "\n"));
            }
            
            xml.append(escapeXml("\nScenarios:\n"));
            
            for (Scenario scenario : filteredScenarios) {
                // Skip background scenarios
                if (scenario.isBackground()) {
                    continue;
                }
                
                String prefix = scenario.isOutline() ? "Scenario Outline: " : "Scenario: ";
                xml.append(escapeXml("- " + prefix + scenario.getName() + "\n"));
                
                if (!scenario.getTags().isEmpty()) {
                    xml.append(escapeXml("  Tags: " + String.join(", ", scenario.getTags()) + "\n"));
                }
                
                // For scenario outlines, show examples summary
                if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                    int totalExamples = scenario.getExamples().stream()
                            .mapToInt(ex -> ex.getRows().size())
                            .sum();
                    xml.append(escapeXml("  Examples: " + totalExamples + "\n"));
                }
            }
            
            xml.append("</system-out>\n");
            xml.append("  </testcase>\n");
        }
        
        xml.append("</testsuite>");
        return xml.toString();
    }
    
    /**
     * Helper method to generate details for tag quality warnings.
     */
    private String generateWarningDetails(List<TagQualityAnalyzer.Warning> warnings) {
        StringBuilder details = new StringBuilder();
        for (TagQualityAnalyzer.Warning warning : warnings) {
            details.append(warning.getType().name()).append(": ");
            details.append(warning.getMessage());
            if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                details.append(" (in ").append(warning.getLocation()).append(")");
            }
            details.append("\n");
        }
        return details.toString();
    }
    
    /**
     * Helper method to generate details for anti-pattern warnings.
     */
    private String generateAntiPatternDetails(List<FeatureAntiPatternAnalyzer.Warning> warnings) {
        StringBuilder details = new StringBuilder();
        for (FeatureAntiPatternAnalyzer.Warning warning : warnings) {
            details.append(warning.getType().name()).append(": ");
            details.append(warning.getMessage());
            if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                details.append(" (in ").append(warning.getLocation()).append(")");
            }
            details.append("\n");
        }
        return details.toString();
    }
    
    /**
     * Get the current timestamp in ISO 8601 format.
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * Escape special characters in XML content.
     */
    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}