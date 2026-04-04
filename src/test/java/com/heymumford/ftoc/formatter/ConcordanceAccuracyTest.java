package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that verify the mathematical correctness of concordance
 * calculations: Jaccard coefficients, TF-IDF significance,
 * linear regression growth rates, and co-occurrence counts.
 */
public class ConcordanceAccuracyTest {

    // --- Jaccard coefficient tests ---

    @Test
    public void testJaccardCoefficientForIdenticalTags() {
        // Two scenarios with the exact same tags -> Jaccard = 1.0
        List<Feature> features = new ArrayList<>();
        Feature f = new Feature("test.feature");
        f.setName("Test");

        Scenario s1 = new Scenario("S1", "Scenario", 1);
        s1.addTag("@A");
        s1.addTag("@B");

        Scenario s2 = new Scenario("S2", "Scenario", 2);
        s2.addTag("@A");
        s2.addTag("@B");

        f.addScenario(s1);
        f.addScenario(s2);
        features.add(f);

        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences =
            ConcordanceAnalyzer.calculateCoOccurrences(features);

        // @A and @B always co-occur: intersection=2, union=2 -> Jaccard=1.0
        Optional<ConcordanceAnalyzer.CoOccurrence> pair = findPair(coOccurrences, "@A", "@B");
        assertTrue(pair.isPresent(), "Should find @A/@B co-occurrence");
        assertEquals(1.0, pair.get().getCoefficient(), 0.001,
            "Jaccard coefficient should be 1.0 for always-co-occurring tags");
        assertEquals(2, pair.get().getCount(),
            "Co-occurrence count should be 2 (once per scenario)");
    }

    @Test
    public void testJaccardCoefficientForDisjointTags() {
        // @A appears only in S1, @C appears only in S2 -> Jaccard = 0.0
        List<Feature> features = new ArrayList<>();
        Feature f = new Feature("test.feature");
        f.setName("Test");

        Scenario s1 = new Scenario("S1", "Scenario", 1);
        s1.addTag("@A");

        Scenario s2 = new Scenario("S2", "Scenario", 2);
        s2.addTag("@C");

        f.addScenario(s1);
        f.addScenario(s2);
        features.add(f);

        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences =
            ConcordanceAnalyzer.calculateCoOccurrences(features);

        // @A and @C never appear together in the same scenario
        Optional<ConcordanceAnalyzer.CoOccurrence> pair = findPair(coOccurrences, "@A", "@C");
        assertFalse(pair.isPresent(),
            "Disjoint tags should have no co-occurrence entry");
    }

    @Test
    public void testJaccardCoefficientPartialOverlap() {
        // @A in S1,S2; @B in S2,S3 -> intersection={S2}, union={S1,S2,S3} -> J=1/3
        List<Feature> features = new ArrayList<>();
        Feature f = new Feature("test.feature");
        f.setName("Test");

        Scenario s1 = new Scenario("S1", "Scenario", 1);
        s1.addTag("@A");

        Scenario s2 = new Scenario("S2", "Scenario", 2);
        s2.addTag("@A");
        s2.addTag("@B");

        Scenario s3 = new Scenario("S3", "Scenario", 3);
        s3.addTag("@B");

        f.addScenario(s1);
        f.addScenario(s2);
        f.addScenario(s3);
        features.add(f);

        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences =
            ConcordanceAnalyzer.calculateCoOccurrences(features);

        Optional<ConcordanceAnalyzer.CoOccurrence> pair = findPair(coOccurrences, "@A", "@B");
        assertTrue(pair.isPresent());
        assertEquals(1.0 / 3.0, pair.get().getCoefficient(), 0.001,
            "Jaccard should be 1/3 for 1-of-3 overlap");
        assertEquals(1, pair.get().getCount(),
            "Co-occurrence count should be 1 (only S2)");
    }

    // --- Co-occurrence count tests ---

    @Test
    public void testCoOccurrenceCountWithFeatureTags() {
        // Feature-level tag @F combines with scenario-level tags
        List<Feature> features = new ArrayList<>();
        Feature f = new Feature("test.feature");
        f.setName("Test");
        f.addTag("@F");

        Scenario s1 = new Scenario("S1", "Scenario", 1);
        s1.addTag("@A");

        Scenario s2 = new Scenario("S2", "Scenario", 2);
        s2.addTag("@A");

        f.addScenario(s1);
        f.addScenario(s2);
        features.add(f);

        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences =
            ConcordanceAnalyzer.calculateCoOccurrences(features);

        // @F and @A appear together in both scenarios
        Optional<ConcordanceAnalyzer.CoOccurrence> pair = findPair(coOccurrences, "@F", "@A");
        assertTrue(pair.isPresent());
        assertEquals(2, pair.get().getCount(),
            "@F and @A co-occur in both scenarios");
    }

    @Test
    public void testEmptyFeatureListProducesNoCoOccurrences() {
        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences =
            ConcordanceAnalyzer.calculateCoOccurrences(Collections.emptyList());
        assertTrue(coOccurrences.isEmpty());
    }

    // --- TF-IDF significance tests ---

    @Test
    public void testSignificanceHigherForRareTag() {
        // A tag appearing in one feature should have higher significance
        // than a tag appearing in all features (for similar counts)
        List<Feature> features = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Feature f = new Feature("f" + i + ".feature");
            f.setName("Feature " + i);
            Scenario s = new Scenario("S" + i, "Scenario", 1);
            s.addTag("@Common");
            if (i == 0) {
                s.addTag("@Rare");
            }
            f.addScenario(s);
            features.add(f);
        }

