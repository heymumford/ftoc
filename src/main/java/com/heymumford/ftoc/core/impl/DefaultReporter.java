package com.heymumford.ftoc.core.impl;

import com.heymumford.ftoc.analyzer.FeatureAntiPatternAnalyzer;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import com.heymumford.ftoc.config.WarningConfiguration;
import com.heymumford.ftoc.core.Reporter;
import com.heymumford.ftoc.formatter.AntiPatternFormatter;
import com.heymumford.ftoc.formatter.ConcordanceFormatter;
import com.heymumford.ftoc.formatter.TagQualityFormatter;
import com.heymumford.ftoc.formatter.TocFormatter;
import com.heymumford.ftoc.model.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Default implementation of Reporter interface.
 * This class is responsible for generating various reports from processed feature data.
 */
public class DefaultReporter implements Reporter {
    private static final Logger logger = LoggerFactory.getLogger(DefaultReporter.class);
    
    private final TocFormatter tocFormatter;
    private final ConcordanceFormatter concordanceFormatter;
    private final TagQualityFormatter tagQualityFormatter;
    private final AntiPatternFormatter antiPatternFormatter;
    private final WarningConfiguration warningConfig;
    private Consumer<String> outputHandler;
    
    /**
     * Create a new reporter with default formatters and warning configuration.
     */
    public DefaultReporter() {
        this(new WarningConfiguration());
    }
    
