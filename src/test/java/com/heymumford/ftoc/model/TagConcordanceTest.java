package com.heymumford.ftoc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the TagConcordance value object.
 */
@DisplayName("TagConcordance Value Object Tests")
class TagConcordanceTest {

    private TagConcordance concordance;

    @BeforeEach
    void setUp() {
        Map<String, Integer> stringCounts = new HashMap<>();
        stringCounts.put("@P0", 5);
        stringCounts.put("@P1", 3);
        stringCounts.put("@UI", 10);
        stringCounts.put("@API", 8);
        stringCounts.put("@WIP", 1);
        stringCounts.put("@Flaky", 1);
        stringCounts.put("@Smoke", 7);

        concordance = TagConcordance.fromStrings(stringCounts);
    }

    @Test
    @DisplayName("Should create empty concordance")
    void shouldCreateEmptyConcordance() {
        TagConcordance empty = TagConcordance.empty();

        assertTrue(empty.isEmpty());
        assertEquals(0, empty.getUniqueTagCount());
        assertEquals(0, empty.getTotalOccurrences());
    }

    @Test
    @DisplayName("Should create concordance from string map")
    void shouldCreateFromStringMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("@P0", 5);
        map.put("@UI", 10);

        TagConcordance tc = TagConcordance.fromStrings(map);

