package com.heymumford.ftoc.analyzer;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import com.heymumford.ftoc.parser.FeatureParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TagQualityAnalyzerTest {

    private TagQualityAnalyzer analyzer;
    private Map<String, Integer> tagConcordance;
    private List<Feature> features;

    @BeforeEach
    public void setUp() {
        // Create sample feature for testing
        Feature feature = new Feature("test.feature");
        feature.setName("Test Feature");
        feature.addTag("@Feature");
        feature.addTag("@Test");

        // Create scenario with missing priority tag
        Scenario scenarioMissingPriority = new Scenario("Missing Priority Tag", "Scenario", 10);
        scenarioMissingPriority.addTag("@UI");
        scenarioMissingPriority.addTag("@Smoke");
        scenarioMissingPriority.addStep("Given I have no priority tag");
        feature.addScenario(scenarioMissingPriority);

        // Create scenario with missing type tag
        Scenario scenarioMissingType = new Scenario("Missing Type Tag", "Scenario", 20);
        scenarioMissingType.addTag("@P1");
        scenarioMissingType.addTag("@Smoke");
        scenarioMissingType.addStep("Given I have no type tag");
        feature.addScenario(scenarioMissingType);

        // Create scenario with typo in tag
        Scenario scenarioWithTypo = new Scenario("Tag With Typo", "Scenario", 30);
        scenarioWithTypo.addTag("@P1");
        scenarioWithTypo.addTag("@Regressionn");
        scenarioWithTypo.addStep("Given I have a tag with typo");
        feature.addScenario(scenarioWithTypo);

        // Create scenario with duplicate tags
        Scenario scenarioWithDuplicates = new Scenario("Duplicate Tags", "Scenario", 40);
        scenarioWithDuplicates.addTag("@P1");
        scenarioWithDuplicates.addTag("@UI");
        scenarioWithDuplicates.addTag("@P1");  // Duplicate tag
        scenarioWithDuplicates.addStep("Given I have duplicate tags");
        feature.addScenario(scenarioWithDuplicates);

        // Create scenario with excessive tags
        Scenario scenarioWithExcessiveTags = new Scenario("Excessive Tags", "Scenario", 50);
        scenarioWithExcessiveTags.addTag("@P0");
        scenarioWithExcessiveTags.addTag("@UI");
        scenarioWithExcessiveTags.addTag("@Smoke");
        scenarioWithExcessiveTags.addTag("@Regression");
        scenarioWithExcessiveTags.addTag("@API");
        scenarioWithExcessiveTags.addTag("@Backend");
        scenarioWithExcessiveTags.addTag("@Frontend");
        scenarioWithExcessiveTags.addTag("@Integration");
        scenarioWithExcessiveTags.addTag("@Flaky");
        scenarioWithExcessiveTags.addTag("@Debug");
        scenarioWithExcessiveTags.addTag("@Security");
        scenarioWithExcessiveTags.addStep("Given I have too many tags");
        feature.addScenario(scenarioWithExcessiveTags);

        features = List.of(feature);

        // Create tag concordance
        tagConcordance = new HashMap<>();
        tagConcordance.put("@Feature", 1);
        tagConcordance.put("@Test", 1);
        tagConcordance.put("@UI", 3);
        tagConcordance.put("@Smoke", 2);
        tagConcordance.put("@P1", 4);
        tagConcordance.put("@Regressionn", 1);
        tagConcordance.put("@P0", 1);
        tagConcordance.put("@Regression", 1);
        tagConcordance.put("@API", 1);
        tagConcordance.put("@Backend", 1);
        tagConcordance.put("@Frontend", 1);
        tagConcordance.put("@Integration", 1);
        tagConcordance.put("@Flaky", 1);
        tagConcordance.put("@Debug", 1);
        tagConcordance.put("@Security", 1);

        // Create analyzer
        analyzer = new TagQualityAnalyzer(tagConcordance, features);
    }

    @Test
    public void testAnalysisGeneratesWarnings() {
        List<TagQualityAnalyzer.Warning> warnings = analyzer.analyzeTagQuality();
        
        assertFalse(warnings.isEmpty(), "Analysis should generate warnings");
        assertTrue(warnings.size() >= 5, "Analysis should find at least 5 issues");
        
        // Verify warning types
        boolean foundMissingPriority = false;
        boolean foundMissingType = false;
        boolean foundTypo = false;
        boolean foundDuplicate = false;
        boolean foundExcessive = false;
        
        for (TagQualityAnalyzer.Warning warning : warnings) {
            System.out.println("Found warning: " + warning.getType() + " - " + warning.getMessage());
            if (warning.getType() == TagQualityAnalyzer.WarningType.MISSING_PRIORITY_TAG) {
                foundMissingPriority = true;
            } else if (warning.getType() == TagQualityAnalyzer.WarningType.MISSING_TYPE_TAG) {
                foundMissingType = true;
            } else if (warning.getType() == TagQualityAnalyzer.WarningType.TAG_TYPO) {
                foundTypo = true;
            } else if (warning.getType() == TagQualityAnalyzer.WarningType.DUPLICATE_TAG) {
                foundDuplicate = true;
            } else if (warning.getType() == TagQualityAnalyzer.WarningType.EXCESSIVE_TAGS) {
                foundExcessive = true;
            }
        }
        
        assertTrue(foundMissingPriority, "Should find missing priority tag");
        assertTrue(foundMissingType, "Should find missing type tag");
        assertTrue(foundTypo, "Should find tag typo");
        assertTrue(foundDuplicate, "Should find duplicate tag");
        assertTrue(foundExcessive, "Should find excessive tags");
    }
    
    @Test
    public void testWarningReportGeneration() {
        List<TagQualityAnalyzer.Warning> warnings = analyzer.analyzeTagQuality();
        String report = analyzer.generateWarningReport(warnings);
        
        assertNotNull(report);
        assertFalse(report.isEmpty());
        assertTrue(report.contains("TAG QUALITY WARNINGS"), "Report should have a header");
        assertTrue(report.contains("Found "), "Report should mention number of issues");
        
        // Check for warning types in the report
        assertTrue(report.contains("MISSING PRIORITY TAG") || 
                 report.contains("Missing priority tag"), 
                 "Report should mention missing priority tags");
        
        assertTrue(report.contains("MISSING TYPE TAG") || 
                 report.contains("Missing type tag"), 
                 "Report should mention missing type tags");
        
        assertTrue(report.contains("TAG TYPO") || 
                 report.contains("tag typo"), 
                 "Report should mention tag typos");
        
        assertTrue(report.contains("DUPLICATE TAG") || 
                 report.contains("Duplicate tag"), 
                 "Report should mention duplicate tags");
        
        assertTrue(report.contains("EXCESSIVE TAGS") || 
                 report.contains("Excessive tags"), 
                 "Report should mention excessive tags");
        
        // Print the first part of the report for debugging
        System.out.println("Report preview: " + report.substring(0, Math.min(report.length(), 500)));
    }

    @Test
    public void testWithRealFeatureFile() {
        try {
            // Parse a real feature file with tag issues
            File testFile = new File("src/test/resources/ftoc/test-feature-files/tag_issues.feature");
            assertTrue(testFile.exists(), "Test feature file should exist");
            
            FeatureParser parser = new FeatureParser();
            Feature feature = parser.parseFeatureFile(testFile);
            
            // Create concordance from the feature
            Map<String, Integer> concordance = new HashMap<>();
            for (String tag : feature.getTags()) {
                concordance.merge(tag, 1, Integer::sum);
            }
            
            for (Scenario scenario : feature.getScenarios()) {
                for (String tag : scenario.getTags()) {
                    concordance.merge(tag, 1, Integer::sum);
                }
            }
            
            // Create analyzer with just this feature
            TagQualityAnalyzer singleFileAnalyzer = new TagQualityAnalyzer(concordance, List.of(feature));
            
            // Analyze and generate report
            List<TagQualityAnalyzer.Warning> warnings = singleFileAnalyzer.analyzeTagQuality();
            String report = singleFileAnalyzer.generateWarningReport(warnings);
            
            System.out.println("Test feature file analysis found " + warnings.size() + " issues");
            System.out.println("Report snippet: " + report.substring(0, Math.min(report.length(), 500)));

            assertFalse(warnings.isEmpty(), "Should find warnings in the feature file");
            assertNotNull(report);
            assertFalse(report.isEmpty());
            
            warnings.forEach(warning -> System.out.println(warning.getType() + ": " + warning.getMessage()));
        } catch (Exception e) {
            fail("Exception while testing with real feature file: " + e.getMessage());
        }
    }
}