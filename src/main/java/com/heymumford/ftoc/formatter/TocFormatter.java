package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Formatter for generating Table of Contents (TOC) in different formats.
 */
public class TocFormatter {

    // Default number of scenarios per page for paginated HTML output
    private static final int DEFAULT_SCENARIOS_PER_PAGE = 20;

    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON,
        JUNIT_XML
    }

    /**
     * Generate a TOC for a list of features in the specified format.
     *
     * @param features List of features to include in the TOC
     * @param format The output format
     * @return The formatted TOC as a string
     */
    public String generateToc(List<Feature> features, Format format) {
        return generateToc(features, format,
            Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Generate a TOC for a list of features in the specified format
     * with tag filtering.
     *
     * @param features List of features to include in the TOC
     * @param format The output format
     * @param includeTags Scenarios must have at least one of these
     *     tags to be included (empty list means include all)
     * @param excludeTags Scenarios with any of these tags will be
     *     excluded
     * @return The formatted TOC as a string
     */
    public String generateToc(List<Feature> features, Format format,
            List<String> includeTags, List<String> excludeTags) {
        switch (format) {
            case PLAIN_TEXT:
                return generatePlainTextToc(
                    features, includeTags, excludeTags);
            case MARKDOWN:
                return generateMarkdownToc(
                    features, includeTags, excludeTags);
            case HTML:
                return generateHtmlToc(
                    features, includeTags, excludeTags);
            case JSON:
                return generateJsonToc(
                    features, includeTags, excludeTags);
            case JUNIT_XML:
                return generateJUnitXmlToc(
                    features, includeTags, excludeTags);
            default:
                return generatePlainTextToc(
                    features, includeTags, excludeTags);
        }
    }

    /**
     * Generate a TOC in plain text format.
     */
    private String generatePlainTextToc(List<Feature> features) {
        return generatePlainTextToc(features,
            Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Generate a TOC in plain text format with tag filtering.
     */
    private String generatePlainTextToc(List<Feature> features,
            List<String> includeTags, List<String> excludeTags) {
        StringBuilder toc = new StringBuilder();
        toc.append("TABLE OF CONTENTS\n");
        toc.append("=================\n\n");

        // Add tag filter information if any filters are applied
        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            toc.append("FILTERS APPLIED:\n");
            if (!includeTags.isEmpty()) {
                toc.append("  Include tags: ");
                toc.append(String.join(", ", includeTags));
                toc.append("\n");
            }
            if (!excludeTags.isEmpty()) {
                toc.append("  Exclude tags: ");
                toc.append(String.join(", ", excludeTags));
                toc.append("\n");
            }
            toc.append("\n");
        }

        boolean hasMatchingScenarios = false;

        for (Feature feature : features) {
            List<Scenario> filteredScenarios =
                feature.getFilteredScenarios(
                    includeTags, excludeTags);

            if (filteredScenarios.isEmpty()
                    || filteredScenarios.stream()
                        .allMatch(Scenario::isBackground)) {
                continue;
            }

            hasMatchingScenarios = true;
            toc.append(feature.getName()).append(" (");
            toc.append(feature.getFilename()).append(")\n");

            if (!feature.getTags().isEmpty()) {
                toc.append("  Tags: ");
                toc.append(String.join(", ", feature.getTags()));
                toc.append("\n");
            }

            if (feature.hasMetadata("hasKarateSyntax")
                    && "true".equals(
                        feature.getMetadata("hasKarateSyntax"))) {
                toc.append("  [Karate-style Feature File]\n");

                if ("true".equals(
                        feature.getMetadata("hasApiCalls"))) {
                    toc.append("  - Contains API calls\n");
                }
                if ("true".equals(
                        feature.getMetadata("hasJsonSchema"))) {
                    toc.append("  - Contains JSON schema ");
                    toc.append("validation\n");
                }
                if ("true".equals(
                        feature.getMetadata("hasJsonMatching"))) {
                    toc.append("  - Contains JSON matching\n");
                }
                if ("true".equals(feature.getMetadata(
                        "hasEmbeddedJavaScript"))) {
                    toc.append("  - Contains embedded ");
                    toc.append("JavaScript\n");
                }
                if ("true".equals(
                        feature.getMetadata("hasApiOperations"))) {
                    toc.append("  - Contains API operations ");
                    toc.append("(GET, POST, etc.)\n");
                }
            }

            int scenarioDepth = 0;
            Scenario lastScenarioOutline = null;

            for (Scenario scenario : filteredScenarios) {
                if (scenario.isBackground()) {
                    continue;
                }

                if (!scenario.isOutline()) {
                    scenarioDepth = 0;
                    lastScenarioOutline = null;
                }

                String indent = "  "
                    + "  ".repeat(scenarioDepth);

                String prefix = scenario.isOutline()
                    ? indent + "Scenario Outline: "
                    : indent + "Scenario: ";
                toc.append(prefix);
                toc.append(scenario.getName()).append("\n");

                if (!scenario.getTags().isEmpty()) {
                    toc.append(indent).append("  Tags: ");
                    toc.append(String.join(
                        ", ", scenario.getTags()));
                    toc.append("\n");
                }

                if (scenario.isOutline()
                        && !scenario.getExamples().isEmpty()) {
                    lastScenarioOutline = scenario;
                    scenarioDepth++;

                    int totalExamples = scenario.getExamples()
                        .stream()
                        .mapToInt(ex -> ex.getRows().size())
                        .sum();

                    String exIndent = "  "
                        + "  ".repeat(scenarioDepth);
                    toc.append(exIndent).append("Examples: ");
                    toc.append(totalExamples);
                    toc.append(" total\n");

                    for (Scenario.Example example
                            : scenario.getExamples()) {
                        String exName = example.getName().isEmpty()
                            ? "Unnamed" : example.getName();
                        toc.append(exIndent).append("  - ");
                        toc.append(exName).append(": ");
                        toc.append(example.getRows().size());
                        toc.append(" variations\n");
                    }
                }
            }

            toc.append("\n");
        }

        if (!hasMatchingScenarios) {
            toc.append("No scenarios match the specified ");
            toc.append("tag filters.\n");
        }

        return toc.toString();
    }

    /**
     * Generate a TOC in Markdown format.
     */
    private String generateMarkdownToc(List<Feature> features) {
        return generateMarkdownToc(features,
            Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Generate a TOC in Markdown format with tag filtering.
     */
    private String generateMarkdownToc(List<Feature> features,
            List<String> includeTags, List<String> excludeTags) {
        StringBuilder toc = new StringBuilder();
        toc.append("# Table of Contents\n\n");
        toc.append("## Contents\n\n");

        List<Feature> filteredFeatures =
            getFilteredFeatures(features, includeTags, excludeTags);

        if (filteredFeatures.isEmpty()) {
            toc.append("- No matching scenarios\n\n");
        } else {
            for (Feature feature : filteredFeatures) {
                String anchor = feature.getName().toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-");
                toc.append("- [").append(feature.getName());
                toc.append("](#").append(anchor).append(")\n");

                List<Scenario> scenarios = feature
                    .getFilteredScenarios(includeTags, excludeTags)
                    .stream()
                    .filter(s -> !s.isBackground())
                    .collect(Collectors.toList());

                if (!scenarios.isEmpty()) {
                    int limit = Math.min(scenarios.size(), 5);
                    for (int i = 0; i < limit; i++) {
                        String sAnchor = anchor + "-"
                            + scenarios.get(i).getName()
                                .toLowerCase()
                                .replaceAll("[^a-z0-9]+", "-");
                        toc.append("  - [");
                        toc.append(scenarios.get(i).getName());
                        toc.append("](#").append(sAnchor);
                        toc.append(")\n");
                    }
                    if (scenarios.size() > 5) {
                        toc.append("  - ... and ");
                        toc.append(scenarios.size() - 5);
                        toc.append(" more\n");
                    }
                }
            }
            toc.append("\n");
        }

        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            toc.append("## Filters Applied\n\n");
            if (!includeTags.isEmpty()) {
                toc.append("**Include tags:** ");
                for (String tag : includeTags) {
                    toc.append("`").append(tag).append("` ");
                }
                toc.append("\n\n");
            }
            if (!excludeTags.isEmpty()) {
                toc.append("**Exclude tags:** ");
                for (String tag : excludeTags) {
                    toc.append("`").append(tag).append("` ");
                }
                toc.append("\n\n");
            }
        }

        boolean hasMatchingScenarios = false;

        for (Feature feature : features) {
            List<Scenario> filteredScenarios =
                feature.getFilteredScenarios(
                    includeTags, excludeTags);

            if (filteredScenarios.isEmpty()
                    || filteredScenarios.stream()
                        .allMatch(Scenario::isBackground)) {
                continue;
            }

            hasMatchingScenarios = true;

            String anchor = feature.getName().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-");
            toc.append("<h2 id=\"").append(anchor).append("\">");
            toc.append(feature.getName()).append("</h2>\n\n");

            toc.append("*File: ");
            toc.append(feature.getFilename()).append("*\n\n");

            if (!feature.getTags().isEmpty()) {
                toc.append("**Tags:** ");
                for (String tag : feature.getTags()) {
                    toc.append("`").append(tag).append("` ");
                }
                toc.append("\n\n");
            }

            if (feature.getDescription() != null
                    && !feature.getDescription().isEmpty()) {
                toc.append("> ");
                toc.append(feature.getDescription()
                    .replace("\n", "\n> "));
                toc.append("\n\n");
            }

            if (feature.hasMetadata("hasKarateSyntax")
                    && "true".equals(
                        feature.getMetadata("hasKarateSyntax"))) {
                toc.append("**Karate API Test File:**\n\n");
                toc.append("<details>\n");
                toc.append("<summary>API Test Details");
                toc.append("</summary>\n\n");

                if ("true".equals(
                        feature.getMetadata("hasApiCalls"))) {
                    toc.append("- Contains API calls\n");
                }
                if ("true".equals(
                        feature.getMetadata("hasJsonSchema"))) {
                    toc.append("- Contains JSON schema ");
                    toc.append("validation\n");
                }
                if ("true".equals(
                        feature.getMetadata("hasJsonMatching"))) {
                    toc.append("- Contains JSON matching\n");
                }
                if ("true".equals(feature.getMetadata(
                        "hasEmbeddedJavaScript"))) {
                    toc.append("- Contains embedded ");
                    toc.append("JavaScript\n");
                }
                if ("true".equals(
                        feature.getMetadata("hasApiOperations"))) {
                    toc.append("- Contains API operations ");
                    toc.append("(GET, POST, etc.)\n");
                }

                toc.append("</details>\n\n");
            }

            for (Scenario scenario : filteredScenarios) {
                if (scenario.isBackground()) {
                    continue;
                }

                String sAnchor = anchor + "-"
                    + scenario.getName().toLowerCase()
                        .replaceAll("[^a-z0-9]+", "-");

                String prefix = scenario.isOutline()
                    ? "### Scenario Outline: "
                    : "### Scenario: ";
                toc.append("<h3 id=\"").append(sAnchor);
                toc.append("\">");
                toc.append(prefix.substring(4));
                toc.append(scenario.getName());
                toc.append("</h3>\n\n");

                if (!scenario.getTags().isEmpty()) {
                    toc.append("**Tags:** ");
                    for (String tag : scenario.getTags()) {
                        toc.append("`").append(tag).append("` ");
                    }
                    toc.append("\n\n");
                }

                if (scenario.getDescription() != null
                        && !scenario.getDescription().isEmpty()) {
                    toc.append("> ");
                    toc.append(scenario.getDescription()
                        .replace("\n", "\n> "));
                    toc.append("\n\n");
                }

                if (scenario.isOutline()
                        && !scenario.getExamples().isEmpty()) {
                    int totalEx = scenario.getExamples().stream()
                        .mapToInt(ex -> ex.getRows().size())
                        .sum();

                    toc.append("**Examples:** ");
                    toc.append(totalEx);
                    toc.append(" total variations\n\n");

                    toc.append("<details>\n");
                    toc.append("<summary>Example Details");
                    toc.append("</summary>\n\n");

                    for (Scenario.Example example
                            : scenario.getExamples()) {
                        if (!example.getName().isEmpty()) {
                            toc.append("#### ");
                            toc.append(example.getName());
                            toc.append("\n\n");
                        } else {
                            toc.append("#### Examples\n\n");
                        }

                        if (!example.getHeaders().isEmpty()) {
                            toc.append("| ");
                            for (String header
                                    : example.getHeaders()) {
                                toc.append(header).append(" | ");
                            }
                            toc.append("\n");

                            toc.append("| ");
                            for (int i = 0;
                                    i < example.getHeaders()
                                        .size(); i++) {
                                toc.append("--- | ");
                            }
                            toc.append("\n");

                            int rowsToShow = Math.min(
                                example.getRows().size(), 3);
                            for (int i = 0;
                                    i < rowsToShow; i++) {
                                toc.append("| ");
                                for (String cell
                                        : example.getRows()
                                            .get(i)) {
                                    toc.append(cell);
                                    toc.append(" | ");
                                }
                                toc.append("\n");
                            }

                            if (example.getRows().size() > 3) {
                                toc.append("| ");
                                for (int i = 0;
                                        i < example.getHeaders()
                                            .size(); i++) {
                                    toc.append("... | ");
                                }
                                toc.append(" _(");
                                toc.append(
                                    example.getRows().size() - 3);
                                toc.append(" more rows)_\n");
                            }

                            toc.append("\n");
                        }
                    }

                    toc.append("</details>\n\n");
                }

                if (!scenario.getSteps().isEmpty()) {
                    toc.append("<details>\n");
                    toc.append("<summary>Steps</summary>\n\n");
                    toc.append("```gherkin\n");
                    for (String step : scenario.getSteps()) {
                        toc.append(step).append("\n");
                    }
                    toc.append("```\n");
                    toc.append("</details>\n\n");
                }
            }

            toc.append("[Back to Contents](#contents)\n\n");
            toc.append("---\n\n");
        }

        if (!hasMatchingScenarios) {
            toc.append("## No Matching Scenarios\n\n");
            toc.append("No scenarios match the specified ");
            toc.append("tag filters.\n");
        }

        return toc.toString();
    }

    /**
     * Generate a TOC in HTML format.
     */
    private String generateHtmlToc(List<Feature> features) {
        return generateHtmlToc(features,
            Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Generate a TOC in HTML format with tag filtering.
     */
    private String generateHtmlToc(List<Feature> features,
            List<String> includeTags, List<String> excludeTags) {
        List<Feature> filteredFeatures =
            getFilteredFeatures(features, includeTags, excludeTags);

        StringBuilder html = new StringBuilder();
        html.append(buildHtmlHeader());
        html.append(buildHtmlNavigation(filteredFeatures));
        html.append("  <div class=\"main-content\">\n");
        html.append(buildHtmlFilterMetadata(
            includeTags, excludeTags));

        long totalScenarios = 0;
        for (Feature feature : features) {
            totalScenarios += feature
                .getFilteredScenarios(includeTags, excludeTags)
                .stream().filter(s -> !s.isBackground()).count();
        }
        boolean needsPagination =
            totalScenarios > DEFAULT_SCENARIOS_PER_PAGE;

        html.append("    <h1>Feature Table of Contents</h1>\n");

        if (needsPagination) {
            html.append("    <div class=\"pagination\"");
            html.append(" id=\"pagination-top\">\n");
            html.append("      <!-- Pagination controls ");
            html.append("inserted by JavaScript -->\n");
            html.append("    </div>\n");
            html.append("    <div id=\"pages-container\">\n");
            html.append("      <div class=\"page active\"");
            html.append(" id=\"page-1\">\n");
        }

        int currentPage = 1;
        int scenariosOnCurrentPage = 0;
        boolean hasMatchingScenarios = false;

        for (Feature feature : features) {
            List<Scenario> filteredScenarios =
                feature.getFilteredScenarios(
                    includeTags, excludeTags);
            if (filteredScenarios.isEmpty()
                    || filteredScenarios.stream()
                        .allMatch(Scenario::isBackground)) {
                continue;
            }

            hasMatchingScenarios = true;

            if (needsPagination
                    && scenariosOnCurrentPage
                        >= DEFAULT_SCENARIOS_PER_PAGE) {
                html.append("      </div>\n");
                currentPage++;
                scenariosOnCurrentPage = 0;
                html.append("      <div class=\"page\"");
                html.append(" id=\"page-");
                html.append(currentPage).append("\">\n");
            }

            html.append(buildHtmlFeatureSection(
                feature, filteredScenarios,
                includeTags, excludeTags));

            for (Scenario scenario : filteredScenarios) {
                if (!scenario.isBackground()) {
                    scenariosOnCurrentPage++;
                }
            }
        }

        if (needsPagination) {
            html.append("      </div>\n");
            html.append("    </div>\n");
        }

        if (!hasMatchingScenarios) {
            html.append("    <div class=\"no-matches\">\n");
            html.append("      <h2>No Matching Scenarios</h2>\n");
            html.append("      <p>No scenarios match the ");
            html.append("specified tag filters.</p>\n");
            html.append("    </div>\n");
        }

        if (needsPagination) {
            html.append("    <div class=\"pagination\"");
            html.append(" id=\"pagination-bottom\">\n");
            html.append("      <!-- Pagination controls ");
            html.append("inserted by JavaScript -->\n");
            html.append("    </div>\n");
        }

        html.append(buildHtmlFooter(
            needsPagination, currentPage));
        return html.toString();
    }

    /**
     * Generate a TOC in JSON format.
     */
    private String generateJsonToc(List<Feature> features) {
        return generateJsonToc(features,
            Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Generate a TOC in JSON format with tag filtering.
     */
    private String generateJsonToc(List<Feature> features,
            List<String> includeTags, List<String> excludeTags) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"tableOfContents\": {\n");

        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            json.append("    \"filters\": {\n");

            json.append("      \"includeTags\": [");
            if (!includeTags.isEmpty()) {
                for (int t = 0; t < includeTags.size(); t++) {
                    if (t > 0) json.append(", ");
                    json.append("\"");
                    json.append(escapeJson(includeTags.get(t)));
                    json.append("\"");
                }
            }
            json.append("],\n");

            json.append("      \"excludeTags\": [");
            if (!excludeTags.isEmpty()) {
                for (int t = 0; t < excludeTags.size(); t++) {
                    if (t > 0) json.append(", ");
                    json.append("\"");
                    json.append(escapeJson(excludeTags.get(t)));
                    json.append("\"");
                }
            }
            json.append("]\n");

            json.append("    },\n");
        }

        json.append("    \"features\": [\n");

        List<Feature> filteredFeatures =
            getFilteredFeatures(features, includeTags, excludeTags);

        for (int i = 0; i < filteredFeatures.size(); i++) {
            Feature feature = filteredFeatures.get(i);
            json.append("      {\n");
            json.append("        \"name\": \"");
            json.append(escapeJson(feature.getName()));
            json.append("\",\n");
            json.append("        \"file\": \"");
            json.append(escapeJson(feature.getFilename()));
            json.append("\",\n");

            if (feature.getDescription() != null
                    && !feature.getDescription().isEmpty()) {
                json.append("        \"description\": \"");
                json.append(
                    escapeJson(feature.getDescription()));
                json.append("\",\n");
            }

            json.append("        \"tags\": [");
            if (!feature.getTags().isEmpty()) {
                for (int t = 0;
                        t < feature.getTags().size(); t++) {
                    if (t > 0) json.append(", ");
                    json.append("\"");
                    json.append(
                        escapeJson(feature.getTags().get(t)));
                    json.append("\"");
                }
            }
            json.append("],\n");

            if (!feature.getMetadata().isEmpty()) {
                json.append("        \"metadata\": {\n");
                int metaCount = 0;
                for (Map.Entry<String, String> entry
                        : feature.getMetadata().entrySet()) {
                    if (metaCount > 0) json.append(",\n");
                    json.append("          \"");
                    json.append(escapeJson(entry.getKey()));
                    json.append("\": \"");
                    json.append(escapeJson(entry.getValue()));
                    json.append("\"");
                    metaCount++;
                }
                json.append("\n        },\n");
            }

            json.append("        \"scenarios\": [\n");
            List<Scenario> scenarios = feature
                .getFilteredScenarios(includeTags, excludeTags)
                .stream()
                .filter(s -> !s.isBackground())
                .collect(Collectors.toList());

            for (int s = 0; s < scenarios.size(); s++) {
                Scenario scenario = scenarios.get(s);
                json.append("          {\n");
                json.append("            \"type\": \"");
                json.append(scenario.getType()).append("\",\n");
                json.append("            \"name\": \"");
                json.append(escapeJson(scenario.getName()));
                json.append("\",\n");
                json.append("            \"line\": ");
                json.append(scenario.getLineNumber());
                json.append(",\n");

                if (scenario.getDescription() != null
                        && !scenario.getDescription().isEmpty()) {
                    json.append("            \"description\": \"");
                    json.append(
                        escapeJson(scenario.getDescription()));
                    json.append("\",\n");
                }

                json.append("            \"tags\": [");
                if (!scenario.getTags().isEmpty()) {
                    for (int t = 0;
                            t < scenario.getTags().size(); t++) {
                        if (t > 0) json.append(", ");
                        json.append("\"");
                        json.append(escapeJson(
                            scenario.getTags().get(t)));
                        json.append("\"");
                    }
                }
                json.append("],\n");

                json.append("            \"steps\": [");
                if (!scenario.getSteps().isEmpty()) {
                    for (int t = 0;
                            t < scenario.getSteps().size(); t++) {
                        if (t > 0) json.append(", ");
                        json.append("\"");
                        json.append(escapeJson(
                            scenario.getSteps().get(t)));
                        json.append("\"");
                    }
                }
                json.append("],\n");

                if (scenario.isOutline()
                        && !scenario.getExamples().isEmpty()) {
                    json.append("            \"examples\": [\n");

                    for (int e = 0;
                            e < scenario.getExamples().size();
                            e++) {
                        Scenario.Example example =
                            scenario.getExamples().get(e);
                        json.append("              {\n");
                        json.append("                \"name\": \"");
                        json.append(
                            escapeJson(example.getName()));
                        json.append("\",\n");
                        json.append("                \"count\": ");
                        json.append(example.getRows().size());
                        json.append(",\n");

                        json.append(
                            "                \"headers\": [");
                        if (!example.getHeaders().isEmpty()) {
                            for (int h = 0;
                                    h < example.getHeaders()
                                        .size(); h++) {
                                if (h > 0) json.append(", ");
                                json.append("\"");
                                json.append(escapeJson(
                                    example.getHeaders().get(h)));
                                json.append("\"");
                            }
                        }
                        json.append("],\n");

                        json.append(
                            "                \"rows\": [\n");
                        for (int r = 0;
                                r < example.getRows().size();
                                r++) {
                            json.append("                  [");
                            List<String> row =
                                example.getRows().get(r);
                            for (int c = 0;
                                    c < row.size(); c++) {
                                if (c > 0) json.append(", ");
                                json.append("\"");
                                json.append(
                                    escapeJson(row.get(c)));
                                json.append("\"");
                            }
                            json.append("]");

                            if (r < example.getRows().size() - 1) {
                                json.append(",");
                            }
                            json.append("\n");
                        }
                        json.append("                ]\n");
                        json.append("              }");

                        if (e < scenario.getExamples()
                                .size() - 1) {
                            json.append(",");
                        }
                        json.append("\n");
                    }

                    json.append("            ]\n");
                } else {
                    json.append("            \"examples\": []\n");
                }

                json.append("          }");

                if (s < scenarios.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("        ]\n");
            json.append("      }");

            if (i < filteredFeatures.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("    ],\n");

        json.append("    \"summary\": {\n");
        json.append("      \"totalFeatures\": ");
        json.append(filteredFeatures.size()).append(",\n");

        int totalScenarios = 0;
        int totalOutlines = 0;
        int totalExamples = 0;

        for (Feature feature : filteredFeatures) {
            List<Scenario> scenarios = feature
                .getFilteredScenarios(includeTags, excludeTags)
                .stream()
                .filter(s -> !s.isBackground())
                .collect(Collectors.toList());

            totalScenarios += scenarios.size();

            for (Scenario scenario : scenarios) {
                if (scenario.isOutline()) {
                    totalOutlines++;
                    for (Scenario.Example example
                            : scenario.getExamples()) {
                        totalExamples += example.getRows().size();
                    }
                }
            }
        }

        json.append("      \"totalScenarios\": ");
        json.append(totalScenarios).append(",\n");
        json.append("      \"scenarioOutlines\": ");
        json.append(totalOutlines).append(",\n");
        json.append("      \"totalExamples\": ");
        json.append(totalExamples).append("\n");
        json.append("    }\n");

        json.append("  }\n");
        json.append("}");

        return json.toString();
    }

    /**
     * Generate a JUnit XML report for the table of contents.
     */
    private String generateJUnitXmlToc(List<Feature> features,
            List<String> includeTags, List<String> excludeTags) {
        JUnitFormatter formatter = new JUnitFormatter();
        return formatter.generateTocReport(
            features, includeTags, excludeTags);
    }

    /**
     * Get a filtered list of features that have matching scenarios.
     */
    private List<Feature> getFilteredFeatures(
            List<Feature> features, List<String> includeTags,
            List<String> excludeTags) {
        List<Feature> filteredFeatures = new ArrayList<>();

        for (Feature feature : features) {
            List<Scenario> filteredScenarios =
                feature.getFilteredScenarios(
                    includeTags, excludeTags);

            if (filteredScenarios.isEmpty()
                    || filteredScenarios.stream()
                        .allMatch(Scenario::isBackground)) {
                continue;
            }

            filteredFeatures.add(feature);
        }

        return filteredFeatures;
    }

    /**
     * Sanitize a string to be used as an HTML ID.
     */
    private String sanitizeForId(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /**
     * Escape special characters in JSON strings.
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ---- HTML generation helpers ----

    private String buildHtmlHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n");
        sb.append("  <title>Feature Table of Contents</title>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <meta name=\"viewport\" ");
        sb.append("content=\"width=device-width, ");
        sb.append("initial-scale=1.0\">\n");
        sb.append("  <style>\n");
        sb.append(buildHtmlStyles());
        sb.append("  </style>\n</head>\n<body>\n");
        return sb.toString();
    }

    private String buildHtmlStyles() {
        StringBuilder s = new StringBuilder();
        s.append("    body { font-family: Arial, sans-serif; ");
        s.append("margin: 20px; line-height: 1.6; }\n");
        s.append("    h1 { color: #333; ");
        s.append("margin-bottom: 1em; }\n");
        s.append("    h2 { color: #3c7a89; margin-top: 30px; ");
        s.append("border-bottom: 1px solid #ddd; ");
        s.append("padding-bottom: 5px; }\n");
        s.append("    h3 { color: #555; margin-left: 20px; ");
        s.append("margin-top: 20px; }\n");
        s.append("    .tag { background-color: #e8f4f8; ");
        s.append("padding: 2px 6px; border-radius: 4px; ");
        s.append("margin-right: 5px; font-size: 0.8em; }\n");
        s.append("    .tag.include ");
        s.append("{ background-color: #dff0d8; }\n");
        s.append("    .tag.exclude ");
        s.append("{ background-color: #f2dede; }\n");
        s.append("    .file { color: #777; ");
        s.append("font-style: italic; }\n");
        s.append("    .feature { margin-bottom: 40px; }\n");
        s.append("    .scenario { margin-left: 20px; ");
        s.append("margin-bottom: 20px; padding: 10px; ");
        s.append("border-left: 3px solid #e8e8e8; }\n");
        s.append("    .examples { margin-left: 40px; }\n");
        s.append("    .filters { background-color: #f5f5f5; ");
        s.append("padding: 10px; border-radius: 5px; ");
        s.append("margin-bottom: 20px; }\n");
        s.append("    .no-matches { color: #a94442; ");
        s.append("background-color: #f2dede; padding: 15px; ");
        s.append("border-radius: 4px; }\n");
        s.append("    .collapsible { cursor: pointer; ");
        s.append("padding: 10px; background-color: #f1f1f1; ");
        s.append("width: 100%; text-align: left; ");
        s.append("outline: none; }\n");
        s.append("    .active, .collapsible:hover ");
        s.append("{ background-color: #ccc; }\n");
        s.append("    .content { padding: 0 18px; ");
        s.append("max-height: 0; overflow: hidden; ");
        s.append("transition: max-height 0.2s ease-out; }\n");
        s.append("    .toc-nav { position: fixed; top: 10px; ");
        s.append("right: 10px; width: 250px; ");
        s.append("background: #f8f9fa; padding: 10px; ");
        s.append("border-radius: 5px; ");
        s.append("box-shadow: 0 0 10px rgba(0,0,0,0.1); ");
        s.append("max-height: 90vh; overflow-y: auto; }\n");
        s.append("    .toc-nav ul { padding-left: 20px; }\n");
        s.append("    .toc-nav li { margin-bottom: 5px; }\n");
        s.append("    .main-content { margin-right: 280px; }\n");
        s.append("    .steps { font-family: monospace; ");
        s.append("white-space: pre-wrap; ");
        s.append("background-color: #f8f8f8; padding: 10px; }\n");
        s.append("    .scenario-outline ");
        s.append("{ border-left-color: #3c7a89; }\n");
        s.append("    .pagination { display: flex; ");
        s.append("justify-content: center; margin: 20px 0; }\n");
        s.append("    .pagination button { margin: 0 5px; ");
        s.append("padding: 5px 10px; cursor: pointer; }\n");
        s.append("    .pagination .current { font-weight: bold; ");
        s.append("background-color: #3c7a89; color: white; }\n");
        s.append("    .page { display: none; }\n");
        s.append("    .page.active { display: block; }\n");
        s.append("    table { border-collapse: collapse; ");
        s.append("width: 100%; margin: 10px 0; }\n");
        s.append("    th, td { border: 1px solid #ddd; ");
        s.append("padding: 8px; text-align: left; }\n");
        s.append("    tr:nth-child(even) ");
        s.append("{ background-color: #f2f2f2; }\n");
        s.append("    th { background-color: #f1f1f1; }\n");
        return s.toString();
    }

    private String buildHtmlNavigation(
            List<Feature> filteredFeatures) {
        StringBuilder nav = new StringBuilder();
        nav.append("  <div class=\"toc-nav\">\n");
        nav.append("    <h3>Navigation</h3>\n");
        nav.append("    <ul>\n");
        for (Feature feature : filteredFeatures) {
            String fid = sanitizeForId(feature.getName());
            nav.append("      <li><a href=\"#").append(fid);
            nav.append("\">").append(feature.getName());
            nav.append("</a></li>\n");
        }
        nav.append("    </ul>\n");
        nav.append("  </div>\n");
        return nav.toString();
    }

    private String buildHtmlFilterMetadata(
            List<String> includeTags,
            List<String> excludeTags) {
        if (includeTags.isEmpty() && excludeTags.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    <div class=\"filters\">\n");
        sb.append("      <h2>Filters Applied</h2>\n");
        if (!includeTags.isEmpty()) {
            sb.append("      <p><strong>Include tags:");
            sb.append("</strong></p>\n      <p>\n");
            for (String tag : includeTags) {
                sb.append("        <span class=\"tag include\">");
                sb.append(tag).append("</span>\n");
            }
            sb.append("      </p>\n");
        }
        if (!excludeTags.isEmpty()) {
            sb.append("      <p><strong>Exclude tags:");
            sb.append("</strong></p>\n      <p>\n");
            for (String tag : excludeTags) {
                sb.append("        <span class=\"tag exclude\">");
                sb.append(tag).append("</span>\n");
            }
            sb.append("      </p>\n");
        }
        sb.append("    </div>\n");
        return sb.toString();
    }

    private String buildHtmlFeatureSection(Feature feature,
            List<Scenario> filteredScenarios,
            List<String> includeTags,
            List<String> excludeTags) {
        StringBuilder sb = new StringBuilder();
        String fid = sanitizeForId(feature.getName());

        sb.append("      <div class=\"feature\" id=\"");
        sb.append(fid).append("\">\n");
        sb.append("        <h2>");
        sb.append(feature.getName()).append("</h2>\n");
        sb.append("        <p class=\"file\">File: ");
        sb.append(feature.getFilename()).append("</p>\n");

        if (!feature.getTags().isEmpty()) {
            sb.append("        <p>\n");
            for (String tag : feature.getTags()) {
                String tc = htmlTagClass(
                    tag, includeTags, excludeTags);
                sb.append("          <span class=\"");
                sb.append(tc).append("\">").append(tag);
                sb.append("</span>\n");
            }
            sb.append("        </p>\n");
        }

        if (feature.getDescription() != null
                && !feature.getDescription().isEmpty()) {
            sb.append("        <button type=\"button\" ");
            sb.append("class=\"collapsible\">");
            sb.append("Description</button>\n");
            sb.append("        <div class=\"content\">\n");
            sb.append("          <p>");
            sb.append(feature.getDescription()
                .replace("\n", "<br/>"));
            sb.append("</p>\n");
            sb.append("        </div>\n");
        }

        if (feature.hasMetadata("hasKarateSyntax")
                && "true".equals(
                    feature.getMetadata("hasKarateSyntax"))) {
            sb.append(buildHtmlKarateMetadata(feature));
        }

        long count = filteredScenarios.stream()
            .filter(s -> !s.isBackground()).count();
        sb.append("        <button type=\"button\" ");
        sb.append("class=\"collapsible\">Scenarios (");
        sb.append(count).append(")</button>\n");
        sb.append("        <div class=\"content\" ");
        sb.append("style=\"max-height: none;\">\n");

        for (Scenario scenario : filteredScenarios) {
            if (!scenario.isBackground()) {
                sb.append(buildHtmlScenarioSection(
                    scenario, feature,
                    includeTags, excludeTags));
            }
        }

        sb.append("        </div>\n");
        sb.append("      </div>\n");
        return sb.toString();
    }

    private String buildHtmlScenarioSection(Scenario scenario,
            Feature feature, List<String> includeTags,
            List<String> excludeTags) {
        StringBuilder sb = new StringBuilder();
        String cls = scenario.isOutline()
            ? "scenario scenario-outline" : "scenario";
        String sid = sanitizeForId(
            feature.getName() + "-" + scenario.getName());

        sb.append("          <div class=\"").append(cls);
        sb.append("\" id=\"").append(sid).append("\">\n");
        String prefix = scenario.isOutline()
            ? "Scenario Outline: " : "Scenario: ";
        sb.append("            <h3>").append(prefix);
        sb.append(scenario.getName()).append("</h3>\n");

        if (!scenario.getTags().isEmpty()) {
            sb.append("            <p>\n");
            for (String tag : scenario.getTags()) {
                String tc = htmlTagClass(
                    tag, includeTags, excludeTags);
                sb.append("              <span class=\"");
                sb.append(tc).append("\">").append(tag);
                sb.append("</span>\n");
            }
            sb.append("            </p>\n");
        }

        if (scenario.getDescription() != null
                && !scenario.getDescription().isEmpty()) {
            sb.append("            ");
            sb.append("<div class=\"description\">\n");
            sb.append("              <p>");
            sb.append(scenario.getDescription()
                .replace("\n", "<br/>"));
            sb.append("</p>\n");
            sb.append("            </div>\n");
        }

        if (!scenario.getSteps().isEmpty()) {
            sb.append("            <button type=\"button\" ");
            sb.append("class=\"collapsible\">");
            sb.append("Steps</button>\n");
            sb.append("            <div class=\"content\">\n");
            sb.append("              ");
            sb.append("<div class=\"steps\">\n");
            for (String step : scenario.getSteps()) {
                sb.append(step).append("\n");
            }
            sb.append("              </div>\n");
            sb.append("            </div>\n");
        }

        if (scenario.isOutline()
                && !scenario.getExamples().isEmpty()) {
            sb.append(buildHtmlExamplesSection(scenario));
        }

        sb.append("          </div>\n");
        return sb.toString();
    }

    private String buildHtmlExamplesSection(Scenario scenario) {
        StringBuilder sb = new StringBuilder();
        int totalVariations = scenario.getExamples().stream()
            .mapToInt(e -> e.getRows().size()).sum();
        sb.append("            <div class=\"examples\">\n");
        sb.append("              <button type=\"button\" ");
        sb.append("class=\"collapsible\">Examples (");
        sb.append(totalVariations);
        sb.append(" variations)</button>\n");
        sb.append("              <div class=\"content\">\n");

        for (Scenario.Example example
                : scenario.getExamples()) {
            sb.append("                ");
            sb.append("<div class=\"example\">\n");
            if (!example.getName().isEmpty()) {
                sb.append("                  <h4>");
                sb.append(example.getName());
                sb.append("</h4>\n");
            }

            if (!example.getHeaders().isEmpty()
                    && !example.getRows().isEmpty()) {
                sb.append("                  <table>\n");
                sb.append("                    <tr>\n");
                for (String h : example.getHeaders()) {
                    sb.append("                      <th>");
                    sb.append(h).append("</th>\n");
                }
                sb.append("                    </tr>\n");

                int rowsToShow = Math.min(
                    example.getRows().size(), 10);
                for (int i = 0; i < rowsToShow; i++) {
                    sb.append("                    <tr>\n");
                    for (String cell
                            : example.getRows().get(i)) {
                        sb.append("                      <td>");
                        sb.append(cell).append("</td>\n");
                    }
                    sb.append("                    </tr>\n");
                }
                sb.append("                  </table>\n");

                if (example.getRows().size() > 10) {
                    int rem = example.getRows().size() - 10;
                    sb.append("                  ");
                    sb.append("<button type=\"button\" ");
                    sb.append("class=\"collapsible\">Show ");
                    sb.append(rem);
                    sb.append(" more rows</button>\n");
                    sb.append("                  ");
                    sb.append("<div class=\"content\">\n");
                    sb.append("                    <table>\n");
                    sb.append("                      <tr>\n");
                    for (String h : example.getHeaders()) {
                        sb.append("                        <th>");
                        sb.append(h).append("</th>\n");
                    }
                    sb.append("                      </tr>\n");
                    for (int i = 10;
                            i < example.getRows().size(); i++) {
                        sb.append(
                            "                      <tr>\n");
                        for (String cell
                                : example.getRows().get(i)) {
                            sb.append(
                                "                        <td>");
                            sb.append(cell);
                            sb.append("</td>\n");
                        }
                        sb.append(
                            "                      </tr>\n");
                    }
                    sb.append("                    </table>\n");
                    sb.append("                  </div>\n");
                }
            } else {
                sb.append("                  ");
                sb.append("<p>No example data available</p>\n");
            }
            sb.append("                </div>\n");
        }

        sb.append("              </div>\n");
        sb.append("            </div>\n");
        return sb.toString();
    }

    private String buildHtmlKarateMetadata(Feature feature) {
        StringBuilder sb = new StringBuilder();
        sb.append("        <button type=\"button\" ");
        sb.append("class=\"collapsible\">");
        sb.append("Karate API Test Details</button>\n");
        sb.append("        <div class=\"content\">\n");
        sb.append("          <ul>\n");
        if ("true".equals(
                feature.getMetadata("hasApiCalls"))) {
            sb.append("            ");
            sb.append("<li>Contains API calls</li>\n");
        }
        if ("true".equals(
                feature.getMetadata("hasJsonSchema"))) {
            sb.append("            <li>");
            sb.append("Contains JSON schema validation");
            sb.append("</li>\n");
        }
        if ("true".equals(
                feature.getMetadata("hasJsonMatching"))) {
            sb.append("            ");
            sb.append("<li>Contains JSON matching</li>\n");
        }
        if ("true".equals(
                feature.getMetadata("hasEmbeddedJavaScript"))) {
            sb.append("            <li>");
            sb.append("Contains embedded JavaScript");
            sb.append("</li>\n");
        }
        if ("true".equals(
                feature.getMetadata("hasApiOperations"))) {
            sb.append("            <li>");
            sb.append("Contains API operations ");
            sb.append("(GET, POST, etc.)</li>\n");
        }
        sb.append("          </ul>\n");
        sb.append("        </div>\n");
        return sb.toString();
    }

    private String buildHtmlFooter(
            boolean needsPagination, int totalPages) {
        StringBuilder sb = new StringBuilder();
        sb.append("  </div>\n");

        sb.append("<script>\n");
        sb.append("document.addEventListener(");
        sb.append("'DOMContentLoaded', function() {\n");
        sb.append("  var coll = document");
        sb.append(".getElementsByClassName('collapsible');\n");
        sb.append("  for (var i = 0; ");
        sb.append("i < coll.length; i++) {\n");
        sb.append("    coll[i].addEventListener(");
        sb.append("'click', function() {\n");
        sb.append("      this.classList.toggle('active');\n");
        sb.append("      var content = ");
        sb.append("this.nextElementSibling;\n");
        sb.append("      if (content.style.maxHeight ");
        sb.append("&& content.style.maxHeight !== 'none'");
        sb.append(") {\n");
        sb.append("        content.style.maxHeight = null;\n");
        sb.append("      } else {\n");
        sb.append("        content.style.maxHeight = ");
        sb.append("content.scrollHeight + 'px';\n");
        sb.append("      }\n");
        sb.append("    });\n");
        sb.append("  }\n");

        if (needsPagination) {
            sb.append("  var totalPages = ");
            sb.append(totalPages).append(";\n");
            sb.append("  var currentPageNum = 1;\n");

            sb.append("  function setupPagination() {\n");
            sb.append("    var paginationTop = document");
            sb.append(".getElementById('pagination-top');\n");
            sb.append("    var paginationBottom = document");
            sb.append(".getElementById(");
            sb.append("'pagination-bottom');\n");
            sb.append("    paginationTop.innerHTML = '';\n");
            sb.append("    paginationBottom");
            sb.append(".innerHTML = '';\n");

            sb.append("    var prevButton = document");
            sb.append(".createElement('button');\n");
            sb.append("    prevButton.innerHTML = ");
            sb.append("'&laquo; Previous';\n");
            sb.append("    prevButton.disabled = ");
            sb.append("currentPageNum === 1;\n");
            sb.append("    prevButton.addEventListener(");
            sb.append("'click', function() { ");
            sb.append("changePage(currentPageNum - 1); });\n");
            sb.append("    paginationTop.appendChild(");
            sb.append("prevButton.cloneNode(true));\n");
            sb.append("    paginationBottom");
            sb.append(".appendChild(prevButton);\n");

            sb.append("    for (var i = 1; ");
            sb.append("i <= totalPages; i++) {\n");
            sb.append("      var pageButton = document");
            sb.append(".createElement('button');\n");
            sb.append("      pageButton.textContent = i;\n");
            sb.append("      pageButton.classList.toggle(");
            sb.append("'current', i === currentPageNum);\n");
            sb.append("      pageButton.dataset.page = i;\n");
            sb.append("      pageButton.addEventListener(");
            sb.append("'click', function() { changePage(");
            sb.append("parseInt(this.dataset.page)); });\n");
            sb.append("      paginationTop.appendChild(");
            sb.append("pageButton.cloneNode(true));\n");
            sb.append("      paginationBottom");
            sb.append(".appendChild(pageButton);\n");
            sb.append("    }\n");

            sb.append("    var nextButton = document");
            sb.append(".createElement('button');\n");
            sb.append("    nextButton.innerHTML = ");
            sb.append("'Next &raquo;';\n");
            sb.append("    nextButton.disabled = ");
            sb.append("currentPageNum === totalPages;\n");
            sb.append("    nextButton.addEventListener(");
            sb.append("'click', function() { ");
            sb.append("changePage(currentPageNum + 1); });\n");
            sb.append("    paginationTop.appendChild(");
            sb.append("nextButton.cloneNode(true));\n");
            sb.append("    paginationBottom");
            sb.append(".appendChild(nextButton);\n");
            sb.append("  }\n");

            sb.append("  function changePage(pageNum) {\n");
            sb.append("    if (pageNum < 1 ");
            sb.append("|| pageNum > totalPages) return;\n");
            sb.append("    var pages = document");
            sb.append(".getElementsByClassName('page');\n");
            sb.append("    for (var i = 0; ");
            sb.append("i < pages.length; i++) {\n");
            sb.append("      pages[i].classList");
            sb.append(".remove('active');\n");
            sb.append("    }\n");
            sb.append("    document.getElementById(");
            sb.append("'page-' + pageNum)");
            sb.append(".classList.add('active');\n");
            sb.append("    currentPageNum = pageNum;\n");
            sb.append("    setupPagination();\n");
            sb.append("    window.scrollTo(0, 0);\n");
            sb.append("  }\n");

            sb.append("  setupPagination();\n");
        }

        sb.append("});\n");
        sb.append("</script>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        return sb.toString();
    }

    private String htmlTagClass(String tag,
            List<String> includeTags,
            List<String> excludeTags) {
        String tagClass = "tag";
        if (includeTags.contains(tag)) {
            tagClass += " include";
        } else if (excludeTags.contains(tag)) {
            tagClass += " exclude";
        }
        return tagClass;
    }
}
