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
 * End-to-end integration tests for TOC generation across all output formats.
 * Uses real feature files from test-feature-files/ -- no mocks.
 */
public class TocGenerationTest {

    private static final String TEST_DIR = "src/test/resources/ftoc/test-feature-files";

    private ByteArrayOutputStream capturedOutput;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private String runWithArgs(String... args) {
        FtocUtility.main(args);
        return capturedOutput.toString();
    }

    /**
     * A1: Single feature file directory produces text TOC with feature and scenario names.
     */
    @Test
    void singleFileProducesTextTocWithFeatureAndScenarioNames() {
        String output = runWithArgs("-d", TEST_DIR);

        assertTrue(output.contains("TABLE OF CONTENTS"), "Text TOC header missing");
        assertTrue(output.contains("Basic feature file structure"),
                "Feature name 'Basic feature file structure' missing from text TOC");
        assertTrue(output.contains("Simple scenario with basic elements"),
                "Scenario 'Simple scenario with basic elements' missing from text TOC");
        assertTrue(output.contains("Another simple scenario with different tags"),
                "Scenario 'Another simple scenario with different tags' missing from text TOC");
    }

    /**
     * A2: Directory scan finds all features across multiple files.
     */
    @Test
    void directoryProducesTextTocContainingAllFeatures() {
        String output = runWithArgs("-d", TEST_DIR);

        assertTrue(output.contains("Basic feature file structure"),
                "Feature 'Basic feature file structure' missing");
        assertTrue(output.contains("Examples of complex scenarios with common issues"),
                "Feature 'Examples of complex scenarios' missing");
        assertTrue(output.contains("TAG CONCORDANCE"),
                "Tag concordance section missing from directory scan");
    }

    /**
     * A3: Markdown output contains markdown headers and feature names.
     */
    @Test
    void markdownOutputContainsHeadersAndFeatureNames() {
        String output = runWithArgs("-d", TEST_DIR, "--format", "markdown");

        assertTrue(output.contains("# Table of Contents"),
                "Markdown H1 header missing");
        assertTrue(output.contains("## Contents"),
                "Markdown H2 header missing");
        assertTrue(output.contains("Basic feature file structure"),
                "Feature name missing from markdown output");
    }

    /**
     * A4: HTML output contains HTML structure and feature names.
     */
    @Test
    void htmlOutputContainsHtmlStructureAndFeatureNames() {
        String output = runWithArgs("-d", TEST_DIR, "--format", "html");

        assertTrue(output.contains("<html") || output.contains("<!DOCTYPE"),
                "HTML document start missing");
        assertTrue(output.contains("<body") || output.contains("<div"),
                "HTML body/div element missing");
        assertTrue(output.contains("Basic feature file structure"),
                "Feature name missing from HTML output");
    }

    /**
     * A5: JSON output is structurally valid and contains feature names.
     */
    @Test
    void jsonOutputIsValidAndContainsFeatureNames() {
        String output = runWithArgs("-d", TEST_DIR, "--format", "json");

        String jsonBlock = extractJsonBlock(output);
        assertNotNull(jsonBlock, "No JSON block found in output");
        assertTrue(jsonBlock.contains("\"tableOfContents\""),
                "JSON root key 'tableOfContents' missing");
        assertTrue(jsonBlock.contains("Basic feature file structure"),
                "Feature name missing from JSON output");

        long opens = jsonBlock.chars().filter(c -> c == '{').count();
        long closes = jsonBlock.chars().filter(c -> c == '}').count();
        assertEquals(opens, closes, "Mismatched JSON braces");
    }

    /**
     * A6: JUnit XML output contains testsuite and testcase elements.
     */
    @Test
    void junitXmlOutputContainsTestsuiteAndTestcaseElements() {
        String output = runWithArgs("-d", TEST_DIR, "--format", "junit-xml");

        assertTrue(output.contains("<testsuite"),
                "JUnit XML <testsuite> element missing");
        assertTrue(output.contains("<testcase"),
                "JUnit XML <testcase> element missing");
        assertTrue(output.contains("Basic feature file structure")
                        || output.contains("basic_feature_file_structure"),
                "Feature reference missing from JUnit XML output");
    }

    /**
     * Extract the first balanced JSON object containing tableOfContents.
     */
    private String extractJsonBlock(String output) {
        int tocIndex = output.indexOf("\"tableOfContents\"");
        if (tocIndex < 0) return null;

        int start = output.lastIndexOf('{', tocIndex);
        if (start < 0) return null;

        int depth = 0;
        for (int i = start; i < output.length(); i++) {
            char c = output.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            if (depth == 0) {
                return output.substring(start, i + 1);
            }
        }
        return null;
    }
}
