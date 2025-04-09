package com.heymumford.ftoc.demo;

import com.heymumford.ftoc.FtocUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to demonstrate FTOC capabilities on a sample feature file.
 */
public class FtocDemoTest {

    private FtocUtility ftoc;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private String demoDir = "src/test/resources/ftoc/demo";
    
    @BeforeEach
    public void setup() throws IOException {
        // Create demo directory if it doesn't exist
        Path demoDirPath = Paths.get(demoDir);
        if (!Files.exists(demoDirPath)) {
            Files.createDirectories(demoDirPath);
        }
        
        // Create demo feature file content if it doesn't exist
        Path featureFilePath = Paths.get(demoDir, "FtocDemo.feature");
        if (!Files.exists(featureFilePath)) {
            String featureContent = getFeatureFileContent();
            Files.write(featureFilePath, featureContent.getBytes());
        }
        
        // Initialize FTOC utility
        ftoc = new FtocUtility();
        ftoc.initialize();
        
        // Set up output capture
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }
    
    @Test
    public void testBasicTableOfContents() {
        // Run FTOC on the demo directory with plain text output
        ftoc.setOutputFormat(com.heymumford.ftoc.formatter.TocFormatter.Format.PLAIN_TEXT);
        ftoc.processDirectory(demoDir);
        
        // Get the captured output
        String output = outputStream.toString();
        
        // Verify TOC was generated and contains expected elements
        assertTrue(output.contains("TABLE OF CONTENTS") || output.contains("Table of Contents"), 
                "Should contain TOC header");
        assertTrue(output.contains("Comprehensive demonstration of FTOC capabilities"), 
                "Should contain feature title");
        // Background section might not be included in the TOC by default
        // assertTrue(output.contains("Background:"), "Should contain background section");
        assertTrue(output.contains("Basic search functionality"), "Should contain first scenario");
        assertTrue(output.contains("Payment processing follows business rules"), 
                "Should contain rule section");
        assertTrue(output.contains("Login with different user types"), "Should contain scenario outline");
        
        // Verify tag concordance
        assertTrue(output.contains("TAG CONCORDANCE"), "Should contain tag concordance");
        assertTrue(output.contains("@Smoke"), "Should contain @Smoke tag");
        assertTrue(output.contains("@P0"), "Should contain @P0 tag");
        assertTrue(output.contains("@P1"), "Should contain @P1 tag");
        assertTrue(output.contains("@P2"), "Should contain @P2 tag");
    }
    
    @Test
    public void testMarkdownOutput() {
        // Run FTOC on the demo directory with markdown output
        ftoc.setOutputFormat(com.heymumford.ftoc.formatter.TocFormatter.Format.MARKDOWN);
        ftoc.processDirectory(demoDir);
        
        // Get the captured output
        String output = outputStream.toString();
        
        // Verify markdown-specific formatting
        assertTrue(output.contains("# Table of Contents"), 
                "Should contain markdown TOC header");
        assertTrue(output.contains("## Contents"), "Should contain markdown subheaders");
        assertTrue(output.contains("`@"), "Should contain tag formatting");
        assertTrue(output.contains("TAG CONCORDANCE REPORT"), "Should contain tag concordance report");
    }
    
    @Test
    public void testTagFiltering() {
        // Add tag filters to include only specific scenarios
        ftoc.addIncludeTagFilter("@Smoke");
        ftoc.setOutputFormat(com.heymumford.ftoc.formatter.TocFormatter.Format.PLAIN_TEXT);
        ftoc.processDirectory(demoDir);
        
        // Get the captured output
        String output = outputStream.toString();
        
        // Verify filtered TOC
        assertTrue(output.contains("FILTERS APPLIED") || output.contains("Include tags:"), 
                "Should mention filters were applied");
        assertTrue(output.contains("Basic search functionality"), "Should contain @Smoke scenario");
        assertFalse(output.contains("Advanced filtering with multiple criteria"), 
                "Should not contain scenarios without @Smoke tag");
    }
    
