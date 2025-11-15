package com.heymumford.ftoc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the Tag value object.
 * Tests validation, categorization, comparison, and business logic.
 */
@DisplayName("Tag Value Object Tests")
class TagTest {

    @Test
    @DisplayName("Should create tag with @ prefix")
    void shouldCreateTagWithAtPrefix() {
        Tag tag = Tag.of("P0");
        assertEquals("@P0", tag.getName());
    }

    @Test
    @DisplayName("Should preserve @ prefix if already present")
    void shouldPreserveExistingAtPrefix() {
        Tag tag = Tag.of("@P1");
        assertEquals("@P1", tag.getName());
    }

    @Test
    @DisplayName("Should throw exception for null tag name")
    void shouldThrowExceptionForNull() {
        assertThrows(IllegalArgumentException.class, () -> Tag.of(null));
    }

    @Test
    @DisplayName("Should throw exception for empty tag name")
    void shouldThrowExceptionForEmpty() {
        assertThrows(IllegalArgumentException.class, () -> Tag.of(""));
        assertThrows(IllegalArgumentException.class, () -> Tag.of("   "));
    }

    // Priority tag tests
    @Test
    @DisplayName("Should recognize P0-P4 as priority tags")
    void shouldRecognizePriorityTags() {
        assertTrue(Tag.of("@P0").isPriority());
        assertTrue(Tag.of("@P1").isPriority());
        assertTrue(Tag.of("@P2").isPriority());
        assertTrue(Tag.of("@P3").isPriority());
        assertTrue(Tag.of("@P4").isPriority());

        // Case insensitive
        assertTrue(Tag.of("@p0").isPriority());
        assertTrue(Tag.of("@p1").isPriority());
    }

    @Test
    @DisplayName("Should recognize semantic priority tags")
    void shouldRecognizeSemanticPriorityTags() {
        assertTrue(Tag.of("@Critical").isPriority());
        assertTrue(Tag.of("@High").isPriority());
        assertTrue(Tag.of("@Medium").isPriority());
        assertTrue(Tag.of("@Low").isPriority());

        // Case insensitive
        assertTrue(Tag.of("@critical").isPriority());
        assertTrue(Tag.of("@HIGH").isPriority());
    }

    @Test
    @DisplayName("Should not recognize non-priority tags as priority")
    void shouldNotRecognizeNonPriorityTags() {
        assertFalse(Tag.of("@UI").isPriority());
        assertFalse(Tag.of("@Smoke").isPriority());
        assertFalse(Tag.of("@WIP").isPriority());
    }

    // Type tag tests
    @Test
    @DisplayName("Should recognize type tags")
    void shouldRecognizeTypeTags() {
        assertTrue(Tag.of("@UI").isType());
        assertTrue(Tag.of("@API").isType());
        assertTrue(Tag.of("@Integration").isType());
        assertTrue(Tag.of("@E2E").isType());
        assertTrue(Tag.of("@Smoke").isType());
        assertTrue(Tag.of("@Regression").isType());

        // Case insensitive
        assertTrue(Tag.of("@ui").isType());
        assertTrue(Tag.of("@api").isType());
    }

    @Test
    @DisplayName("Should not recognize non-type tags as type")
    void shouldNotRecognizeNonTypeTags() {
        assertFalse(Tag.of("@P0").isType());
        assertFalse(Tag.of("@WIP").isType());
        assertFalse(Tag.of("@CustomTag").isType());
    }

    // Status tag tests
    @Test
    @DisplayName("Should recognize status tags")
    void shouldRecognizeStatusTags() {
        assertTrue(Tag.of("@WIP").isStatus());
        assertTrue(Tag.of("@Ready").isStatus());
        assertTrue(Tag.of("@Flaky").isStatus());
        assertTrue(Tag.of("@Deprecated").isStatus());

        // Case insensitive
        assertTrue(Tag.of("@wip").isStatus());
        assertTrue(Tag.of("@FLAKY").isStatus());
    }

    @Test
    @DisplayName("Should not recognize non-status tags as status")
    void shouldNotRecognizeNonStatusTags() {
        assertFalse(Tag.of("@P0").isStatus());
        assertFalse(Tag.of("@UI").isStatus());
        assertFalse(Tag.of("@CustomTag").isStatus());
    }

    // Category tests
    @Test
    @DisplayName("Should categorize priority tags correctly")
    void shouldCategorizePriorityTags() {
        assertEquals(Tag.TagCategory.PRIORITY, Tag.of("@P0").getCategory());
        assertEquals(Tag.TagCategory.PRIORITY, Tag.of("@Critical").getCategory());
    }

    @Test
    @DisplayName("Should categorize type tags correctly")
    void shouldCategorizeTypeTags() {
        assertEquals(Tag.TagCategory.TYPE, Tag.of("@UI").getCategory());
        assertEquals(Tag.TagCategory.TYPE, Tag.of("@API").getCategory());
    }

