package com.heymumford.ftoc.analyzer;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureAntiPatternAnalyzerTest {

    private FeatureAntiPatternAnalyzer analyzer;
    private List<Feature> features;

    @BeforeEach
    public void setUp() {
        // Create sample features for testing
        features = new ArrayList<>();
        
        // Feature 1: Contains various anti-patterns
        Feature feature = new Feature("test-anti-patterns.feature");
        feature.setName("Feature with Anti-Patterns");
        
        // Scenario 1: Too many steps
        Scenario longScenario = new Scenario("Long Scenario", "Scenario", 10);
        longScenario.addStep("Given I am on the login page");
        longScenario.addStep("When I enter username \"user1\"");
        longScenario.addStep("And I enter password \"password123\"");
        longScenario.addStep("And I click the login button");
        longScenario.addStep("Then I should be logged in");
        longScenario.addStep("When I navigate to the dashboard");
        longScenario.addStep("Then I should see my account summary");
        longScenario.addStep("When I click on the reports tab");
        longScenario.addStep("Then I should see the reports list");
        longScenario.addStep("When I click on the first report");
        longScenario.addStep("Then I should see the report details");
        longScenario.addStep("When I download the report");
        longScenario.addStep("Then the file should be downloaded");
        feature.addScenario(longScenario);
        
        // Scenario 2: Missing Given/When/Then
        Scenario incompleteScenario = new Scenario("Incomplete Steps", "Scenario", 30);
        incompleteScenario.addStep("Given I am logged in");
        incompleteScenario.addStep("And I have an active subscription");
        // No When or Then steps
        feature.addScenario(incompleteScenario);
        
        // Scenario 3: UI focused steps
        Scenario uiFocusedScenario = new Scenario("UI Focused", "Scenario", 50);
        uiFocusedScenario.addStep("Given I am on the homepage");
        uiFocusedScenario.addStep("When I click on the menu button");
        uiFocusedScenario.addStep("And I click on the settings link");
        uiFocusedScenario.addStep("And I scroll down to the privacy section");
        uiFocusedScenario.addStep("And I check the checkbox for anonymous data");
        uiFocusedScenario.addStep("And I click the save button");
        uiFocusedScenario.addStep("Then I should see a success message");
        feature.addScenario(uiFocusedScenario);
        
        // Scenario 4: Implementation details
        Scenario implementationScenario = new Scenario("Implementation Details", "Scenario", 70);
        implementationScenario.addStep("Given the API endpoint /users is available");
        implementationScenario.addStep("When I send a GET request with the header X-API-KEY");
        implementationScenario.addStep("Then the HTTP response code should be 200");
        implementationScenario.addStep("And the database should have a record in the users table");
        feature.addScenario(implementationScenario);
        
        // Scenario 5: Outline with few examples
        Scenario outlineScenario = new Scenario("Outline with Few Examples", "Scenario Outline", 90);
        outlineScenario.addStep("Given I have a <subscription_type> subscription");
        outlineScenario.addStep("When I access the <feature_name> feature");
        outlineScenario.addStep("Then I should <outcome>");
        
        Scenario.Example example = new Scenario.Example("Single Example");
        example.setHeaders(List.of("subscription_type", "feature_name", "outcome"));
        example.addRow(List.of("premium", "advanced reports", "have access"));
        outlineScenario.addExample(example);
        
        feature.addScenario(outlineScenario);
        
        // Scenario 6: Incorrect step order
        Scenario wrongOrderScenario = new Scenario("Wrong Step Order", "Scenario", 110);
        wrongOrderScenario.addStep("Given I am logged in");
        wrongOrderScenario.addStep("Then I should see the dashboard");
        wrongOrderScenario.addStep("When I click on my profile");
        feature.addScenario(wrongOrderScenario);
        
        // Scenario 7: Ambiguous language
        Scenario ambiguousScenario = new Scenario("Ambiguous Language", "Scenario", 130);
        ambiguousScenario.addStep("Given I am on the products page");
        ambiguousScenario.addStep("When I add it to the cart");
        ambiguousScenario.addStep("And I proceed to checkout");
        ambiguousScenario.addStep("Then I see it in my order");
        feature.addScenario(ambiguousScenario);
        
        // Scenario 8: Mixed tense
        Scenario mixedTenseScenario = new Scenario("Mixed Tense", "Scenario", 150);
        mixedTenseScenario.addStep("Given I am on the homepage");
        mixedTenseScenario.addStep("When I clicked on the search button");
        mixedTenseScenario.addStep("And I enter a search term");
        mixedTenseScenario.addStep("Then I saw the search results");
        feature.addScenario(mixedTenseScenario);
        
        // Scenario 9: Conjunctions
        Scenario conjunctionScenario = new Scenario("Conjunctions", "Scenario", 170);
        conjunctionScenario.addStep("Given I am a registered user");
        conjunctionScenario.addStep("When I login and navigate to my profile");
        conjunctionScenario.addStep("Then I can see my details and my purchase history");
        feature.addScenario(conjunctionScenario);
        
        features.add(feature);
        
        // Create analyzer
        analyzer = new FeatureAntiPatternAnalyzer(features);
    }

    @Test
    public void testAnalysisGeneratesWarnings() {
        List<FeatureAntiPatternAnalyzer.Warning> warnings = analyzer.analyzeAntiPatterns();
        
        assertFalse(warnings.isEmpty(), "Analysis should generate warnings");
        
        // Print all warnings for debugging
        System.out.println("Found " + warnings.size() + " anti-pattern warnings:");
        for (FeatureAntiPatternAnalyzer.Warning warning : warnings) {
            System.out.println("- " + warning.getType() + ": " + warning.getMessage());
        }
        
        // Verify we found various types of warnings
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.LONG_SCENARIO) > 0, 
                "Should find long scenario warnings");
        
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.MISSING_WHEN) > 0 ||
                countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.MISSING_THEN) > 0, 
                "Should find missing step warnings");
        
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.UI_FOCUSED_STEP) > 0, 
                "Should find UI-focused step warnings");
        
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.IMPLEMENTATION_DETAIL) > 0, 
                "Should find implementation detail warnings");
        
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.TOO_FEW_EXAMPLES) > 0, 
                "Should find too few examples warnings");
        
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.INCORRECT_STEP_ORDER) > 0, 
                "Should find incorrect step order warnings");
        
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.AMBIGUOUS_PRONOUN) > 0, 
                "Should find ambiguous pronoun warnings");
        
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.INCONSISTENT_TENSE) > 0, 
                "Should find inconsistent tense warnings");
        
        assertTrue(countWarningsByType(warnings, FeatureAntiPatternAnalyzer.WarningType.CONJUNCTION_IN_STEP) > 0, 
                "Should find conjunction warnings");
    }
    
    @Test
    public void testWarningReportGeneration() {
        List<FeatureAntiPatternAnalyzer.Warning> warnings = analyzer.analyzeAntiPatterns();
        String report = analyzer.generateWarningReport(warnings);
        
        assertNotNull(report);
        assertFalse(report.isEmpty());
        
        // Print the report for debugging
        System.out.println("Report preview:");
        System.out.println(report.substring(0, Math.min(report.length(), 500)) + "...");
        
        // Verify report contains expected sections
        assertTrue(report.contains("FEATURE ANTI-PATTERN WARNINGS"), "Report should have a header");
        assertTrue(report.contains("SUMMARY"), "Report should contain a summary section");
        assertTrue(report.contains("Found "), "Report should mention number of issues");
        
        // Check for warning types in the report
        assertTrue(report.contains("LONG SCENARIO") || report.contains("Long scenario"), 
                "Report should mention long scenarios");
        
        assertTrue(report.contains("UI-FOCUSED STEP") || report.contains("UI-focused step"), 
                "Report should mention UI-focused steps");
        
        assertTrue(report.contains("IMPLEMENTATION DETAIL") || report.contains("Implementation detail"), 
                "Report should mention implementation details");
    }
    
    private int countWarningsByType(List<FeatureAntiPatternAnalyzer.Warning> warnings, 
            FeatureAntiPatternAnalyzer.WarningType type) {
        int count = 0;
        for (FeatureAntiPatternAnalyzer.Warning warning : warnings) {
            if (warning.getType() == type) {
                count++;
            }
        }
        return count;
    }
}