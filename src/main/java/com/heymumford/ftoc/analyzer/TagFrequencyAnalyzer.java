package com.heymumford.ftoc.analyzer;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer for detecting tag frequency issues: orphaned tags, excessive tags,
 * inconsistent patterns, and duplicates.
 */
public class TagFrequencyAnalyzer {

    private final Map<String, Integer> tagConcordance;
    private final List<Feature> features;
    private final com.heymumford.ftoc.config.WarningConfiguration config;
    private final List<String> PRIORITY_TAGS;

    public TagFrequencyAnalyzer(Map<String, Integer> tagConcordance, List<Feature> features,
                               com.heymumford.ftoc.config.WarningConfiguration config,
                               List<String> priorityTags) {
        this.tagConcordance = new HashMap<>(tagConcordance);
        this.features = new ArrayList<>(features);
        this.config = config;
        this.PRIORITY_TAGS = new ArrayList<>(priorityTags);
    }

    /**
     * Detect tags that are only used once (might be typos or orphaned).
     */
    public List<TagQualityAnalyzer.Warning> detectOrphanedTags() {
        List<TagQualityAnalyzer.Warning> warnings = new ArrayList<>();
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

                    warnings.add(new TagQualityAnalyzer.Warning(
                            TagQualityAnalyzer.WarningType.TAG_TYPO,
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

                    warnings.add(new TagQualityAnalyzer.Warning(
                            TagQualityAnalyzer.WarningType.ORPHANED_TAG,
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
    public List<TagQualityAnalyzer.Warning> detectExcessiveTags() {
        List<TagQualityAnalyzer.Warning> warnings = new ArrayList<>();

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

                    warnings.add(new TagQualityAnalyzer.Warning(
                            TagQualityAnalyzer.WarningType.EXCESSIVE_TAGS,
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
    public List<TagQualityAnalyzer.Warning> detectInconsistentTagging() {
        List<TagQualityAnalyzer.Warning> warnings = new ArrayList<>();

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

            warnings.add(new TagQualityAnalyzer.Warning(
                    TagQualityAnalyzer.WarningType.INCONSISTENT_TAGGING,
                    "Multiple priority tag styles used across features (" + String.join(", ", allPriorityStyles) + ")",
                    "Multiple features",
                    remediation
            ));
        }

        return warnings;
    }

    /**
     * Detect duplicate tags on the same scenario.
     */
    public List<TagQualityAnalyzer.Warning> detectDuplicateTags() {
        List<TagQualityAnalyzer.Warning> warnings = new ArrayList<>();

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

                warnings.add(new TagQualityAnalyzer.Warning(
                        TagQualityAnalyzer.WarningType.DUPLICATE_TAG,
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

                    warnings.add(new TagQualityAnalyzer.Warning(
                            TagQualityAnalyzer.WarningType.DUPLICATE_TAG,
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
     */
    private List<String> findTagLocations(String tag) {
        List<String> locations = new ArrayList<>();

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
}
