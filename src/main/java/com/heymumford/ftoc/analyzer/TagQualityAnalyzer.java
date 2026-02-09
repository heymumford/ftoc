package com.heymumford.ftoc.analyzer;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer for detecting and reporting tag quality issues in feature files.
 * This class identifies different types of tag problems and generates warnings
 * that can help teams improve their BDD tagging practices.
 */
public class TagQualityAnalyzer {

    // Default patterns for tag categorization (can be overridden by configuration)
    private static final List<String> DEFAULT_PRIORITY_TAGS = Arrays.asList(
            "@p0", "@p1", "@p2", "@p3", "@p4",
            "@critical", "@high", "@medium", "@low",
            "@priority0", "@priority1", "@priority2", "@priority3");

    private static final List<String> DEFAULT_TYPE_TAGS = Arrays.asList(
            "@ui", "@api", "@backend", "@frontend", "@integration", "@unit",
            "@performance", "@security", "@regression", "@smoke", "@e2e",
            "@functional", "@acceptance", "@system", "@component");

    private static final List<String> DEFAULT_STATUS_TAGS = Arrays.asList(
            "@wip", "@ready", "@review", "@flaky", "@deprecated", "@legacy",
            "@todo", "@debug", "@inprogress", "@completed", "@blocked");

    private static final List<String> DEFAULT_LOW_VALUE_TAGS = Arrays.asList(
            "@test", "@tests", "@feature", "@cucumber", "@scenario", "@gherkin",
            "@temp", "@temporary", "@pending", "@fixme", "@workaround",
            "@ignore", "@skip", "@manual");

    // Actual lists that will be used (may be overridden by configuration)
    private List<String> PRIORITY_TAGS = new ArrayList<>(DEFAULT_PRIORITY_TAGS);
    private List<String> TYPE_TAGS = new ArrayList<>(DEFAULT_TYPE_TAGS);
    private List<String> STATUS_TAGS = new ArrayList<>(DEFAULT_STATUS_TAGS);
    private List<String> KNOWN_LOW_VALUE_TAGS = new ArrayList<>(DEFAULT_LOW_VALUE_TAGS);

    private Map<String, Integer> tagConcordance;
    private List<Feature> features;
    private com.heymumford.ftoc.config.WarningConfiguration config;

    // Focused analyzer dependencies
    private TagTypoAnalyzer typoAnalyzer;
    private LowValueTagAnalyzer lowValueAnalyzer;
    private TagFrequencyAnalyzer frequencyAnalyzer;
    
    /**
     * Types of tag warnings that can be detected.
     */
    public enum WarningType {
        MISSING_PRIORITY_TAG("Missing priority tag"),
        MISSING_TYPE_TAG("Missing type tag"),
        LOW_VALUE_TAG("Low-value tag"),
        INCONSISTENT_TAGGING("Inconsistent tagging"),
        EXCESSIVE_TAGS("Excessive tags"),
        TAG_TYPO("Possible tag typo"),
        ORPHANED_TAG("Orphaned tag (used only once)"),
        AMBIGUOUS_TAG("Ambiguous tag"),
        TOO_GENERIC_TAG("Too generic tag"),
        INCONSISTENT_NESTING("Inconsistent tag nesting"),
        DUPLICATE_TAG("Duplicate tag");
        
        private final String description;
        
        WarningType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Warning message for a tag quality issue.
     */
    public static class Warning {
        private final WarningType type;
        private final String message;
        private final String location;
        private final List<String> remediation;
        private final com.heymumford.ftoc.config.WarningConfiguration.Severity severity;
        private final List<String> standardAlternatives;
        
        public Warning(WarningType type, String message, String location, List<String> remediation) {
            this(type, message, location, remediation, com.heymumford.ftoc.config.WarningConfiguration.Severity.WARNING, null);
        }
        
        public Warning(WarningType type, String message, String location, List<String> remediation, 
                      com.heymumford.ftoc.config.WarningConfiguration.Severity severity,
                      List<String> standardAlternatives) {
            this.type = type;
            this.message = message;
            this.location = location;
            this.remediation = remediation;
            this.severity = severity;
            this.standardAlternatives = standardAlternatives != null ? 
                                        new ArrayList<>(standardAlternatives) : 
                                        Collections.emptyList();
        }
        
        public WarningType getType() {
            return type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getLocation() {
            return location;
        }
        
        public List<String> getRemediation() {
            return remediation;
        }
        
        public com.heymumford.ftoc.config.WarningConfiguration.Severity getSeverity() {
            return severity;
        }
        
        public String getSeverityDisplayName() {
            return severity.getDisplayName();
        }
        
        public List<String> getStandardAlternatives() {
            return standardAlternatives;
        }
        
        public boolean hasStandardAlternatives() {
            return standardAlternatives != null && !standardAlternatives.isEmpty();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(severity.getDisplayName()).append(": ")
              .append(type.getDescription()).append(": ").append(message);
            if (location != null && !location.isEmpty()) {
                sb.append(" (in ").append(location).append(")");
            }
            return sb.toString();
        }
    }
    
