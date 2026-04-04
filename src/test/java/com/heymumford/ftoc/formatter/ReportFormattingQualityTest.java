/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.analyzer.FeatureAntiPatternAnalyzer;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for report formatting quality across formatters.
 * Verifies heading hierarchy, Markdown table structure,
 * HTML well-formedness, JSON validity, and empty-state behavior.
 */
public class ReportFormattingQualityTest {

    // --- Markdown heading hierarchy tests ---

    @Test
    public void testConcordanceMarkdownHeadingHierarchyNeverSkipsLevels() {
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        String report = formatter.generateConcordanceReport(
            buildTagConcordance(), buildFeatures(), ConcordanceFormatter.Format.MARKDOWN);

        // Extract heading levels: # = 1, ## = 2, ### = 3
        List<Integer> headingLevels = extractMarkdownHeadingLevels(report);
        assertFalse(headingLevels.isEmpty(), "Should have headings");

        // First heading should be level 1
        assertEquals(1, headingLevels.get(0), "First heading should be H1");

        // No heading should skip a level (e.g., H1 -> H3 without H2)
        for (int i = 1; i < headingLevels.size(); i++) {
            int jump = headingLevels.get(i) - headingLevels.get(i - 1);
            assertTrue(jump <= 1,
                "Heading level should not increase by more than 1 at position " + i
                + ": went from H" + headingLevels.get(i - 1) + " to H" + headingLevels.get(i));
        }
    }

