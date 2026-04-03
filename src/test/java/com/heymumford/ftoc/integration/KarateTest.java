package com.heymumford.ftoc.integration;

import com.heymumford.ftoc.FtocUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Karate-style feature file support (VS6).
 * Exercises Karate detection and mixed-directory parsing via FtocUtility.main().
 */
@DisplayName("Karate Integration Tests")
class KarateTest {

    private static final String TEST_FEATURES_DIR = "src/test/resources/ftoc/test-feature-files";

    private PrintStream originalOut;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void redirectStreams() {
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("A13: Karate file detected and parsed")
    void karateFileDetectedAndParsed() {
        FtocUtility.main(new String[]{"-d", TEST_FEATURES_DIR});

        String output = capturedOut.toString();

        // Karate feature should be parsed and appear in TOC with its name and tags
        assertTrue(output.contains("User API endpoints"),
                "Expected Karate feature name in TOC output");
        assertTrue(output.contains("@KarateTest"),
                "Expected @KarateTest tag from Karate feature in output");
        // Karate-specific tags from the fixture should appear in concordance
        assertTrue(output.contains("@GET"),
                "Expected @GET tag from Karate scenarios in concordance");
        assertTrue(output.contains("@POST"),
                "Expected @POST tag from Karate scenarios in concordance");
    }

    @Test
    @DisplayName("A13b: Mixed directory with Cucumber and Karate files parses both")
    void mixedDirectoryParsesBothFormats() {
        FtocUtility.main(new String[]{"-d", TEST_FEATURES_DIR});

        String output = capturedOut.toString();

        // Standard Cucumber features should appear
        assertTrue(output.contains("Basic feature file structure"),
                "Expected standard Cucumber feature in output");
        // Karate feature should also appear
        assertTrue(output.contains("User API endpoints"),
                "Expected Karate feature in output");
        // Tag concordance should include tags from both types
        assertTrue(output.contains("@Smoke"),
                "Expected @Smoke tag from standard features in concordance");
        assertTrue(output.contains("@API"),
                "Expected @API tag from Karate feature in concordance");
    }
}
