package com.heymumford.ftoc.steps;

import com.heymumford.ftoc.FtocUtility;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class FtocUtilityStepDefs {
    private FtocUtility ftoc;

    @Given("the ftoc utility is initialized")
    public void initializeFtoc() {
        ftoc = new FtocUtility();
        ftoc.initialize();
    }

    @When("I run the utility on the {string} directory")
    public void runFtocUtility(String directoryPath) {
        ftoc.processDirectory(directoryPath);  // Changed from run() to processDirectory()
    }

    @Then("it should generate a table of contents")
    public void validateTocGeneration() {
        // Implement validation logic here
        // For example:
        // assertTrue(ftoc.isTocGenerated());
    }

    @Then("the output should contain a valid concordance summary")
    public void validateConcordanceSummary() {
        // Implement validation logic here
        // For example:
        // assertTrue(ftoc.isConcordanceGenerated());
    }
}