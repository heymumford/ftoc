package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConcordanceAnalyzerTest {

    @Test
    public void testCalculateCoOccurrences() {
        // Create test features with tags
        List<Feature> features = createTestFeatures();
        
        // Calculate co-occurrences
        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences = ConcordanceAnalyzer.calculateCoOccurrences(features);
        
        // Verify results
        assertNotNull(coOccurrences);
        assertFalse(coOccurrences.isEmpty());
        
        // Find a specific co-occurrence
        Optional<ConcordanceAnalyzer.CoOccurrence> apiAndP1 = coOccurrences.stream()
                .filter(co -> (co.getTag1().equals("@API") && co.getTag2().equals("@P1")) || 
                             (co.getTag1().equals("@P1") && co.getTag2().equals("@API")))
                .findFirst();
        
        assertTrue(apiAndP1.isPresent());
        assertEquals(2, apiAndP1.get().getCount());
        assertTrue(apiAndP1.get().getCoefficient() > 0);
    }
    
    @Test
    public void testCalculateTagTrends() {
        // Create test features with tags
        List<Feature> features = createTestFeatures();
        
        // Create tag concordance map
        Map<String, Integer> tagConcordance = new HashMap<>();
        tagConcordance.put("@P0", 1);
        tagConcordance.put("@P1", 2);
        tagConcordance.put("@P2", 1);
        tagConcordance.put("@API", 2);
        tagConcordance.put("@UI", 1);
        tagConcordance.put("@Regression", 2);
        
        // Calculate tag trends
        Map<String, ConcordanceAnalyzer.TagTrend> trends = ConcordanceAnalyzer.calculateTagTrends(features, tagConcordance);
        
        // Verify results
        assertNotNull(trends);
        assertEquals(6, trends.size());
        
        // Check a specific trend
        assertTrue(trends.containsKey("@API"));
        assertEquals(2, trends.get("@API").getTotalCount());
        assertNotNull(trends.get("@API").getTrend());
        assertFalse(trends.get("@API").getAssociatedTags().isEmpty());
    }
    
    @Test
    public void testGenerateVisualizationJson() {
        // Create test features with tags
        List<Feature> features = createTestFeatures();
        
        // Create co-occurrences
        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences = ConcordanceAnalyzer.calculateCoOccurrences(features);
        
        // Create tag concordance map
        Map<String, Integer> tagConcordance = new HashMap<>();
        tagConcordance.put("@P0", 1);
        tagConcordance.put("@P1", 2);
        tagConcordance.put("@P2", 1);
        tagConcordance.put("@API", 2);
        tagConcordance.put("@UI", 1);
        tagConcordance.put("@Regression", 2);
        
        // Generate visualization JSON
        String visualizationJson = ConcordanceAnalyzer.generateVisualizationJson(coOccurrences, tagConcordance);
        
        // Verify results
        assertNotNull(visualizationJson);
        assertTrue(visualizationJson.contains("nodes"));
        assertTrue(visualizationJson.contains("links"));
        assertTrue(visualizationJson.contains("\"id\": \"@API\""));
    }
    
    @Test
    public void testCalculateTagSignificance() {
        // Create test features with tags
        List<Feature> features = createTestFeatures();
        
        // Create tag concordance map
        Map<String, Integer> tagConcordance = new HashMap<>();
        tagConcordance.put("@P0", 1);
        tagConcordance.put("@P1", 2);
        tagConcordance.put("@P2", 1);
        tagConcordance.put("@API", 2);
        tagConcordance.put("@UI", 1);
        tagConcordance.put("@Regression", 2);
        
        // Calculate tag significance
        Map<String, Double> significance = ConcordanceAnalyzer.calculateTagSignificance(features, tagConcordance);
        
        // Verify results
        assertNotNull(significance);
        assertEquals(6, significance.size());
        
        // All scores should be positive
        for (Double score : significance.values()) {
            assertTrue(score >= 0);
        }
    }
    
    private List<Feature> createTestFeatures() {
        List<Feature> features = new ArrayList<>();
        
        // Feature 1
        Feature feature1 = new Feature();
        feature1.setFile("feature1.feature");
        feature1.setName("Feature 1");
        feature1.setTags(Arrays.asList("@P0", "@API"));
        
        Scenario scenario1 = new Scenario();
        scenario1.setName("Scenario 1");
        scenario1.setTags(Arrays.asList("@API", "@Regression"));
        
        Scenario scenario2 = new Scenario();
        scenario2.setName("Scenario 2");
        scenario2.setTags(Arrays.asList("@P1", "@API"));
        
        feature1.setScenarios(Arrays.asList(scenario1, scenario2));
        
        // Feature 2
        Feature feature2 = new Feature();
        feature2.setFile("feature2.feature");
        feature2.setName("Feature 2");
        feature2.setTags(Arrays.asList("@P1", "@UI"));
        
        Scenario scenario3 = new Scenario();
        scenario3.setName("Scenario 3");
        scenario3.setTags(Arrays.asList("@P2", "@Regression"));
        
        feature2.setScenarios(Arrays.asList(scenario3));
        
        features.add(feature1);
        features.add(feature2);
        
        return features;
    }
}