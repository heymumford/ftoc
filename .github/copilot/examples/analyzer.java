package com.heymumford.ftoc.analyzer;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example analyzer class for FTOC project.
 * This demonstrates the typical pattern for analyzers in the project.
 */
public class ExampleAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(ExampleAnalyzer.class);
    
    private final List<Feature> features;
    private final Map<String, Integer> tagConcordance;
    
    /**
     * Create a new analyzer with the specified features and tag concordance.
     * 
     * @param features The features to analyze
     * @param tagConcordance Map of tags to their occurrence counts
     */
    public ExampleAnalyzer(List<Feature> features, Map<String, Integer> tagConcordance) {
        this.features = new ArrayList<>(features);
        this.tagConcordance = new HashMap<>(tagConcordance);
        logger.debug("Created analyzer with {} features and {} distinct tags", features.size(), tagConcordance.size());
    }
    
    /**
     * Different types of issues that can be detected.
     */
    public enum IssueType {
        UNUSED_TAG("Unused tag"),
        OVERUSED_TAG("Overused tag"),
        INCONSISTENT_TAG("Inconsistent tag usage"),
        DUPLICATE_TAG("Duplicate tag"),
        MISSING_TAG("Missing tag");
        
        private final String description;
        
        IssueType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Represents an issue found during analysis.
     */
    public static class Issue {
        private final IssueType type;
        private final String message;
        private final String location;
        private final List<String> recommendations;
        
        public Issue(IssueType type, String message, String location, List<String> recommendations) {
            this.type = type;
            this.message = message;
            this.location = location;
            this.recommendations = new ArrayList<>(recommendations);
        }
        
        public IssueType getType() {
            return type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getLocation() {
            return location;
        }
        
        public List<String> getRecommendations() {
            return Collections.unmodifiableList(recommendations);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(type.getDescription()).append(": ").append(message);
            if (location != null && !location.isEmpty()) {
                sb.append(" (in ").append(location).append(")");
            }
            return sb.toString();
        }
    }
    
    /**
     * Analyze the features and tags to detect issues.
     * 
     * @return List of detected issues
     */
    public List<Issue> analyze() {
        logger.info("Starting analysis of {} features", features.size());
        List<Issue> issues = new ArrayList<>();
        
        issues.addAll(findUnusedTags());
        issues.addAll(findOverusedTags());
        issues.addAll(findInconsistentTags());
        issues.addAll(findDuplicateTags());
        issues.addAll(findMissingTags());
        
        logger.info("Analysis complete. Found {} issues", issues.size());
        return issues;
    }
    
    /**
     * Find tags that are defined but not used.
     */
    private List<Issue> findUnusedTags() {
        List<Issue> issues = new ArrayList<>();
        
        // Find tags with zero or one occurrence
        for (Map.Entry<String, Integer> entry : tagConcordance.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();
            
            if (count == 0) {
                issues.add(new Issue(
                    IssueType.UNUSED_TAG,
                    "Tag '" + tag + "' is defined but never used",
                    null,
                    Arrays.asList(
                        "Remove the unused tag definition",
                        "Use the tag in relevant scenarios"
                    )
                ));
            } else if (count == 1) {
                // Find where the tag is used
                String location = findTagLocation(tag);
                
                issues.add(new Issue(
                    IssueType.UNUSED_TAG,
                    "Tag '" + tag + "' is only used once",
                    location,
                    Arrays.asList(
                        "Consider if this tag provides value",
                        "If it's a typo, correct it",
                        "If intentional, use consistently across similar scenarios"
                    )
                ));
            }
        }
        
        return issues;
    }
    
    /**
     * Find tags that are used too frequently to be useful.
     */
    private List<Issue> findOverusedTags() {
        List<Issue> issues = new ArrayList<>();
        int totalScenarios = countScenarios();
        
        // Find tags used in more than 90% of scenarios
        for (Map.Entry<String, Integer> entry : tagConcordance.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();
            
            double percentage = (double) count / totalScenarios;
            if (percentage > 0.9 && totalScenarios > 10) {
                issues.add(new Issue(
                    IssueType.OVERUSED_TAG,
                    "Tag '" + tag + "' is used in " + Math.round(percentage * 100) + "% of scenarios",
                    null,
                    Arrays.asList(
                        "Overly common tags don't help discriminate between tests",
                        "Consider removing this tag or making it more specific",
                        "Reconsider your tagging strategy if you have many common tags"
                    )
                ));
            }
        }
        
        return issues;
    }
    
    /**
     * Find inconsistent tag usage patterns.
     */
    private List<Issue> findInconsistentTags() {
        // Implementation would look for patterns like inconsistent casing,
        // inconsistent formatting, etc.
        return new ArrayList<>();
    }
    
    /**
     * Find duplicate tags on the same scenario.
     */
    private List<Issue> findDuplicateTags() {
        List<Issue> issues = new ArrayList<>();
        
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
                issues.add(new Issue(
                    IssueType.DUPLICATE_TAG,
                    "Feature has duplicate tags: " + String.join(", ", featureDuplicates),
                    feature.getFilename(),
                    Arrays.asList(
                        "Remove duplicate tags",
                        "Ensure tags are only added once"
                    )
                ));
            }
            
            // Check for duplicates in scenario tags
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                Set<String> scenarioTags = new HashSet<>(featureTags);
                List<String> scenarioDuplicates = new ArrayList<>();
                
                for (String tag : scenario.getTags()) {
                    String lowerTag = tag.toLowerCase();
                    if (!scenarioTags.add(lowerTag)) {
                        // Check if it's duplicated at the scenario level or already at feature level
                        if (featureTags.contains(lowerTag)) {
                            scenarioDuplicates.add(tag + " (already on feature)");
                        } else {
                            scenarioDuplicates.add(tag);
                        }
                    }
                }
                
                if (!scenarioDuplicates.isEmpty()) {
                    issues.add(new Issue(
                        IssueType.DUPLICATE_TAG,
                        "Scenario has duplicate tags: " + String.join(", ", scenarioDuplicates),
                        feature.getFilename() + " - " + scenario.getName(),
                        Arrays.asList(
                            "Remove duplicate tags",
                            "Avoid repeating feature-level tags on scenarios",
                            "Tags at the feature level apply to all scenarios"
                        )
                    ));
                }
            }
        }
        
        return issues;
    }
    
    /**
     * Find scenarios missing important tags.
     */
    private List<Issue> findMissingTags() {
        List<Issue> issues = new ArrayList<>();
        
        // Example: Check for scenarios missing priority tags
        List<String> priorityTags = Arrays.asList("@p0", "@p1", "@p2", "@p3", "@critical", "@high", "@medium", "@low");
        
        for (Feature feature : features) {
            boolean featureHasPriorityTag = feature.getTags().stream()
                .anyMatch(tag -> priorityTags.contains(tag.toLowerCase()));
                
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                boolean scenarioHasPriorityTag = scenario.getTags().stream()
                    .anyMatch(tag -> priorityTags.contains(tag.toLowerCase()));
                    
                if (!featureHasPriorityTag && !scenarioHasPriorityTag) {
                    issues.add(new Issue(
                        IssueType.MISSING_TAG,
                        "Scenario is missing a priority tag",
                        feature.getFilename() + " - " + scenario.getName(),
                        Arrays.asList(
                            "Add a priority tag like @P0, @P1, @P2, or @P3",
                            "Or add a semantic priority tag like @Critical, @High, @Medium, or @Low",
                            "Apply priority tags consistently across all scenarios"
                        )
                    ));
                }
            }
        }
        
        return issues;
    }
    
    /**
     * Find the location where a tag is used.
     */
    private String findTagLocation(String tag) {
        for (Feature feature : features) {
            if (feature.getTags().contains(tag)) {
                return feature.getFilename();
            }
            
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.getTags().contains(tag)) {
                    return feature.getFilename() + " - " + scenario.getName();
                }
            }
        }
        
        return "Unknown location";
    }
    
    /**
     * Count the total number of scenarios.
     */
    private int countScenarios() {
        int count = 0;
        for (Feature feature : features) {
            count += feature.getScenarios().stream()
                .filter(scenario -> !scenario.isBackground())
                .count();
        }
        return count;
    }
    
    /**
     * Generate a report of the analysis results.
     * 
     * @param issues The issues to include in the report
     * @return Formatted report string
     */
    public String generateReport(List<Issue> issues) {
        if (issues.isEmpty()) {
            return "No issues detected.";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("ANALYSIS REPORT\n");
        report.append("==============\n\n");
        report.append("Found ").append(issues.size()).append(" potential issues.\n\n");
        
        // Group issues by type
        Map<IssueType, List<Issue>> issuesByType = issues.stream()
            .collect(Collectors.groupingBy(Issue::getType));
            
        // Generate summary section
        report.append("SUMMARY\n");
        report.append("-------\n");
        
        for (IssueType type : IssueType.values()) {
            List<Issue> typeIssues = issuesByType.getOrDefault(type, Collections.emptyList());
            if (!typeIssues.isEmpty()) {
                report.append(String.format("%-25s: %d\n", type.getDescription(), typeIssues.size()));
            }
        }
        
        report.append("\n");
        
        // Generate detailed section for each issue type
        for (Map.Entry<IssueType, List<Issue>> entry : issuesByType.entrySet()) {
            IssueType type = entry.getKey();
            List<Issue> typeIssues = entry.getValue();
            
            report.append(type.getDescription().toUpperCase()).append("\n");
            report.append("-".repeat(type.getDescription().length())).append("\n");
            
            // Include recommendations
            if (!typeIssues.isEmpty()) {
                report.append("Recommendations:\n");
                for (String recommendation : typeIssues.get(0).getRecommendations()) {
                    report.append("- ").append(recommendation).append("\n");
                }
                report.append("\n");
            }
            
            // List all instances of this issue type
            for (Issue issue : typeIssues) {
                report.append("- ").append(issue.getMessage());
                if (issue.getLocation() != null && !issue.getLocation().isEmpty()) {
                    report.append(" (in ").append(issue.getLocation()).append(")");
                }
                report.append("\n");
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
}