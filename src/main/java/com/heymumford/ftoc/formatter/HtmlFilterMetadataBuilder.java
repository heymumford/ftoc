package com.heymumford.ftoc.formatter;

import java.util.List;

/**
 * Builds the filter metadata section that displays applied tag filters.
 * Extracted from TocFormatter.generateHtmlToc() for improved modularity.
 */
public class HtmlFilterMetadataBuilder {

    /**
     * Build the filter metadata HTML section if filters are applied.
     *
     * @param includeTags Tags scenarios must have (empty = all included)
     * @param excludeTags Tags that exclude scenarios
     * @return HTML for filter metadata, or empty string if no filters applied
     */
    public String build(List<String> includeTags, List<String> excludeTags) {
        if (includeTags.isEmpty() && excludeTags.isEmpty()) {
            return "";
        }

        StringBuilder metadata = new StringBuilder();
        metadata.append("    <div class=\"filters\">\n");
        metadata.append("      <h2>Filters Applied</h2>\n");

        if (!includeTags.isEmpty()) {
            metadata.append("      <p><strong>Include tags:</strong></p>\n");
            metadata.append("      <p>\n");
            for (String tag : includeTags) {
                metadata.append("        <span class=\"tag include\">").append(tag).append("</span>\n");
            }
            metadata.append("      </p>\n");
        }

        if (!excludeTags.isEmpty()) {
            metadata.append("      <p><strong>Exclude tags:</strong></p>\n");
            metadata.append("      <p>\n");
            for (String tag : excludeTags) {
                metadata.append("        <span class=\"tag exclude\">").append(tag).append("</span>\n");
            }
            metadata.append("      </p>\n");
        }

        metadata.append("    </div>\n");
        return metadata.toString();
    }
}
