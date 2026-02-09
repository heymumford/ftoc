package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the main body content of HTML TOC including features, scenarios, and examples.
 * Extracted from TocFormatter.generateHtmlToc() for improved modularity.
 * Supports pagination for large feature sets.
 */
public class HtmlBodyBuilder {

    private static final int DEFAULT_SCENARIOS_PER_PAGE = 20;

    /**
     * Build the main HTML body content for features and scenarios.
     *
     * @param features List of features to display
     * @param includeTags Tags to include (empty = all included)
     * @param excludeTags Tags that exclude scenarios
     * @return HTML body content with feature/scenario structure
     */
    public HtmlBodyContent build(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        StringBuilder body = new StringBuilder();
        StringBuilder contentBuilder = new StringBuilder();

        // Count total scenarios for pagination preparation
        int totalScenarios = 0;
        for (Feature feature : features) {
            List<Scenario> scenarios = feature.getFilteredScenarios(includeTags, excludeTags)
                .stream()
                .filter(s -> !s.isBackground())
                .collect(Collectors.toList());
            totalScenarios += scenarios.size();
        }

        // Determine if pagination is needed
        boolean needsPagination = totalScenarios > DEFAULT_SCENARIOS_PER_PAGE;

        // Build body structure
        contentBuilder.append("    <h1>Feature Table of Contents</h1>\n");

        // Setup pagination if needed
        if (needsPagination) {
            contentBuilder.append("    <div class=\"pagination\" id=\"pagination-top\">\n");
            contentBuilder.append("      <!-- Pagination controls will be inserted here by JavaScript -->\n");
            contentBuilder.append("    </div>\n");
        }

        // If pagination is needed, create a container for all pages
        if (needsPagination) {
            contentBuilder.append("    <div id=\"pages-container\">\n");
        }

        // Track current page and scenarios on current page
        int currentPage = 1;
        int scenariosOnCurrentPage = 0;

        // Start the first page
        if (needsPagination) {
            contentBuilder.append("      <div class=\"page active\" id=\"page-1\">\n");
        }

        // Track if any features have scenarios that match filters
        boolean hasMatchingScenarios = false;

        // Build features and scenarios
        for (Feature feature : features) {
            List<Scenario> filteredScenarios = feature.getFilteredScenarios(includeTags, excludeTags);

            // Skip features with no matching scenarios
            if (filteredScenarios.isEmpty() ||
                    filteredScenarios.stream().allMatch(Scenario::isBackground)) {
                continue;
            }

            hasMatchingScenarios = true;

            // If pagination is needed and we've reached the limit, start a new page
            if (needsPagination && scenariosOnCurrentPage >= DEFAULT_SCENARIOS_PER_PAGE) {
                contentBuilder.append("      </div>\n"); // Close current page
                currentPage++;
                scenariosOnCurrentPage = 0;
                contentBuilder.append("      <div class=\"page\" id=\"page-").append(currentPage).append("\">\n");
            }

            // Build feature section
            contentBuilder.append(buildFeatureSection(feature, filteredScenarios, includeTags, excludeTags));

            // Count scenarios for pagination
            for (Scenario scenario : filteredScenarios) {
                if (!scenario.isBackground()) {
                    scenariosOnCurrentPage++;
                }
            }
        }

        // Close the current page and pages container if pagination is enabled
        if (needsPagination) {
            contentBuilder.append("      </div>\n"); // Close current page
            contentBuilder.append("    </div>\n"); // Close pages container
        }

        // If no scenarios match the filters, add a message
        if (!hasMatchingScenarios) {
            contentBuilder.append("    <div class=\"no-matches\">\n");
            contentBuilder.append("      <h2>No Matching Scenarios</h2>\n");
            contentBuilder.append("      <p>No scenarios match the specified tag filters.</p>\n");
            contentBuilder.append("    </div>\n");
        }

        // Add bottom pagination controls if needed
        if (needsPagination) {
            contentBuilder.append("    <div class=\"pagination\" id=\"pagination-bottom\">\n");
            contentBuilder.append("      <!-- Pagination controls will be inserted here by JavaScript -->\n");
            contentBuilder.append("    </div>\n");
        }

        return new HtmlBodyContent(contentBuilder.toString(), needsPagination, currentPage);
    }

