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
 * VS3 integration tests: anti-pattern detection via CLI entry point.
 */
public class AntiPatternTest {

    private static final String ANTI_PATTERN_DIR = "src/test/resources/ftoc/test-feature-files";

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    /**
     * A9: --detect-anti-patterns finds known issues in anti_patterns.feature.
     * The fixture contains long scenarios, missing Given/When/Then steps,
     * UI-focused steps, implementation details, inconsistent tense,
     * ambiguous pronouns, and conjunction steps.
     */
    @Test
    public void detectAntiPatternsFindsKnownIssues() {
        FtocUtility.main(new String[]{
            "-d", ANTI_PATTERN_DIR,
            "--detect-anti-patterns"
        });

        String output = outputStream.toString();

        // Report header present
        assertTrue(output.contains("FEATURE ANTI-PATTERN REPORT"),
                "Output should contain the anti-pattern report header");

        // At least some anti-pattern categories detected from the fixture
        assertTrue(output.contains("Long scenario"),
                "Should detect the long-scenario anti-pattern");
        assertTrue(output.contains("Missing") || output.contains("missing"),
                "Should detect missing-step anti-patterns (Given/When/Then)");
    }

    /**
     * A9b: --detect-anti-patterns with markdown format produces markdown output.
     */
    @Test
    public void antiPatternMarkdownFormatProducesMarkdown() {
        FtocUtility.main(new String[]{
            "-d", ANTI_PATTERN_DIR,
            "--detect-anti-patterns",
            "--anti-pattern-format", "markdown"
        });

        String output = outputStream.toString();

        // Markdown-specific markers
        assertTrue(output.contains("# Feature Anti-Pattern Report"),
                "Markdown output should contain H1 header");
        assertTrue(output.contains("## Summary"),
                "Markdown output should contain Summary section");
        assertTrue(output.contains("| Warning Type"),
                "Markdown output should contain a summary table");
    }
}
