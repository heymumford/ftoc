/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that WarningConfiguration validates YAML structure
 * and reports clear errors for invalid configurations.
 */
public class WarningConfigurationValidationTest {

    @TempDir
    Path tempDir;

    @Test
    public void testValidConfigLoadsWithoutErrors() throws IOException {
        Path config = tempDir.resolve("valid.yml");
        Files.writeString(config, String.join("\n",
            "warnings:",
            "  disabled:",
            "    - MISSING_PRIORITY_TAG",
            "  severity:",
            "    DUPLICATE_TAG: error",
            "thresholds:",
            "  maxSteps: 15"
        ));

        WarningConfiguration wc = new WarningConfiguration(config.toString());
        assertTrue(wc.getValidationErrors().isEmpty(),
            "Valid config should produce no validation errors");
        assertFalse(wc.isWarningEnabled("MISSING_PRIORITY_TAG"));
        assertEquals(15, wc.getIntThreshold("maxSteps", 10));
    }

    @Test
    public void testUnknownTopLevelKeyReported() throws IOException {
        Path config = tempDir.resolve("unknown-top.yml");
        Files.writeString(config, String.join("\n",
            "warnings:",
            "  disabled: []",
            "bogusSection:",
            "  foo: bar"
        ));

        WarningConfiguration wc = new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should report unknown top-level key");
        assertTrue(errors.stream().anyMatch(e -> e.contains("bogusSection")),
            "Error should mention the unknown key 'bogusSection'");
    }

    @Test
    public void testUnknownWarningNameReported() throws IOException {
        Path config = tempDir.resolve("unknown-warning.yml");
        Files.writeString(config, String.join("\n",
            "warnings:",
            "  severity:",
            "    NONEXISTENT_WARNING: error"
        ));

        WarningConfiguration wc = new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should report unknown warning name");
        assertTrue(errors.stream().anyMatch(e -> e.contains("NONEXISTENT_WARNING")),
            "Error should mention 'NONEXISTENT_WARNING'");
    }

    @Test
    public void testInvalidSeverityValueReported() throws IOException {
        Path config = tempDir.resolve("bad-severity.yml");
        Files.writeString(config, String.join("\n",
            "warnings:",
            "  severity:",
            "    DUPLICATE_TAG: catastrophic"
        ));

        WarningConfiguration wc = new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should report invalid severity value");
        assertTrue(errors.stream().anyMatch(e -> e.contains("catastrophic")),
            "Error should mention the invalid value 'catastrophic'");
    }

    @Test
    public void testWrongTypeForThresholdReported() throws IOException {
        Path config = tempDir.resolve("bad-threshold.yml");
        Files.writeString(config, String.join("\n",
            "thresholds:",
            "  maxSteps: not-a-number"
        ));

        WarningConfiguration wc = new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should report non-numeric threshold");
        assertTrue(errors.stream().anyMatch(e -> e.contains("maxSteps")),
            "Error should mention 'maxSteps'");
    }

    @Test
    public void testDisabledListWithWrongTypeReported() throws IOException {
        Path config = tempDir.resolve("bad-disabled.yml");
        Files.writeString(config, String.join("\n",
            "warnings:",
            "  disabled: not-a-list"
        ));

        WarningConfiguration wc = new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should report disabled is not a list");
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("disabled")),
            "Error should mention 'disabled'");
    }

    @Test
    public void testNegativeThresholdReported() throws IOException {
        Path config = tempDir.resolve("negative-threshold.yml");
        Files.writeString(config, String.join("\n",
            "thresholds:",
            "  maxSteps: -5"
        ));

        WarningConfiguration wc = new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should report negative threshold");
        assertTrue(errors.stream().anyMatch(e -> e.contains("maxSteps")),
            "Error should mention 'maxSteps'");
    }

    @Test
    public void testEmptyConfigFileProducesNoErrors() throws IOException {
        Path config = tempDir.resolve("empty.yml");
        Files.writeString(config, "");

        // Empty file should use defaults without validation errors
        WarningConfiguration wc = new WarningConfiguration(config.toString());
        assertTrue(wc.getValidationErrors().isEmpty(),
            "Empty config should produce no validation errors");
    }

    @Test
    public void testNullTagQualitySectionDoesNotNPE()
            throws IOException {
        Path config = tempDir.resolve("null-section.yml");
        Files.writeString(config, String.join("\n",
            "warnings:",
            "  tagQuality:"
        ));

        assertDoesNotThrow(
            () -> new WarningConfiguration(config.toString()),
            "Empty tagQuality section must not throw NPE");
    }

    @Test
    public void testWarningsNonMapIsReported() throws IOException {
        Path config = tempDir.resolve("warnings-bool.yml");
        Files.writeString(config, "warnings: true\n");

        WarningConfiguration wc =
            new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(),
            "'warnings: true' should produce a validation error");
    }

    @Test
    public void testThresholdsNonMapIsReported() throws IOException {
        Path config = tempDir.resolve("thresholds-str.yml");
        Files.writeString(config, "thresholds: \"string\"\n");

        WarningConfiguration wc =
            new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(),
            "'thresholds: string' should produce a validation error");
    }

    @Test
    public void testSeverityNonMapIsReported() throws IOException {
        Path config = tempDir.resolve("severity-int.yml");
        Files.writeString(config, String.join("\n",
            "warnings:",
            "  severity: 42"
        ));

        WarningConfiguration wc =
            new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(),
            "'severity: 42' should produce a validation error");
    }

    @Test
    public void testNegativeFloatThresholdDetected()
            throws IOException {
        Path config = tempDir.resolve("neg-float.yml");
        Files.writeString(config, String.join("\n",
            "thresholds:",
            "  maxSteps: -0.5"
        ));

        WarningConfiguration wc =
            new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(),
            "-0.5 threshold must be detected as negative");
        assertTrue(errors.stream().anyMatch(
            e -> e.contains("negative") || e.contains("maxSteps")),
            "Error should flag the negative threshold");
    }

    @Test
    public void testValidationErrorsNotAccumulatedAcrossLoads()
            throws IOException {
        Path badConfig = tempDir.resolve("bad-accum.yml");
        Files.writeString(badConfig, String.join("\n",
            "bogusKey:",
            "  foo: bar"
        ));

        WarningConfiguration wc1 =
            new WarningConfiguration(badConfig.toString());
        assertFalse(wc1.getValidationErrors().isEmpty());

        WarningConfiguration wc2 =
            new WarningConfiguration(badConfig.toString());
        assertEquals(wc1.getValidationErrors().size(),
            wc2.getValidationErrors().size(),
            "Separate instances must not leak errors");
    }

    @Test
    public void testDefaultConfigHasNoValidationErrors() {
        WarningConfiguration wc = new WarningConfiguration();
        List<String> errors = wc.getValidationErrors();
        assertTrue(errors.isEmpty(),
            "Default config should produce no validation errors, but got: " + errors);
    }

    @Test
    public void testUnknownDisabledWarningReported() throws IOException {
        Path config = tempDir.resolve("unknown-disabled.yml");
        Files.writeString(config, String.join("\n",
            "warnings:",
            "  disabled:",
            "    - TOTALLY_FAKE_WARNING"
        ));

        WarningConfiguration wc = new WarningConfiguration(config.toString());
        List<String> errors = wc.getValidationErrors();
        assertFalse(errors.isEmpty(), "Should report unknown warning in disabled list");
        assertTrue(errors.stream().anyMatch(e -> e.contains("TOTALLY_FAKE_WARNING")),
            "Error should mention 'TOTALLY_FAKE_WARNING'");
    }
}
