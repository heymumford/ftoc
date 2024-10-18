package com.heymumford.ftoc.steps;

import com.heymumford.ftoc.FtocUtility;  // Import FtocUtility class
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FtocUtilityStepDefs {

    private FtocUtility ftoc;

    @Given("the ftoc utility is initialized")
    public void initializeFtoc() {
        ftoc = new FtocUtility();
    }

    @When("I run the utility on the {string} directory")
    public void runFtocUtility(String directory) {
        String fullPath = System.getProperty("user.dir") + "/" + directory;
        ftoc.run(fullPath);
    }


    @Then("it should generate a table of contents")
    public void validateTocGeneration() {
        assertTrue(ftoc.isTocGenerated());
    }

    @Then("the output should contain a valid concordance summary")
    public void validateConcordanceSummary() {
        assertTrue(ftoc.isConcordanceGenerated());
    }
}
