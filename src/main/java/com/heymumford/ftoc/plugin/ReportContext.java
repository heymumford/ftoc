package com.heymumford.ftoc.plugin;

import com.heymumford.ftoc.core.Reporter;
import com.heymumford.ftoc.model.Feature;

import java.util.List;
import java.util.Map;

/**
 * Context object for report generation events.
 * This object contains all the information needed for report generation.
 */
public class ReportContext {
    private final List<Feature> features;
    private final Map<String, Integer> tagConcordance;
    private final Reporter.Format outputFormat;
    private final List<String> includeTagFilters;
    private final List<String> excludeTagFilters;
    private final boolean analyzeTagQuality;
    private final boolean detectAntiPatterns;
    private final boolean concordanceOnly;
    
    /**
     * Create a new report context.
     * 
     * @param features List of features
     * @param tagConcordance Tag concordance map
     * @param outputFormat Output format
     * @param includeTagFilters Tags to include
     * @param excludeTagFilters Tags to exclude
     * @param analyzeTagQuality Whether to analyze tag quality
     * @param detectAntiPatterns Whether to detect anti-patterns
     * @param concordanceOnly Whether to generate only the concordance report
     */
    public ReportContext(
            List<Feature> features,
            Map<String, Integer> tagConcordance,
            Reporter.Format outputFormat,
            List<String> includeTagFilters,
            List<String> excludeTagFilters,
            boolean analyzeTagQuality,
            boolean detectAntiPatterns,
            boolean concordanceOnly) {
        this.features = features;
        this.tagConcordance = tagConcordance;
        this.outputFormat = outputFormat;
        this.includeTagFilters = includeTagFilters;
        this.excludeTagFilters = excludeTagFilters;
        this.analyzeTagQuality = analyzeTagQuality;
        this.detectAntiPatterns = detectAntiPatterns;
        this.concordanceOnly = concordanceOnly;
    }
    
    /**
     * Get the features to report on.
     * 
     * @return List of features
     */
    public List<Feature> getFeatures() {
        return features;
    }
    
    /**
     * Get the tag concordance map.
     * 
     * @return Tag concordance map
     */
    public Map<String, Integer> getTagConcordance() {
        return tagConcordance;
    }
    
    /**
     * Get the output format.
     * 
     * @return Output format
     */
    public Reporter.Format getOutputFormat() {
        return outputFormat;
    }
    
    /**
     * Get the include tag filters.
     * 
     * @return Include tag filters
     */
    public List<String> getIncludeTagFilters() {
        return includeTagFilters;
    }
    
    /**
     * Get the exclude tag filters.
     * 
     * @return Exclude tag filters
     */
    public List<String> getExcludeTagFilters() {
        return excludeTagFilters;
    }
    
    /**
     * Check if tag quality analysis is enabled.
     * 
     * @return true if enabled
     */
    public boolean isAnalyzeTagQuality() {
        return analyzeTagQuality;
    }
    
    /**
     * Check if anti-pattern detection is enabled.
     * 
     * @return true if enabled
     */
    public boolean isDetectAntiPatterns() {
        return detectAntiPatterns;
    }
    
    /**
     * Check if only the concordance report should be generated.
     * 
     * @return true if concordance-only mode
     */
    public boolean isConcordanceOnly() {
        return concordanceOnly;
    }
}