package com.heymumford.ftoc.integration;

import com.heymumford.ftoc.formatter.ConcordanceAnalyzer;
import com.heymumford.ftoc.formatter.ConcordanceFormatter;
import com.heymumford.ftoc.formatter.TocFormatter;
import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import com.heymumford.ftoc.parser.FeatureParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance baseline fitness functions.
 * These tests establish timing thresholds to detect regressions.
 * Thresholds are generous (10x typical) to avoid flaky failures
 * on slow CI runners while still catching order-of-magnitude regressions.
 */
@DisplayName("Performance Baseline Tests")
class PerformanceBaselineTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Parse 100 feature files in under 5 seconds")
    void parse100FeaturesUnder5Seconds() throws Exception {
        // Generate 100 realistic feature files
        for (int i = 0; i < 100; i++) {
            Path file = tempDir.resolve("feature_" + i + ".feature");
            Files.writeString(file, generateFeatureContent(i, 5));
        }

        FeatureParser parser = new FeatureParser();
        long start = System.nanoTime();

        List<Feature> features = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Path file = tempDir.resolve("feature_" + i + ".feature");
            features.add(parser.parseFeatureFile(file.toFile()));
        }

        long elapsed = System.nanoTime() - start;
        long elapsedMs = elapsed / 1_000_000;

        assertEquals(100, features.size(), "Should parse all 100 files");
        assertTrue(elapsedMs < 5000,
            "Parsing 100 files took " + elapsedMs + "ms, threshold is 5000ms");
    }

    @Test
    @DisplayName("Generate TOC for 50 features in under 2 seconds")
    void generateTocFor50FeaturesUnder2Seconds() {
        List<Feature> features = generateFeatureModels(50, 10);

        TocFormatter formatter = new TocFormatter();
        long start = System.nanoTime();

        String plainText = formatter.generateToc(features, TocFormatter.Format.PLAIN_TEXT);
        String markdown = formatter.generateToc(features, TocFormatter.Format.MARKDOWN);
        String json = formatter.generateToc(features, TocFormatter.Format.JSON);
        String html = formatter.generateToc(features, TocFormatter.Format.HTML);

        long elapsed = System.nanoTime() - start;
        long elapsedMs = elapsed / 1_000_000;

        assertFalse(plainText.isEmpty());
        assertFalse(markdown.isEmpty());
        assertFalse(json.isEmpty());
        assertFalse(html.isEmpty());
        assertTrue(elapsedMs < 2000,
            "Generating TOC in all 4 formats took " + elapsedMs + "ms, threshold is 2000ms");
    }

    @Test
    @DisplayName("Calculate co-occurrences for 50 features in under 2 seconds")
    void coOccurrenceAnalysisFor50FeaturesUnder2Seconds() {
        List<Feature> features = generateFeatureModels(50, 10);

        long start = System.nanoTime();

        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences =
            ConcordanceAnalyzer.calculateCoOccurrences(features);

        long elapsed = System.nanoTime() - start;
        long elapsedMs = elapsed / 1_000_000;

        assertFalse(coOccurrences.isEmpty(), "Should find co-occurrences");
        assertTrue(elapsedMs < 2000,
            "Co-occurrence analysis took " + elapsedMs + "ms, threshold is 2000ms");
    }

    @Test
    @DisplayName("Full concordance report for 50 features in under 3 seconds")
    void concordanceReportFor50FeaturesUnder3Seconds() {
        List<Feature> features = generateFeatureModels(50, 10);
        Map<String, Integer> tagConcordance = buildTagConcordance(features);

        ConcordanceFormatter formatter = new ConcordanceFormatter();
        long start = System.nanoTime();

        String report = formatter.generateConcordanceReport(
            tagConcordance, features, ConcordanceFormatter.Format.PLAIN_TEXT);

        long elapsed = System.nanoTime() - start;
        long elapsedMs = elapsed / 1_000_000;

        assertFalse(report.isEmpty());
        assertTrue(elapsedMs < 3000,
            "Concordance report took " + elapsedMs + "ms, threshold is 3000ms");
    }

    @Test
    @DisplayName("Tag trend analysis for 50 features in under 2 seconds")
    void tagTrendAnalysisFor50FeaturesUnder2Seconds() {
        List<Feature> features = generateFeatureModels(50, 10);
        Map<String, Integer> tagConcordance = buildTagConcordance(features);

        long start = System.nanoTime();

        Map<String, ConcordanceAnalyzer.TagTrend> trends =
            ConcordanceAnalyzer.calculateTagTrends(features, tagConcordance);

        long elapsed = System.nanoTime() - start;
        long elapsedMs = elapsed / 1_000_000;

        assertFalse(trends.isEmpty(), "Should calculate trends");
        assertTrue(elapsedMs < 2000,
            "Tag trend analysis took " + elapsedMs + "ms, threshold is 2000ms");
    }

    @Test
    @DisplayName("Tag significance calculation for 50 features in under 1 second")
    void tagSignificanceFor50FeaturesUnder1Second() {
        List<Feature> features = generateFeatureModels(50, 10);
        Map<String, Integer> tagConcordance = buildTagConcordance(features);

        long start = System.nanoTime();

        Map<String, Double> significance =
            ConcordanceAnalyzer.calculateTagSignificance(features, tagConcordance);

        long elapsed = System.nanoTime() - start;
        long elapsedMs = elapsed / 1_000_000;

        assertFalse(significance.isEmpty());
        assertTrue(elapsedMs < 1000,
            "Tag significance took " + elapsedMs + "ms, threshold is 1000ms");
    }

    // --- Helpers ---

    private String generateFeatureContent(int featureIndex, int scenarioCount) {
        StringBuilder sb = new StringBuilder();
        String[] tags = {"@P0", "@P1", "@P2", "@API", "@UI", "@Smoke", "@Regression"};

        sb.append("@").append(tags[featureIndex % tags.length].substring(1));
        sb.append(" @Feature").append(featureIndex).append("\n");
        sb.append("Feature: Feature number ").append(featureIndex).append("\n");
        sb.append("  As a tester\n");
        sb.append("  I want to verify feature ").append(featureIndex).append("\n\n");

        for (int s = 0; s < scenarioCount; s++) {
            sb.append("  @").append(tags[(featureIndex + s) % tags.length].substring(1));
            sb.append(" @Scenario").append(s).append("\n");

            if (s % 3 == 0) {
                sb.append("  Scenario Outline: Scenario ").append(s);
                sb.append(" of feature ").append(featureIndex).append("\n");
                sb.append("    Given a precondition with <param>\n");
                sb.append("    When the user performs action ").append(s).append("\n");
                sb.append("    And provides input <input>\n");
                sb.append("    Then the result is <expected>\n");
                sb.append("    Examples:\n");
                sb.append("      | param | input | expected |\n");
                sb.append("      | A     | 1     | pass     |\n");
                sb.append("      | B     | 2     | fail     |\n");
            } else {
                sb.append("  Scenario: Scenario ").append(s);
                sb.append(" of feature ").append(featureIndex).append("\n");
                sb.append("    Given a precondition for scenario ").append(s).append("\n");
                sb.append("    When the user performs action ").append(s).append("\n");
                sb.append("    Then the expected outcome ").append(s).append(" occurs\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private List<Feature> generateFeatureModels(int featureCount, int scenariosPerFeature) {
        String[] tags = {"@P0", "@P1", "@P2", "@API", "@UI", "@Smoke", "@Regression",
                         "@E2E", "@Integration", "@Performance"};
        List<Feature> features = new ArrayList<>();

        for (int f = 0; f < featureCount; f++) {
            Feature feature = new Feature("feature_" + f + ".feature");
            feature.setName("Feature " + f);
            feature.addTag(tags[f % tags.length]);

            for (int s = 0; s < scenariosPerFeature; s++) {
                String type = (s % 3 == 0) ? "Scenario Outline" : "Scenario";
                Scenario scenario = new Scenario(
                    "Scenario " + s + " of feature " + f, type, s * 5 + 1);
                scenario.addTag(tags[(f + s) % tags.length]);
                scenario.addTag(tags[(f + s + 1) % tags.length]);
                scenario.addStep("Given a precondition");
                scenario.addStep("When an action");
                scenario.addStep("Then a result");

                if (type.equals("Scenario Outline")) {
                    Scenario.Example ex = new Scenario.Example("Example set");
                    ex.setHeaders(List.of("param", "value"));
                    ex.addRow(List.of("A", "1"));
                    ex.addRow(List.of("B", "2"));
                    scenario.addExample(ex);
                }

                feature.addScenario(scenario);
            }

            features.add(feature);
        }

        return features;
    }

    private Map<String, Integer> buildTagConcordance(List<Feature> features) {
        Map<String, Integer> concordance = new HashMap<>();
        for (Feature f : features) {
            for (String tag : f.getTags()) {
                concordance.merge(tag, 1, Integer::sum);
            }
            for (Scenario s : f.getScenarios()) {
                for (String tag : s.getTags()) {
                    concordance.merge(tag, 1, Integer::sum);
                }
            }
        }
        return concordance;
    }
}
