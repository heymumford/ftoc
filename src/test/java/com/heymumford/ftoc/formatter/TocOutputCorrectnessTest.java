/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Structural correctness tests for each TOC output format.
 * Verifies that generated output meets the format's contract
 * (valid Markdown, well-formed HTML, parseable JSON, valid JUnit XML).
 */
public class TocOutputCorrectnessTest {

    private TocFormatter formatter;
    private List<Feature> features;

    @BeforeEach
    public void setUp() {
        formatter = new TocFormatter();
        features = buildTestFeatures();
    }

    // --- Plain Text format ---

    @Test
    public void testPlainTextHasHeader() {
        String output = formatter.generateToc(features, TocFormatter.Format.PLAIN_TEXT);
        assertTrue(output.startsWith("TABLE OF CONTENTS"),
            "Plain text should start with header");
        assertTrue(output.contains("================="),
            "Plain text should have underline separator");
    }

    @Test
    public void testPlainTextContainsAllFeatures() {
        String output = formatter.generateToc(features, TocFormatter.Format.PLAIN_TEXT);
        assertTrue(output.contains("Login Feature"), "Should contain feature name");
        assertTrue(output.contains("Search Feature"), "Should contain second feature");
    }

    @Test
    public void testPlainTextContainsScenarioDetails() {
        String output = formatter.generateToc(features, TocFormatter.Format.PLAIN_TEXT);
        assertTrue(output.contains("Scenario: Valid login"),
            "Should list scenarios");
        assertTrue(output.contains("Tags: @P0, @Smoke"),
            "Should show scenario tags");
    }

    @Test
    public void testPlainTextShowsFilename() {
        String output = formatter.generateToc(features, TocFormatter.Format.PLAIN_TEXT);
        assertTrue(output.contains("login.feature"),
            "Should show filename in parens");
    }

    // --- Markdown format ---

    @Test
    public void testMarkdownStartsWithH1() {
        String output = formatter.generateToc(features, TocFormatter.Format.MARKDOWN);
        assertTrue(output.startsWith("# Table of Contents"),
            "Markdown should start with H1");
    }

    @Test
    public void testMarkdownContainsTableOfContentsLinks() {
        String output = formatter.generateToc(features, TocFormatter.Format.MARKDOWN);
        // Table of contents section has links
        assertTrue(output.contains("- [Login Feature](#"),
            "Should have TOC link for feature");
        assertTrue(output.contains("- [Search Feature](#"),
            "Should have TOC link for second feature");
    }

    @Test
    public void testMarkdownAnchorsAreLowerCaseHyphenated() {
        String output = formatter.generateToc(features, TocFormatter.Format.MARKDOWN);
        assertTrue(output.contains("#login-feature"),
            "Anchor should be lowercase hyphenated");
    }

    @Test
    public void testMarkdownHasFeatureHeadings() {
        String output = formatter.generateToc(features, TocFormatter.Format.MARKDOWN);
        // Feature sections use H2 with id attributes
        assertTrue(output.contains("<h2 id=\"login-feature\">Login Feature</h2>"),
            "Should have H2 heading with id for feature");
    }

    @Test
    public void testMarkdownHasScenarioHeadings() {
        String output = formatter.generateToc(features, TocFormatter.Format.MARKDOWN);
        // Scenario sections use H3 with id attributes
        assertTrue(output.contains("<h3 id="),
            "Should have H3 heading for scenario");
    }

    @Test
    public void testMarkdownTagsInCodeFences() {
        String output = formatter.generateToc(features, TocFormatter.Format.MARKDOWN);
        assertTrue(output.contains("`@P0`"),
            "Tags should be wrapped in backtick code fences");
    }

    // --- JSON format ---

    @Test
    public void testJsonIsWellFormed() {
        String output = formatter.generateToc(features, TocFormatter.Format.JSON);
        assertTrue(output.trim().startsWith("{"),
            "JSON should start with {");
        assertTrue(output.trim().endsWith("}"),
            "JSON should end with }");
    }

    @Test
    public void testJsonContainsFeatureArray() {
        String output = formatter.generateToc(features, TocFormatter.Format.JSON);
        assertTrue(output.contains("\"features\""),
            "JSON should have features key");
        assertTrue(output.contains("\"name\": \"Login Feature\""),
            "JSON should contain feature name");
    }

    @Test
    public void testJsonContainsScenarios() {
        String output = formatter.generateToc(features, TocFormatter.Format.JSON);
        assertTrue(output.contains("\"scenarios\""),
            "JSON should have scenarios key");
        assertTrue(output.contains("\"name\": \"Valid login\""),
            "JSON should contain scenario name");
    }

