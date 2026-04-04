/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.parser;

import com.heymumford.ftoc.exception.FtocException;
import com.heymumford.ftoc.model.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KarateParser.
 * Covers both parseFeatureFile(String) and parseFeatureFile(File) code paths.
 */
@DisplayName("KarateParser unit tests")
class KarateParserTest {

    private static final String KARATE_FEATURE_CONTENT =
            "@API @KarateTest\n" +
            "Feature: User API endpoints\n" +
            "  Karate-style API test for user management\n" +
            "\n" +
            "  Background:\n" +
            "    * url 'http://localhost:8080'\n" +
            "\n" +
            "  @GET @Positive\n" +
            "  Scenario: Get user by ID\n" +
            "    * path '/users/1'\n" +
            "    * method GET\n" +
            "    * status 200\n" +
            "    * match response.name == '#string'\n";

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("File path: parseFeatureFile(File) sets hasKarateSyntax metadata")
    void fileBasedParseSetsKarateMetadata() throws IOException, FtocException {
        Path featureFile = tempDir.resolve("user_api.feature");
        Files.writeString(featureFile, KARATE_FEATURE_CONTENT);

        KarateParser parser = new KarateParser();
        Feature feature = parser.parseFeatureFile(featureFile.toFile());

        assertNotNull(feature.getMetadata("hasKarateSyntax"),
                "parseFeatureFile(File) must set hasKarateSyntax metadata");
        assertEquals("true", feature.getMetadata("hasKarateSyntax"),
                "hasKarateSyntax must be 'true' for a Karate-style file");
    }

    @Test
    @DisplayName("File path: parseFeatureFile(File) detects API calls")
    void fileBasedParseDetectsApiCalls() throws IOException, FtocException {
        Path featureFile = tempDir.resolve("user_api.feature");
        Files.writeString(featureFile, KARATE_FEATURE_CONTENT);

        KarateParser parser = new KarateParser();
        Feature feature = parser.parseFeatureFile(featureFile.toFile());

        assertEquals("true", feature.getMetadata("hasApiCalls"),
                "parseFeatureFile(File) must detect API calls (url/method/status steps)");
    }

    @Test
    @DisplayName("String path: parseFeatureFile(String) still sets hasKarateSyntax metadata")
    void stringBasedParseSetsKarateMetadata() throws IOException, FtocException {
        Path featureFile = tempDir.resolve("user_api_str.feature");
        Files.writeString(featureFile, KARATE_FEATURE_CONTENT);

        KarateParser parser = new KarateParser();
        Feature feature = parser.parseFeatureFile(featureFile.toAbsolutePath().toString());

        assertEquals("true", feature.getMetadata("hasKarateSyntax"),
                "parseFeatureFile(String) must set hasKarateSyntax metadata (regression guard)");
    }
}
