package com.heymumford.ftoc.core;

import com.heymumford.ftoc.analyzer.FeatureAntiPatternAnalyzer;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import com.heymumford.ftoc.model.Feature;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reporter interface for generating reports from analyzed Feature objects.
 * This interface defines operations for generating different types of reports.
 */
public interface Reporter {

    /**
     * Output format options for reports.
     */
    enum Format {
        PLAIN_TEXT,
        MARKDOWN, 
        HTML,
        JSON,
        JUNIT_XML
    }
    
    /**
     * Generate a table of contents from a list of features.
     * 
     * @param features List of features to include in the TOC
     * @param format Output format
     * @param includeTags Tags to include (optional)
     * @param excludeTags Tags to exclude (optional)
     * @return Formatted table of contents
     */
    String generateTableOfContents(List<Feature> features, Format format, 
                                  List<String> includeTags, List<String> excludeTags);
    
    /**
     * Generate a tag concordance report from tag data.
     * 
     * @param tagConcordance Map of tags to occurrence counts
     * @param features List of features the tags were extracted from
     * @param format Output format
     * @return Formatted concordance report
     */
    String generateConcordanceReport(Map<String, Integer> tagConcordance, 
                                    List<Feature> features, Format format);
    
    /**
     * Generate a tag quality analysis report.
     * 
     * @param features List of features to analyze
     * @param tagConcordance Map of tags to occurrence counts
     * @param format Output format
     * @return Formatted tag quality report
     */
    String generateTagQualityReport(List<Feature> features, 
                                   Map<String, Integer> tagConcordance,
                                   Format format);
    
    /**
     * Generate an anti-pattern detection report.
     * 
     * @param features List of features to analyze
     * @param format Output format
     * @return Formatted anti-pattern report
     */
    String generateAntiPatternReport(List<Feature> features, Format format);
    
    /**
     * Set the output handler for reports (e.g., console, file, etc.)
     * 
     * @param outputHandler Consumer that handles the output
     */
    void setOutputHandler(Consumer<String> outputHandler);
}