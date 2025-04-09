package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer for tag concordance metrics and statistics.
 * This class performs advanced analysis on tag usage patterns.
 */
public class ConcordanceAnalyzer {
    
    /**
     * Represents a co-occurrence relationship between two tags.
     */
    public static class CoOccurrence {
        private final String tag1;
        private final String tag2;
        private final int count;
        private final double coefficient;

        public CoOccurrence(String tag1, String tag2, int count, double coefficient) {
            this.tag1 = tag1;
            this.tag2 = tag2;
            this.count = count;
            this.coefficient = coefficient;
        }

        public String getTag1() {
            return tag1;
        }

        public String getTag2() {
            return tag2;
        }

        public int getCount() {
            return count;
        }

        public double getCoefficient() {
            return coefficient;
        }
        
        @Override
        public String toString() {
            return String.format("%s & %s (Count: %d, Coefficient: %.2f)", tag1, tag2, count, coefficient);
        }
    }
    
    /**
     * Represents a trend analysis for a tag across features and scenarios.
     */
    public static class TagTrend {
        private final String tag;
        private final int totalCount;
        private final int scenarioCount;
        private final int featureCount;
        private final double growthRate;
        private final Map<String, Integer> associatedTags;

        public TagTrend(String tag, int totalCount, int scenarioCount, int featureCount, double growthRate, 
                Map<String, Integer> associatedTags) {
            this.tag = tag;
            this.totalCount = totalCount;
            this.scenarioCount = scenarioCount;
            this.featureCount = featureCount;
            this.growthRate = growthRate;
            this.associatedTags = associatedTags;
        }

        public String getTag() {
            return tag;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getScenarioCount() {
            return scenarioCount;
        }

        public int getFeatureCount() {
            return featureCount;
        }

        public double getGrowthRate() {
            return growthRate;
        }

        public Map<String, Integer> getAssociatedTags() {
            return Collections.unmodifiableMap(associatedTags);
        }
        
        public String getTrend() {
            if (growthRate > 0.1) return "Rising";
            if (growthRate < -0.1) return "Declining";
            return "Stable";
        }
    }
    
    /**
     * Calculate tag co-occurrence metrics.
     * 
     * @param features List of features to analyze
     * @return List of tag co-occurrence relationships
     */
    public static List<CoOccurrence> calculateCoOccurrences(List<Feature> features) {
        Map<String, Integer> tagCounts = new HashMap<>();
        Map<String, Set<String>> scenariosByTag = new HashMap<>();
        Map<Set<String>, Integer> pairCoOccurrences = new HashMap<>();
        Set<String> allScenarioIds = new HashSet<>();
        
        // First pass: collect all tags and their individual occurrences
        int scenarioId = 0;
        for (Feature feature : features) {
            // Process each scenario
            for (Scenario scenario : feature.getScenarios()) {
                String uniqueId = feature.getFile() + "#" + scenarioId++;
                allScenarioIds.add(uniqueId);
                
                // Create a set with both feature and scenario tags
                Set<String> combinedTags = new HashSet<>(feature.getTags());
                combinedTags.addAll(scenario.getTags());
                
                // Count individual tags
                for (String tag : combinedTags) {
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                    
                    // Track which scenarios have this tag
                    if (!scenariosByTag.containsKey(tag)) {
                        scenariosByTag.put(tag, new HashSet<>());
                    }
                    scenariosByTag.get(tag).add(uniqueId);
                }
                
                // Count tag pairs (co-occurrences)
                List<String> tagList = new ArrayList<>(combinedTags);
                for (int i = 0; i < tagList.size(); i++) {
                    for (int j = i + 1; j < tagList.size(); j++) {
                        Set<String> pair = new HashSet<>();
                        pair.add(tagList.get(i));
                        pair.add(tagList.get(j));
                        
                        pairCoOccurrences.put(pair, pairCoOccurrences.getOrDefault(pair, 0) + 1);
                    }
                }
            }
        }
        
        // Calculate Jaccard coefficients for each pair
        List<CoOccurrence> coOccurrences = new ArrayList<>();
        for (Map.Entry<Set<String>, Integer> entry : pairCoOccurrences.entrySet()) {
            List<String> tags = new ArrayList<>(entry.getKey());
            if (tags.size() != 2) continue;
            
            String tag1 = tags.get(0);
            String tag2 = tags.get(1);
            int count = entry.getValue();
            
            Set<String> tag1Scenarios = scenariosByTag.getOrDefault(tag1, Collections.emptySet());
            Set<String> tag2Scenarios = scenariosByTag.getOrDefault(tag2, Collections.emptySet());
            
            // Calculate intersection and union sizes
            Set<String> intersection = new HashSet<>(tag1Scenarios);
            intersection.retainAll(tag2Scenarios);
            
            Set<String> union = new HashSet<>(tag1Scenarios);
            union.addAll(tag2Scenarios);
            
            // Calculate Jaccard coefficient (size of intersection / size of union)
            double coefficient = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
            
            coOccurrences.add(new CoOccurrence(tag1, tag2, count, coefficient));
        }
        
        // Sort by coefficient (descending)
        coOccurrences.sort(Comparator.comparing(CoOccurrence::getCoefficient).reversed());
        
        return coOccurrences;
    }
    