    @Test
    public void testJsonContainsTags() {
        String output = formatter.generateToc(features, TocFormatter.Format.JSON);
        assertTrue(output.contains("\"tags\""),
            "JSON should have tags key");
        assertTrue(output.contains("\"@P0\""),
            "JSON should contain tag value");
    }

    // --- HTML format ---

    @Test
    public void testHtmlHasDoctype() {
        String output = formatter.generateToc(features, TocFormatter.Format.HTML);
        assertTrue(output.contains("<!DOCTYPE html>"),
            "HTML should have DOCTYPE");
    }

    @Test
    public void testHtmlHasStructuralElements() {
        String output = formatter.generateToc(features, TocFormatter.Format.HTML);
        assertTrue(output.contains("<html"), "Should have html tag");
        assertTrue(output.contains("<head>"), "Should have head tag");
        assertTrue(output.contains("<body>"), "Should have body tag");
        assertTrue(output.contains("</html>"), "Should close html tag");
    }

    @Test
    public void testHtmlContainsFeatureNames() {
        String output = formatter.generateToc(features, TocFormatter.Format.HTML);
        assertTrue(output.contains("Login Feature"),
            "HTML should contain feature name");
    }

    // --- JUnit XML format ---

    @Test
    public void testJunitXmlHasHeader() {
        String output = formatter.generateToc(features, TocFormatter.Format.JUNIT_XML);
        assertTrue(output.contains("<?xml version=\"1.0\""),
            "JUnit XML should have XML declaration");
    }

    @Test
    public void testJunitXmlHasTestsuiteWithClosingTag() {
        String output = formatter.generateToc(features, TocFormatter.Format.JUNIT_XML);
        assertTrue(output.contains("<testsuite"),
            "JUnit XML should have testsuite element");
        assertTrue(output.contains("</testsuite>"),
            "JUnit XML should close testsuite element");
    }

    @Test
    public void testJunitXmlContainsFeatureInfo() {
        String output = formatter.generateToc(features, TocFormatter.Format.JUNIT_XML);
        assertTrue(output.contains("Login Feature"),
            "JUnit XML should contain feature name");
    }

    // --- Tag filtering output ---

    @Test
    public void testPlainTextWithTagFilterShowsFilterInfo() {
        List<String> include = List.of("@P0");
        String output = formatter.generateToc(features, TocFormatter.Format.PLAIN_TEXT,
            include, Collections.emptyList());
        assertTrue(output.contains("FILTERS APPLIED"),
            "Filtered output should show filter info");
        assertTrue(output.contains("@P0"),
            "Should show the include tag");
    }

    @Test
    public void testTagFilterExcludesCorrectScenarios() {
        List<String> exclude = List.of("@P0");
        String output = formatter.generateToc(features, TocFormatter.Format.PLAIN_TEXT,
            Collections.emptyList(), exclude);
        // Valid login has @P0 so should be excluded
        assertFalse(output.contains("Valid login"),
            "@P0 scenario should be excluded");
    }

    @Test
    public void testEmptyFeaturesProducesValidOutput() {
        String output = formatter.generateToc(
            Collections.emptyList(), TocFormatter.Format.PLAIN_TEXT);
        assertNotNull(output);
        assertFalse(output.isEmpty(), "Even empty features should produce some output");
    }

    // --- Helpers ---

    private List<Feature> buildTestFeatures() {
        List<Feature> result = new ArrayList<>();

        Feature login = new Feature("/path/to/login.feature");
        login.setName("Login Feature");
        login.addTag("@Authentication");

        Scenario valid = new Scenario("Valid login", "Scenario", 5);
        valid.addTag("@P0");
        valid.addTag("@Smoke");
        valid.addStep("Given a registered user");
        valid.addStep("When the user logs in");
        valid.addStep("Then the user sees the dashboard");
        login.addScenario(valid);

        Scenario invalid = new Scenario("Invalid login", "Scenario", 10);
        invalid.addTag("@P1");
        invalid.addTag("@Negative");
        login.addScenario(invalid);

        Feature search = new Feature("/path/to/search.feature");
        search.setName("Search Feature");
        search.addTag("@Search");

        Scenario basic = new Scenario("Basic search", "Scenario", 1);
        basic.addTag("@P2");
        search.addScenario(basic);

        result.add(login);
        result.add(search);
        return result;
    }
}