        assertEquals(2, tc.getUniqueTagCount());
        assertEquals(15, tc.getTotalOccurrences());
        assertEquals(5, tc.getCount("@P0"));
        assertEquals(10, tc.getCount("@UI"));
    }

    @Test
    @DisplayName("Should get count for existing tag")
    void shouldGetCountForExistingTag() {
        assertEquals(5, concordance.getCount(Tag.of("@P0")));
        assertEquals(10, concordance.getCount(Tag.of("@UI")));
        assertEquals(1, concordance.getCount(Tag.of("@WIP")));
    }

    @Test
    @DisplayName("Should return zero for non-existent tag")
    void shouldReturnZeroForNonExistentTag() {
        assertEquals(0, concordance.getCount(Tag.of("@NonExistent")));
        assertEquals(0, concordance.getCount("@Missing"));
    }

    @Test
    @DisplayName("Should get count by string name")
    void shouldGetCountByStringName() {
        assertEquals(5, concordance.getCount("@P0"));
        assertEquals(10, concordance.getCount("@UI"));

        // Should handle tags without @ prefix
        assertEquals(5, concordance.getCount("P0"));
    }

    @Test
    @DisplayName("Should get all unique tags")
    void shouldGetAllUniqueTags() {
        Set<Tag> allTags = concordance.getAllTags();

        assertEquals(7, allTags.size());
        assertTrue(allTags.contains(Tag.of("@P0")));
        assertTrue(allTags.contains(Tag.of("@UI")));
        assertTrue(allTags.contains(Tag.of("@WIP")));

        // Should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () ->
            allTags.add(Tag.of("@New")));
    }

    @Test
    @DisplayName("Should count unique tags correctly")
    void shouldCountUniqueTags() {
        assertEquals(7, concordance.getUniqueTagCount());

        TagConcordance empty = TagConcordance.empty();
        assertEquals(0, empty.getUniqueTagCount());
    }

    @Test
    @DisplayName("Should count total occurrences correctly")
    void shouldCountTotalOccurrences() {
        // 5 + 3 + 10 + 8 + 1 + 1 + 7 = 35
        assertEquals(35, concordance.getTotalOccurrences());
    }

    @Test
    @DisplayName("Should sort tags by frequency descending")
    void shouldSortTagsByFrequency() {
        List<Tag> sorted = concordance.getTagsSortedByFrequency();

        assertEquals(7, sorted.size());
        assertEquals(Tag.of("@UI"), sorted.get(0));      // 10 occurrences
        assertEquals(Tag.of("@API"), sorted.get(1));     // 8 occurrences
        assertEquals(Tag.of("@Smoke"), sorted.get(2));   // 7 occurrences
        assertEquals(Tag.of("@P0"), sorted.get(3));      // 5 occurrences

        // Last two should be the orphaned tags (1 occurrence each)
        assertTrue(sorted.get(5).equals(Tag.of("@WIP")) ||
                   sorted.get(5).equals(Tag.of("@Flaky")));
        assertTrue(sorted.get(6).equals(Tag.of("@WIP")) ||
                   sorted.get(6).equals(Tag.of("@Flaky")));
    }

    @Test
    @DisplayName("Should sort tags alphabetically")
    void shouldSortTagsAlphabetically() {
        List<Tag> sorted = concordance.getTagsSortedAlphabetically();

        assertEquals(7, sorted.size());
        // Should be sorted by category first, then alphabetically
        // Priority tags (@P0, @P1) should come first
        assertTrue(sorted.get(0).isPriority());
        assertTrue(sorted.get(1).isPriority());
    }

    @Test
    @DisplayName("Should filter by category")
    void shouldFilterByCategory() {
        TagConcordance priorityTags = concordance.filterByCategory(Tag.TagCategory.PRIORITY);
        TagConcordance typeTags = concordance.filterByCategory(Tag.TagCategory.TYPE);
        TagConcordance statusTags = concordance.filterByCategory(Tag.TagCategory.STATUS);

        assertEquals(2, priorityTags.getUniqueTagCount()); // @P0, @P1
        assertEquals(3, typeTags.getUniqueTagCount());     // @UI, @API, @Smoke
        assertEquals(2, statusTags.getUniqueTagCount());   // @WIP, @Flaky

        assertTrue(priorityTags.getAllTags().contains(Tag.of("@P0")));
        assertTrue(typeTags.getAllTags().contains(Tag.of("@UI")));
        assertTrue(statusTags.getAllTags().contains(Tag.of("@WIP")));
    }

    @Test
    @DisplayName("Should find orphaned tags")
    void shouldFindOrphanedTags() {
        List<Tag> orphaned = concordance.getOrphanedTags();

        assertEquals(2, orphaned.size());
        assertTrue(orphaned.contains(Tag.of("@WIP")));
        assertTrue(orphaned.contains(Tag.of("@Flaky")));
    }

    @Test
    @DisplayName("Should find tags above threshold")
    void shouldFindTagsAboveThreshold() {
        List<Tag> aboveThreshold = concordance.getTagsAboveThreshold(5);

        assertEquals(4, aboveThreshold.size());
        assertTrue(aboveThreshold.contains(Tag.of("@P0")));    // 5
        assertTrue(aboveThreshold.contains(Tag.of("@UI")));    // 10
        assertTrue(aboveThreshold.contains(Tag.of("@API")));   // 8
        assertTrue(aboveThreshold.contains(Tag.of("@Smoke"))); // 7
    }

    @Test
    @DisplayName("Should find tags below threshold")
    void shouldFindTagsBelowThreshold() {
        List<Tag> belowThreshold = concordance.getTagsBelowThreshold(3);

        assertEquals(3, belowThreshold.size());
        assertTrue(belowThreshold.contains(Tag.of("@P1")));   // 3
        assertTrue(belowThreshold.contains(Tag.of("@WIP")));  // 1
        assertTrue(belowThreshold.contains(Tag.of("@Flaky"))); // 1
    }

    @Test
    @DisplayName("Should find similar tags")
    void shouldFindSimilarTags() {
        Map<String, Integer> withTypos = new HashMap<>();
        withTypos.put("@Regression", 10);
        withTypos.put("@Regressionn", 1);  // Typo
        withTypos.put("@Regresssion", 1);  // Typo
        withTypos.put("@Smoke", 5);

        TagConcordance tc = TagConcordance.fromStrings(withTypos);
        Map<Tag, List<Tag>> similar = tc.findSimilarTags();

        // Should find the typos
        assertTrue(similar.size() > 0);

        // @Regression should be similar to the typos
        Tag regression = Tag.of("@Regression");
        if (similar.containsKey(regression)) {
            List<Tag> similarToRegression = similar.get(regression);
            assertTrue(similarToRegression.size() >= 1);
        }
    }

    @Test
    @DisplayName("Should convert to string map")
    void shouldConvertToStringMap() {
        Map<String, Integer> stringMap = concordance.toStringMap();

        assertEquals(7, stringMap.size());
        assertEquals(5, stringMap.get("@P0"));
        assertEquals(10, stringMap.get("@UI"));
        assertEquals(1, stringMap.get("@WIP"));
    }

    @Test
    @DisplayName("Should check if empty")
    void shouldCheckIfEmpty() {
        assertFalse(concordance.isEmpty());
        assertTrue(TagConcordance.empty().isEmpty());
    }

    @Test
    @DisplayName("Should generate summary")
    void shouldGenerateSummary() {
        String summary = concordance.getSummary();

        assertTrue(summary.contains("7"));  // unique tags
        assertTrue(summary.contains("35")); // total occurrences
        assertTrue(summary.contains("TagConcordance"));
    }

    @Test
    @DisplayName("Should have proper equals and hashCode")
    void shouldHaveProperEqualsAndHashCode() {
        Map<String, Integer> map1 = new HashMap<>();
        map1.put("@P0", 5);
        map1.put("@UI", 10);

        Map<String, Integer> map2 = new HashMap<>();
        map2.put("@P0", 5);
        map2.put("@UI", 10);

        Map<String, Integer> map3 = new HashMap<>();
        map3.put("@P0", 5);
        map3.put("@UI", 11); // Different count

        TagConcordance tc1 = TagConcordance.fromStrings(map1);
        TagConcordance tc2 = TagConcordance.fromStrings(map2);
        TagConcordance tc3 = TagConcordance.fromStrings(map3);

        assertEquals(tc1, tc2);
        assertNotEquals(tc1, tc3);
        assertEquals(tc1.hashCode(), tc2.hashCode());
    }

    @Test
    @DisplayName("Should create concordance from features")
    void shouldCreateFromFeatures() {
        // Create mock features with tags
        List<Feature> features = new ArrayList<>();

        // This test would require actual Feature objects
        // For now, we'll just verify the method exists and handles empty list
        TagConcordance tc = TagConcordance.fromFeatures(features);

        assertTrue(tc.isEmpty());
        assertEquals(0, tc.getUniqueTagCount());
    }

    @Test
    @DisplayName("Should have immutable behavior")
    void shouldBeImmutable() {
        Set<Tag> tags = concordance.getAllTags();
        int originalSize = tags.size();

        // Attempting to modify should throw
        assertThrows(UnsupportedOperationException.class, () ->
            tags.add(Tag.of("@New")));

        // Original should be unchanged
        assertEquals(originalSize, concordance.getUniqueTagCount());
    }

    @Test
    @DisplayName("Should handle toString properly")
    void shouldHandleToStringProperly() {
        String str = concordance.toString();

        assertNotNull(str);
        assertTrue(str.contains("TagConcordance"));
    }
}
