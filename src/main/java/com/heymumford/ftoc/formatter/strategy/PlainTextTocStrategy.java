package com.heymumford.ftoc.formatter.strategy;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.List;

/**
 * Strategy for generating Table of Contents in plain text format.
 *
 * This class extracts the plain text formatting logic from the monolithic TocFormatter,
 * following the Strategy pattern and Single Responsibility Principle.
 *
 * Responsibilities:
 * - Format features and scenarios as plain text
 * - Handle tag filtering display
 * - Format Karate-specific metadata
 * - Handle scenario outline examples
 *
 * This class is focused solely on plain text output, making it:
 * - Easy to understand (single format, ~120 lines)
 * - Easy to test (no dependencies on other formats)
 * - Easy to maintain (changes only affect plain text output)
 */
public class PlainTextTocStrategy implements TocFormatStrategy {

    @Override
    public String generateToc(TocContext context) {
        StringBuilder toc = new StringBuilder();
        toc.append("TABLE OF CONTENTS\n");
        toc.append("=================\n\n");

        // Add tag filter information if any filters are applied
        if (context.hasFilters()) {
            toc.append("FILTERS APPLIED:\n");
            if (!context.getIncludeTags().isEmpty()) {
                toc.append("  Include tags: ")
                   .append(String.join(", ", context.getIncludeTags()))
                   .append("\n");
            }
            if (!context.getExcludeTags().isEmpty()) {
                toc.append("  Exclude tags: ")
                   .append(String.join(", ", context.getExcludeTags()))
                   .append("\n");
            }
            toc.append("\n");
        }

        // Track if any features have scenarios that match the filters
        boolean hasMatchingScenarios = false;

        for (Feature feature : context.getFeatures()) {
            // Get scenarios filtered by tags
            List<Scenario> filteredScenarios = feature.getFilteredScenarios(
                    context.getIncludeTags(),
                    context.getExcludeTags());

            // Skip features with no matching scenarios
            if (filteredScenarios.isEmpty() ||
                filteredScenarios.stream().allMatch(Scenario::isBackground)) {
                continue;
            }

            hasMatchingScenarios = true;
            appendFeature(toc, feature, filteredScenarios);
        }

        // If no scenarios match the filters, add a message
        if (!hasMatchingScenarios) {
            toc.append("No scenarios match the specified tag filters.\n");
        }

        return toc.toString();
    }

    @Override
    public String getFormatName() {
        return "Plain Text";
    }

    /**
     * Append a feature and its scenarios to the TOC.
     */
    private void appendFeature(StringBuilder toc, Feature feature, List<Scenario> scenarios) {
        toc.append(feature.getName())
           .append(" (")
           .append(feature.getFilename())
           .append(")\n");

        if (!feature.getTags().isEmpty()) {
            toc.append("  Tags: ")
               .append(String.join(", ", feature.getTags()))
               .append("\n");
        }

        appendKarateMetadata(toc, feature);

        // Track scenario nesting level for outlines
        int scenarioDepth = 0;

        for (Scenario scenario : scenarios) {
            // Skip background scenarios in TOC
            if (scenario.isBackground()) {
                continue;
            }

            // Reset nesting depth for regular scenarios
            if (!scenario.isOutline()) {
                scenarioDepth = 0;
            }

            appendScenario(toc, scenario, scenarioDepth);

            // Handle scenario outline examples
            if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
                scenarioDepth++;
                appendExamples(toc, scenario, scenarioDepth);
            }
        }

        toc.append("\n");
    }

    /**
     * Append Karate-specific metadata if present.
     */
    private void appendKarateMetadata(StringBuilder toc, Feature feature) {
        if (feature.hasMetadata("hasKarateSyntax") &&
            "true".equals(feature.getMetadata("hasKarateSyntax"))) {

            toc.append("  [Karate-style Feature File]\n");

            if ("true".equals(feature.getMetadata("hasApiCalls"))) {
                toc.append("  - Contains API calls\n");
            }
            if ("true".equals(feature.getMetadata("hasJsonSchema"))) {
                toc.append("  - Contains JSON schema validation\n");
            }
            if ("true".equals(feature.getMetadata("hasJsonMatching"))) {
                toc.append("  - Contains JSON matching\n");
            }
            if ("true".equals(feature.getMetadata("hasEmbeddedJavaScript"))) {
                toc.append("  - Contains embedded JavaScript\n");
            }
            if ("true".equals(feature.getMetadata("hasApiOperations"))) {
                toc.append("  - Contains API operations (GET, POST, etc.)\n");
            }
        }
    }

    /**
     * Append a scenario to the TOC.
     */
    private void appendScenario(StringBuilder toc, Scenario scenario, int depth) {
        String indent = "  " + "  ".repeat(depth);
        String prefix = scenario.isOutline() ?
                indent + "Scenario Outline: " :
                indent + "Scenario: ";

        toc.append(prefix)
           .append(scenario.getName())
           .append("\n");

        if (!scenario.getTags().isEmpty()) {
            toc.append(indent)
               .append("  Tags: ")
               .append(String.join(", ", scenario.getTags()))
               .append("\n");
        }
    }

    /**
     * Append examples for scenario outlines.
     */
    private void appendExamples(StringBuilder toc, Scenario scenario, int depth) {
        int totalExamples = scenario.getExamples().stream()
                .mapToInt(ex -> ex.getRows().size())
                .sum();

        String exampleIndent = "  " + "  ".repeat(depth);
        toc.append(exampleIndent)
           .append("Examples: ")
           .append(totalExamples)
           .append(" total\n");

        // Add detail about individual example groups
        for (Scenario.Example example : scenario.getExamples()) {
            String exampleName = example.getName().isEmpty() ? "Unnamed" : example.getName();
            toc.append(exampleIndent)
               .append("  - ")
               .append(exampleName)
               .append(": ")
               .append(example.getRows().size())
               .append(" variations\n");
        }
    }
}