    @Test
    @DisplayName("Should categorize status tags correctly")
    void shouldCategorizeStatusTags() {
        assertEquals(Tag.TagCategory.STATUS, Tag.of("@WIP").getCategory());
        assertEquals(Tag.TagCategory.STATUS, Tag.of("@Flaky").getCategory());
    }

    @Test
    @DisplayName("Should categorize unknown tags as OTHER")
    void shouldCategorizeUnknownTags() {
        assertEquals(Tag.TagCategory.OTHER, Tag.of("@CustomTag").getCategory());
        assertEquals(Tag.TagCategory.OTHER, Tag.of("@MyFeature").getCategory());
    }

    // Normalization tests
    @Test
    @DisplayName("Should normalize tags for comparison")
    void shouldNormalizeTags() {
        assertEquals("p0", Tag.of("@P0").getNormalized());
        assertEquals("p0", Tag.of("@p0").getNormalized());
        assertEquals("myfeature", Tag.of("@MyFeature").getNormalized());
    }

    @Test
    @DisplayName("Should remove separators during normalization")
    void shouldRemoveSeparators() {
        assertEquals("myfeature", Tag.of("@My-Feature").getNormalized());
        assertEquals("myfeature", Tag.of("@My_Feature").getNormalized());
        assertEquals("myfeature", Tag.of("@My.Feature").getNormalized());
    }

    // Equality tests
    @Test
    @DisplayName("Should consider tags equal ignoring case")
    void shouldConsiderTagsEqualIgnoringCase() {
        Tag tag1 = Tag.of("@P0");
        Tag tag2 = Tag.of("@p0");
        Tag tag3 = Tag.of("P0");

        assertEquals(tag1, tag2);
        assertEquals(tag1, tag3);
        assertEquals(tag2, tag3);
    }

    @Test
    @DisplayName("Should have consistent hashCode for equal tags")
    void shouldHaveConsistentHashCode() {
        Tag tag1 = Tag.of("@P0");
        Tag tag2 = Tag.of("@p0");

        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal to different tags")
    void shouldNotBeEqualToDifferentTags() {
        Tag tag1 = Tag.of("@P0");
        Tag tag2 = Tag.of("@P1");

        assertNotEquals(tag1, tag2);
    }

    // Distance tests
    @Test
    @DisplayName("Should calculate Levenshtein distance correctly")
    void shouldCalculateLevenshteinDistance() {
        Tag tag1 = Tag.of("@Regression");
        Tag tag2 = Tag.of("@Regressionn"); // One extra character

        assertEquals(1, tag1.distanceTo(tag2));
    }

    @Test
    @DisplayName("Should detect similar tags")
    void shouldDetectSimilarTags() {
        Tag tag1 = Tag.of("@Regression");
        Tag tag2 = Tag.of("@Regressionn"); // Typo
        Tag tag3 = Tag.of("@Regresssion"); // Typo

        assertTrue(tag1.isSimilarTo(tag2));
        assertTrue(tag1.isSimilarTo(tag3));
    }

    @Test
    @DisplayName("Should not detect dissimilar tags as similar")
    void shouldNotDetectDissimilarTags() {
        Tag tag1 = Tag.of("@Regression");
        Tag tag2 = Tag.of("@Smoke");

        assertFalse(tag1.isSimilarTo(tag2));
    }

    // Sorting tests
    @Test
    @DisplayName("Should sort tags by category then alphabetically")
    void shouldSortTagsByCategoryThenAlphabetically() {
        Tag priority = Tag.of("@P0");
        Tag type = Tag.of("@UI");
        Tag status = Tag.of("@WIP");
        Tag other = Tag.of("@Custom");

        // Priority < Type < Status < Other
        assertTrue(priority.compareTo(type) < 0);
        assertTrue(type.compareTo(status) < 0);
        assertTrue(status.compareTo(other) < 0);
    }

    @Test
    @DisplayName("Should sort tags alphabetically within same category")
    void shouldSortAlphabeticallyWithinCategory() {
        Tag p0 = Tag.of("@P0");
        Tag p1 = Tag.of("@P1");
        Tag p2 = Tag.of("@P2");

        assertTrue(p0.compareTo(p1) < 0);
        assertTrue(p1.compareTo(p2) < 0);
    }

    // toString tests
    @Test
    @DisplayName("Should convert to string correctly")
    void shouldConvertToString() {
        Tag tag = Tag.of("@P0");
        assertEquals("@P0", tag.toString());
    }

    @Test
    @DisplayName("Should preserve original case in toString")
    void shouldPreserveOriginalCaseInToString() {
        Tag tag = Tag.of("@MyFeature");
        assertEquals("@MyFeature", tag.toString());
    }
}
