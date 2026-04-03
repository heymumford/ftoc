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
 * Integration tests for custom YAML configuration loading (VS5).
 * Exercises --config-file via FtocUtility.main() against fixtures
 * in src/test/resources/ftoc/test-configs/.
 */
@DisplayName("Configuration Integration Tests")
class ConfigurationTest {

    private static final String TEST_FEATURES_DIR = "src/test/resources/ftoc/test-feature-files";
    private static final String CUSTOM_WARNINGS_CONFIG = "src/test/resources/ftoc/test-configs/custom-warnings.yml";
    private static final String DISABLED_WARNINGS_CONFIG = "src/test/resources/ftoc/test-configs/disabled-warnings.yml";

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
    @DisplayName("A12: --config-file loads custom warnings and affects tag analysis")
    void configFileLoadsCustomWarnings() {
        FtocUtility.main(new String[]{
                "--config-file", CUSTOM_WARNINGS_CONFIG,
                "--analyze-tags",
                "--detect-anti-patterns",
                "-d", TEST_FEATURES_DIR
        });

        String output = capturedOut.toString();

        assertTrue(output.contains("TAG QUALITY REPORT"),
                "Expected tag quality report when --analyze-tags is used with custom config");
        // custom-warnings.yml enables MISSING_PRIORITY_TAG at severity=error;
        // test-feature-files has scenarios without priority tags.
        // The all-caps section header "MISSING PRIORITY TAG" only appears when the warning is active.
        assertTrue(output.contains("MISSING PRIORITY TAG"),
                "Expected MISSING PRIORITY TAG warning section from custom config");
    }

    @Test
    @DisplayName("A12b: Custom config changes analysis output vs default config")
    void customConfigChangesAnalysisOutput() {
        // Run 1: default config (no --config-file)
        FtocUtility.main(new String[]{
                "--analyze-tags",
                "--detect-anti-patterns",
                "-d", TEST_FEATURES_DIR
        });
        String defaultOutput = capturedOut.toString();

        // Reset capture for second run
        capturedOut.reset();

        // Run 2: disabled-warnings config suppresses several warnings
        FtocUtility.main(new String[]{
                "--config-file", DISABLED_WARNINGS_CONFIG,
                "--analyze-tags",
                "--detect-anti-patterns",
                "-d", TEST_FEATURES_DIR
        });
        String customOutput = capturedOut.toString();

        // Both runs should produce reports
        assertTrue(defaultOutput.contains("TAG QUALITY REPORT"),
                "Default run should produce tag quality report");
        assertTrue(customOutput.contains("TAG QUALITY REPORT"),
                "Custom config run should produce tag quality report");

        // disabled-warnings.yml disables MISSING_PRIORITY_TAG -- default has it enabled.
        // The all-caps section header "MISSING PRIORITY TAG" only appears when the warning fires.
        assertTrue(defaultOutput.contains("MISSING PRIORITY TAG"),
                "Default config should include MISSING PRIORITY TAG warning section");
        assertFalse(customOutput.contains("MISSING PRIORITY TAG"),
                "Disabled-warnings config should suppress MISSING PRIORITY TAG warning section");
    }
}