    /**
     * Build HTML for a single feature section with all its scenarios.
     *
     * @param feature The feature to build
     * @param filteredScenarios The filtered scenarios for this feature
     * @param includeTags Tags to include
     * @param excludeTags Tags to exclude
     * @return HTML for the feature section
     */
    private String buildFeatureSection(Feature feature, List<Scenario> filteredScenarios,
                                       List<String> includeTags, List<String> excludeTags) {
        StringBuilder section = new StringBuilder();
        String featureId = sanitizeForId(feature.getName());

        section.append("      <div class=\"feature\" id=\"").append(featureId).append("\">\n");
        section.append("        <h2>").append(feature.getName()).append("</h2>\n");
        section.append("        <p class=\"file\">File: ").append(feature.getFilename()).append("</p>\n");

        // Add feature tags
        if (!feature.getTags().isEmpty()) {
            section.append("        <p>\n");
            for (String tag : feature.getTags()) {
                String tagClass = buildTagClass(tag, includeTags, excludeTags);
                section.append("          <span class=\"").append(tagClass).append("\">").append(tag).append("</span>\n");
            }
            section.append("        </p>\n");
        }

        // Add feature description if it exists
        if (feature.getDescription() != null && !feature.getDescription().isEmpty()) {
            section.append("        <button type=\"button\" class=\"collapsible\">Description</button>\n");
            section.append("        <div class=\"content\">\n");
            section.append("          <p>").append(feature.getDescription().replace("\n", "<br/>")).append("</p>\n");
            section.append("        </div>\n");
        }

        // Add Karate-specific information if present
        if (feature.hasMetadata("hasKarateSyntax") && "true".equals(feature.getMetadata("hasKarateSyntax"))) {
            section.append(buildKarateMetadata(feature));
        }

        // Scenarios section
        section.append("        <button type=\"button\" class=\"collapsible\">Scenarios (")
           .append(filteredScenarios.stream().filter(s -> !s.isBackground()).count())
           .append(")</button>\n");
        section.append("        <div class=\"content\" style=\"max-height: none;\">\n");

        for (Scenario scenario : filteredScenarios) {
            if (!scenario.isBackground()) {
                section.append(buildScenarioSection(scenario, feature, includeTags, excludeTags));
            }
        }

        section.append("        </div>\n"); // Close scenarios content
        section.append("      </div>\n"); // Close feature div

        return section.toString();
    }

    /**
     * Build HTML for a single scenario.
     *
     * @param scenario The scenario to build
     * @param feature The parent feature
     * @param includeTags Tags to include
     * @param excludeTags Tags to exclude
     * @return HTML for the scenario
     */
    private String buildScenarioSection(Scenario scenario, Feature feature,
                                       List<String> includeTags, List<String> excludeTags) {
        StringBuilder section = new StringBuilder();
        String scenarioClass = scenario.isOutline() ? "scenario scenario-outline" : "scenario";
        String scenarioId = sanitizeForId(feature.getName() + "-" + scenario.getName());

        section.append("          <div class=\"").append(scenarioClass).append("\" id=\"").append(scenarioId).append("\">\n");
        String prefix = scenario.isOutline() ? "Scenario Outline: " : "Scenario: ";
        section.append("            <h3>").append(prefix).append(scenario.getName()).append("</h3>\n");

        // Scenario tags
        if (!scenario.getTags().isEmpty()) {
            section.append("            <p>\n");
            for (String tag : scenario.getTags()) {
                String tagClass = buildTagClass(tag, includeTags, excludeTags);
                section.append("              <span class=\"").append(tagClass).append("\">").append(tag).append("</span>\n");
            }
            section.append("            </p>\n");
        }

        // Scenario description
        if (scenario.getDescription() != null && !scenario.getDescription().isEmpty()) {
            section.append("            <div class=\"description\">\n");
            section.append("              <p>").append(scenario.getDescription().replace("\n", "<br/>")).append("</p>\n");
            section.append("            </div>\n");
        }

        // Scenario steps
        if (!scenario.getSteps().isEmpty()) {
            section.append("            <button type=\"button\" class=\"collapsible\">Steps</button>\n");
            section.append("            <div class=\"content\">\n");
            section.append("              <div class=\"steps\">\n");
            for (String step : scenario.getSteps()) {
                section.append(step).append("\n");
            }
            section.append("              </div>\n");
            section.append("            </div>\n");
        }

        // Scenario examples (for outlines)
        if (scenario.isOutline() && !scenario.getExamples().isEmpty()) {
            section.append(buildExamplesSection(scenario));
        }

        section.append("          </div>\n");
        return section.toString();
    }