        Map<String, Integer> tagConcordance = new HashMap<>();
        tagConcordance.put("@Common", 5);
        tagConcordance.put("@Rare", 1);

        Map<String, Double> significance =
            ConcordanceAnalyzer.calculateTagSignificance(features, tagConcordance);

        // @Rare has higher IDF (fewer documents), so higher TF-IDF even with lower TF
        // @Common: TF=5/5=1.0, IDF=ln(5/6)~=-0.182 -> score ~ -0.182
        // @Rare:   TF=1/5=0.2, IDF=ln(5/2)~=0.916  -> score ~ 0.183
        assertTrue(significance.get("@Rare") > significance.get("@Common"),
            "@Rare should have higher significance than @Common");
    }

    @Test
    public void testSignificanceWithSingleFeature() {
        List<Feature> features = new ArrayList<>();
        Feature f = new Feature("solo.feature");
        f.setName("Solo");
        Scenario s = new Scenario("S1", "Scenario", 1);
        s.addTag("@Only");
        f.addScenario(s);
        features.add(f);

        Map<String, Integer> tagConcordance = new HashMap<>();
        tagConcordance.put("@Only", 1);

        Map<String, Double> significance =
            ConcordanceAnalyzer.calculateTagSignificance(features, tagConcordance);

        assertNotNull(significance.get("@Only"));
        assertFalse(Double.isNaN(significance.get("@Only")),
            "Significance should not be NaN for single feature");
        assertFalse(Double.isInfinite(significance.get("@Only")),
            "Significance should not be infinite for single feature");
    }

    // --- Tag trend tests ---

    @Test
    public void testTrendForConsistentlyPresentTag() {
        List<Feature> features = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Feature f = new Feature("f" + i + ".feature");
            f.setName("Feature " + i);
            f.addTag("@Stable");
            Scenario s = new Scenario("S" + i, "Scenario", 1);
            f.addScenario(s);
            features.add(f);
        }

        Map<String, Integer> tagConcordance = new HashMap<>();
        tagConcordance.put("@Stable", 5);

        Map<String, ConcordanceAnalyzer.TagTrend> trends =
            ConcordanceAnalyzer.calculateTagTrends(features, tagConcordance);

        ConcordanceAnalyzer.TagTrend stable = trends.get("@Stable");
        assertNotNull(stable);
        assertEquals(5, stable.getTotalCount());
        assertEquals(5, stable.getFeatureCount());
        // Consistent presence -> stable or flat growth
        assertEquals("Stable", stable.getTrend(),
            "Tag present in every feature should be stable");
    }

    @Test
    public void testTrendScenarioAndFeatureCounts() {
        List<Feature> features = new ArrayList<>();
        Feature f1 = new Feature("f1.feature");
        f1.setName("F1");
        Scenario s1 = new Scenario("S1", "Scenario", 1);
        s1.addTag("@X");
        Scenario s2 = new Scenario("S2", "Scenario", 2);
        s2.addTag("@X");
        f1.addScenario(s1);
        f1.addScenario(s2);

        Feature f2 = new Feature("f2.feature");
        f2.setName("F2");
        Scenario s3 = new Scenario("S3", "Scenario", 3);
        s3.addTag("@X");
        f2.addScenario(s3);

        features.add(f1);
        features.add(f2);

        Map<String, Integer> tagConcordance = new HashMap<>();
        tagConcordance.put("@X", 3);

        Map<String, ConcordanceAnalyzer.TagTrend> trends =
            ConcordanceAnalyzer.calculateTagTrends(features, tagConcordance);

        ConcordanceAnalyzer.TagTrend x = trends.get("@X");
        assertNotNull(x);
        assertEquals(3, x.getTotalCount());
        assertEquals(3, x.getScenarioCount(), "3 scenarios have @X");
        // Feature count tracks features with that tag -- @X is only on scenarios not features
        // so featureCount=0 (feature tags only, not scenario tags)
        assertEquals(0, x.getFeatureCount(),
            "@X is a scenario-level tag, so feature count should be 0");
    }

    // --- Visualization JSON tests ---

    @Test
    public void testVisualizationJsonHasCorrectNodeCount() {
        Map<String, Integer> tagConcordance = new LinkedHashMap<>();
        tagConcordance.put("@A", 5);
        tagConcordance.put("@B", 3);
        tagConcordance.put("@C", 1);

        List<ConcordanceAnalyzer.CoOccurrence> coOccurrences = new ArrayList<>();
        coOccurrences.add(new ConcordanceAnalyzer.CoOccurrence("@A", "@B", 2, 0.5));

        String json = ConcordanceAnalyzer.generateVisualizationJson(coOccurrences, tagConcordance);

        // Should have 3 nodes and 1 link
        // Count "id" occurrences for nodes
        long nodeCount = json.lines()
            .filter(l -> l.contains("\"id\""))
            .count();
        assertEquals(3, nodeCount, "Should have 3 nodes for 3 tags");

        // Count "source" occurrences for links
        long linkCount = json.lines()
            .filter(l -> l.contains("\"source\""))
            .count();
        assertEquals(1, linkCount, "Should have 1 link for 1 co-occurrence");
    }

    // --- Helpers ---

    private Optional<ConcordanceAnalyzer.CoOccurrence> findPair(
            List<ConcordanceAnalyzer.CoOccurrence> list, String tag1, String tag2) {
        return list.stream()
            .filter(co -> (co.getTag1().equals(tag1) && co.getTag2().equals(tag2))
                       || (co.getTag1().equals(tag2) && co.getTag2().equals(tag1)))
            .findFirst();
    }
}
