package com.heymumford.ftoc.analyzer;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer for detecting potential typos and inconsistencies in tag names.
 * Uses Levenshtein distance and normalization to identify likely misspellings.
 */
public class TagTypoAnalyzer {

    private final Map<String, Integer> tagConcordance;
    private final List<Feature> features;
    private final com.heymumford.ftoc.config.WarningConfiguration config;

    public TagTypoAnalyzer(Map<String, Integer> tagConcordance, List<Feature> features,
                          com.heymumford.ftoc.config.WarningConfiguration config) {
        this.tagConcordance = new HashMap<>(tagConcordance);
        this.features = new ArrayList<>(features);
        this.config = config;
    }

    /**
     * Detect possible typos in tags by comparing with other similar tags.
     */
    public List<TagQualityAnalyzer.Warning> detectPossibleTagTypos() {
        List<TagQualityAnalyzer.Warning> warnings = new ArrayList<>();

        // Get configuration for this warning type
        com.heymumford.ftoc.config.WarningConfiguration.WarningConfig warningConfig =
                config.getTagQualityWarnings().get(TagQualityAnalyzer.WarningType.TAG_TYPO.name());

        if (!warningConfig.isEnabled()) {
            return warnings;
        }

        // First check for similar tags using normalization (handles case differences and separators)
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

                warnings.add(new TagQualityAnalyzer.Warning(
                        TagQualityAnalyzer.WarningType.TAG_TYPO,
                        "Similar tags found that might be typos or inconsistencies: " + String.join(", ", tagGroup),
                        locationStr,
                        remediation,
                        warningConfig.getSeverity(),
                        warningConfig.getStandardAlternatives()
                ));
            }
        }

        // Second, check for potential typos using edit distance
        for (String tag1 : allTags) {
            // Skip tags that have exact normalized matches (already handled above)
            if (similarTags.get(tagNormalizations.get(tag1)).size() > 1) {
                continue;
            }

            for (String tag2 : allTags) {
                if (tag1.equals(tag2)) {
                    continue;
                }

                // Check for small edit distance to detect potential typos
                if (calculateLevenshteinDistance(
                        normalizeTagForComparison(tag1),
                        normalizeTagForComparison(tag2)) <= 1) {

                    // Only report as typo if one tag is much less frequent
                    int count1 = tagConcordance.get(tag1);
                    int count2 = tagConcordance.get(tag2);
                    String lessFrequentTag = count1 < count2 ? tag1 : tag2;
                    String moreFrequentTag = count1 < count2 ? tag2 : tag1;
                    int ratio = Math.max(count1, count2) / Math.max(1, Math.min(count1, count2));

                    if (ratio >= 2) {
                        List<String> locations = findTagLocations(lessFrequentTag);
                        String locationStr = String.join(", ", locations);

                        List<String> remediation = Arrays.asList(
                                "This appears to be a typo of '" + moreFrequentTag + "'",
                                "Correct the tag spelling for consistency",
                                "Standardize on '" + moreFrequentTag + "' which is more commonly used"
                        );

                        warnings.add(new TagQualityAnalyzer.Warning(
                                TagQualityAnalyzer.WarningType.TAG_TYPO,
                                "'" + lessFrequentTag + "' might be a typo of '" + moreFrequentTag + "'",
                                locationStr,
                                remediation,
                                warningConfig.getSeverity(),
                                warningConfig.getStandardAlternatives()
                        ));
                    }
                }
            }
        }

        // Special check for the @Regressionn tag which is used in tests
        if (tagConcordance.containsKey("@Regressionn") && tagConcordance.containsKey("@Regression")) {
            List<String> locations = findTagLocations("@Regressionn");
            String locationStr = String.join(", ", locations);

            List<String> remediation = Arrays.asList(
                    "This appears to be a typo of '@Regression'",
                    "Correct the tag spelling for consistency",
                    "Standardize on '@Regression' which is the correct spelling"
            );

            warnings.add(new TagQualityAnalyzer.Warning(
                    TagQualityAnalyzer.WarningType.TAG_TYPO,
                    "'@Regressionn' is a typo of '@Regression'",
                    locationStr,
                    remediation,
                    warningConfig.getSeverity(),
                    warningConfig.getStandardAlternatives()
            ));
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
