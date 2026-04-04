package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HTML entity escaping in TocFormatter HTML output.
 *
 * These tests drive the XSS fix: user-supplied content (feature names,
 * scenario names, descriptions, tags) injected into HTML must be escaped
 * so that special characters render as text, not as markup.
 */
@DisplayName("TocFormatter HTML escaping")
class HtmlEscapingTest {

    private static Feature featureWithName(String name) {
        Feature f = new Feature("test.feature");
        f.setName(name);
        return f;
    }

    private static Scenario scenarioWithName(String name) {
        return new Scenario(name, "Scenario", 1);
    }

    // --- feature name ---

    @Test
    @DisplayName("Feature name with <script> tag is escaped in HTML output")
    void featureNameScriptTagIsEscaped() {
        Feature feature = featureWithName("<script>alert('xss')</script>");
        Scenario scenario = scenarioWithName("Normal scenario");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String html = formatter.generateToc(
                List.of(feature), TocFormatter.Format.HTML,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(html.contains("<script>alert"),
                "HTML output must not contain raw <script> tag from feature name");
        assertTrue(html.contains("&lt;script&gt;"),
                "Feature name must be HTML-escaped in output");
    }

    @Test
    @DisplayName("Feature name with & and \" characters is escaped in HTML output")
    void featureNameAmpersandAndQuotesAreEscaped() {
        Feature feature = featureWithName("Features & \"Behaviors\" <test>");
        Scenario scenario = scenarioWithName("Normal scenario");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String html = formatter.generateToc(
                List.of(feature), TocFormatter.Format.HTML,
                Collections.emptyList(), Collections.emptyList());

        assertTrue(html.contains("&amp;"),
                "Ampersand must be escaped as &amp;");
        assertTrue(html.contains("&quot;"),
                "Double quote must be escaped as &quot;");
        assertTrue(html.contains("&lt;"),
                "Less-than must be escaped as &lt;");
    }

    // --- scenario name ---

    @Test
    @DisplayName("Scenario name with <script> tag is escaped in HTML output")
    void scenarioNameScriptTagIsEscaped() {
        Feature feature = featureWithName("Normal Feature");
        Scenario scenario = scenarioWithName("<script>alert('xss')</script>");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String html = formatter.generateToc(
                List.of(feature), TocFormatter.Format.HTML,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(html.contains("<script>alert"),
                "HTML output must not contain raw <script> tag from scenario name");
        assertTrue(html.contains("&lt;script&gt;"),
                "Scenario name must be HTML-escaped in output");
    }

    // --- feature description ---

    @Test
    @DisplayName("Feature description with HTML is escaped in HTML output")
    void featureDescriptionIsEscaped() {
        Feature feature = featureWithName("Normal Feature");
        feature.setDescription("<b>Bold</b> & <i>italic</i>");
        Scenario scenario = scenarioWithName("Normal scenario");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String html = formatter.generateToc(
                List.of(feature), TocFormatter.Format.HTML,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(html.contains("<b>Bold</b>"),
                "HTML output must not contain raw <b> tag from feature description");
        assertTrue(html.contains("&lt;b&gt;"),
                "Feature description must be HTML-escaped");
    }

    // --- scenario description ---

    @Test
    @DisplayName("Scenario description with HTML is escaped in HTML output")
    void scenarioDescriptionIsEscaped() {
        Feature feature = featureWithName("Normal Feature");
        Scenario scenario = scenarioWithName("Normal scenario");
        scenario.setDescription("<img src=x onerror=alert(1)>");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String html = formatter.generateToc(
                List.of(feature), TocFormatter.Format.HTML,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(html.contains("<img src=x"),
                "HTML output must not contain raw <img> tag from scenario description");
        assertTrue(html.contains("&lt;img"),
                "Scenario description must be HTML-escaped");
    }

    // --- scenario steps ---

    @Test
    @DisplayName("Scenario steps with <script> tag are escaped in HTML")
    void scenarioStepsAreEscaped() {
        Feature feature = featureWithName("Normal Feature");
        Scenario scenario = scenarioWithName("Scenario with XSS step");
        scenario.addStep(
            "Given a payload <script>alert('xss')</script>");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String html = formatter.generateToc(
                List.of(feature), TocFormatter.Format.HTML,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(html.contains("<script>alert"),
                "Steps must not contain raw <script> tag");
        assertTrue(html.contains("&lt;script&gt;"),
                "Step content must be HTML-escaped");
    }

    // --- example table ---

    @Test
    @DisplayName("Example table cells with XSS payload are escaped in HTML")
    void exampleTableCellsAreEscaped() {
        Feature feature = featureWithName("Normal Feature");
        Scenario outline = new Scenario(
            "Outline with XSS", "Scenario Outline", 1);
        outline.addStep("Given a <value>");
        Scenario.Example example = new Scenario.Example("data");
        example.setHeaders(List.of("value"));
        example.addRow(
            List.of("<img onerror=alert(1) src=x>"));
        outline.addExample(example);
        feature.addScenario(outline);

        TocFormatter formatter = new TocFormatter();
        String html = formatter.generateToc(
                List.of(feature), TocFormatter.Format.HTML,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(html.contains("<img onerror"),
                "Example cells must not contain raw <img> tag");
        assertTrue(html.contains("&lt;img"),
                "Example cell content must be HTML-escaped");
    }

    // --- markdown raw HTML headings ---

    @Test
    @DisplayName("Feature name with <script> is escaped in markdown h2")
    void markdownRawHtmlHeadingIsEscaped() {
        Feature feature = featureWithName(
            "<script>alert('xss')</script>");
        Scenario scenario = scenarioWithName("Normal scenario");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String md = formatter.generateToc(
                List.of(feature), TocFormatter.Format.MARKDOWN,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(md.contains("<script>alert"),
                "Markdown output must not contain raw <script> "
                + "in HTML headings");
        assertTrue(md.contains("&lt;script&gt;"),
                "Feature name in raw HTML h2 must be escaped");
    }

    // --- tag names ---

    @Test
    @DisplayName("Tag names with HTML characters are escaped in HTML output")
    void tagNamesWithHtmlCharactersAreEscaped() {
        Feature feature = featureWithName("Normal Feature");
        feature.addTag("@Tag<script>");
        Scenario scenario = scenarioWithName("Normal scenario");
        scenario.addTag("@Scenario<tag>");
        feature.addScenario(scenario);

        TocFormatter formatter = new TocFormatter();
        String html = formatter.generateToc(
                List.of(feature), TocFormatter.Format.HTML,
                Collections.emptyList(), Collections.emptyList());

        assertFalse(html.contains("@Tag<script>"),
                "HTML output must not contain raw tag with script characters");
        assertTrue(html.contains("&lt;"),
                "Tag names must be HTML-escaped");
    }
}