    /**
     * Calculate tag trend analysis.
     * 
     * @param features List of features to analyze
     * @param tagConcordance Map of tags and their counts
     * @return Map of tags to their trend analysis
     */
    public static Map<String, TagTrend> calculateTagTrends(List<Feature> features, Map<String, Integer> tagConcordance) {
        Map<String, TagTrend> trends = new HashMap<>();
        
        // Sort features by their modification time or name to establish a chronology
        // For this simplified version, we'll just use the order they appear in the list
        
        // Track tag occurrences over time (per feature)
        Map<String, List<Integer>> tagOccurrenceTimeline = new HashMap<>();
        
        // Track scenario and feature counts per tag
        Map<String, Set<Scenario>> tagScenarioCounts = new HashMap<>();
        Map<String, Set<Feature>> tagFeatureCounts = new HashMap<>();
        Map<String, Map<String, Integer>> tagAssociations = new HashMap<>();
        
        for (Feature feature : features) {
            // Initialize feature counts for this feature
            for (String tag : feature.getTags()) {
                if (!tagFeatureCounts.containsKey(tag)) {
                    tagFeatureCounts.put(tag, new HashSet<>());
                }
                tagFeatureCounts.get(tag).add(feature);
                
                // Track timeline
                if (!tagOccurrenceTimeline.containsKey(tag)) {
                    tagOccurrenceTimeline.put(tag, new ArrayList<>());
                }
                tagOccurrenceTimeline.get(tag).add(1);
                
                // Track associations
                for (String otherTag : feature.getTags()) {
                    if (!tag.equals(otherTag)) {
                        if (!tagAssociations.containsKey(tag)) {
                            tagAssociations.put(tag, new HashMap<>());
                        }
                        
                        tagAssociations.get(tag).put(otherTag, 
                            tagAssociations.get(tag).getOrDefault(otherTag, 0) + 1);
                    }
                }
            }
            
            // Process scenarios
            for (Scenario scenario : feature.getScenarios()) {
                // Track scenario counts
                for (String tag : scenario.getTags()) {
                    if (!tagScenarioCounts.containsKey(tag)) {
                        tagScenarioCounts.put(tag, new HashSet<>());
                    }
                    tagScenarioCounts.get(tag).add(scenario);
                    
                    // Track associations
                    for (String otherTag : scenario.getTags()) {
                        if (!tag.equals(otherTag)) {
                            if (!tagAssociations.containsKey(tag)) {
                                tagAssociations.put(tag, new HashMap<>());
                            }
                            
                            tagAssociations.get(tag).put(otherTag, 
                                tagAssociations.get(tag).getOrDefault(otherTag, 0) + 1);
                        }
                    }
                }
            }
            
            // Update timeline for all tags that didn't appear in this feature
            for (String tag : tagConcordance.keySet()) {
                if (!tagOccurrenceTimeline.containsKey(tag)) {
                    tagOccurrenceTimeline.put(tag, new ArrayList<>());
                }
                
                List<Integer> timeline = tagOccurrenceTimeline.get(tag);
                while (timeline.size() < features.indexOf(feature)) {
                    timeline.add(0);
                }
                
                if (!feature.getTags().contains(tag) && !feature.getScenarios().stream()
                    .anyMatch(s -> s.getTags().contains(tag))) {
                    timeline.add(0);
                }
            }
        }
        
        // Calculate trends for each tag
        for (String tag : tagConcordance.keySet()) {
            int totalCount = tagConcordance.get(tag);
            int scenarioCount = tagScenarioCounts.containsKey(tag) ? tagScenarioCounts.get(tag).size() : 0;
            int featureCount = tagFeatureCounts.containsKey(tag) ? tagFeatureCounts.get(tag).size() : 0;
            
            // Calculate growth rate based on timeline
            double growthRate = 0.0;
            List<Integer> timeline = tagOccurrenceTimeline.getOrDefault(tag, Collections.emptyList());
            
            if (timeline.size() >= 2) {
                // Calculate linear regression slope as a simple growth indicator
                double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
                int n = timeline.size();
                
                for (int i = 0; i < n; i++) {
                    sumX += i;
                    sumY += timeline.get(i);
                    sumXY += i * timeline.get(i);
                    sumX2 += i * i;
                }
                
                double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
                growthRate = slope;
            }
            
            // Get associated tags
            Map<String, Integer> associatedTags = tagAssociations.getOrDefault(tag, Collections.emptyMap());
            
            // Create trend object
            trends.put(tag, new TagTrend(tag, totalCount, scenarioCount, featureCount, growthRate, associatedTags));
        }
        
        return trends;
    }
    