    /**
     * Create a new tag quality analyzer with default configuration.
     * 
     * @param tagConcordance Map of tags to their occurrences
     * @param features List of features to analyze
     */
    public TagQualityAnalyzer(Map<String, Integer> tagConcordance, List<Feature> features) {
        this(tagConcordance, features, new com.heymumford.ftoc.config.WarningConfiguration());
    }
    
    /**
     * Create a new tag quality analyzer with specific configuration.
     *
     * @param tagConcordance Map of tags to their occurrences
     * @param features List of features to analyze
     * @param config The warning configuration to use
     */
    public TagQualityAnalyzer(Map<String, Integer> tagConcordance, List<Feature> features,
                              com.heymumford.ftoc.config.WarningConfiguration config) {
        this.tagConcordance = new HashMap<>(tagConcordance);
        this.features = new ArrayList<>(features);
        this.config = config;

        // Apply custom tag lists from configuration if available
        List<String> customPriorityTags = config.getCustomTags("priority");
        if (!customPriorityTags.isEmpty()) {
            this.PRIORITY_TAGS = new ArrayList<>(customPriorityTags);
        }

        List<String> customTypeTags = config.getCustomTags("type");
        if (!customTypeTags.isEmpty()) {
            this.TYPE_TAGS = new ArrayList<>(customTypeTags);
        }

        List<String> customStatusTags = config.getCustomTags("status");
        if (!customStatusTags.isEmpty()) {
            this.STATUS_TAGS = new ArrayList<>(customStatusTags);
        }

        List<String> customLowValueTags = config.getCustomTags("lowValue");
        if (!customLowValueTags.isEmpty()) {
            this.KNOWN_LOW_VALUE_TAGS = new ArrayList<>(customLowValueTags);
        }

        // Initialize focused analyzers
        initializeAnalyzers();
    }

    /**
     * Initialize focused analyzer instances.
     */
    private void initializeAnalyzers() {
        this.typoAnalyzer = new TagTypoAnalyzer(tagConcordance, features, config);
        this.lowValueAnalyzer = new LowValueTagAnalyzer(tagConcordance, config, KNOWN_LOW_VALUE_TAGS, PRIORITY_TAGS);
        this.frequencyAnalyzer = new TagFrequencyAnalyzer(tagConcordance, features, config, PRIORITY_TAGS);
    }
    
    /**
     * Perform a comprehensive tag quality analysis and generate warnings.
     * 
     * @return List of all warnings found during analysis
     */
    public List<Warning> analyzeTagQuality() {
        List<Warning> allWarnings = new ArrayList<>();
        
        // Run all analysis methods only if they're enabled in the configuration
        if (config.isWarningEnabled(WarningType.MISSING_PRIORITY_TAG.name())) {
            allWarnings.addAll(detectMissingPriorityTags());
        }
        
        if (config.isWarningEnabled(WarningType.MISSING_TYPE_TAG.name())) {
            allWarnings.addAll(detectMissingTypeTags());
        }
        
        if (config.isWarningEnabled(WarningType.LOW_VALUE_TAG.name()) || 
            config.isWarningEnabled(WarningType.TOO_GENERIC_TAG.name()) ||
            config.isWarningEnabled(WarningType.AMBIGUOUS_TAG.name())) {
            allWarnings.addAll(detectLowValueTags());
        }
        
        if (config.isWarningEnabled(WarningType.ORPHANED_TAG.name()) ||
            config.isWarningEnabled(WarningType.TAG_TYPO.name())) {
            allWarnings.addAll(detectOrphanedTags());
        }
        
        if (config.isWarningEnabled(WarningType.EXCESSIVE_TAGS.name())) {
            allWarnings.addAll(detectExcessiveTags());
        }
        
        if (config.isWarningEnabled(WarningType.INCONSISTENT_TAGGING.name())) {
            allWarnings.addAll(detectInconsistentTagging());
        }
        
        if (config.isWarningEnabled(WarningType.TAG_TYPO.name())) {
            allWarnings.addAll(detectPossibleTagTypos());
        }
        
        if (config.isWarningEnabled(WarningType.DUPLICATE_TAG.name())) {
            allWarnings.addAll(detectDuplicateTags());
        }
        
        // Filter out any disabled warnings
        allWarnings = allWarnings.stream()
                .filter(warning -> config.isWarningEnabled(warning.getType().name()))
                .collect(Collectors.toList());
        
        return allWarnings;
    }
    
