package com.heymumford.ftoc.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class KarateIntegrationStepDefs {
    private static final Logger logger = LoggerFactory.getLogger(KarateIntegrationStepDefs.class);
    
    private final FtocUtilityStepDefs ftocStepDefs;
    
    public KarateIntegrationStepDefs(FtocUtilityStepDefs ftocStepDefs) {
        this.ftocStepDefs = ftocStepDefs;
    }
    
    @Then("the output should identify it as a Karate-style feature file")
    public void outputIdentifiesKarateFeatureFile() {
        String capturedOutput = ftocStepDefs.getCapturedOutput();
        assertNotNull(capturedOutput, "No output was captured");
        
        // Check for Karate identification in the output
        boolean isIdentifiedAsKarate = capturedOutput.contains("[Karate-style Feature File]");
        assertTrue(isIdentifiedAsKarate, "Output does not identify as Karate-style feature file");
    }
    
    @And("the output should show Karate-specific metadata")
    public void outputShowsKarateMetadata() {
        String capturedOutput = ftocStepDefs.getCapturedOutput();
        assertNotNull(capturedOutput, "No output was captured");
        
        // Check for at least one type of Karate metadata
        boolean hasMetadata = 
            capturedOutput.contains("Contains API calls") ||
            capturedOutput.contains("Contains JSON schema validation") ||
            capturedOutput.contains("Contains JSON matching") ||
            capturedOutput.contains("Contains embedded JavaScript") ||
            capturedOutput.contains("Contains API operations");
        
        assertTrue(hasMetadata, "Output does not show Karate-specific metadata");
    }
    
    @Then("the output should include Karate-specific information for Karate files")
    public void outputIncludesKarateInfoForKarateFiles() {
        String capturedOutput = ftocStepDefs.getCapturedOutput();
        assertNotNull(capturedOutput, "No output was captured");
        
        // Verify Karate information is present
        assertTrue(capturedOutput.contains("[Karate-style Feature File]"), 
                   "Output doesn't include Karate-specific information");
        
        // Verify it shows Karate info for the Karate-style.feature file
        assertTrue(capturedOutput.contains("karate_style.feature") && 
                   capturedOutput.contains("[Karate-style Feature File]"),
                   "Output doesn't show Karate info for the karate_style.feature file");
    }
    
    @And("the output should not include Karate-specific information for non-Karate files")
    public void outputDoesNotIncludeKarateInfoForNonKarateFiles() {
        String capturedOutput = ftocStepDefs.getCapturedOutput();
        assertNotNull(capturedOutput, "No output was captured");
        
        // Count occurrences of Karate-style marker
        int karateStyleCount = countOccurrences(capturedOutput, "[Karate-style Feature File]");
        
        // Verify there are multiple feature files but only one is marked as Karate
        int featureFileCount = countOccurrences(capturedOutput, ".feature");
        assertTrue(featureFileCount > karateStyleCount, 
                   "There should be more feature files than Karate-style feature files");
        
        // Verify basic.feature isn't marked as Karate
        assertTrue(capturedOutput.contains("basic.feature"), 
                  "Output should contain basic.feature");
        assertFalse(capturedOutput.contains("basic.feature [Karate-style Feature File]"),
                   "basic.feature should not be marked as a Karate-style feature file");
    }
    
    @Given("I have Karate tests for the FTOC CLI")
    public void iHaveKarateTestsForFtocCli() {
        // Verify the CLI test feature file exists
        File karateCliFeature = new File("src/test/java/karate/cli.feature");
        assertTrue(karateCliFeature.exists(), "Karate CLI test feature file doesn't exist");
    }
    
    @When("I run the Karate tests")
    public void iRunKarateTests() {
        // For now, this is just a placeholder since we're not executing the tests in this step
        logger.info("Karate tests would be executed here");
        // In a real implementation, we'd use KarateRunner to execute the tests
    }
    
    @Then("they should validate the CLI behavior at the system level")
    public void theyValidateCliBehavior() {
        // This is a placeholder for verification
        logger.info("Karate tests should validate CLI behavior");
        // In a real implementation, we'd check the test results
    }
    
    @And("report results in a JUnit-compatible format")
    public void reportResultsInJunitFormat() {
        // This is a placeholder for verification
        logger.info("Karate tests should report results in JUnit format");
        // In a real implementation, we'd check for JUnit XML output
    }
    
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}