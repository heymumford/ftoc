package com.heymumford.ftoc.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a Gherkin/Cucumber tag.
 *
 * This class eliminates primitive obsession by wrapping tag strings in a type-safe object
 * with business logic and validation. Following Martin Fowler's "Replace Data Value with Object"
 * refactoring pattern.
 *
 * Tags are immutable and provide semantic methods for categorization.
 *
 * @see <a href="https://refactoring.com/catalog/replaceDataValueWithObject.html">Replace Data Value with Object</a>
 */
public final class Tag implements Comparable<Tag> {

    private static final Pattern PRIORITY_PATTERN = Pattern.compile("@[Pp][0-4]|@(critical|high|medium|low)", Pattern.CASE_INSENSITIVE);

    private static final List<String> TYPE_TAGS = Arrays.asList(
        "@ui", "@api", "@backend", "@frontend", "@integration", "@unit",
        "@performance", "@security", "@regression", "@smoke", "@e2e",
        "@functional", "@acceptance", "@system", "@component"
    );

    private static final List<String> STATUS_TAGS = Arrays.asList(
        "@wip", "@ready", "@review", "@flaky", "@deprecated", "@legacy",
        "@todo", "@debug", "@inprogress", "@completed", "@blocked"
    );

    private final String name;

    /**
     * Private constructor to enforce factory method usage.
     *
     * @param name The tag name (will be normalized to include @ prefix)
     * @throws IllegalArgumentException if name is null or empty
     */
    private Tag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }

        // Normalize: ensure @ prefix
        this.name = name.startsWith("@") ? name : "@" + name;
    }

    /**
     * Factory method to create a Tag instance.
     * This is the preferred way to create Tag objects.
     *
     * @param name The tag name (with or without @ prefix)
     * @return A new Tag instance
     * @throws IllegalArgumentException if name is null or empty
     */
    public static Tag of(String name) {
        return new Tag(name);
    }

    /**
     * Get the tag name (always includes @ prefix).
     *
     * @return The tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Check if this tag represents a priority level.
     * Priority tags include: @P0-@P4, @Critical, @High, @Medium, @Low
     *
     * @return true if this is a priority tag
     */
    public boolean isPriority() {
        return PRIORITY_PATTERN.matcher(name).matches();
    }

    /**
     * Check if this tag represents a test type.
     * Type tags include: @UI, @API, @Integration, @E2E, etc.
     *
     * @return true if this is a type tag
     */
    public boolean isType() {
        return TYPE_TAGS.contains(name.toLowerCase());
    }

    /**
     * Check if this tag represents a status.
     * Status tags include: @WIP, @Ready, @Flaky, @Deprecated, etc.
     *
     * @return true if this is a status tag
     */
    public boolean isStatus() {
        return STATUS_TAGS.contains(name.toLowerCase());
    }

    /**
     * Get the category of this tag.
     *
     * @return The tag category
     */
    public TagCategory getCategory() {
        if (isPriority()) {
            return TagCategory.PRIORITY;
        } else if (isType()) {
            return TagCategory.TYPE;
        } else if (isStatus()) {
            return TagCategory.STATUS;
        } else {
            return TagCategory.OTHER;
        }
    }

    /**
     * Get the normalized form of this tag for comparison purposes.
     * Removes @ prefix and converts to lowercase.
     *
     * @return Normalized tag name
     */
    public String getNormalized() {
        return name.substring(1).toLowerCase().replaceAll("[_\\-\\.]", "");
    }

    /**
     * Calculate Levenshtein distance to another tag.
     * Useful for detecting typos and similar tags.
     *
     * @param other The other tag
     * @return The edit distance between the normalized forms
     */
    public int distanceTo(Tag other) {
        String s1 = this.getNormalized();
        String s2 = other.getNormalized();

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
     * Check if this tag is similar to another tag (Levenshtein distance <= 2).
     *
     * @param other The other tag
     * @return true if the tags are similar
     */
    public boolean isSimilarTo(Tag other) {
        return distanceTo(other) <= 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        // Case-insensitive comparison for tags
        return name.equalsIgnoreCase(tag.name);
    }

    @Override
    public int hashCode() {
        // Use lowercase for consistent hashing
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Tag other) {
        // Sort by category first, then alphabetically within category
        int categoryCompare = this.getCategory().compareTo(other.getCategory());
        if (categoryCompare != 0) {
            return categoryCompare;
        }
        return this.name.compareToIgnoreCase(other.name);
    }

    /**
     * Enumeration of tag categories.
     */
    public enum TagCategory {
        /** Priority tags like @P0, @Critical */
        PRIORITY,

        /** Type tags like @UI, @API */
        TYPE,

        /** Status tags like @WIP, @Flaky */
        STATUS,

        /** Other tags */
        OTHER
    }
}
