package com.heymumford.ftoc.analyzer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer for detecting low-value, generic, and ambiguous tags.
 * Identifies tags that don't provide useful discriminatory value.
 */
public class LowValueTagAnalyzer {

    private final Map<String, Integer> tagConcordance;
    private final com.heymumford.ftoc.config.WarningConfiguration config;
    private final List<String> KNOWN_LOW_VALUE_TAGS;
    private final List<String> PRIORITY_TAGS;

    public LowValueTagAnalyzer(Map<String, Integer> tagConcordance,
                              com.heymumford.ftoc.config.WarningConfiguration config,
                              List<String> knownLowValueTags, List<String> priorityTags) {
        this.tagConcordance = new HashMap<>(tagConcordance);
        this.config = config;
        this.KNOWN_LOW_VALUE_TAGS = new ArrayList<>(knownLowValueTags);
        this.PRIORITY_TAGS = new ArrayList<>(priorityTags);
    }

    /**
     * Detect tags that are likely to be low-value or problematic.
     */
    public List<TagQualityAnalyzer.Warning> detectLowValueTags() {
        List<TagQualityAnalyzer.Warning> warnings = new ArrayList<>();
        // Estimate feature count from concordance (max tag occurrence)
        int totalFeatures = Math.max(1, tagConcordance.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1));

        // Get configuration for relevant warning types
        com.heymumford.ftoc.config.WarningConfiguration.WarningConfig lowValueConfig =
                config.getTagQualityWarnings().get(TagQualityAnalyzer.WarningType.LOW_VALUE_TAG.name());
        com.heymumford.ftoc.config.WarningConfiguration.WarningConfig tooGenericConfig =
                config.getTagQualityWarnings().get(TagQualityAnalyzer.WarningType.TOO_GENERIC_TAG.name());
        com.heymumford.ftoc.config.WarningConfiguration.WarningConfig ambiguousConfig =
                config.getTagQualityWarnings().get(TagQualityAnalyzer.WarningType.AMBIGUOUS_TAG.name());

        // Check for known low-value tags
        for (Map.Entry<String, Integer> entry : tagConcordance.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();

            // Check against known low-value tag patterns
            if (checkIfTagIsLowValue(tag) && lowValueConfig.isEnabled()) {
                List<String> remediation = new ArrayList<>();
                remediation.add("Replace with more specific, meaningful tags");
                remediation.add("Consider what information the tag should convey");
                remediation.add("Tags should help with test selection and documentation");

                // Add standard alternatives if they exist
                if (!lowValueConfig.getStandardAlternatives().isEmpty()) {
                    remediation.add("Consider using standard alternatives: " +
                                    String.join(", ", lowValueConfig.getStandardAlternatives()));
                }

                warnings.add(new TagQualityAnalyzer.Warning(
                        TagQualityAnalyzer.WarningType.LOW_VALUE_TAG,
                        "'" + tag + "' is a known low-value tag that doesn't provide useful context",
                        tag,
                        remediation,
                        lowValueConfig.getSeverity(),
                        lowValueConfig.getStandardAlternatives()
                ));
            }

            // Check for tags used on nearly all features (>90%)
            if (totalFeatures > 5 && count >= totalFeatures * 0.9 && tooGenericConfig.isEnabled()) {
                List<String> remediation = Arrays.asList(
                        "Overly common tags don't help discriminate between tests",
                        "Consider if this tag is providing useful information",
                        "If this tag is needed on most features, consider making it a convention rather than a tag"
                );

                warnings.add(new TagQualityAnalyzer.Warning(
                        TagQualityAnalyzer.WarningType.TOO_GENERIC_TAG,
                        "'" + tag + "' is used on nearly all features, making it too generic to be useful",
                        "Used in " + count + " out of " + totalFeatures + " features",
                        remediation,
                        tooGenericConfig.getSeverity(),
                        null
                ));
            }

            // Check for ambiguous short tags (1-2 characters)
            String lowerTag = tag.toLowerCase();
            if (tag.length() <= 3 && !PRIORITY_TAGS.contains(lowerTag) && ambiguousConfig.isEnabled()) {
                List<String> remediation = Arrays.asList(
                        "Use more descriptive tag names",
                        "Short tags are hard to understand and maintain",
                        "Consider what information the tag should convey"
                );

                warnings.add(new TagQualityAnalyzer.Warning(
                        TagQualityAnalyzer.WarningType.AMBIGUOUS_TAG,
                        "'" + tag + "' is too short and ambiguous",
                        tag,
                        remediation,
                        ambiguousConfig.getSeverity(),
                        null
                ));
            }
        }

        return warnings;
    }

    /**
     * Check if a tag is considered a low-value tag.
     */
    public boolean checkIfTagIsLowValue(String tag) {
        // Get properly normalized tag for comparison
        String normalizedTag = tag.toLowerCase();
        if (normalizedTag.startsWith("@")) {
            normalizedTag = normalizedTag.substring(1);
        }

        // Check against all known low-value tags, normalizing them for comparison
        for (String lowValueTag : KNOWN_LOW_VALUE_TAGS) {
            String normalizedLowValue = lowValueTag.toLowerCase();
            if (normalizedLowValue.startsWith("@")) {
                normalizedLowValue = normalizedLowValue.substring(1);
            }

            if (normalizedTag.equals(normalizedLowValue)) {
                return true;
            }
        }

        return false;
    }
}
