package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConcordanceFormatterTest {

    @Test
    public void testGeneratePlainTextReport() {
        // Create test features with tags
        List<Feature> features = createTestFeatures();
        
        // Create tag concordance map
        Map<String, Integer> tagConcordance = createTagConcordanceMap();
        
        // Create formatter
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        
        // Generate plain text report
        String report = formatter.generateConcordanceReport(tagConcordance, features, ConcordanceFormatter.Format.PLAIN_TEXT);
        
        // Verify results
        assertNotNull(report);
        assertFalse(report.isEmpty());
        
        // Check for expected sections
        assertTrue(report.contains("TAG CONCORDANCE REPORT"));
        assertTrue(report.contains("TAG FREQUENCY"));
        assertTrue(report.contains("TAG CATEGORIES"));
        assertTrue(report.contains("TAG CO-OCCURRENCE METRICS"));
        assertTrue(report.contains("TAG TREND ANALYSIS"));
        assertTrue(report.contains("STATISTICALLY SIGNIFICANT TAGS"));
        
        // Check for specific tags
        assertTrue(report.contains("@API"));
        assertTrue(report.contains("@P1"));
        assertTrue(report.contains("@Regression"));
    }
    
    @Test
    public void testGenerateMarkdownReport() {
        // Create test features with tags
        List<Feature> features = createTestFeatures();
        
        // Create tag concordance map
        Map<String, Integer> tagConcordance = createTagConcordanceMap();
        
        // Create formatter
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        
        // Generate markdown report
        String report = formatter.generateConcordanceReport(tagConcordance, features, ConcordanceFormatter.Format.MARKDOWN);
        
        // Verify results
        assertNotNull(report);
        assertFalse(report.isEmpty());
        
        // Check for expected sections
        assertTrue(report.contains("# Tag Concordance Report"));
        assertTrue(report.contains("## Tag Frequency"));
        assertTrue(report.contains("## Tag Categories"));
        assertTrue(report.contains("## Tag Co-occurrence Metrics"));
        assertTrue(report.contains("## Tag Trend Analysis"));
        assertTrue(report.contains("## Statistically Significant Tags"));
        
        // Check for markdown formatting
        assertTrue(report.contains("|"));
        assertTrue(report.contains("`@API`"));
    }
    
    @Test
    public void testGenerateHtmlReport() {
        // Create test features with tags
        List<Feature> features = createTestFeatures();
        
        // Create tag concordance map
        Map<String, Integer> tagConcordance = createTagConcordanceMap();
        
        // Create formatter
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        
        // Generate HTML report
        String report = formatter.generateConcordanceReport(tagConcordance, features, ConcordanceFormatter.Format.HTML);
        
        // Verify results
        assertNotNull(report);
        assertFalse(report.isEmpty());
        
        // Check for expected HTML elements
        assertTrue(report.contains("<!DOCTYPE html>"));
        assertTrue(report.contains("<html>"));
        assertTrue(report.contains("<head>"));
        assertTrue(report.contains("<body>"));
        
        // Check for visualization
        assertTrue(report.contains("d3.js"));
        assertTrue(report.contains("visualization"));
        
        // Check for tabbed interface
        assertTrue(report.contains("tab-button"));
        assertTrue(report.contains("tab-content"));
    }
    
    @Test
    public void testGenerateJsonReport() {
        // Create test features with tags
        List<Feature> features = createTestFeatures();
        
        // Create tag concordance map
        Map<String, Integer> tagConcordance = createTagConcordanceMap();
        
        // Create formatter
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        
        // Generate JSON report
        String report = formatter.generateConcordanceReport(tagConcordance, features, ConcordanceFormatter.Format.JSON);
        
        // Verify results
        assertNotNull(report);
        assertFalse(report.isEmpty());
        
        // Check for expected JSON structure
        assertTrue(report.contains("{"));
        assertTrue(report.contains("}"));
        assertTrue(report.contains("\"tagConcordanceReport\""));
        assertTrue(report.contains("\"tagFrequency\""));
        assertTrue(report.contains("\"coOccurrences\""));
        assertTrue(report.contains("\"tagTrends\""));
        assertTrue(report.contains("\"visualization\""));
        
        // Check for specific tags
        assertTrue(report.contains("\"@API\""));
        assertTrue(report.contains("\"@P1\""));
        assertTrue(report.contains("\"@Regression\""));
    }
    
    private List<Feature> createTestFeatures() {
        List<Feature> features = new ArrayList<>();
        
        // Feature 1
        Feature feature1 = new Feature("feature1.feature");
        feature1.setName("Feature 1");
        feature1.addTag("@P0");
        feature1.addTag("@API");
        
        Scenario scenario1 = new Scenario("Scenario 1", "Scenario", 1);
        scenario1.addTag("@API");
        scenario1.addTag("@Regression");
        
        Scenario scenario2 = new Scenario("Scenario 2", "Scenario", 2);
        scenario2.addTag("@P1");
        scenario2.addTag("@API");
        
        feature1.addScenario(scenario1);
        feature1.addScenario(scenario2);
        
        // Feature 2
        Feature feature2 = new Feature("feature2.feature");
        feature2.setName("Feature 2");
        feature2.addTag("@P1");
        feature2.addTag("@UI");
        
        Scenario scenario3 = new Scenario("Scenario 3", "Scenario", 3);
        scenario3.addTag("@P2");
        scenario3.addTag("@Regression");
        
        feature2.addScenario(scenario3);
        
        features.add(feature1);
        features.add(feature2);
        
        return features;
    }
    
    private Map<String, Integer> createTagConcordanceMap() {
        Map<String, Integer> tagConcordance = new HashMap<>();
        tagConcordance.put("@P0", 1);
        tagConcordance.put("@P1", 2);
        tagConcordance.put("@P2", 1);
        tagConcordance.put("@API", 3);
        tagConcordance.put("@UI", 1);
        tagConcordance.put("@Regression", 2);
        return tagConcordance;
    }
}