package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import java.util.List;

/**
 * Builds the fixed-position navigation sidebar for HTML TOC output.
 * Extracted from TocFormatter.generateHtmlToc() for improved modularity.
 */
public class HtmlNavigationBuilder {

    /**
     * Build the navigation sidebar with links to all features.
     *
     * @param filteredFeatures List of features to include in navigation
     * @return HTML for navigation sidebar
     */
    public String build(List<Feature> filteredFeatures) {
        StringBuilder nav = new StringBuilder();
        nav.append("  <div class=\"toc-nav\">\n");
        nav.append("    <h3>Navigation</h3>\n");
        nav.append("    <ul>\n");

        // Add navigation links for each feature
        for (Feature feature : filteredFeatures) {
            String featureId = sanitizeForId(feature.getName());
            nav.append("      <li><a href=\"#").append(featureId).append("\">")
               .append(feature.getName()).append("</a></li>\n");
        }

        nav.append("    </ul>\n");
        nav.append("  </div>\n");

        return nav.toString();
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
}
