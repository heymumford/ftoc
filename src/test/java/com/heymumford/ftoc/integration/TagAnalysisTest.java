/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.integration;

import com.heymumford.ftoc.FtocUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for tag analysis end-to-end (VS2).
 * Exercises --concordance and --analyze-tags via FtocUtility.main()
 * against real feature fixtures in src/test/resources/ftoc/test-feature-files/.
 */
public class TagAnalysisTest {

    private static final String TEST_FEATURES_DIR = "src/test/resources/ftoc/test-feature-files";

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void captureStdout() {
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void restoreStdout() {
        System.setOut(originalOut);
    }

    private String capturedOutput() {
        return outputStream.toString();
    }

    /**
     * A7: --concordance produces tag frequency table with tag names and counts.
     */
    @Test
    public void concordanceProducesTagFrequency() {
        FtocUtility.main(new String[]{"--concordance", "-d", TEST_FEATURES_DIR});

        String output = capturedOutput();

        assertTrue(output.contains("TAG CONCORDANCE REPORT"),
                "Expected concordance report header in output");
        assertTrue(output.contains("TAG FREQUENCY"),
                "Expected tag frequency section in output");
        // Tags present in the fixture files should appear with counts
        assertTrue(output.contains("@P1"),
                "Expected @P1 tag in concordance output");
        assertTrue(output.contains("@Smoke"),
                "Expected @Smoke tag in concordance output");
        // Counts are formatted as integers in the frequency table
        assertTrue(output.matches("(?s).*@P1\\s+\\d+.*"),
                "Expected @P1 to be followed by a numeric count");
    }

    /**
     * A8: --analyze-tags detects missing priority tags in scenarios
     * that lack @P0/@P1/@P2/@P3 etc.
     */
    @Test
    public void analyzeTagsDetectsMissingPriorityTags() {
        FtocUtility.main(new String[]{"--concordance", "--analyze-tags", "-d", TEST_FEATURES_DIR});

        String output = capturedOutput();

        assertTrue(output.contains("TAG QUALITY REPORT"),
                "Expected tag quality report header in output");
        // missing_tags.feature has scenarios without priority tags
        String outputLower = output.toLowerCase();
        assertTrue(outputLower.contains("missing priority tag")
                        || outputLower.contains("missing_priority_tag"),
                "Expected warning about missing priority tags");
    }

    /**
     * A8b: --analyze-tags detects low-value tags like @test or @temp.
     */
    @Test
    public void analyzeTagsDetectsLowValueTags() {
        FtocUtility.main(new String[]{"--concordance", "--analyze-tags", "-d", TEST_FEATURES_DIR});

        String output = capturedOutput();

        assertTrue(output.contains("TAG QUALITY REPORT"),
                "Expected tag quality report header in output");
        // tag_issues.feature and missing_tags.feature use @test which is a low-value tag
        String outputLower = output.toLowerCase();
        assertTrue(outputLower.contains("low-value tag")
                        || outputLower.contains("low_value_tag"),
                "Expected warning about low-value tags");
        assertTrue(output.contains("@test"),
                "Expected @test to appear in low-value tag warnings");
    }

    /**
     * A8c: --analyze-tags detects possible typos in tag names.
     * tag_issues.feature contains @Regressionn (double 'n') which should
     * be flagged as similar to @Regression.
     */
    @Test
    public void analyzeTagsDetectsTypos() {
        FtocUtility.main(new String[]{"--concordance", "--analyze-tags", "-d", TEST_FEATURES_DIR});

        String output = capturedOutput();

        assertTrue(output.contains("TAG QUALITY REPORT"),
                "Expected tag quality report header in output");
        String outputLower = output.toLowerCase();
        assertTrue(outputLower.contains("typo")
                        || outputLower.contains("similar")
                        || output.contains("@Regressionn"),
                "Expected typo detection for @Regressionn (similar to @Regression)");
    }
}
