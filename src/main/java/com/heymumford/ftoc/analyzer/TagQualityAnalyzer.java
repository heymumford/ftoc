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
        
        public Warning(WarningType type, String message, String location, List<String> remediation) {
            this.type = type;
            this.message = message;
            this.location = location;
            this.remediation = remediation;
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
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(type.getDescription()).append(": ").append(message);
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
                    List<String> remediation = Arrays.asList(
                            "Add a priority tag like @P0 (highest), @P1, @P2, or @P3 (lowest)",
                            "Alternatively, add a semantic priority tag like @Critical, @High, @Medium, or @Low",
                            "Apply priority tags consistently across all scenarios"
                    );
                    
                    warnings.add(new Warning(
                            WarningType.MISSING_PRIORITY_TAG,
                            "Scenario is missing a priority tag",
                            location,
                            remediation
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
     */
    private List<Warning> detectLowValueTags() {
        List<Warning> warnings = new ArrayList<>();
        int totalFeatures = features.size();
        
        // Check for known low-value tags
        for (Map.Entry<String, Integer> entry : tagConcordance.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();
            String lowerTag = tag.toLowerCase();
            
            // Check against known low-value tag patterns
            if (KNOWN_LOW_VALUE_TAGS.contains(lowerTag)) {
                List<String> locations = findTagLocations(tag);
                String locationStr = String.join(", ", locations);
                
                List<String> remediation = Arrays.asList(
                        "Replace with more specific, meaningful tags",
                        "Consider what information the tag should convey",
                        "Tags should help with test selection and documentation"
                );
                
                warnings.add(new Warning(
                        WarningType.LOW_VALUE_TAG,
                        "'" + tag + "' is a known low-value tag that doesn't provide useful context",
                        locationStr,
                        remediation
                ));
            }
            
            // Check for tags used on nearly all features (>90%)
            if (totalFeatures > 5 && count >= totalFeatures * 0.9) {
                List<String> remediation = Arrays.asList(
                        "Overly common tags don't help discriminate between tests",
                        "Consider if this tag is providing useful information",
                        "If this tag is needed on most features, consider making it a convention rather than a tag"
                );
                
                warnings.add(new Warning(
                        WarningType.TOO_GENERIC_TAG,
                        "'" + tag + "' is used on nearly all features, making it too generic to be useful",
                        "Used in " + count + " out of " + totalFeatures + " features",
                        remediation
                ));
            }
            
            // Check for ambiguous short tags (1-2 characters)
            if (tag.length() <= 3 && !PRIORITY_TAGS.contains(lowerTag)) {
                List<String> locations = findTagLocations(tag);
                String locationStr = String.join(", ", locations);
                
                List<String> remediation = Arrays.asList(
                        "Use more descriptive tag names",
                        "Short tags are hard to understand and maintain",
                        "Consider what information the tag should convey"
                );
                
                warnings.add(new Warning(
                        WarningType.AMBIGUOUS_TAG,
                        "'" + tag + "' is too short and ambiguous",
                        locationStr,
                        remediation
                ));
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect tags that are only used once (might be typos or orphaned).
     */
    private List<Warning> detectOrphanedTags() {
        List<Warning> warnings = new ArrayList<>();
        int totalFeatures = features.size();
        
        // Only check for orphaned tags if we have enough features
        if (totalFeatures <= 2) {
            return warnings;
        }
        
        for (Map.Entry<String, Integer> entry : tagConcordance.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();
            
            // Tags used only once might be typos or orphaned
            if (count == 1) {
                // Find where this tag is used
                List<String> locations = findTagLocations(tag);
                String locationStr = String.join(", ", locations);
                
                // Check if this might be a typo of another tag
                String possibleCorrectTag = findPossibleCorrectTag(tag);
                List<String> remediation;
                
                if (possibleCorrectTag != null) {
                    remediation = Arrays.asList(
                            "This might be a typo of '" + possibleCorrectTag + "'",
                            "Correct the tag if it's a typo",
                            "If intentional, consider using consistent naming conventions for related tags"
                    );
                    
                    warnings.add(new Warning(
                            WarningType.TAG_TYPO,
                            "'" + tag + "' is only used once and might be a typo",
                            locationStr,
                            remediation
                    ));
                } else {
                    remediation = Arrays.asList(
                            "Tags used only once don't help group related scenarios",
                            "Consider if this tag is valuable or if it should be removed",
                            "Check if it should be consistent with other similar tags"
                    );
                    
                    warnings.add(new Warning(
                            WarningType.ORPHANED_TAG,
                            "'" + tag + "' is only used once across all features",
                            locationStr,
                            remediation
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect scenarios with an excessive number of tags.
     */
    private List<Warning> detectExcessiveTags() {
        List<Warning> warnings = new ArrayList<>();
        
        // Get the maximum tags threshold from configuration (default: 6)
        final int MAX_RECOMMENDED_TAGS = config.getIntThreshold("maxTags", 6);
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                // Skip backgrounds
                if (scenario.isBackground()) {
                    continue;
                }
                
                // Count the total tags (feature + scenario)
                Set<String> allTags = new HashSet<>(feature.getTags());
                allTags.addAll(scenario.getTags());
                
                if (allTags.size() > MAX_RECOMMENDED_TAGS) {
                    String location = feature.getFilename() + " - " + scenario.getName();
                    List<String> remediation = Arrays.asList(
                            "Having too many tags makes it harder to understand the test's purpose",
                            "Consider consolidating similar tags",
                            "Remove redundant tags",
                            "Ensure tags serve a clear purpose (selection, documentation, or automation)"
                    );
                    
                    warnings.add(new Warning(
                            WarningType.EXCESSIVE_TAGS,
                            "Scenario has " + allTags.size() + " tags, which is excessive (recommended max: " + MAX_RECOMMENDED_TAGS + ")",
                            location,
                            remediation
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect inconsistent tagging patterns across similar scenarios.
     */
    private List<Warning> detectInconsistentTagging() {
        List<Warning> warnings = new ArrayList<>();
        
        // Check for inconsistent priority tag usage
        Map<Feature, Set<String>> featurePriorityTags = new HashMap<>();
        
        // Collect priority tags used in each feature
        for (Feature feature : features) {
            Set<String> priorityTags = new HashSet<>();
            
            // Check feature-level tags
            for (String tag : feature.getTags()) {
                if (PRIORITY_TAGS.contains(tag.toLowerCase())) {
                    priorityTags.add(tag.toLowerCase());
                }
            }
            
            // Check scenario-level tags
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                for (String tag : scenario.getTags()) {
                    if (PRIORITY_TAGS.contains(tag.toLowerCase())) {
                        priorityTags.add(tag.toLowerCase());
                    }
                }
            }
            
            featurePriorityTags.put(feature, priorityTags);
        }
        
        // Check if multiple priority tag styles are used
        Set<String> allPriorityStyles = new HashSet<>();
        for (Set<String> tags : featurePriorityTags.values()) {
            for (String tag : tags) {
                if (tag.startsWith("@p")) {
                    allPriorityStyles.add("p-style");
                } else if (tag.startsWith("@priority")) {
                    allPriorityStyles.add("priority-style");
                } else if (tag.startsWith("@critical") || tag.startsWith("@high") || 
                           tag.startsWith("@medium") || tag.startsWith("@low")) {
                    allPriorityStyles.add("severity-style");
                }
            }
        }
        
        if (allPriorityStyles.size() > 1) {
            List<String> remediation = Arrays.asList(
                    "Standardize on a single priority tag style (e.g., @P0-@P3 or @Critical/@High/@Medium/@Low)",
                    "Consistent tag formatting improves readability and maintainability",
                    "Document the preferred tag style in a team guideline"
            );
            
            warnings.add(new Warning(
                    WarningType.INCONSISTENT_TAGGING,
                    "Multiple priority tag styles used across features (" + String.join(", ", allPriorityStyles) + ")",
                    "Multiple features",
                    remediation
            ));
        }
        
        // Check for inconsistent type tag usage with similar logic
        // (Implementation similar to priority tag check above)
        
        return warnings;
    }
    
    /**
     * Detect possible typos in tags by comparing with other similar tags.
     */
    private List<Warning> detectPossibleTagTypos() {
        List<Warning> warnings = new ArrayList<>();
        
        // Find all typo candidates (case insensitive comparison)
        Map<String, String> tagNormalizations = new HashMap<>();
        List<String> allTags = new ArrayList<>(tagConcordance.keySet());
        
        // Normalize tag names for comparison
        for (String tag : allTags) {
            String normalized = normalizeTagForComparison(tag);
            tagNormalizations.put(tag, normalized);
        }
        
        // Find tags with similar normalized forms
        Map<String, List<String>> similarTags = new HashMap<>();
        for (String tag : allTags) {
            String normalized = tagNormalizations.get(tag);
            similarTags.computeIfAbsent(normalized, k -> new ArrayList<>()).add(tag);
        }
        
        // Report warnings for tags with similar names
        for (List<String> tagGroup : similarTags.values()) {
            if (tagGroup.size() > 1) {
                List<String> locations = new ArrayList<>();
                for (String tag : tagGroup) {
                    locations.addAll(findTagLocations(tag));
                }
                
                String locationStr = locations.size() > 3 
                    ? String.join(", ", locations.subList(0, 3)) + " and " + (locations.size() - 3) + " more"
                    : String.join(", ", locations);
                
                List<String> remediation = Arrays.asList(
                        "Standardize on a single tag format",
                        "Update all similar tags to use the same format",
                        "Consider adding a tag glossary to documentation"
                );
                
                warnings.add(new Warning(
                        WarningType.TAG_TYPO,
                        "Similar tags found that might be typos or inconsistencies: " + String.join(", ", tagGroup),
                        locationStr,
                        remediation
                ));
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect duplicate tags on the same scenario.
     */
    private List<Warning> detectDuplicateTags() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            // Check for duplicates in feature tags
            Set<String> featureTags = new HashSet<>();
            List<String> featureDuplicates = new ArrayList<>();
            
            for (String tag : feature.getTags()) {
                String lowerTag = tag.toLowerCase();
                if (!featureTags.add(lowerTag)) {
                    featureDuplicates.add(tag);
                }
            }
            
            if (!featureDuplicates.isEmpty()) {
                List<String> remediation = Arrays.asList(
                        "Remove duplicate tags",
                        "Duplicate tags add noise without value"
                );
                
                warnings.add(new Warning(
                        WarningType.DUPLICATE_TAG,
                        "Feature has duplicate tags: " + String.join(", ", featureDuplicates),
                        feature.getFilename(),
                        remediation
                ));
            }
            
            // Check for duplicates in scenario tags and with feature tags
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                Set<String> scenarioTags = new HashSet<>(featureTags);
                List<String> scenarioDuplicates = new ArrayList<>();
                
                for (String tag : scenario.getTags()) {
                    String lowerTag = tag.toLowerCase();
                    if (!scenarioTags.add(lowerTag)) {
                        // Check if this tag is already at the feature level
                        if (featureTags.contains(lowerTag)) {
                            scenarioDuplicates.add(tag + " (already on feature)");
                        } else {
                            scenarioDuplicates.add(tag);
                        }
                    }
                }
                
                if (!scenarioDuplicates.isEmpty()) {
                    String location = feature.getFilename() + " - " + scenario.getName();
                    List<String> remediation = Arrays.asList(
                            "Remove duplicate tags",
                            "Avoid repeating feature-level tags on scenarios",
                            "Tags at the feature level apply to all scenarios"
                    );
                    
                    warnings.add(new Warning(
                            WarningType.DUPLICATE_TAG,
                            "Scenario has duplicate tags: " + String.join(", ", scenarioDuplicates),
                            location,
                            remediation
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Find all locations where a specific tag is used.
     * 
     * @param tag The tag to find
     * @return List of locations (feature/scenario descriptors)
     */
    private List<String> findTagLocations(String tag) {
        List<String> locations = new ArrayList<>();
        String lowerTag = tag.toLowerCase();
        
        for (Feature feature : features) {
            boolean featureHasTag = feature.getTags().stream()
                    .anyMatch(t -> t.equalsIgnoreCase(tag));
            
            if (featureHasTag) {
                locations.add(feature.getFilename());
            }
            
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                boolean scenarioHasTag = scenario.getTags().stream()
                        .anyMatch(t -> t.equalsIgnoreCase(tag));
                
                if (scenarioHasTag) {
                    locations.add(feature.getFilename() + " - " + scenario.getName());
                }
            }
        }
        
        return locations;
    }
    
    /**
     * Find a possible correct tag if this tag might be a typo.
     * 
     * @param tag The tag that might be a typo
     * @return A similar tag that might be the correct version, or null if none found
     */
    private String findPossibleCorrectTag(String tag) {
        String normalizedTag = normalizeTagForComparison(tag);
        
        // Find tags with similar names
        for (String otherTag : tagConcordance.keySet()) {
            if (otherTag.equals(tag)) {
                continue;
            }
            
            String normalizedOther = normalizeTagForComparison(otherTag);
            
            // Check for small edit distance
            if (calculateLevenshteinDistance(normalizedTag, normalizedOther) <= 2) {
                // Return the more common tag as the likely correct one
                if (tagConcordance.get(otherTag) > tagConcordance.get(tag)) {
                    return otherTag;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Normalize a tag for similarity comparison.
     * 
     * @param tag The tag to normalize
     * @return Normalized tag string
     */
    private String normalizeTagForComparison(String tag) {
        // Remove @ prefix
        String normalized = tag.startsWith("@") ? tag.substring(1) : tag;
        
        // Convert to lowercase
        normalized = normalized.toLowerCase();
        
        // Remove separators and special characters
        normalized = normalized.replaceAll("[_\\-\\.]", "");
        
        return normalized;
    }
    
    /**
     * Calculate the Levenshtein distance between two strings.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return Levenshtein distance (number of edits required to transform s1 into s2)
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j] + 1,         // deletion
                           Math.min(dp[i][j - 1] + 1,          // insertion
                                   dp[i - 1][j - 1] + cost));  // substitution
            }
        }
        
        return dp[s1.length()][s2.length()];
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
        
        // Group warnings by type
        Map<WarningType, List<Warning>> warningsByType = new HashMap<>();
        for (Warning warning : warnings) {
            warningsByType.computeIfAbsent(warning.getType(), k -> new ArrayList<>()).add(warning);
        }
        
        StringBuilder report = new StringBuilder();
        report.append("TAG QUALITY WARNINGS\n");
        report.append("===================\n\n");
        report.append("Found ").append(warnings.size()).append(" potential tag quality issues.\n\n");
        
        // Generate summary section
        report.append("SUMMARY\n");
        report.append("-------\n");
        
        for (WarningType type : WarningType.values()) {
            List<Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
            if (!typeWarnings.isEmpty()) {
                report.append(String.format("%-25s: %d\n", type.getDescription(), typeWarnings.size()));
            }
        }
        
        report.append("\n");
        
        // Generate detailed section for each warning type
        for (Map.Entry<WarningType, List<Warning>> entry : warningsByType.entrySet()) {
            WarningType type = entry.getKey();
            List<Warning> typeWarnings = entry.getValue();
            
            report.append(type.getDescription().toUpperCase()).append("\n");
            report.append(String.join("", Collections.nCopies(type.getDescription().length(), "-"))).append("\n");
            
            // Include a general remediation suggestion for this warning type
            if (!typeWarnings.isEmpty()) {
                report.append("Remediation:\n");
                for (String remedy : typeWarnings.get(0).getRemediation()) {
                    report.append("- ").append(remedy).append("\n");
                }
                report.append("\n");
            }
            
            // List all instances of this warning type
            for (Warning warning : typeWarnings) {
                report.append("- ").append(warning.getMessage());
                if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                    report.append(" (in ").append(warning.getLocation()).append(")");
                }
                report.append("\n");
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
}