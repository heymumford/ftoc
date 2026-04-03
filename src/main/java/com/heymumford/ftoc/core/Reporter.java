package com.heymumford.ftoc.core;

import com.heymumford.ftoc.analyzer.FeatureAntiPatternAnalyzer;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import com.heymumford.ftoc.config.WarningConfiguration;
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
 * Reporter for generating various reports from processed feature data.
 */
public class Reporter {
    private static final Logger logger = LoggerFactory.getLogger(Reporter.class);

    /**
     * Output format options for reports.
     */
    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON,
        JUNIT_XML
    }

    private final TocFormatter tocFormatter;
    private final ConcordanceFormatter concordanceFormatter;
    private final TagQualityFormatter tagQualityFormatter;
    private final AntiPatternFormatter antiPatternFormatter;
    private final WarningConfiguration warningConfig;
    private Consumer<String> outputHandler;

    /**
     * Create a new reporter with default formatters and warning configuration.
     */
    public Reporter() {
        this(new WarningConfiguration());
    }

    /**
     * Create a new reporter with default formatters and custom warning configuration.
     *
     * @param warningConfig The warning configuration to use
     */
    public Reporter(WarningConfiguration warningConfig) {
        this.tocFormatter = new TocFormatter();
        this.concordanceFormatter = new ConcordanceFormatter();
        this.tagQualityFormatter = new TagQualityFormatter();
        this.antiPatternFormatter = new AntiPatternFormatter();
        this.warningConfig = warningConfig;
        this.outputHandler = System.out::println;
    }

    /**
     * Generate a table of contents from a list of features.
     */
    public String generateTableOfContents(List<Feature> features, Format format,
                                          List<String> includeTags, List<String> excludeTags) {
        logger.info("Generating table of contents...");

        if (features.isEmpty()) {
            logger.warn("No features to include in table of contents.");
            return "No features to include in table of contents.";
        }

        TocFormatter.Format tocFormat = convertFormat(format);

        String toc;
        if (!includeTags.isEmpty() || !excludeTags.isEmpty()) {
            logger.info("Applying tag filters - include: {}, exclude: {}",
                    includeTags, excludeTags);
            toc = tocFormatter.generateToc(features, tocFormat, includeTags, excludeTags);
        } else {
            toc = tocFormatter.generateToc(features, tocFormat);
        }

        logger.info("Table of contents generated successfully.");
        outputHandler.accept("\n" + toc);
        return toc;
    }

    /**
     * Generate a tag concordance report from tag data.
     */
    public String generateConcordanceReport(Map<String, Integer> tagConcordance,
                                           List<Feature> features, Format format) {
        logger.info("Generating tag concordance report...");

        if (tagConcordance.isEmpty()) {
            logger.warn("No tags to include in concordance report.");
            return "No tags to include in concordance report.";
        }

        ConcordanceFormatter.Format concordanceFormat = convertConcordanceFormat(format);
        String report = concordanceFormatter.generateConcordanceReport(
                tagConcordance, features, concordanceFormat);

        logger.info("Concordance report generated successfully.");
        outputHandler.accept("\n" + report);
        return report;
    }

    /**
     * Generate a tag quality analysis report.
     */
    public String generateTagQualityReport(List<Feature> features,
                                          Map<String, Integer> tagConcordance,
                                          Format format) {
        logger.info("Generating tag quality analysis report...");

        if (features.isEmpty()) {
            logger.warn("No features to analyze for tag quality.");
            return "No features to analyze for tag quality.";
        }

        TagQualityFormatter.Format tagQualityFormat = convertTagQualityFormat(format);
        TagQualityAnalyzer analyzer = new TagQualityAnalyzer(tagConcordance, features, warningConfig);
        List<TagQualityAnalyzer.Warning> warnings = analyzer.analyzeTagQuality();
        String report = tagQualityFormatter.generateTagQualityReport(warnings, tagQualityFormat);

        logger.info("Tag quality analysis found {} potential issues.", warnings.size());
        outputHandler.accept("\n" + report);
        return report;
    }

    /**
     * Generate an anti-pattern detection report.
     */
    public String generateAntiPatternReport(List<Feature> features, Format format) {
        logger.info("Generating feature anti-pattern report...");

        if (features.isEmpty()) {
            logger.warn("No features to analyze for anti-patterns.");
            return "No features to analyze for anti-patterns.";
        }

        AntiPatternFormatter.Format antiPatternFormat = convertAntiPatternFormat(format);
        FeatureAntiPatternAnalyzer analyzer = new FeatureAntiPatternAnalyzer(features, warningConfig);
        List<FeatureAntiPatternAnalyzer.Warning> warnings = analyzer.analyzeAntiPatterns();
        String report = antiPatternFormatter.generateAntiPatternReport(warnings, antiPatternFormat);

        logger.info("Anti-pattern analysis found {} potential issues.", warnings.size());
        outputHandler.accept("\n" + report);
        return report;
    }

    /**
     * Set the output handler for reports (e.g., console, file, etc.)
     */
    public void setOutputHandler(Consumer<String> outputHandler) {
        this.outputHandler = outputHandler;
    }

    private TocFormatter.Format convertFormat(Format format) {
        switch (format) {
            case PLAIN_TEXT: return TocFormatter.Format.PLAIN_TEXT;
            case MARKDOWN: return TocFormatter.Format.MARKDOWN;
            case HTML: return TocFormatter.Format.HTML;
            case JSON: return TocFormatter.Format.JSON;
            case JUNIT_XML: return TocFormatter.Format.JUNIT_XML;
            default: return TocFormatter.Format.PLAIN_TEXT;
        }
    }

    private ConcordanceFormatter.Format convertConcordanceFormat(Format format) {
        switch (format) {
            case PLAIN_TEXT: return ConcordanceFormatter.Format.PLAIN_TEXT;
            case MARKDOWN: return ConcordanceFormatter.Format.MARKDOWN;
            case HTML: return ConcordanceFormatter.Format.HTML;
            case JSON: return ConcordanceFormatter.Format.JSON;
            case JUNIT_XML: return ConcordanceFormatter.Format.JUNIT_XML;
            default: return ConcordanceFormatter.Format.PLAIN_TEXT;
        }
    }

    private TagQualityFormatter.Format convertTagQualityFormat(Format format) {
        switch (format) {
            case PLAIN_TEXT: return TagQualityFormatter.Format.PLAIN_TEXT;
            case MARKDOWN: return TagQualityFormatter.Format.MARKDOWN;
            case HTML: return TagQualityFormatter.Format.HTML;
            case JSON: return TagQualityFormatter.Format.JSON;
            case JUNIT_XML: return TagQualityFormatter.Format.JUNIT_XML;
            default: return TagQualityFormatter.Format.PLAIN_TEXT;
        }
    }

    private AntiPatternFormatter.Format convertAntiPatternFormat(Format format) {
        switch (format) {
            case PLAIN_TEXT: return AntiPatternFormatter.Format.PLAIN_TEXT;
            case MARKDOWN: return AntiPatternFormatter.Format.MARKDOWN;
            case HTML: return AntiPatternFormatter.Format.HTML;
            case JSON: return AntiPatternFormatter.Format.JSON;
            case JUNIT_XML: return AntiPatternFormatter.Format.JUNIT_XML;
            default: return AntiPatternFormatter.Format.PLAIN_TEXT;
        }
    }
}