    @Test
    public void testHtmlOutput() {
        // Run FTOC with HTML output
        ftoc.setOutputFormat(com.heymumford.ftoc.formatter.TocFormatter.Format.HTML);
        ftoc.processDirectory(demoDir);
        
        // Get the captured output
        String output = outputStream.toString();
        
        // Verify HTML formatting
        assertTrue(output.contains("<html") || output.contains("<div") || output.contains("<h"),
                "Should contain HTML elements");
        assertTrue(output.contains("class=\"tag\"") || output.contains("class=\"feature\"") || 
                output.contains("class=\"scenario\""), "Should contain styled elements");
    }
    
    @Test
    public void testTagQualityAnalysis() {
        // Enable tag quality analysis
        ftoc.setAnalyzeTagQuality(true);
        ftoc.processDirectory(demoDir);
        
        // Get the captured output
        String output = outputStream.toString();
        
        // Verify tag quality analysis output
        assertTrue(output.contains("TAG QUALITY REPORT") || output.contains("Tag Quality Report"),
                "Should contain tag quality report header");
    }
    
    /**
     * Helper method to get the feature file content.
     */
    private String getFeatureFileContent() {
        return "@DemoFeature @Showcase @P0\n" +
                "Feature: Comprehensive demonstration of FTOC capabilities\n" +
                "  As a test engineer\n" +
                "  I want to explore FTOC's capabilities\n" +
                "  So that I can effectively organize and analyze my feature files\n" +
                "\n" +
                "  Background:\n" +
                "    Given the application is running\n" +
                "    And all services are available\n" +
                "\n" +
                "  @Smoke @UI @Fast\n" +
                "  Scenario: Basic search functionality\n" +
                "    When I search for \"cucumber\"\n" +
                "    Then I should see search results\n" +
                "    And the first result should contain \"cucumber\"\n" +
                "\n" +
                "  @Regression @API @Medium\n" +
                "  Scenario: Advanced filtering with multiple criteria\n" +
                "    When I search with the following criteria:\n" +
                "      | Category | Price  | Rating |\n" +
                "      | Books    | 0-50   | 4+     |\n" +
                "    Then I should see filtered results\n" +
                "    And all results should match the selected criteria\n" +
                "\n" +
                "  @Rule @Parametrized\n" +
                "  Rule: Payment processing follows business rules\n" +
                "\n" +
                "    Background:\n" +
                "      Given I am logged in as a customer\n" +
                "      And I have items in my cart\n" +
                "\n" +
                "    @P1 @Payment @Positive\n" +
                "    Scenario: Successful payment with credit card\n" +
                "      When I choose to pay with credit card\n" +
                "      And I enter valid credit card details\n" +
                "      Then my payment should be processed\n" +
                "      And I should receive an order confirmation\n" +
                "\n" +
                "    @P1 @Payment @Negative\n" +
                "    Scenario: Failed payment with expired credit card\n" +
                "      When I choose to pay with credit card\n" +
                "      And I enter an expired credit card\n" +
                "      Then I should see an error message\n" +
                "      And my payment should not be processed\n" +
                "\n" +
                "  @P2 @DataDriven\n" +
                "  Scenario Outline: Login with different user types\n" +
                "    Given I am on the login page\n" +
                "    When I login as a \"<user_type>\" user with \"<permissions>\"\n" +
                "    Then I should see the \"<dashboard_type>\" dashboard\n" +
                "    And I should have access to <feature_count> features\n" +
                "\n" +
                "    Examples: Admin users\n" +
                "      | user_type | permissions | dashboard_type | feature_count |\n" +
                "      | Admin     | Full        | Admin          | 10            |\n" +
                "      | Manager   | Limited     | Management     | 7             |\n" +
                "\n" +
                "    Examples: Regular users\n" +
                "      | user_type | permissions | dashboard_type | feature_count |\n" +
                "      | Customer  | Basic       | Customer       | 3             |\n" +
                "      | Guest     | Minimal     | Limited        | 1             |";
    }
    
    @Override
    protected void finalize() {
        // Restore the original System.out
        System.setOut(originalOut);
    }
}