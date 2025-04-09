package com.heymumford.ftoc.parser;

import com.heymumford.ftoc.exception.FileException;
import com.heymumford.ftoc.exception.ParsingException;
import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KarateParserTest {

    @Test
    void shouldDetectKarateFiles() throws FileException {
        FeatureParser parser = new FeatureParser();
        
        // Test a standard Karate file
        String karateFilePath = "src/test/resources/ftoc/test-feature-files/karate_style.feature";
        File karateFile = new File(karateFilePath);
        assertTrue(parser.isKarateFile(karateFile), "Should detect file as Karate-style");
        
        // Test a standard Cucumber file
        String cucumberFilePath = "src/test/resources/ftoc/test-feature-files/basic.feature";
        File cucumberFile = new File(cucumberFilePath);
        assertFalse(parser.isKarateFile(cucumberFile), "Should not detect file as Karate-style");
    }

    @Test
    void shouldParseKarateFeatureFile() throws FileException, ParsingException {
        // Create the Karate parser
        KarateParser parser = new KarateParser();
        
        // Parse a Karate file
        String karateFilePath = "src/test/resources/ftoc/test-feature-files/karate_style.feature";
        Feature feature = parser.parseFeatureFile(karateFilePath);
        
        // Basic assertions
        assertNotNull(feature, "Feature should not be null");
        assertEquals("Karate framework style feature file", feature.getName());
        assertTrue(feature.hasMetadata("hasKarateSyntax"), "Feature should have Karate syntax metadata");
        
        // Validate scenarios
        List<Scenario> scenarios = feature.getScenarios();
        assertFalse(scenarios.isEmpty(), "Feature should have scenarios");
        
        // Look for API-related tags
        boolean foundApiTag = false;
        for (Scenario scenario : scenarios) {
            if (scenario.getTags().contains("@API")) {
                foundApiTag = true;
                break;
            }
        }
        assertTrue(foundApiTag, "Should find API tag");
    }
    
    @Test
    void shouldParserFactoryChooseCorrectParser() throws FileException {
        // Test with a Karate file
        String karateFilePath = "src/test/resources/ftoc/test-feature-files/karate_style.feature";
        FeatureParser karateParser = FeatureParserFactory.getParser(karateFilePath);
        assertTrue(karateParser instanceof KarateParser, "Factory should return KarateParser for Karate files");
        
        // Test with a standard Cucumber file
        String cucumberFilePath = "src/test/resources/ftoc/test-feature-files/basic.feature";
        FeatureParser standardParser = FeatureParserFactory.getParser(cucumberFilePath);
        assertFalse(standardParser instanceof KarateParser, "Factory should return FeatureParser for standard files");
    }
}