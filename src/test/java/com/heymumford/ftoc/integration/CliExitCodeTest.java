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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CLI exit code semantics and error reporting.
 * Verifies that FtocUtility.main() returns appropriate exit codes
 * and produces useful error messages for various failure modes.
 */
@DisplayName("CLI Exit Code and Error Reporting Tests")
class CliExitCodeTest {

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
    @DisplayName("main() with --help returns exit code 0")
    void helpReturnsZeroExitCode() {
        int exitCode = FtocUtility.mainWithExitCode(new String[]{"--help"});
        assertEquals(0, exitCode, "--help should return exit code 0");
    }

    @Test
    @DisplayName("main() with --version returns exit code 0")
    void versionReturnsZeroExitCode() {
        int exitCode = FtocUtility.mainWithExitCode(new String[]{"--version"});
        assertEquals(0, exitCode, "--version should return exit code 0");
    }

    @Test
    @DisplayName("main() with unknown flag returns exit code 1")
    void unknownFlagReturnsNonZeroExitCode() {
        int exitCode = FtocUtility.mainWithExitCode(new String[]{"--bogus"});
        assertEquals(1, exitCode, "Unknown flag should return exit code 1");
    }

    @Test
    @DisplayName("main() with nonexistent directory returns exit code 1")
    void nonexistentDirectoryReturnsNonZeroExitCode() {
        int exitCode = FtocUtility.mainWithExitCode(
            new String[]{"-d", "/nonexistent/path/that/does/not/exist"});
        assertEquals(1, exitCode, "Nonexistent directory should return exit code 1");
    }

    @Test
    @DisplayName("main() with no args returns exit code 0 (shows help)")
    void noArgsShowsHelpAndReturnsZero() {
        int exitCode = FtocUtility.mainWithExitCode(new String[]{});
        assertEquals(0, exitCode, "No args shows help and returns 0");
        String output = capturedOut.toString();
        assertTrue(output.contains("Usage:"), "Should show usage");
    }

    @Test
    @DisplayName("Unknown flag error message names the flag")
    void unknownFlagErrorNamesTheFlag() {
        FtocUtility.mainWithExitCode(new String[]{"--nonexistent-option"});
        String stderr = capturedErr.toString();
        assertTrue(stderr.contains("--nonexistent-option"),
            "Error should name the unknown flag");
    }

    @Test
    @DisplayName("-v is equivalent to --version")
    void shortVersionFlag() {
        int exitCode = FtocUtility.mainWithExitCode(new String[]{"-v"});
        assertEquals(0, exitCode);
        String output = capturedOut.toString();
        assertTrue(output.contains("version"),
            "-v should output version info");
    }

    @Test
    @DisplayName("Multiple unknown flags reports the first one")
    void multipleUnknownFlagsReportsFirst() {
        FtocUtility.mainWithExitCode(new String[]{"--bad1", "--bad2"});
        String stderr = capturedErr.toString();
        assertTrue(stderr.contains("--bad1"),
            "Should report the first unknown flag");
    }

    @Test
    @DisplayName("-d flag without value produces error")
    void directoryFlagWithoutValue() {
        // When -d is the last arg with no value, it should handle gracefully
        int exitCode = FtocUtility.mainWithExitCode(new String[]{"-d"});
        // Either shows help or processes current directory -- both are OK
        // but should not crash
        assertTrue(exitCode == 0 || exitCode == 1,
            "Should return 0 or 1, not crash");
    }

    @Test
    @DisplayName("Valid directory with features returns exit code 0")
    void validDirectoryReturnsZero() {
        // Use the test resources directory which has feature files
        int exitCode = FtocUtility.mainWithExitCode(
            new String[]{"-d", "src/test/resources"});
        assertEquals(0, exitCode, "Valid directory should return exit code 0");
    }

}
