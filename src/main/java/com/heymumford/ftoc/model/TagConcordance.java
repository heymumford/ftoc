package com.heymumford.ftoc.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Value object representing a concordance (frequency index) of tags.
 *
 * This class eliminates primitive obsession by wrapping tag-to-count mappings
 * in a type-safe object with business logic. Following Martin Fowler's
 * "Replace Data Value with Object" refactoring pattern.
 *
 * TagConcordance is immutable and provides rich querying capabilities.
 *
 * @see <a href="https://refactoring.com/catalog/replaceDataValueWithObject.html">Replace Data Value with Object</a>
 */
public final class TagConcordance {

    private final Map<Tag, Integer> counts;
    private final int totalOccurrences;

    /**
     * Private constructor to enforce factory methods.
     *
     * @param counts Map of tags to their occurrence counts
     */
    private TagConcordance(Map<Tag, Integer> counts) {
        this.counts = new HashMap<>(counts);
        this.totalOccurrences = counts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Create an empty TagConcordance.
     *
     * @return An empty TagConcordance
     */
    public static TagConcordance empty() {
        return new TagConcordance(Collections.emptyMap());
    }

    /**
     * Create a TagConcordance from a map of tag strings to counts.
     * This is a bridge method for legacy code using Map&lt;String, Integer&gt;.
     *
     * @param stringCounts Map of tag strings to counts
     * @return A new TagConcordance instance
     */
    public static TagConcordance fromStrings(Map<String, Integer> stringCounts) {
        Map<Tag, Integer> tagCounts = stringCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Tag.of(e.getKey()),
                        Map.Entry::getValue
                ));
        return new TagConcordance(tagCounts);
    }

    /**
     * Create a TagConcordance from a collection of features.
     *
     * @param features The features to analyze
     * @return A new TagConcordance with tag counts
     */
    public static TagConcordance fromFeatures(List<Feature> features) {
        Map<Tag, Integer> counts = new HashMap<>();

        for (Feature feature : features) {
            // Count feature-level tags
            for (String tagStr : feature.getTags()) {
                Tag tag = Tag.of(tagStr);
                counts.merge(tag, 1, Integer::sum);
            }

            // Count scenario-level tags
            for (Scenario scenario : feature.getScenarios()) {
                for (String tagStr : scenario.getTags()) {
                    Tag tag = Tag.of(tagStr);
                    counts.merge(tag, 1, Integer::sum);
                }
            }
        }

        return new TagConcordance(counts);
    }

    /**
     * Get the count for a specific tag.
     *
     * @param tag The tag to query
     * @return The count, or 0 if the tag doesn't exist
     */
    public int getCount(Tag tag) {
        return counts.getOrDefault(tag, 0);
    }

    /**
     * Get the count for a tag by string name.
     * Bridge method for legacy code.
     *
     * @param tagName The tag name
     * @return The count, or 0 if the tag doesn't exist
     */
    public int getCount(String tagName) {
        return getCount(Tag.of(tagName));
    }

    /**
     * Get all unique tags.
     *
     * @return Unmodifiable set of all tags
     */
    public Set<Tag> getAllTags() {
        return Collections.unmodifiableSet(counts.keySet());
    }

    /**
     * Get the total number of unique tags.
     *
     * @return The number of unique tags
     */
    public int getUniqueTagCount() {
        return counts.size();
    }

    /**
     * Get the total number of tag occurrences across all tags.
     *
     * @return The total count
     */
    public int getTotalOccurrences() {
        return totalOccurrences;
    }

    /**
     * Get tags sorted by frequency (descending).
     *
     * @return List of tags ordered by frequency
     */
    public List<Tag> getTagsSortedByFrequency() {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<Tag, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get tags sorted alphabetically.
     *
     * @return List of tags ordered alphabetically
     */
    public List<Tag> getTagsSortedAlphabetically() {
        return counts.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Filter tags by category.
     *
     * @param category The category to filter by
     * @return A new TagConcordance containing only tags of the specified category
     */
    public TagConcordance filterByCategory(Tag.TagCategory category) {
        Map<Tag, Integer> filtered = counts.entrySet().stream()
                .filter(e -> e.getKey().getCategory() == category)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new TagConcordance(filtered);
    }

    /**
     * Get tags that appear only once (orphaned tags).
     *
     * @return List of tags with count == 1
     */
    public List<Tag> getOrphanedTags() {
        return counts.entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get tags that appear above a threshold count.
     *
     * @param threshold The minimum count
     * @return List of tags with count >= threshold
     */
    public List<Tag> getTagsAboveThreshold(int threshold) {
        return counts.entrySet().stream()
                .filter(e -> e.getValue() >= threshold)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get tags that appear below a threshold count.
     *
     * @param threshold The maximum count
     * @return List of tags with count <= threshold
     */
    public List<Tag> getTagsBelowThreshold(int threshold) {
        return counts.entrySet().stream()
                .filter(e -> e.getValue() <= threshold)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Find similar tags (potential typos).
     * Tags are similar if their Levenshtein distance is <= 2.
     *
     * @return Map of tags to their similar tags
     */
    public Map<Tag, List<Tag>> findSimilarTags() {
        Map<Tag, List<Tag>> similarTags = new HashMap<>();
        List<Tag> allTags = new ArrayList<>(counts.keySet());

        for (int i = 0; i < allTags.size(); i++) {
            Tag tag1 = allTags.get(i);
            List<Tag> similar = new ArrayList<>();

            for (int j = i + 1; j < allTags.size(); j++) {
                Tag tag2 = allTags.get(j);
                if (tag1.isSimilarTo(tag2)) {
                    similar.add(tag2);
                }
            }

            if (!similar.isEmpty()) {
                similarTags.put(tag1, similar);
            }
        }

        return similarTags;
    }

    /**
     * Convert to legacy Map&lt;String, Integer&gt; format.
     * Bridge method for backward compatibility.
     *
     * @return Map of tag strings to counts
     */
    public Map<String, Integer> toStringMap() {
        return counts.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getName(),
                        Map.Entry::getValue
                ));
    }

    /**
     * Check if this concordance is empty.
     *
     * @return true if no tags are present
     */
    public boolean isEmpty() {
        return counts.isEmpty();
    }

    /**
     * Get a summary string of the concordance.
     *
     * @return A human-readable summary
     */
    public String getSummary() {
        return String.format("TagConcordance[uniqueTags=%d, totalOccurrences=%d]",
                getUniqueTagCount(), getTotalOccurrences());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagConcordance that = (TagConcordance) o;
        return Objects.equals(counts, that.counts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(counts);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