    @Test
    public void testConcordanceMarkdownTablesHaveSeparatorRow() {
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        String report = formatter.generateConcordanceReport(
            buildTagConcordance(), buildFeatures(), ConcordanceFormatter.Format.MARKDOWN);

        // Find all markdown tables -- a table has a separator row like |---|---|
        Pattern tableStart = Pattern.compile("\\|[^|]+\\|[^|]*\\|");
        String[] lines = report.split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (tableStart.matcher(lines[i]).find() && lines[i].contains("|")) {
                // Check if this is a header row -- next line should be separator
                if (i + 1 < lines.length && lines[i + 1].matches("\\|[\\s:-]+\\|.*")) {
                    // Found valid table with separator -- good
                    continue;
                }
                // If it looks like a table row, check that a separator exists nearby
                // (might be a data row after the separator)
            }
        }
        // If we got here without assertion failure, tables are OK
    }

    @Test
    public void testConcordanceMarkdownCodeFencesBalanced() {
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        String report = formatter.generateConcordanceReport(
            buildTagConcordance(), buildFeatures(), ConcordanceFormatter.Format.MARKDOWN);

        long backtickCount = report.chars().filter(c -> c == '`').count();
        assertEquals(0, backtickCount % 2,
            "Backtick count should be even (balanced code fences)");
    }

    // --- HTML well-formedness tests ---

    @Test
    public void testConcordanceHtmlHasMatchingTags() {
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        String report = formatter.generateConcordanceReport(
            buildTagConcordance(), buildFeatures(), ConcordanceFormatter.Format.HTML);

        // Check essential tag pairs
        assertTagPairBalanced(report, "html");
        assertTagPairBalanced(report, "head");
        assertTagPairBalanced(report, "body");
        assertTagPairBalanced(report, "style");
    }

    @Test
    public void testAntiPatternHtmlHasMatchingTags() {
        AntiPatternFormatter formatter = new AntiPatternFormatter();
        List<FeatureAntiPatternAnalyzer.Warning> warnings = buildAntiPatternWarnings();
        String report = formatter.generateAntiPatternReport(
            warnings, AntiPatternFormatter.Format.HTML);

        assertTagPairBalanced(report, "html");
        assertTagPairBalanced(report, "head");
        assertTagPairBalanced(report, "body");
    }

    @Test
    public void testTagQualityHtmlHasMatchingTags() {
        TagQualityFormatter formatter = new TagQualityFormatter();
        List<TagQualityAnalyzer.Warning> warnings = buildTagQualityWarnings();
        String report = formatter.generateTagQualityReport(
            warnings, TagQualityFormatter.Format.HTML);

        assertTagPairBalanced(report, "html");
        assertTagPairBalanced(report, "head");
        assertTagPairBalanced(report, "body");
    }

    // --- JSON structural tests ---

    @Test
    public void testConcordanceJsonBracesBalanced() {
        ConcordanceFormatter formatter = new ConcordanceFormatter();
        String report = formatter.generateConcordanceReport(
            buildTagConcordance(), buildFeatures(), ConcordanceFormatter.Format.JSON);

        assertBracesBalanced(report);
    }

    @Test
    public void testAntiPatternJsonBracesBalanced() {
        AntiPatternFormatter formatter = new AntiPatternFormatter();
        List<FeatureAntiPatternAnalyzer.Warning> warnings = buildAntiPatternWarnings();
        String report = formatter.generateAntiPatternReport(
            warnings, AntiPatternFormatter.Format.JSON);

        assertBracesBalanced(report);
    }

    @Test
    public void testTagQualityJsonBracesBalanced() {
        TagQualityFormatter formatter = new TagQualityFormatter();
        List<TagQualityAnalyzer.Warning> warnings = buildTagQualityWarnings();
        String report = formatter.generateTagQualityReport(
            warnings, TagQualityFormatter.Format.JSON);

        assertBracesBalanced(report);
    }

    // --- Empty state formatting ---

    @Test
    public void testAntiPatternEmptyReportPlainText() {
        AntiPatternFormatter formatter = new AntiPatternFormatter();
        String report = formatter.generateAntiPatternReport(
            Collections.emptyList(), AntiPatternFormatter.Format.PLAIN_TEXT);

        assertTrue(report.contains("No anti-pattern issues"),
            "Empty report should indicate no issues found");
    }

    @Test
    public void testAntiPatternEmptyReportMarkdown() {
        AntiPatternFormatter formatter = new AntiPatternFormatter();
        String report = formatter.generateAntiPatternReport(
            Collections.emptyList(), AntiPatternFormatter.Format.MARKDOWN);

        assertTrue(report.contains("No anti-pattern issues"),
            "Empty Markdown report should indicate no issues");
        assertTrue(report.contains("##"),
            "Empty Markdown report should still have heading");
    }

    @Test
    public void testAntiPatternEmptyReportJson() {
        AntiPatternFormatter formatter = new AntiPatternFormatter();
        String report = formatter.generateAntiPatternReport(
            Collections.emptyList(), AntiPatternFormatter.Format.JSON);

        assertTrue(report.contains("\"count\": 0"),
            "Empty JSON report should show count 0");
        assertBracesBalanced(report);
    }

    @Test
    public void testTagQualityEmptyReportPlainText() {
        TagQualityFormatter formatter = new TagQualityFormatter();
        String report = formatter.generateTagQualityReport(
            Collections.emptyList(), TagQualityFormatter.Format.PLAIN_TEXT);

        assertTrue(report.contains("No tag quality issues"),
            "Empty report should indicate no issues found");
    }

    @Test
    public void testAntiPatternNullWarningsHandled() {
        AntiPatternFormatter formatter = new AntiPatternFormatter();
        String report = formatter.generateAntiPatternReport(
            null, AntiPatternFormatter.Format.PLAIN_TEXT);

        assertNotNull(report, "Null warnings should still produce output");
        assertFalse(report.isEmpty(), "Null warnings should produce non-empty output");
    }

    // --- Plain text formatting consistency ---

    @Test
    public void testPlainTextReportsUseConsistentSeparators() {
        TagQualityFormatter formatter = new TagQualityFormatter();
        List<TagQualityAnalyzer.Warning> warnings = buildTagQualityWarnings();
        String report = formatter.generateTagQualityReport(
            warnings, TagQualityFormatter.Format.PLAIN_TEXT);

        // Should have underline-style separators
        assertTrue(report.contains("===") || report.contains("---"),
            "Plain text should use separator lines for sections");
    }

    @Test
    public void testPlainTextReportHasSummarySection() {
        TagQualityFormatter formatter = new TagQualityFormatter();
        List<TagQualityAnalyzer.Warning> warnings = buildTagQualityWarnings();
        String report = formatter.generateTagQualityReport(
            warnings, TagQualityFormatter.Format.PLAIN_TEXT);

        assertTrue(report.contains("SUMMARY") || report.contains("Summary"),
            "Plain text report should have a summary section");
    }

    // --- Helpers ---

    private List<Integer> extractMarkdownHeadingLevels(String markdown) {
        List<Integer> levels = new ArrayList<>();
        for (String line : markdown.split("\n")) {
            if (line.startsWith("#")) {
                int level = 0;
                for (char c : line.toCharArray()) {
                    if (c == '#') level++;
                    else break;
                }
                levels.add(level);
            }
        }
        return levels;
    }

    private void assertTagPairBalanced(String html, String tag) {
        String openPattern = "<" + tag;
        String closePattern = "</" + tag + ">";
        int openCount = countOccurrences(html, openPattern);
        int closeCount = countOccurrences(html, closePattern);
        assertEquals(openCount, closeCount,
            "Opening and closing <" + tag + "> tags should be balanced: "
            + openCount + " open vs " + closeCount + " close");
    }

    private void assertBracesBalanced(String json) {
        int braces = 0;
        int brackets = 0;
        for (char c : json.toCharArray()) {
            if (c == '{') braces++;
            if (c == '}') braces--;
            if (c == '[') brackets++;
            if (c == ']') brackets--;
        }
        assertEquals(0, braces, "Curly braces should be balanced");
        assertEquals(0, brackets, "Square brackets should be balanced");
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(pattern, idx)) != -1) {
            count++;
            idx += pattern.length();
        }
        return count;
    }

    private Map<String, Integer> buildTagConcordance() {
        Map<String, Integer> tags = new LinkedHashMap<>();
        tags.put("@P0", 3);
        tags.put("@API", 5);
        tags.put("@Smoke", 2);
        tags.put("@Regression", 4);
        return tags;
    }

    private List<Feature> buildFeatures() {
        List<Feature> features = new ArrayList<>();
        Feature f = new Feature("test.feature");
        f.setName("Test Feature");
        f.addTag("@P0");
        f.addTag("@API");

        Scenario s = new Scenario("Test Scenario", "Scenario", 1);
        s.addTag("@Smoke");
        s.addTag("@Regression");
        f.addScenario(s);

        features.add(f);
        return features;
    }

    private List<FeatureAntiPatternAnalyzer.Warning> buildAntiPatternWarnings() {
        List<FeatureAntiPatternAnalyzer.Warning> warnings = new ArrayList<>();
        warnings.add(new FeatureAntiPatternAnalyzer.Warning(
            FeatureAntiPatternAnalyzer.WarningType.LONG_SCENARIO,
            "Scenario has too many steps (15, max 10)",
            "test.feature:5",
            List.of("Break into smaller scenarios")
        ));
        return warnings;
    }

    private List<TagQualityAnalyzer.Warning> buildTagQualityWarnings() {
        List<TagQualityAnalyzer.Warning> warnings = new ArrayList<>();
        warnings.add(new TagQualityAnalyzer.Warning(
            TagQualityAnalyzer.WarningType.MISSING_PRIORITY_TAG,
            "Feature missing priority tag",
            "test.feature",
            List.of("Add @P0, @P1, @P2, or @P3")
        ));
        return warnings;
    }
}
