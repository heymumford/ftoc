package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that Markdown link text does not break when feature or scenario
 * names contain characters that have special meaning in Markdown links.
 *
 * The Markdown link syntax [text](#anchor) breaks when text contains
 * unescaped ']', '(', ')', or '\\'. This test suite drives the addition
 * of escapeMarkdown() applied to link text.
 */
@DisplayName("TocFormatter Markdown link text escaping")
class MarkdownEscapingTest {

    private static Feature featureWithName(String name) {
        Feature f = new Feature("test.feature");
        f.setName(name);
        return f;
    }

    private static Scenario scenarioWithName(String name) {
        return new Scenario(name, "Scenario", 1);
    }

    @Test
    @DisplayName("Feature name with ] in markdown link text is escaped")
    void featureNameClosingBracketEscapedInLinkText() {
        Feature feature = featureWithName("Test [v2] Feature");
        Scenario scenario = scenarioWithName("Normal scenario");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String md = formatter.generateToc(
                List.of(feature), TocFormatter.Format.MARKDOWN,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(md.contains("- [Test [v2] Feature](#"),
            "Unescaped ] in link text breaks Markdown link syntax");
    }

    @Test
    @DisplayName("Feature name with ( and ) in markdown link text is escaped")
    void featureNameParenthesesEscapedInLinkText() {
        Feature feature = featureWithName("Test (beta) Feature");
        Scenario scenario = scenarioWithName("Normal scenario");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String md = formatter.generateToc(
                List.of(feature), TocFormatter.Format.MARKDOWN,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(md.contains("- [Test (beta) Feature](#"),
            "Unescaped () in link text breaks Markdown link syntax");
    }

    @Test
    @DisplayName("Feature name with backslash in markdown link text is escaped")
    void featureNameBackslashEscapedInLinkText() {
        Feature feature = featureWithName("Test \\path Feature");
        Scenario scenario = scenarioWithName("Normal scenario");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String md = formatter.generateToc(
                List.of(feature), TocFormatter.Format.MARKDOWN,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(md.contains("- [Test \\path Feature](#"),
            "Unescaped backslash in link text causes invalid Markdown");
    }

    @Test
    @DisplayName("Feature name 'Test [v2] (beta)' produces valid markdown link")
    void combinedSpecialCharsProduceValidMarkdownLink() {
        Feature feature = featureWithName("Test [v2] (beta)");
        Scenario scenario = scenarioWithName("Normal scenario");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String md = formatter.generateToc(
                List.of(feature), TocFormatter.Format.MARKDOWN,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(md.contains("- [Test [v2] (beta)](#"),
            "Combined special chars in link text must be escaped");
        assertTrue(md.contains("\\[v2\\]") || md.contains("\\[v2\\] \\(beta\\)"),
            "Special chars must be escaped with backslash in link text");
    }

    @Test
    @DisplayName("Scenario name with brackets in markdown link text is escaped")
    void scenarioNameBracketsEscapedInLinkText() {
        Feature feature = featureWithName("Normal Feature");
        Scenario scenario = scenarioWithName("Verify [API] response");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String md = formatter.generateToc(
                List.of(feature), TocFormatter.Format.MARKDOWN,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(md.contains("  - [Verify [API] response](#"),
            "Scenario name with brackets in link text must be escaped");
    }
}
