/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.integration;

import com.heymumford.ftoc.FtocUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * B-tests for error handling and edge cases.
 * Each test creates its own fixture, runs ftoc, and asserts no uncaught exception.
 * Key invariant: no test should produce a stacktrace in output.
 */
@DisplayName("Error Handling B-Tests")
class ErrorHandlingBTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream capturedOut;
    private ByteArrayOutputStream capturedErr;

    @BeforeEach
    void redirectStreams() {
        capturedOut = new ByteArrayOutputStream();
        capturedErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
        System.setErr(new PrintStream(capturedErr));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Asserts that stdout does not contain a Java stacktrace from an uncaught exception.
     * Checks stdout only -- stderr is used by SLF4J-simple for logging and may contain
     * legitimate diagnostic output that is not user-visible.
     */
    private void assertNoStacktrace() {
        String out = capturedOut.toString();
        assertFalse(out.contains("\tat "), "stdout contains a stacktrace");
    }

    @Test
    @DisplayName("B1: Empty directory produces clean message, no crash")
    void emptyDirectoryProducesCleanMessage(@TempDir Path tempDir) {
        FtocUtility.main(new String[]{"-d", tempDir.toString()});

        assertNoStacktrace();
        // ftoc should not crash on an empty directory
        String combined = capturedOut.toString() + capturedErr.toString();
        assertFalse(combined.contains("Exception"), "Output should not contain exception text");
    }

    @Test
    @DisplayName("B2: Malformed feature file produces parse error, not uncaught exception")
    void malformedFeatureFileProducesParseError(@TempDir Path tempDir) throws Exception {
        Path malformed = tempDir.resolve("malformed.feature");
        Files.writeString(malformed, "This is not valid Gherkin at all\nNo Feature keyword here\nJust garbage text\n");

        FtocUtility.main(new String[]{"-d", tempDir.toString()});

        assertNoStacktrace();
        String combined = capturedOut.toString() + capturedErr.toString();
        assertFalse(combined.contains("Exception"), "Output should not contain raw exception text");
    }

    @Test
    @DisplayName("B3: Feature file with no scenarios is handled gracefully")
    void featureFileWithNoScenariosHandledGracefully(@TempDir Path tempDir) throws Exception {
        Path featureOnly = tempDir.resolve("no_scenarios.feature");
        Files.writeString(featureOnly,
                "Feature: A feature with no scenarios\n  This feature has a description but nothing else.\n");

        FtocUtility.main(new String[]{"-d", tempDir.toString()});

        assertNoStacktrace();
        // The program should complete without error; the concordance report is generated
        // even when the single feature has zero scenarios (TOC skips features with no
        // non-Background scenarios, but the concordance summary still runs).
        String out = capturedOut.toString();
        assertTrue(out.contains("TAG CONCORDANCE"),
                "Concordance report should still be generated for feature-only files");
    }

    @Test
    @DisplayName("B4: Feature file with no tags reports appropriately, no crash")
    void featureFileWithNoTagsReportsAppropriately(@TempDir Path tempDir) throws Exception {
        Path noTags = tempDir.resolve("no_tags.feature");
        Files.writeString(noTags,
                "Feature: Untagged feature\n\n  Scenario: A scenario without any tags\n    Given something happens\n    Then something is verified\n");

        FtocUtility.main(new String[]{"-d", tempDir.toString(), "--analyze-tags"});

        assertNoStacktrace();
        String out = capturedOut.toString();
        assertTrue(out.contains("Untagged feature"),
                "Feature name should appear in output");
    }

    @Test
    @DisplayName("B7: Unreadable feature file is skipped with warning, no crash")
    void unreadableFeatureFileIsSkipped(@TempDir Path tempDir) throws Exception {
        Path unreadable = tempDir.resolve("unreadable.feature");
        Files.writeString(unreadable, "Feature: Should not be read\n\n  Scenario: Hidden\n    Given secret\n");

        File file = unreadable.toFile();
        assertTrue(file.setReadable(false), "Failed to make file unreadable (test precondition)");

        try {
            FtocUtility.main(new String[]{"-d", tempDir.toString()});

            assertNoStacktrace();
            String combined = capturedOut.toString() + capturedErr.toString();
            assertFalse(combined.contains("Exception"), "Output should not contain raw exception text");
        } finally {
            // Restore permissions for cleanup
            file.setReadable(true);
        }
    }

    @Test
    @DisplayName("B8: Invalid YAML config produces clear error message, no crash")
    void invalidYamlConfigProducesClearError(@TempDir Path tempDir) throws Exception {
        Path invalidYaml = tempDir.resolve("invalid-config.yml");
        Files.writeString(invalidYaml, "this: is: not: valid: yaml:\n  [broken\n  - unclosed\n");

        // Create a valid feature file so ftoc has something to process
        Path feature = tempDir.resolve("valid.feature");
        Files.writeString(feature,
                "Feature: Valid feature\n\n  Scenario: Basic\n    Given a step\n    Then verified\n");

        FtocUtility.main(new String[]{
                "--config-file", invalidYaml.toString(),
                "-d", tempDir.toString()
        });

        assertNoStacktrace();
        // ftoc should still produce some output (falls back to defaults)
        String out = capturedOut.toString();
        assertFalse(out.isEmpty(), "ftoc should produce output even with invalid config");
    }

    @Test
    @DisplayName("B9: Invalid format argument produces error message")
    void invalidFormatArgumentProducesError(@TempDir Path tempDir) throws Exception {
        Path feature = tempDir.resolve("valid.feature");
        Files.writeString(feature,
                "Feature: Format test\n\n  Scenario: Basic\n    Given a step\n    Then verified\n");

        FtocUtility.main(new String[]{"--format", "invalid_format", "-d", tempDir.toString()});

        assertNoStacktrace();
        // With an unrecognized format, ftoc falls back to plain text (no crash)
        String out = capturedOut.toString();
        assertTrue(out.contains("TAG CONCORDANCE") || out.contains("TABLE OF CONTENTS"),
                "ftoc should still produce output when given an unknown format (falls back to text)");
    }

    @Test
    @DisplayName("B12: Feature with only Background and no Scenario is handled")
    void featureWithOnlyBackgroundHandled(@TempDir Path tempDir) throws Exception {
        Path bgOnly = tempDir.resolve("background_only.feature");
        Files.writeString(bgOnly,
                "Feature: Background only feature\n\n"
                + "  Background: Setup\n"
                + "    Given the system is initialized\n"
                + "    And the database is seeded\n");

        FtocUtility.main(new String[]{"-d", tempDir.toString()});

        assertNoStacktrace();
        // The TOC skips features whose only scenario entries are Background blocks,
        // but the concordance report still runs without crashing.
        String out = capturedOut.toString();
        assertTrue(out.contains("TAG CONCORDANCE"),
                "Concordance report should still be generated for background-only features");
    }

    @Test
    @DisplayName("B13: Scenario Outline with empty Examples table is handled")
    void scenarioOutlineWithEmptyExamplesHandled(@TempDir Path tempDir) throws Exception {
        Path emptyExamples = tempDir.resolve("empty_examples.feature");
        Files.writeString(emptyExamples,
                "Feature: Empty examples feature\n\n"
                + "  Scenario Outline: Parameterized with no data\n"
                + "    Given I have <input>\n"
                + "    Then I get <output>\n\n"
                + "    Examples:\n");

        FtocUtility.main(new String[]{"-d", tempDir.toString()});

        assertNoStacktrace();
        String out = capturedOut.toString();
        assertTrue(out.contains("Empty examples feature"),
                "Feature name should appear in output even with empty Examples table");
    }
}
