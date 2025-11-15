package com.heymumford.ftoc.formatter.strategy;

import com.heymumford.ftoc.model.Feature;

import java.util.Collections;
import java.util.List;

/**
 * Context object containing all information needed for TOC generation.
 *
 * This class follows the "Introduce Parameter Object" refactoring pattern,
 * replacing long parameter lists with a single cohesive object.
 *
 * Benefits:
 * - Reduces parameter list length (was 4 parameters, now 1)
 * - Groups related data together
 * - Makes it easier to add new parameters without changing method signatures
 * - Immutable design prevents accidental modification
 *
 * @see <a href="https://refactoring.com/catalog/introduceParameterObject.html">Introduce Parameter Object</a>
 */
public final class TocContext {

    private final List<Feature> features;
    private final List<String> includeTags;
    private final List<String> excludeTags;

    /**
     * Create a new TOC context.
     *
     * @param features The features to include in the TOC
     * @param includeTags Tags to filter in (empty = include all)
     * @param excludeTags Tags to filter out
     */
    public TocContext(List<Feature> features, List<String> includeTags, List<String> excludeTags) {
        this.features = Collections.unmodifiableList(features);
        this.includeTags = Collections.unmodifiableList(includeTags);
        this.excludeTags = Collections.unmodifiableList(excludeTags);
    }

    /**
     * Create a context with no tag filtering.
     *
     * @param features The features to include
     * @return A new context with no filters
     */
    public static TocContext withoutFilters(List<Feature> features) {
        return new TocContext(features, Collections.emptyList(), Collections.emptyList());
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public List<String> getIncludeTags() {
        return includeTags;
    }

    public List<String> getExcludeTags() {
        return excludeTags;
    }

    /**
     * Check if any tag filters are applied.
     *
     * @return true if include or exclude tags are specified
     */
    public boolean hasFilters() {
        return !includeTags.isEmpty() || !excludeTags.isEmpty();
    }

    /**
     * Get the number of features in this context.
     *
     * @return The feature count
     */
    public int getFeatureCount() {
        return features.size();
    }
}
