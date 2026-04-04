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
 * VS4 integration tests: tag filtering via CLI entry point.
 *
 * Fixture: basic.feature contains three scenarios:
 *   - "Simple scenario with basic elements"      tags: @Smoke @P1
 *   - "Another simple scenario with different tags" tags: @Regression @P2
 *   - "Scenario with low-value tags"             tags: @Debug @Smoke @Flaky
 */
public class TagFilteringTest {

    private static final String BASIC_DIR = "src/test/resources/ftoc/test-feature-files";

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
     * A10: --tags includes only scenarios that carry the specified tag.
     * @Smoke appears on "Simple scenario" and "Scenario with low-value tags",
     * but NOT on "Another simple scenario with different tags".
     */
    @Test
    public void tagsFilterIncludesOnlyMatchingScenarios() {
        FtocUtility.main(new String[]{
            "-d", BASIC_DIR,
            "--tags", "@Smoke"
        });

        String output = outputStream.toString();

        assertTrue(output.contains("FILTERS APPLIED"),
                "Output should indicate filters were applied");
        assertTrue(output.contains("Include tags:") && output.contains("@Smoke"),
                "Output should list the include tag");

        // Scenarios with @Smoke should appear
        assertTrue(output.contains("Simple scenario with basic elements"),
                "Should include scenario tagged @Smoke");

        // The Regression-only scenario should be absent from the TOC
        assertFalse(output.contains("Another simple scenario with different tags"),
                "Should not include scenario without @Smoke tag");
    }

    /**
     * A11: --exclude-tags removes scenarios that carry the specified tag.
     * Excluding @Regression should drop "Another simple scenario" but keep the rest.
     */
    @Test
    public void excludeTagsFiltersOutMatchingScenarios() {
        FtocUtility.main(new String[]{
            "-d", BASIC_DIR,
            "--exclude-tags", "@Regression"
        });

        String output = outputStream.toString();

        assertTrue(output.contains("FILTERS APPLIED"),
                "Output should indicate filters were applied");
        assertTrue(output.contains("Exclude tags:") && output.contains("@Regression"),
                "Output should list the exclude tag");

        // The excluded scenario should not appear
        assertFalse(output.contains("Another simple scenario with different tags"),
                "Should not include scenario with excluded @Regression tag");

        // Non-excluded scenarios should still appear
        assertTrue(output.contains("Simple scenario with basic elements"),
                "Should still include scenario without @Regression tag");
    }

    /**
     * A10+A11: Combined --tags and --exclude-tags.
     * Include @Smoke, exclude @Flaky.
     * Expected: "Simple scenario with basic elements" (has @Smoke, no @Flaky).
     * Excluded: "Scenario with low-value tags" (has @Smoke AND @Flaky).
     * Excluded: "Another simple scenario" (no @Smoke).
     */
    @Test
    public void combinedTagsAndExcludeTagsFilterCorrectly() {
        FtocUtility.main(new String[]{
            "-d", BASIC_DIR,
            "--tags", "@Smoke",
            "--exclude-tags", "@Flaky"
        });

        String output = outputStream.toString();

        assertTrue(output.contains("FILTERS APPLIED"),
                "Output should indicate filters were applied");

        // Only the @Smoke-without-@Flaky scenario should remain
        assertTrue(output.contains("Simple scenario with basic elements"),
                "Should include @Smoke scenario that is not @Flaky");

        // @Flaky scenario excluded even though it has @Smoke
        assertFalse(output.contains("Scenario with low-value tags"),
                "Should exclude scenario that has @Flaky even if also @Smoke");

        // Non-@Smoke scenario excluded by include filter
        assertFalse(output.contains("Another simple scenario with different tags"),
                "Should not include scenario lacking @Smoke");
    }
}