    /**
     * Generate D3.js compatible JSON for visualizing tag relationships.
     * 
     * @param coOccurrences List of tag co-occurrences
     * @param tagConcordance Map of tags and their counts
     * @return JSON string for visualization
     */
    public static String generateVisualizationJson(List<CoOccurrence> coOccurrences, Map<String, Integer> tagConcordance) {
        // Create nodes (tags)
        Set<String> allTags = new HashSet<>(tagConcordance.keySet());
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"nodes\": [\n");
        
        // Add nodes (tags)
        List<String> tagList = new ArrayList<>(allTags);
        Collections.sort(tagList);
        
        for (int i = 0; i < tagList.size(); i++) {
            String tag = tagList.get(i);
            int count = tagConcordance.get(tag);
            
            json.append("    {\n");
            json.append("      \"id\": \"").append(escapeJson(tag)).append("\",\n");
            json.append("      \"group\": ").append(getTagGroup(tag)).append(",\n");
            json.append("      \"count\": ").append(count).append("\n");
            json.append("    }");
            
            if (i < tagList.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ],\n");
        json.append("  \"links\": [\n");
        
        // Add links (co-occurrences)
        int coOccurrenceCount = coOccurrences.size();
        int maxLinksToShow = Math.min(coOccurrenceCount, 100); // Limit to avoid overloading the visualization
        
        for (int i = 0; i < maxLinksToShow; i++) {
            CoOccurrence co = coOccurrences.get(i);
            
            json.append("    {\n");
            json.append("      \"source\": \"").append(escapeJson(co.getTag1())).append("\",\n");
            json.append("      \"target\": \"").append(escapeJson(co.getTag2())).append("\",\n");
            json.append("      \"value\": ").append(co.getCount()).append(",\n");
            json.append("      \"strength\": ").append(co.getCoefficient()).append("\n");
            json.append("    }");
            
            if (i < maxLinksToShow - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Get a group number for a tag based on its prefix or category.
     */
    private static int getTagGroup(String tag) {
        if (tag.matches("@P[0-9].*")) return 1; // Priority tags
        if (tag.matches("@(Critical|High|Medium|Low).*")) return 1;
        
        if (tag.matches("@(UI|API|Frontend|Backend).*")) return 2; // Component tags
        
        if (tag.matches("@(Smoke|Regression|E2E|Integration|Unit).*")) return 3; // Test type tags
        
        if (tag.matches("@(WIP|Ready|Review|Flaky|Deprecated).*")) return 4; // Status tags
        
        // Extract prefix for other tags
        String prefix = "Other";
        if (tag.startsWith("@")) {
            String tagWithoutAt = tag.substring(1);
            int prefixEnd = 0;
            
            while (prefixEnd < tagWithoutAt.length() && 
                   Character.isLetter(tagWithoutAt.charAt(prefixEnd))) {
                prefixEnd++;
            }
            
            if (prefixEnd > 0) {
                prefix = tagWithoutAt.substring(0, prefixEnd);
            }
        }
        
        // Hash the prefix to get a consistent group number
        return Math.abs(prefix.hashCode() % 5) + 5;
    }
    
    /**
     * Calculate the statistical significance of tag patterns.
     * 
     * @param features List of features to analyze
     * @param tagConcordance Map of tags and their counts
     * @return Map of tags to their significance scores
     */
    public static Map<String, Double> calculateTagSignificance(List<Feature> features, Map<String, Integer> tagConcordance) {
        Map<String, Double> significance = new HashMap<>();
        int totalFeatures = features.size();
        
        // Calculate TF-IDF like metric for each tag
        for (Map.Entry<String, Integer> entry : tagConcordance.entrySet()) {
            String tag = entry.getKey();
            int tagCount = entry.getValue();
            
            // Count how many features have this tag
            int featuresWithTag = (int) features.stream()
                .filter(f -> f.getTags().contains(tag) || 
                       f.getScenarios().stream().anyMatch(s -> s.getTags().contains(tag)))
                .count();
            
            // Calculate significance score
            double tf = (double) tagCount / totalFeatures; // Term frequency
            double idf = Math.log((double) totalFeatures / (featuresWithTag + 1)); // Inverse document frequency
            double score = tf * idf;
            
            significance.put(tag, score);
        }
        
        return significance;
    }
    
    /**
     * Escape special characters in JSON strings.
     */
    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}