    /**
     * Detect scenarios that are missing priority tags (e.g., @P0, @Critical).
     */
    private List<Warning> detectMissingPriorityTags() {
        List<Warning> warnings = new ArrayList<>();
        
        // Get configuration for warning type
        com.heymumford.ftoc.config.WarningConfiguration.WarningConfig warningConfig = 
                config.getTagQualityWarnings().get(WarningType.MISSING_PRIORITY_TAG.name());
        
        if (!warningConfig.isEnabled()) {
            return warnings;
        }
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                // Skip backgrounds
                if (scenario.isBackground()) {
                    continue;
                }
                
                // Check if the scenario has any priority tags
                boolean hasPriorityTag = scenario.getTags().stream()
                        .anyMatch(tag -> PRIORITY_TAGS.contains(tag.toLowerCase()));
                
                // Check if any feature-level priority tags exist
                boolean featureHasPriorityTag = feature.getTags().stream()
                        .anyMatch(tag -> PRIORITY_TAGS.contains(tag.toLowerCase()));
                
                if (!hasPriorityTag && !featureHasPriorityTag) {
                    String location = feature.getFilename() + " - " + scenario.getName();
                    List<String> remediation = new ArrayList<>();
                    remediation.add("Add a priority tag like @P0 (highest), @P1, @P2, or @P3 (lowest)");
                    remediation.add("Alternatively, add a semantic priority tag like @Critical, @High, @Medium, or @Low");
                    remediation.add("Apply priority tags consistently across all scenarios");
                    
                    // Add standard alternatives if they exist
                    if (!warningConfig.getStandardAlternatives().isEmpty()) {
                        remediation.add("Use one of the standard priority tags: " + 
                                        String.join(", ", warningConfig.getStandardAlternatives()));
                    }
                    
                    warnings.add(new Warning(
                            WarningType.MISSING_PRIORITY_TAG,
                            "Scenario is missing a priority tag",
                            location,
                            remediation,
                            warningConfig.getSeverity(),
                            warningConfig.getStandardAlternatives()
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect scenarios that are missing type tags (e.g., @UI, @API).
     */
    private List<Warning> detectMissingTypeTags() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                // Skip backgrounds
                if (scenario.isBackground()) {
                    continue;
                }
                
                // Check if the scenario has any type tags
                boolean hasTypeTag = scenario.getTags().stream()
                        .anyMatch(tag -> TYPE_TAGS.contains(tag.toLowerCase()));
                
                // Check if any feature-level type tags exist
                boolean featureHasTypeTag = feature.getTags().stream()
                        .anyMatch(tag -> TYPE_TAGS.contains(tag.toLowerCase()));
                
                if (!hasTypeTag && !featureHasTypeTag) {
                    String location = feature.getFilename() + " - " + scenario.getName();
                    List<String> remediation = Arrays.asList(
                            "Add a type tag that describes the test type (e.g., @UI, @API, @Integration)",
                            "Type tags help with test selection and organization",
                            "Consider what layer of the application this test is targeting"
                    );
                    
                    warnings.add(new Warning(
                            WarningType.MISSING_TYPE_TAG,
                            "Scenario is missing a type tag",
                            location,
                            remediation
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect tags that are likely to be low-value or problematic.
     * Delegates to LowValueTagAnalyzer.
     */
    private List<Warning> detectLowValueTags() {
        return lowValueAnalyzer.detectLowValueTags();
    }
    
    /**
     * Detect tags that are only used once (might be typos or orphaned).
     * Delegates to TagFrequencyAnalyzer.
     */
    private List<Warning> detectOrphanedTags() {
        return frequencyAnalyzer.detectOrphanedTags();
    }
    
    /**
     * Detect scenarios with an excessive number of tags.
     * Delegates to TagFrequencyAnalyzer.
     */
    private List<Warning> detectExcessiveTags() {
        return frequencyAnalyzer.detectExcessiveTags();
    }
    
    /**
     * Detect inconsistent tagging patterns across similar scenarios.
     * Delegates to TagFrequencyAnalyzer.
     */
    private List<Warning> detectInconsistentTagging() {
        return frequencyAnalyzer.detectInconsistentTagging();
    }
    
    /**
     * Detect possible typos in tags by comparing with other similar tags.
     * Delegates to TagTypoAnalyzer.
     */
    private List<Warning> detectPossibleTagTypos() {
        return typoAnalyzer.detectPossibleTagTypos();
    }
    
    /**
     * Detect duplicate tags on the same scenario.
     * Delegates to TagFrequencyAnalyzer.
     */
    private List<Warning> detectDuplicateTags() {
        return frequencyAnalyzer.detectDuplicateTags();
    }
    
    /**
     * Check if a tag is considered a low-value tag.
     * Public to allow testing - delegates to LowValueTagAnalyzer.
     *
     * @param tag The tag to check
     * @return true if the tag is a low-value tag, false otherwise
     */
    public boolean checkIfTagIsLowValue(String tag) {
        return lowValueAnalyzer.checkIfTagIsLowValue(tag);
    }
    
    /**
     * Generate a formatted report of all warnings.
     * 
     * @param warnings List of warnings to include in the report
     * @return Formatted string with warning details
     */
    public String generateWarningReport(List<Warning> warnings) {
        if (warnings.isEmpty()) {
            return "No tag quality issues detected.";
        }
        
        // Group warnings by severity first, then by type
        Map<com.heymumford.ftoc.config.WarningConfiguration.Severity, Map<WarningType, List<Warning>>> warningsBySeverityAndType = new HashMap<>();
        
        // Initialize severity groups
        for (com.heymumford.ftoc.config.WarningConfiguration.Severity severity : 
                com.heymumford.ftoc.config.WarningConfiguration.Severity.values()) {
            warningsBySeverityAndType.put(severity, new HashMap<>());
        }
        
        // Sort warnings by severity and type
        for (Warning warning : warnings) {
            Map<WarningType, List<Warning>> warningsByType = warningsBySeverityAndType.get(warning.getSeverity());
            warningsByType.computeIfAbsent(warning.getType(), k -> new ArrayList<>()).add(warning);
        }
        
        StringBuilder report = new StringBuilder();
        report.append("TAG QUALITY WARNINGS\n");
        report.append("===================\n\n");
        report.append("Found ").append(warnings.size()).append(" potential tag quality issues.\n\n");
        
        // Generate summary section by severity
        report.append("SUMMARY BY SEVERITY\n");
        report.append("-----------------\n");
        
        for (com.heymumford.ftoc.config.WarningConfiguration.Severity severity : 
                com.heymumford.ftoc.config.WarningConfiguration.Severity.values()) {
            
            Map<WarningType, List<Warning>> warningsByType = warningsBySeverityAndType.get(severity);
            int totalForSeverity = warningsByType.values().stream()
                    .mapToInt(List::size)
                    .sum();
            
            if (totalForSeverity > 0) {
                report.append(String.format("%-10s: %d\n", severity.getDisplayName(), totalForSeverity));
            }
        }
        
        report.append("\n");
        
        // Generate summary section by type
        report.append("SUMMARY BY TYPE\n");
        report.append("-------------\n");
        
        for (WarningType type : WarningType.values()) {
            int count = 0;
            for (Map<WarningType, List<Warning>> warningsByType : warningsBySeverityAndType.values()) {
                List<Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
                count += typeWarnings.size();
            }
            
            if (count > 0) {
                report.append(String.format("%-25s: %d\n", type.getDescription(), count));
            }
        }
        
        report.append("\n");
        
        // Generate detailed section for each severity level
        for (com.heymumford.ftoc.config.WarningConfiguration.Severity severity : 
                com.heymumford.ftoc.config.WarningConfiguration.Severity.values()) {
            
            Map<WarningType, List<Warning>> warningsByType = warningsBySeverityAndType.get(severity);
            int totalForSeverity = warningsByType.values().stream()
                    .mapToInt(List::size)
                    .sum();
            
            if (totalForSeverity > 0) {
                report.append(severity.getDisplayName().toUpperCase()).append(" LEVEL WARNINGS\n");
                report.append(String.join("", Collections.nCopies(severity.getDisplayName().length() + 14, "="))).append("\n\n");
                
                // Generate detailed section for each warning type within this severity
                for (Map.Entry<WarningType, List<Warning>> entry : warningsByType.entrySet()) {
                    WarningType type = entry.getKey();
                    List<Warning> typeWarnings = entry.getValue();
                    
                    if (typeWarnings.isEmpty()) {
                        continue;
                    }
                    
                    report.append(type.getDescription().toUpperCase()).append("\n");
                    report.append(String.join("", Collections.nCopies(type.getDescription().length(), "-"))).append("\n");
                    
                    // Include a general remediation suggestion for this warning type
                    report.append("Remediation:\n");
                    for (String remedy : typeWarnings.get(0).getRemediation()) {
                        report.append("- ").append(remedy).append("\n");
                    }
                    report.append("\n");
                    
                    // List all instances of this warning type
                    for (Warning warning : typeWarnings) {
                        report.append("- ").append(warning.getMessage());
                        if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                            report.append(" (in ").append(warning.getLocation()).append(")");
                        }
                        
                        // Add standard alternatives if available
                        if (warning.hasStandardAlternatives()) {
                            report.append("\n  Suggested alternatives: ")
                                  .append(String.join(", ", warning.getStandardAlternatives()));
                        }
                        
                        report.append("\n");
                    }
                    
                    report.append("\n");
                }
            }
        }
        
        return report.toString();
    }
}