    /**
     * Build HTML for scenario outline examples.
     *
     * @param scenario The scenario outline
     * @return HTML for the examples section
     */
    private String buildExamplesSection(Scenario scenario) {
        StringBuilder section = new StringBuilder();
        section.append("            <div class=\"examples\">\n");
        section.append("              <button type=\"button\" class=\"collapsible\">Examples (")
           .append(scenario.getExamples().stream().mapToInt(e -> e.getRows().size()).sum())
           .append(" variations)</button>\n");
        section.append("              <div class=\"content\">\n");

        for (Scenario.Example example : scenario.getExamples()) {
            section.append("                <div class=\"example\">\n");

            if (!example.getName().isEmpty()) {
                section.append("                  <h4>").append(example.getName()).append("</h4>\n");
            }

            // Display example data in a table
            if (!example.getHeaders().isEmpty() && !example.getRows().isEmpty()) {
                section.append("                  <table>\n");
                section.append("                    <tr>\n");
                for (String header : example.getHeaders()) {
                    section.append("                      <th>").append(header).append("</th>\n");
                }
                section.append("                    </tr>\n");

                // Show first 10 rows
                int rowsToShow = Math.min(example.getRows().size(), 10);
                for (int i = 0; i < rowsToShow; i++) {
                    section.append("                    <tr>\n");
                    for (String cell : example.getRows().get(i)) {
                        section.append("                      <td>").append(cell).append("</td>\n");
                    }
                    section.append("                    </tr>\n");
                }

                section.append("                  </table>\n");

                // Show more rows button if needed
                if (example.getRows().size() > 10) {
                    section.append("                  <button type=\"button\" class=\"collapsible\">Show ")
                       .append(example.getRows().size() - 10).append(" more rows</button>\n");
                    section.append("                  <div class=\"content\">\n");
                    section.append("                    <table>\n");
                    section.append("                      <tr>\n");
                    for (String header : example.getHeaders()) {
                        section.append("                        <th>").append(header).append("</th>\n");
                    }
                    section.append("                      </tr>\n");

                    for (int i = 10; i < example.getRows().size(); i++) {
                        section.append("                      <tr>\n");
                        for (String cell : example.getRows().get(i)) {
                            section.append("                        <td>").append(cell).append("</td>\n");
                        }
                        section.append("                      </tr>\n");
                    }

                    section.append("                    </table>\n");
                    section.append("                  </div>\n");
                }
            } else {
                section.append("                  <p>No example data available</p>\n");
            }

            section.append("                </div>\n");
        }

        section.append("              </div>\n");
        section.append("            </div>\n");
        return section.toString();
    }

    /**
     * Build Karate-specific metadata HTML section.
     *
     * @param feature The feature with Karate metadata
     * @return HTML for Karate metadata
     */
    private String buildKarateMetadata(Feature feature) {
        StringBuilder metadata = new StringBuilder();
        metadata.append("        <button type=\"button\" class=\"collapsible\">Karate API Test Details</button>\n");
        metadata.append("        <div class=\"content\">\n");
        metadata.append("          <ul>\n");

        if ("true".equals(feature.getMetadata("hasApiCalls"))) {
            metadata.append("            <li>Contains API calls</li>\n");
        }
        if ("true".equals(feature.getMetadata("hasJsonSchema"))) {
            metadata.append("            <li>Contains JSON schema validation</li>\n");
        }
        if ("true".equals(feature.getMetadata("hasJsonMatching"))) {
            metadata.append("            <li>Contains JSON matching</li>\n");
        }
        if ("true".equals(feature.getMetadata("hasEmbeddedJavaScript"))) {
            metadata.append("            <li>Contains embedded JavaScript</li>\n");
        }
        if ("true".equals(feature.getMetadata("hasApiOperations"))) {
            metadata.append("            <li>Contains API operations (GET, POST, etc.)</li>\n");
        }

        metadata.append("          </ul>\n");
        metadata.append("        </div>\n");
        return metadata.toString();
    }

    /**
     * Determine CSS class for a tag based on include/exclude lists.
     *
     * @param tag The tag to classify
     * @param includeTags Include list
     * @param excludeTags Exclude list
     * @return CSS class name for the tag
     */
    private String buildTagClass(String tag, List<String> includeTags, List<String> excludeTags) {
        String tagClass = "tag";
        if (includeTags.contains(tag)) {
            tagClass += " include";
        } else if (excludeTags.contains(tag)) {
            tagClass += " exclude";
        }
        return tagClass;
    }

    /**
     * Sanitize a string to be used as an HTML ID.
     *
     * @param input The input string
     * @return A sanitized ID-safe string
     */
    private String sanitizeForId(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /**
     * Data class holding body content and pagination info.
     */
    public static class HtmlBodyContent {
        public final String content;
        public final boolean needsPagination;
        public final int totalPages;

        public HtmlBodyContent(String content, boolean needsPagination, int totalPages) {
            this.content = content;
            this.needsPagination = needsPagination;
            this.totalPages = totalPages;
        }
    }
}
