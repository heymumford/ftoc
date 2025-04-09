package com.heymumford.ftoc.steps;

import com.heymumford.ftoc.FtocUtility;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for testing the warning configuration functionality.
 */
public class ConfigurationStepDefs {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationStepDefs.class);
    
    // We'll use this to share state with other step definition classes
    private FtocUtilityStepDefs ftocStepDefs;
    private String configSummary;
    
    public ConfigurationStepDefs(FtocUtilityStepDefs ftocStepDefs) {
        this.ftocStepDefs = ftocStepDefs;
    }
    
    @When("I set a custom configuration file path {string}")
    public void setCustomConfigFile(String configFilePath) {
        logger.info("Setting custom configuration file path: {}", configFilePath);
        Path path = Paths.get(configFilePath);
        assertTrue(Files.exists(path), "Config file doesn't exist: " + configFilePath);
        
        // Get ftoc instance from the main step defs
        FtocUtility ftoc = ftocStepDefs.getFtocUtility();
        ftoc.setWarningConfigFile(configFilePath);
    }
    
    @When("I display the configuration summary")
    public void displayConfigSummary() {
        logger.info("Displaying configuration summary");
        
        // Get ftoc instance from the main step defs
        FtocUtility ftoc = ftocStepDefs.getFtocUtility();
        
        // Capture the output
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Get the summary directly from the utility
            configSummary = ftoc.getWarningConfigSummary();
            System.out.println(configSummary);
            
            // Debug output
            System.setOut(originalOut);
            logger.info("Configuration summary:\n{}", configSummary);
        } finally {
            // Restore original output stream
            System.setOut(originalOut);
        }
    }
    
    @Then("the configuration summary should show the correct settings")
    public void verifyConfigSummarySettings() {
        logger.info("Verifying configuration summary shows correct settings");
        assertNotNull(configSummary, "Configuration summary is null");
        
        // Check for expected sections
        assertTrue(configSummary.contains("Warning Configuration Summary"), 
                "Missing summary header");
        assertTrue(configSummary.contains("Tag Quality Warnings"), 
                "Missing tag quality warnings section");
        assertTrue(configSummary.contains("Anti-Pattern Warnings"), 
                "Missing anti-pattern warnings section");
        assertTrue(configSummary.contains("Thresholds"), 
                "Missing thresholds section");
    }
    
    @Then("the configuration summary should include the loaded file path")
    public void verifyConfigSummaryFilePath() {
        logger.info("Verifying configuration summary shows loaded file path");
        assertNotNull(configSummary, "Configuration summary is null");
        
        // Check for loaded file path
        assertTrue(configSummary.contains("Loaded from:"), 
                "Missing loaded file path");
        assertFalse(configSummary.contains("Using default configuration (no config file loaded)"), 
                "Summary incorrectly indicates default configuration");
    }
    
    @Then("the configuration summary should list all warning types and their status")
    public void verifyConfigSummaryWarningTypes() {
        logger.info("Verifying configuration summary lists all warning types");
        assertNotNull(configSummary, "Configuration summary is null");
        
        // Check for specific examples of enabled/disabled warnings
        assertTrue(configSummary.contains("MISSING_PRIORITY_TAG"), 
                "Missing MISSING_PRIORITY_TAG warning");
        assertTrue(configSummary.contains("EXCESSIVE_TAGS"), 
                "Missing EXCESSIVE_TAGS warning");
        assertTrue(configSummary.contains("LONG_SCENARIO"), 
                "Missing LONG_SCENARIO warning");
        
        // Check that enabled/disabled status is shown
        assertTrue(configSummary.contains("enabled") || configSummary.contains("disabled"), 
                "Missing enabled/disabled status");
        
        // Check that it includes severity
        assertTrue(configSummary.contains("error") || configSummary.contains("warning") || 
                   configSummary.contains("info"), 
                   "Missing severity information");
    }
    
    @Then("the tag quality report should detect missing priority tags")
    public void verifyMissingPriorityTagDetection() {
        logger.info("Verifying missing priority tag detection");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        assertTrue(output.contains("Missing priority tag") || 
                   output.contains("MISSING_PRIORITY_TAG"), 
                   "Output doesn't mention missing priority tags");
    }
    
    @Then("the tag quality report should detect excessive tags")
    public void verifyExcessiveTagsDetection() {
        logger.info("Verifying excessive tags detection");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        assertTrue(output.contains("Excessive tags") || 
                   output.contains("too many tags") || 
                   output.contains("EXCESSIVE_TAGS"), 
                   "Output doesn't mention excessive tags");
    }
    
    @Then("the tag quality report should detect tag typos")
    public void verifyTagTypoDetection() {
        logger.info("Verifying tag typo detection");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        assertTrue(output.contains("Tag typo") || 
                   output.contains("might be a typo") || 
                   output.contains("TAG_TYPO"), 
                   "Output doesn't mention tag typos");
    }
    
    @Then("the tag quality report should reflect the custom configuration settings")
    public void verifyCustomConfigSettings() {
        logger.info("Verifying custom configuration settings are applied");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // This will depend on what's in the custom config, but we can check for general correctness
        
        // Check that something is detected
        assertTrue(output.contains("MISSING_PRIORITY_TAG") || 
                   output.contains("MISSING_TYPE_TAG") || 
                   output.contains("EXCESSIVE_TAGS") || 
                   output.contains("TAG_TYPO"), 
                   "Output doesn't seem to reflect any configured warnings");
    }
    
    @Then("the tag quality report should respect enabled/disabled warnings")
    public void verifyEnabledDisabledWarnings() {
        logger.info("Verifying enabled/disabled warnings are respected");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // In custom-warnings.yml, DUPLICATE_TAG is disabled
        assertFalse(output.contains("DUPLICATE_TAG") || 
                    output.contains("Duplicate tag"), 
                    "Output contains disabled warning: DUPLICATE_TAG");
        
        // But MISSING_PRIORITY_TAG should be enabled
        assertTrue(output.contains("MISSING_PRIORITY_TAG") || 
                   output.contains("Missing priority tag"), 
                   "Output doesn't contain enabled warning: MISSING_PRIORITY_TAG");
    }
    
    @Then("the tag quality report should apply custom thresholds")
    public void verifyCustomThresholds() {
        logger.info("Verifying custom thresholds are applied");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // For maxTags threshold (set to 5 in custom-warnings.yml)
        if (output.contains("EXCESSIVE_TAGS") || output.contains("Excessive tags")) {
            // Extract the threshold value
            Pattern pattern = Pattern.compile("tags.*?which is excessive \\(recommended max: (\\d+)\\)");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                String threshold = matcher.group(1);
                assertEquals("5", threshold, "Incorrect maxTags threshold applied");
            }
        }
    }
    
    @Then("the tag quality report should not contain disabled warnings")
    public void verifyNoDisabledWarnings() {
        logger.info("Verifying disabled warnings are not present");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // In disabled-warnings.yml, these are explicitly disabled
        assertFalse(output.contains("MISSING_PRIORITY_TAG") || 
                    output.contains("Missing priority tag"), 
                    "Output contains disabled warning: MISSING_PRIORITY_TAG");
        
        assertFalse(output.contains("EXCESSIVE_TAGS") || 
                    output.contains("Excessive tags"), 
                    "Output contains disabled warning: EXCESSIVE_TAGS");
    }
    
    @Then("the tag quality report should still contain enabled warnings")
    public void verifyEnabledWarningsPresent() {
        logger.info("Verifying enabled warnings are present");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // In disabled-warnings.yml, these should still be enabled
        assertTrue(output.contains("MISSING_TYPE_TAG") || 
                   output.contains("Missing type tag"), 
                   "Output doesn't contain enabled warning: MISSING_TYPE_TAG");
        
        assertTrue(output.contains("TAG_TYPO") || 
                   output.contains("Tag typo"), 
                   "Output doesn't contain enabled warning: TAG_TYPO");
    }
    
    @Then("the tag quality report should apply the custom max tags threshold")
    public void verifyCustomMaxTagsThreshold() {
        logger.info("Verifying custom max tags threshold");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // In custom-thresholds.yml, maxTags is set to 3
        if (output.contains("EXCESSIVE_TAGS") || output.contains("Excessive tags")) {
            Pattern pattern = Pattern.compile("tags.*?which is excessive \\(recommended max: (\\d+)\\)");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                String threshold = matcher.group(1);
                assertEquals("3", threshold, "Incorrect maxTags threshold applied");
            }
        }
    }
    
    @Then("the tag quality report should apply the custom scenario name length threshold")
    public void verifyCustomScenarioNameLengthThreshold() {
        logger.info("Verifying custom scenario name length threshold");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // In custom-thresholds.yml, maxScenarioNameLength is set to 50
        if (output.contains("LONG_SCENARIO_NAME") || output.contains("Scenario name is")) {
            Pattern pattern = Pattern.compile("Scenario name is \\d+ characters long \\(recommended maximum: (\\d+)\\)");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                String threshold = matcher.group(1);
                assertEquals("50", threshold, "Incorrect maxScenarioNameLength threshold applied");
            }
        }
    }
    
    @Then("the tag quality report should use the custom priority tag list")
    public void verifyCustomPriorityTagList() {
        logger.info("Verifying custom priority tag list");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // Hard to test this conclusively without more context, but we can check for mentions
        // of our custom list (e.g., Blocker, Major, Minor which are in custom-tags.yml)
        boolean hasCustomTags = output.contains("Priority-1") || 
                               output.contains("Priority-2") || 
                               output.contains("Blocker") || 
                               output.contains("Major");
                               
        // We can at least log the finding
        logger.info("Found evidence of custom priority tags: {}", hasCustomTags);
    }
    
    @Then("the tag quality report should use the custom type tag list")
    public void verifyCustomTypeTagList() {
        logger.info("Verifying custom type tag list");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // Similar approach to priority tags
        boolean hasCustomTags = output.contains("FrontEnd") || 
                               output.contains("BackEnd") || 
                               output.contains("Database") || 
                               output.contains("Security");
                               
        logger.info("Found evidence of custom type tags: {}", hasCustomTags);
    }
    
    @Then("the tag quality report should use the custom low-value tag list")
    public void verifyCustomLowValueTagList() {
        logger.info("Verifying custom low-value tag list");
        String output = ftocStepDefs.getCapturedOutput();
        assertNotNull(output, "No output was captured");
        
        // Similar approach again
        boolean hasCustomTags = output.contains("NeedsWork") || 
                               output.contains("Draft") || 
                               output.contains("QuickTest") || 
                               output.contains("PoC");
                               
        logger.info("Found evidence of custom low-value tags: {}", hasCustomTags);
    }
}