    /**
     * Create a new reporter with default formatters and custom warning configuration.
     * 
     * @param warningConfig The warning configuration to use
     */
    public DefaultReporter(WarningConfiguration warningConfig) {
        this.tocFormatter = new TocFormatter();
        this.concordanceFormatter = new ConcordanceFormatter();
        this.tagQualityFormatter = new TagQualityFormatter();
        this.antiPatternFormatter = new AntiPatternFormatter();
        this.warningConfig = warningConfig;
        this.outputHandler = System.out::println;
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
    @Override
    public String generateTableOfContents(List<Feature> features, Format format, 
                                          List<String> includeTags, List<String> excludeTags) {
        logger.info("Generating table of contents...");
        
        if (features.isEmpty()) {
            logger.warn("No features to include in table of contents.");
            return "No features to include in table of contents.";
        }
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("generate_toc");
        }
        
        // Convert the format enum
        TocFormatter.Format tocFormat = convertFormat(format);
        
        // Apply tag filters if they are set
        String toc;
        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            logger.info("Applying tag filters - include: {}, exclude: {}", 
                    includeTags, excludeTags);
            toc = tocFormatter.generateToc(features, tocFormat, includeTags, excludeTags);
        } else {
            toc = tocFormatter.generateToc(features, tocFormat);
        }
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("generate_toc");
        }
        
        logger.info("Table of contents generated successfully.");
        
        // Send to output handler
        outputHandler.accept("\n" + toc);
        
        return toc;
    }

    /**
     * Generate a tag concordance report from tag data.
     * 
     * @param tagConcordance Map of tags to occurrence counts
     * @param features List of features the tags were extracted from
     * @param format Output format
     * @return Formatted concordance report
     */
    @Override
    public String generateConcordanceReport(Map<String, Integer> tagConcordance, 
                                           List<Feature> features, Format format) {
        logger.info("Generating tag concordance report...");
        
        if (tagConcordance.isEmpty()) {
            logger.warn("No tags to include in concordance report.");
            return "No tags to include in concordance report.";
        }
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("generate_concordance");
        }
        
        // Convert the format enum
        ConcordanceFormatter.Format concordanceFormat = convertConcordanceFormat(format);
        
        // Generate detailed report using the formatter
        String report = concordanceFormatter.generateConcordanceReport(
                tagConcordance, features, concordanceFormat);
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("generate_concordance");
        }
        
        logger.info("Concordance report generated successfully.");
        
        // Send to output handler
        outputHandler.accept("\n" + report);
        
        return report;
    }

    /**
     * Generate a tag quality analysis report.
     * 
     * @param features List of features to analyze
     * @param tagConcordance Map of tags to occurrence counts
     * @param format Output format
     * @return Formatted tag quality report
     */
    @Override
    public String generateTagQualityReport(List<Feature> features, 
                                          Map<String, Integer> tagConcordance,
                                          Format format) {
        logger.info("Generating tag quality analysis report...");
        
        if (features.isEmpty()) {
            logger.warn("No features to analyze for tag quality.");
            return "No features to analyze for tag quality.";
        }
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("generate_tag_quality");
        }
        
        // Convert the format enum
        TagQualityFormatter.Format tagQualityFormat = convertTagQualityFormat(format);
        
        // Create a tag quality analyzer with the current data and warning configuration
        TagQualityAnalyzer analyzer = new TagQualityAnalyzer(tagConcordance, features, warningConfig);
        
        // Perform the analysis
        List<TagQualityAnalyzer.Warning> warnings = analyzer.analyzeTagQuality();
        
        // Generate a report using the formatter
        String report = tagQualityFormatter.generateTagQualityReport(warnings, tagQualityFormat);
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("generate_tag_quality");
        }
        
        logger.info("Tag quality analysis found {} potential issues.", warnings.size());
        
        // Send to output handler
        outputHandler.accept("\n" + report);
        
        return report;
    }

    /**
     * Generate an anti-pattern detection report.
     * 
     * @param features List of features to analyze
     * @param format Output format
     * @return Formatted anti-pattern report
     */
    @Override
    public String generateAntiPatternReport(List<Feature> features, Format format) {
        logger.info("Generating feature anti-pattern report...");
        
        if (features.isEmpty()) {
            logger.warn("No features to analyze for anti-patterns.");
            return "No features to analyze for anti-patterns.";
        }
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.startOperation("generate_anti_pattern");
        }
        
        // Convert the format enum
        AntiPatternFormatter.Format antiPatternFormat = convertAntiPatternFormat(format);
        
        // Create an anti-pattern analyzer with the current features and warning configuration
        FeatureAntiPatternAnalyzer analyzer = new FeatureAntiPatternAnalyzer(features, warningConfig);
        
        // Perform the analysis
        List<FeatureAntiPatternAnalyzer.Warning> warnings = analyzer.analyzeAntiPatterns();
        
        // Generate a report using the formatter
        String report = antiPatternFormatter.generateAntiPatternReport(warnings, antiPatternFormat);
        
        if (com.heymumford.ftoc.performance.PerformanceMonitor.isEnabled()) {
            com.heymumford.ftoc.performance.PerformanceMonitor.endOperation("generate_anti_pattern");
        }
        
        logger.info("Anti-pattern analysis found {} potential issues.", warnings.size());
        
        // Send to output handler
        outputHandler.accept("\n" + report);
        
        return report;
    }

    /**
     * Set the output handler for reports (e.g., console, file, etc.)
     * 
     * @param outputHandler Consumer that handles the output
     */
    @Override
    public void setOutputHandler(Consumer<String> outputHandler) {
        this.outputHandler = outputHandler;
    }
    
    /**
     * Convert Reporter.Format to TocFormatter.Format.
     */
    private TocFormatter.Format convertFormat(Format format) {
        switch (format) {
            case PLAIN_TEXT:
                return TocFormatter.Format.PLAIN_TEXT;
            case MARKDOWN:
                return TocFormatter.Format.MARKDOWN;
            case HTML:
                return TocFormatter.Format.HTML;
            case JSON:
                return TocFormatter.Format.JSON;
            case JUNIT_XML:
                return TocFormatter.Format.JUNIT_XML;
            default:
                return TocFormatter.Format.PLAIN_TEXT;
        }
    }
    
    /**
     * Convert Reporter.Format to ConcordanceFormatter.Format.
     */
    private ConcordanceFormatter.Format convertConcordanceFormat(Format format) {
        switch (format) {
            case PLAIN_TEXT:
                return ConcordanceFormatter.Format.PLAIN_TEXT;
            case MARKDOWN:
                return ConcordanceFormatter.Format.MARKDOWN;
            case HTML:
                return ConcordanceFormatter.Format.HTML;
            case JSON:
                return ConcordanceFormatter.Format.JSON;
            case JUNIT_XML:
                return ConcordanceFormatter.Format.JUNIT_XML;
            default:
                return ConcordanceFormatter.Format.PLAIN_TEXT;
        }
    }
    
    /**
     * Convert Reporter.Format to TagQualityFormatter.Format.
     */
    private TagQualityFormatter.Format convertTagQualityFormat(Format format) {
        switch (format) {
            case PLAIN_TEXT:
                return TagQualityFormatter.Format.PLAIN_TEXT;
            case MARKDOWN:
                return TagQualityFormatter.Format.MARKDOWN;
            case HTML:
                return TagQualityFormatter.Format.HTML;
            case JSON:
                return TagQualityFormatter.Format.JSON;
            case JUNIT_XML:
                return TagQualityFormatter.Format.JUNIT_XML;
            default:
                return TagQualityFormatter.Format.PLAIN_TEXT;
        }
    }
    
    /**
     * Convert Reporter.Format to AntiPatternFormatter.Format.
     */
    private AntiPatternFormatter.Format convertAntiPatternFormat(Format format) {
        switch (format) {
            case PLAIN_TEXT:
                return AntiPatternFormatter.Format.PLAIN_TEXT;
            case MARKDOWN:
                return AntiPatternFormatter.Format.MARKDOWN;
            case HTML:
                return AntiPatternFormatter.Format.HTML;
            case JSON:
                return AntiPatternFormatter.Format.JSON;
            case JUNIT_XML:
                return AntiPatternFormatter.Format.JUNIT_XML;
            default:
                return AntiPatternFormatter.Format.PLAIN_TEXT;
        }
    }
}