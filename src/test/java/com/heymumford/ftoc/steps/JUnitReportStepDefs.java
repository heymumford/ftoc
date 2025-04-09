package com.heymumford.ftoc.steps;

import com.heymumford.ftoc.FtocUtility;
import com.heymumford.ftoc.formatter.AntiPatternFormatter;
import com.heymumford.ftoc.formatter.ConcordanceFormatter;
import com.heymumford.ftoc.formatter.TagQualityFormatter;
import com.heymumford.ftoc.formatter.TocFormatter;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class JUnitReportStepDefs {
    private static final Logger logger = LoggerFactory.getLogger(JUnitReportStepDefs.class);
    
    private final FtocUtilityStepDefs ftocStepDefs;
    
    public JUnitReportStepDefs(FtocUtilityStepDefs ftocStepDefs) {
        this.ftocStepDefs = ftocStepDefs;
    }
    
    @When("I detect anti-patterns")
    public void detectAntiPatterns() {
        FtocUtility ftoc = ftocStepDefs.getFtocUtility();
        ftoc.setDetectAntiPatterns(true);
        logger.info("Enabled anti-pattern detection");
    }
    
    @When("I set anti-pattern format to {string}")
    public void setAntiPatternFormat(String format) {
        FtocUtility ftoc = ftocStepDefs.getFtocUtility();
        AntiPatternFormatter.Format antiPatternFormat;
        switch (format.toLowerCase()) {
            case "md":
            case "markdown":
                antiPatternFormat = AntiPatternFormatter.Format.MARKDOWN;
                break;
            case "html":
                antiPatternFormat = AntiPatternFormatter.Format.HTML;
                break;
            case "json":
                antiPatternFormat = AntiPatternFormatter.Format.JSON;
                break;
            case "junit":
            case "junit-xml":
            case "xml":
                antiPatternFormat = AntiPatternFormatter.Format.JUNIT_XML;
                break;
            default:
                antiPatternFormat = AntiPatternFormatter.Format.PLAIN_TEXT;
        }
        
        ftoc.setAntiPatternFormat(antiPatternFormat);
        logger.info("Set anti-pattern format to: {}", antiPatternFormat);
    }
    
    @When("I set JUnit output format for all reports")
    public void setJUnitOutputFormatForAllReports() {
        FtocUtility ftoc = ftocStepDefs.getFtocUtility();
        
        ftoc.setOutputFormat(TocFormatter.Format.JUNIT_XML);
        ftoc.setConcordanceFormat(ConcordanceFormatter.Format.JUNIT_XML);
        ftoc.setTagQualityFormat(TagQualityFormatter.Format.JUNIT_XML);
        ftoc.setAntiPatternFormat(AntiPatternFormatter.Format.JUNIT_XML);
        
        logger.info("Set all report formats to JUnit XML");
    }
    
    @When("I run the utility with parameters {string}")
    public void runWithCommandLineParameters(String parameters) {
        // Capture the output
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Split the parameters into an array and process
            String[] args = parameters.split("\\s+");
            FtocUtility.main(args);
            
            // Save the captured output
            ftocStepDefs.setCapturedOutput(outputStream.toString());
            
            // Debug output
            System.setOut(originalOut);
            logger.info("Ran with parameters: {}", parameters);
            logger.info("Captured output: {}", 
                    ftocStepDefs.getCapturedOutput().substring(0, Math.min(200, ftocStepDefs.getCapturedOutput().length())) + "...");
        } finally {
            // Restore original output stream
            System.setOut(originalOut);
        }
    }
    
    @Then("an anti-pattern report should be generated")
    public void verifyAntiPatternReportGenerated() {
        String capturedOutput = ftocStepDefs.getCapturedOutput();
        assertNotNull(capturedOutput, "No output was captured");
        
        // Check for anti-pattern report indicators
        boolean hasAntiPatternReport = capturedOutput.contains("FEATURE ANTI-PATTERN REPORT") || 
                                       capturedOutput.contains("Feature Anti-Pattern Report") ||
                                       capturedOutput.contains("antiPatternReport") ||
                                       capturedOutput.contains("FTOC Anti-Pattern Analysis");
        
        logger.info("Has anti-pattern report: {}", hasAntiPatternReport);
        assertTrue(hasAntiPatternReport, "Output does not contain an anti-pattern report");
    }
    
    @Then("the report should be in JUnit XML format")
    public void verifyJUnitXmlFormat() {
        String capturedOutput = ftocStepDefs.getCapturedOutput();
        assertNotNull(capturedOutput, "No output was captured");
        
        // Check for XML format indicators
        boolean isXmlFormat = capturedOutput.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") &&
                              capturedOutput.contains("<testsuite") &&
                              capturedOutput.contains("</testsuite>");
        
        logger.info("Is JUnit XML format: {}", isXmlFormat);
        assertTrue(isXmlFormat, "Output is not in JUnit XML format");
        
        // Check for specific JUnit XML elements
        assertTrue(capturedOutput.contains("<testcase"), "Output doesn't contain testcase elements");
        assertTrue(capturedOutput.contains("timestamp="), "Output doesn't contain timestamp attribute");
    }
    
    @Then("all reports should be in JUnit XML format")
    public void verifyAllReportsInJUnitFormat() {
        String capturedOutput = ftocStepDefs.getCapturedOutput();
        assertNotNull(capturedOutput, "No output was captured");
        
        // Since multiple reports may be generated, we should find multiple XML declarations
        int xmlDeclarationCount = countOccurrences(capturedOutput, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        logger.info("Found {} XML declarations", xmlDeclarationCount);
        
        assertTrue(xmlDeclarationCount >= 1, "No JUnit XML reports found in output");
        
        // Verify the presence of different report test suites
        boolean hasTagQualitySuite = capturedOutput.contains("<testsuite name=\"FTOC Tag Quality Analysis\"");
        boolean hasAntiPatternSuite = capturedOutput.contains("<testsuite name=\"FTOC Anti-Pattern Analysis\"");
        boolean hasTocSuite = capturedOutput.contains("<testsuite name=\"FTOC Table of Contents\"");
        
        logger.info("Has Tag Quality suite: {}", hasTagQualitySuite);
        logger.info("Has Anti-Pattern suite: {}", hasAntiPatternSuite);
        logger.info("Has TOC suite: {}", hasTocSuite);
        
        // At least one of the report types should be present
        assertTrue(hasTagQualitySuite || hasAntiPatternSuite || hasTocSuite, 
                "Output doesn't contain any expected JUnit XML test suites");
    }
    
    /**
     * Count the number of occurrences of a pattern in a string.
     */
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