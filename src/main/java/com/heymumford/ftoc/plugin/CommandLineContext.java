package com.heymumford.ftoc.plugin;

import com.heymumford.ftoc.core.Reporter;

import java.util.List;

/**
 * Context object for command line arguments.
 * This object contains the parsed command line arguments.
 */
public class CommandLineContext {
    private String directory;
    private boolean concordanceOnly;
    private Reporter.Format outputFormat;
    private List<String> includeTagFilters;
    private List<String> excludeTagFilters;
    private boolean analyzeTagQuality;
    private boolean detectAntiPatterns;
    private boolean enablePerformanceMonitoring;
    
    /**
     * Create a new command line context.
     * 
     * @param directory The directory to process
     * @param concordanceOnly Whether to generate only the concordance report
     * @param outputFormat The output format
     * @param includeTagFilters Tags to include
     * @param excludeTagFilters Tags to exclude
     * @param analyzeTagQuality Whether to analyze tag quality
     * @param detectAntiPatterns Whether to detect anti-patterns
     * @param enablePerformanceMonitoring Whether to enable performance monitoring
     */
    public CommandLineContext(
            String directory,
            boolean concordanceOnly,
            Reporter.Format outputFormat,
            List<String> includeTagFilters,
            List<String> excludeTagFilters,
            boolean analyzeTagQuality,
            boolean detectAntiPatterns,
            boolean enablePerformanceMonitoring) {
        this.directory = directory;
        this.concordanceOnly = concordanceOnly;
        this.outputFormat = outputFormat;
        this.includeTagFilters = includeTagFilters;
        this.excludeTagFilters = excludeTagFilters;
        this.analyzeTagQuality = analyzeTagQuality;
        this.detectAntiPatterns = detectAntiPatterns;
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
    }
    
    /**
     * Get the directory to process.
     * 
     * @return The directory
     */
    public String getDirectory() {
        return directory;
    }
    
    /**
     * Set the directory to process.
     * 
     * @param directory The directory
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
    /**
     * Check if only the concordance report should be generated.
     * 
     * @return true if concordance-only mode
     */
    public boolean isConcordanceOnly() {
        return concordanceOnly;
    }
    
    /**
     * Set whether only the concordance report should be generated.
     * 
     * @param concordanceOnly true if concordance-only mode
     */
    public void setConcordanceOnly(boolean concordanceOnly) {
        this.concordanceOnly = concordanceOnly;
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
     * Set the output format.
     * 
     * @param outputFormat Output format
     */
    public void setOutputFormat(Reporter.Format outputFormat) {
        this.outputFormat = outputFormat;
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
     * Set whether tag quality analysis is enabled.
     * 
     * @param analyzeTagQuality true to enable
     */
    public void setAnalyzeTagQuality(boolean analyzeTagQuality) {
        this.analyzeTagQuality = analyzeTagQuality;
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
     * Set whether anti-pattern detection is enabled.
     * 
     * @param detectAntiPatterns true to enable
     */
    public void setDetectAntiPatterns(boolean detectAntiPatterns) {
        this.detectAntiPatterns = detectAntiPatterns;
    }
    
    /**
     * Check if performance monitoring is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnablePerformanceMonitoring() {
        return enablePerformanceMonitoring;
    }
    
    /**
     * Set whether performance monitoring is enabled.
     * 
     * @param enablePerformanceMonitoring true to enable
     */
    public void setEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
    }
}