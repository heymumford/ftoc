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
 * Integration tests for CLI argument parsing: help, version, and error handling.
 * VS7 acceptance tests A14, A15, A16.
 */
@DisplayName("CLI Integration Tests")
class CliIntegrationTest {

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

    @Test
    @DisplayName("A14: --help outputs usage with all key flags")
    void helpFlagOutputsUsageWithAllKeyFlags() {
        FtocUtility.main(new String[]{"--help"});

        String output = capturedOut.toString();
        assertAll("help output contains key flags",
                () -> assertTrue(output.contains("--format"), "missing --format"),
                () -> assertTrue(output.contains("--concordance"), "missing --concordance"),
                () -> assertTrue(output.contains("--analyze-tags"), "missing --analyze-tags"),
                () -> assertTrue(output.contains("--detect-anti-patterns"), "missing --detect-anti-patterns"),
                () -> assertTrue(output.contains("--tags"), "missing --tags"),
                () -> assertTrue(output.contains("--exclude-tags"), "missing --exclude-tags"),
                () -> assertTrue(output.contains("--config-file"), "missing --config-file")
        );
    }

    @Test
    @DisplayName("A15: --version outputs version number")
    void versionFlagOutputsVersionNumber() {
        FtocUtility.main(new String[]{"--version"});

        String output = capturedOut.toString();
        assertTrue(output.contains("FTOC Utility version"), "missing version banner");
        assertTrue(output.matches("(?s).*\\d+\\.\\d+\\.\\d+.*"), "missing semver pattern");
    }

    @Test
    @DisplayName("A16: Unknown flag prints error and usage hint")
    void unknownFlagPrintsErrorAndUsageHint() {
        FtocUtility.main(new String[]{"--invalid-flag"});

        String stderr = capturedErr.toString();
        String stdout = capturedOut.toString();

        assertTrue(stderr.contains("Unknown option: --invalid-flag"), "stderr missing unknown option message");
        assertTrue(stdout.contains("Usage:"), "stdout missing usage hint after error");
    }
}
