package com.heymumford.ftoc.formatter;

/**
 * Builds the HTML header section including DOCTYPE, head metadata, and CSS styles.
 * Extracted from TocFormatter.generateHtmlToc() for improved modularity.
 */
public class HtmlHeaderBuilder {

    /**
     * Build the complete HTML header section.
     *
     * @return HTML header with DOCTYPE, <head>, styles, and opening <body> tag
     */
    public String build() {
        StringBuilder header = new StringBuilder();
        header.append("<!DOCTYPE html>\n");
        header.append("<html>\n");
        header.append("<head>\n");
        header.append("  <title>Feature Table of Contents</title>\n");
        header.append("  <meta charset=\"UTF-8\">\n");
        header.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        header.append("  <style>\n");
        header.append(buildStyles());
        header.append("  </style>\n");
        header.append("</head>\n");
        header.append("<body>\n");

        return header.toString();
    }

    /**
     * Build CSS styles for the HTML document.
     *
     * @return CSS style definitions
     */
    private String buildStyles() {
        StringBuilder styles = new StringBuilder();
        styles.append("    body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }\n");
        styles.append("    h1 { color: #333; margin-bottom: 1em; }\n");
        styles.append("    h2 { color: #3c7a89; margin-top: 30px; border-bottom: 1px solid #ddd; padding-bottom: 5px; }\n");
        styles.append("    h3 { color: #555; margin-left: 20px; margin-top: 20px; }\n");
        styles.append("    .tag { background-color: #e8f4f8; padding: 2px 6px; border-radius: 4px; margin-right: 5px; font-size: 0.8em; }\n");
        styles.append("    .tag.include { background-color: #dff0d8; }\n");
        styles.append("    .tag.exclude { background-color: #f2dede; }\n");
        styles.append("    .file { color: #777; font-style: italic; }\n");
        styles.append("    .feature { margin-bottom: 40px; }\n");
        styles.append("    .scenario { margin-left: 20px; margin-bottom: 20px; padding: 10px; border-left: 3px solid #e8e8e8; }\n");
        styles.append("    .examples { margin-left: 40px; }\n");
        styles.append("    .filters { background-color: #f5f5f5; padding: 10px; border-radius: 5px; margin-bottom: 20px; }\n");
        styles.append("    .no-matches { color: #a94442; background-color: #f2dede; padding: 15px; border-radius: 4px; }\n");
        styles.append("    .collapsible { cursor: pointer; padding: 10px; background-color: #f1f1f1; width: 100%; text-align: left; outline: none; }\n");
        styles.append("    .active, .collapsible:hover { background-color: #ccc; }\n");
        styles.append("    .content { padding: 0 18px; max-height: 0; overflow: hidden; transition: max-height 0.2s ease-out; }\n");
        styles.append("    .toc-nav { position: fixed; top: 10px; right: 10px; width: 250px; background: #f8f9fa; padding: 10px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); max-height: 90vh; overflow-y: auto; }\n");
        styles.append("    .toc-nav ul { padding-left: 20px; }\n");
        styles.append("    .toc-nav li { margin-bottom: 5px; }\n");
        styles.append("    .main-content { margin-right: 280px; }\n");
        styles.append("    .steps { font-family: monospace; white-space: pre-wrap; background-color: #f8f8f8; padding: 10px; }\n");
        styles.append("    .scenario-outline { border-left-color: #3c7a89; }\n");
        styles.append("    .pagination { display: flex; justify-content: center; margin: 20px 0; }\n");
        styles.append("    .pagination button { margin: 0 5px; padding: 5px 10px; cursor: pointer; }\n");
        styles.append("    .pagination .current { font-weight: bold; background-color: #3c7a89; color: white; }\n");
        styles.append("    .page { display: none; }\n");
        styles.append("    .page.active { display: block; }\n");
        styles.append("    table { border-collapse: collapse; width: 100%; margin: 10px 0; }\n");
        styles.append("    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        styles.append("    tr:nth-child(even) { background-color: #f2f2f2; }\n");
        styles.append("    th { background-color: #f1f1f1; }\n");
        return styles.toString();
    }
}
