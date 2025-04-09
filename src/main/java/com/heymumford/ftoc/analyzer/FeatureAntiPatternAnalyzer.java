package com.heymumford.ftoc.analyzer;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Analyzes feature files for common Cucumber/Gherkin anti-patterns.
 * This helps teams identify issues in their BDD scenarios that might make
 * them harder to maintain, understand, or execute.
 */
public class FeatureAntiPatternAnalyzer {

    // Default patterns and constants (can be overridden by configuration)
    private static final int DEFAULT_MAX_RECOMMENDED_STEPS = 10;
    private static final int DEFAULT_MIN_RECOMMENDED_STEPS = 2;
    private static final int DEFAULT_MIN_EXAMPLES_RECOMMENDED = 2;
    private static final int DEFAULT_MAX_SCENARIO_NAME_LENGTH = 100;
    private static final int DEFAULT_MAX_STEP_TEXT_LENGTH = 120;
    
    // Actual values used (may be overridden by configuration)
    private int MAX_RECOMMENDED_STEPS = DEFAULT_MAX_RECOMMENDED_STEPS;
    private int MIN_RECOMMENDED_STEPS = DEFAULT_MIN_RECOMMENDED_STEPS;
    private int MIN_EXAMPLES_RECOMMENDED = DEFAULT_MIN_EXAMPLES_RECOMMENDED;
    private int MAX_SCENARIO_NAME_LENGTH = DEFAULT_MAX_SCENARIO_NAME_LENGTH;
    private int MAX_STEP_TEXT_LENGTH = DEFAULT_MAX_STEP_TEXT_LENGTH;
    
    // UI action patterns
    private static final List<Pattern> UI_ACTION_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)click(s|ed|ing)?\\s+(on\\s+)?the\\s+"),
        Pattern.compile("(?i)select(s|ed|ing)?\\s+(from\\s+)?the\\s+"),
        Pattern.compile("(?i)enter(s|ed|ing)?\\s+.+\\s+into\\s+the\\s+"),
        Pattern.compile("(?i)type(s|ed|ing)?\\s+.+\\s+into\\s+the\\s+"),
        Pattern.compile("(?i)navigate(s|ed|ing)?\\s+to\\s+"),
        Pattern.compile("(?i)scroll(s|ed|ing)?\\s+(down|up|to)\\s+"),
        Pattern.compile("(?i)hover(s|ed|ing)?\\s+over\\s+the\\s+"),
        Pattern.compile("(?i)drag(s|ed|ing)?\\s+.+\\s+to\\s+"),
        Pattern.compile("(?i)check(s|ed|ing)?\\s+the\\s+checkbox"),
        Pattern.compile("(?i)upload(s|ed|ing)?\\s+file")
    );
    
    // Technical implementation details that shouldn't be in scenarios
    private static final List<Pattern> IMPLEMENTATION_DETAIL_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)\\bjs\\b|javascript"),
        Pattern.compile("(?i)\\bcss\\b|stylesheet"),
        Pattern.compile("(?i)\\bapi\\s+endpoint"),
        Pattern.compile("(?i)\\bhttp\\b|\\burl\\b|\\buri\\b"),
        Pattern.compile("(?i)\\bdatabase\\b|\\bsql\\b|\\bquery\\b"),
        Pattern.compile("(?i)\\belement\\s+id\\b|\\bxpath\\b|\\bcss\\s+selector\\b"),
        Pattern.compile("(?i)\\bwait\\s+for\\b|\\btimeout\\b|\\bdelay\\b")
    );
    
    // Given/When/Then step keywords
    private static final Pattern GIVEN_PATTERN = Pattern.compile("^\\s*Given\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHEN_PATTERN = Pattern.compile("^\\s*When\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern THEN_PATTERN = Pattern.compile("^\\s*Then\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern AND_PATTERN = Pattern.compile("^\\s*And\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern BUT_PATTERN = Pattern.compile("^\\s*But\\s+", Pattern.CASE_INSENSITIVE);
    
    private final List<Feature> features;
    private com.heymumford.ftoc.config.WarningConfiguration config;
    
    /**
     * Types of anti-pattern warnings that can be detected.
     */
    public enum WarningType {
        LONG_SCENARIO("Long scenario"),
        TOO_FEW_STEPS("Too few steps"),
        MISSING_GIVEN("Missing Given step"),
        MISSING_WHEN("Missing When step"),
        MISSING_THEN("Missing Then step"),
        UI_FOCUSED_STEP("UI-focused step"),
        IMPLEMENTATION_DETAIL("Implementation detail in step"),
        MISSING_EXAMPLES("Missing examples in Scenario Outline"),
        TOO_FEW_EXAMPLES("Too few examples in Scenario Outline"),
        LONG_SCENARIO_NAME("Long scenario name"),
        LONG_STEP_TEXT("Long step text"),
        INCORRECT_STEP_ORDER("Incorrect step order"),
        AMBIGUOUS_PRONOUN("Ambiguous pronoun in step"),
        INCONSISTENT_TENSE("Inconsistent tense in steps"),
        CONJUNCTION_IN_STEP("Conjunction in step");
        
        private final String description;
        
        WarningType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Warning message for an anti-pattern issue.
     */
    public static class Warning {
        private final WarningType type;
        private final String message;
        private final String location;
        private final List<String> remediation;
        
        public Warning(WarningType type, String message, String location, List<String> remediation) {
            this.type = type;
            this.message = message;
            this.location = location;
            this.remediation = remediation;
        }
        
        public WarningType getType() {
            return type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getLocation() {
            return location;
        }
        
        public List<String> getRemediation() {
            return remediation;
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
     * Create a new feature anti-pattern analyzer with default configuration.
     * 
     * @param features List of features to analyze
     */
    public FeatureAntiPatternAnalyzer(List<Feature> features) {
        this(features, new com.heymumford.ftoc.config.WarningConfiguration());
    }
    
    /**
     * Create a new feature anti-pattern analyzer with specific configuration.
     * 
     * @param features List of features to analyze
     * @param config The warning configuration to use
     */
    public FeatureAntiPatternAnalyzer(List<Feature> features, 
                                     com.heymumford.ftoc.config.WarningConfiguration config) {
        this.features = new ArrayList<>(features);
        this.config = config;
        
        // Apply thresholds from configuration
        this.MAX_RECOMMENDED_STEPS = config.getIntThreshold("maxSteps", DEFAULT_MAX_RECOMMENDED_STEPS);
        this.MIN_RECOMMENDED_STEPS = config.getIntThreshold("minSteps", DEFAULT_MIN_RECOMMENDED_STEPS);
        this.MIN_EXAMPLES_RECOMMENDED = config.getIntThreshold("minExamples", DEFAULT_MIN_EXAMPLES_RECOMMENDED);
        this.MAX_SCENARIO_NAME_LENGTH = config.getIntThreshold("maxScenarioNameLength", DEFAULT_MAX_SCENARIO_NAME_LENGTH);
        this.MAX_STEP_TEXT_LENGTH = config.getIntThreshold("maxStepLength", DEFAULT_MAX_STEP_TEXT_LENGTH);
    }
    
    /**
     * Perform a comprehensive analysis and generate warnings.
     * 
     * @return List of all warnings found during analysis
     */
    public List<Warning> analyzeAntiPatterns() {
        List<Warning> allWarnings = new ArrayList<>();
        
        // Run all analysis methods only if they're enabled in the configuration
        if (config.isWarningEnabled(WarningType.LONG_SCENARIO.name())) {
            allWarnings.addAll(detectLongScenarios());
        }
        
        if (config.isWarningEnabled(WarningType.TOO_FEW_STEPS.name())) {
            allWarnings.addAll(detectTooFewSteps());
        }
        
        if (config.isWarningEnabled(WarningType.MISSING_GIVEN.name()) || 
            config.isWarningEnabled(WarningType.MISSING_WHEN.name()) ||
            config.isWarningEnabled(WarningType.MISSING_THEN.name())) {
            allWarnings.addAll(detectMissingGivenWhenThen());
        }
        
        if (config.isWarningEnabled(WarningType.UI_FOCUSED_STEP.name())) {
            allWarnings.addAll(detectUiFocusedSteps());
        }
        
        if (config.isWarningEnabled(WarningType.IMPLEMENTATION_DETAIL.name())) {
            allWarnings.addAll(detectImplementationDetails());
        }
        
        if (config.isWarningEnabled(WarningType.MISSING_EXAMPLES.name()) ||
            config.isWarningEnabled(WarningType.TOO_FEW_EXAMPLES.name())) {
            allWarnings.addAll(detectScenarioOutlineIssues());
        }
        
        if (config.isWarningEnabled(WarningType.LONG_SCENARIO_NAME.name()) ||
            config.isWarningEnabled(WarningType.LONG_STEP_TEXT.name())) {
            allWarnings.addAll(detectNamingIssues());
        }
        
        if (config.isWarningEnabled(WarningType.INCORRECT_STEP_ORDER.name())) {
            allWarnings.addAll(detectStepOrderIssues());
        }
        
        if (config.isWarningEnabled(WarningType.AMBIGUOUS_PRONOUN.name()) ||
            config.isWarningEnabled(WarningType.INCONSISTENT_TENSE.name()) ||
            config.isWarningEnabled(WarningType.CONJUNCTION_IN_STEP.name())) {
            allWarnings.addAll(detectAmbiguousLanguage());
        }
        
        // Filter out any disabled warnings
        allWarnings = allWarnings.stream()
                .filter(warning -> config.isWarningEnabled(warning.getType().name()))
                .collect(Collectors.toList());
        
        return allWarnings;
    }
    
    /**
     * Detect scenarios that are too long (too many steps).
     */
    private List<Warning> detectLongScenarios() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                // Skip backgrounds
                if (scenario.isBackground()) {
                    continue;
                }
                
                int stepCount = scenario.getSteps().size();
                
                if (stepCount > MAX_RECOMMENDED_STEPS) {
                    String location = feature.getFilename() + " - " + scenario.getName();
                    List<String> remediation = Arrays.asList(
                        "Break the scenario into multiple smaller, focused scenarios",
                        "Consider using a Background for shared setup steps",
                        "Use higher-level steps that encapsulate multiple actions",
                        "Focus each scenario on testing a single behavior or rule"
                    );
                    
                    warnings.add(new Warning(
                        WarningType.LONG_SCENARIO,
                        "Scenario has " + stepCount + " steps (recommended maximum: " + MAX_RECOMMENDED_STEPS + ")",
                        location,
                        remediation
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect scenarios with too few steps (potentially incomplete).
     */
    private List<Warning> detectTooFewSteps() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                // Skip backgrounds
                if (scenario.isBackground()) {
                    continue;
                }
                
                int stepCount = scenario.getSteps().size();
                
                if (stepCount < MIN_RECOMMENDED_STEPS) {
                    String location = feature.getFilename() + " - " + scenario.getName();
                    List<String> remediation = Arrays.asList(
                        "A complete scenario typically needs at least setup (Given) and verification (Then) steps",
                        "Consider if this is a valid standalone scenario or should be combined with another"
                    );
                    
                    warnings.add(new Warning(
                        WarningType.TOO_FEW_STEPS,
                        "Scenario has only " + stepCount + " step(s)",
                        location,
                        remediation
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect scenarios that are missing Given, When, or Then steps.
     */
    private List<Warning> detectMissingGivenWhenThen() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                // Skip backgrounds (they typically only have Given steps)
                if (scenario.isBackground()) {
                    continue;
                }
                
                boolean hasGiven = false;
                boolean hasWhen = false;
                boolean hasThen = false;
                
                // Check for the presence of each step type
                for (String step : scenario.getSteps()) {
                    if (GIVEN_PATTERN.matcher(step).find()) {
                        hasGiven = true;
                    } else if (WHEN_PATTERN.matcher(step).find()) {
                        hasWhen = true;
                    } else if (THEN_PATTERN.matcher(step).find()) {
                        hasThen = true;
                    } else if (AND_PATTERN.matcher(step).find() || BUT_PATTERN.matcher(step).find()) {
                        // And/But steps inherit their type from the previous step
                        // Skip them for detection since we can't determine their type without context
                    }
                }
                
                String location = feature.getFilename() + " - " + scenario.getName();
                
                // Report missing step types
                if (!hasGiven) {
                    List<String> remediation = Arrays.asList(
                        "Add a Given step to establish the initial context/state",
                        "Every scenario should describe the starting state with Given steps"
                    );
                    
                    warnings.add(new Warning(
                        WarningType.MISSING_GIVEN,
                        "Scenario is missing a Given step",
                        location,
                        remediation
                    ));
                }
                
                if (!hasWhen) {
                    List<String> remediation = Arrays.asList(
                        "Add a When step to describe the action being tested",
                        "When steps represent the action or event that triggers the scenario"
                    );
                    
                    warnings.add(new Warning(
                        WarningType.MISSING_WHEN,
                        "Scenario is missing a When step",
                        location,
                        remediation
                    ));
                }
                
                if (!hasThen) {
                    List<String> remediation = Arrays.asList(
                        "Add a Then step to verify the expected outcome",
                        "Then steps assert the expected results after the action"
                    );
                    
                    warnings.add(new Warning(
                        WarningType.MISSING_THEN,
                        "Scenario is missing a Then step",
                        location,
                        remediation
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect steps that focus on UI actions rather than business behaviors.
     */
    private List<Warning> detectUiFocusedSteps() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                String location = feature.getFilename() + " - " + scenario.getName();
                
                // Check each step for UI-focused language
                for (String step : scenario.getSteps()) {
                    for (Pattern pattern : UI_ACTION_PATTERNS) {
                        Matcher matcher = pattern.matcher(step);
                        if (matcher.find()) {
                            List<String> remediation = Arrays.asList(
                                "Focus on the business behavior rather than UI implementation",
                                "Replace UI actions with higher-level business actions",
                                "Example: Instead of \"When I click the Submit button\", use \"When I submit the form\"",
                                "UI details belong in step definitions, not in the Gherkin"
                            );
                            
                            warnings.add(new Warning(
                                WarningType.UI_FOCUSED_STEP,
                                "Step contains UI-focused language: \"" + step + "\"",
                                location,
                                remediation
                            ));
                            
                            // Only report one UI issue per step
                            break;
                        }
                    }
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect implementation details in steps that should be abstracted away.
     */
    private List<Warning> detectImplementationDetails() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                String location = feature.getFilename() + " - " + scenario.getName();
                
                // Check each step for implementation details
                for (String step : scenario.getSteps()) {
                    for (Pattern pattern : IMPLEMENTATION_DETAIL_PATTERNS) {
                        Matcher matcher = pattern.matcher(step);
                        if (matcher.find()) {
                            List<String> remediation = Arrays.asList(
                                "Remove technical implementation details from scenario steps",
                                "Focus on business behavior and outcomes, not technical details",
                                "Technical details belong in step definitions, not in the Gherkin",
                                "Example: Instead of \"When the API returns 200 OK\", use \"When the operation succeeds\""
                            );
                            
                            warnings.add(new Warning(
                                WarningType.IMPLEMENTATION_DETAIL,
                                "Step contains technical implementation details: \"" + step + "\"",
                                location,
                                remediation
                            ));
                            
                            // Only report one implementation issue per step
                            break;
                        }
                    }
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect issues with Scenario Outlines like missing or insufficient examples.
     */
    private List<Warning> detectScenarioOutlineIssues() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                if (!scenario.isOutline()) {
                    continue;
                }
                
                String location = feature.getFilename() + " - " + scenario.getName();
                
                // Check for missing examples
                if (scenario.getExamples().isEmpty()) {
                    List<String> remediation = Arrays.asList(
                        "Add at least one Examples table to the Scenario Outline",
                        "Scenario Outlines require examples to generate test cases"
                    );
                    
                    warnings.add(new Warning(
                        WarningType.MISSING_EXAMPLES,
                        "Scenario Outline has no Examples tables",
                        location,
                        remediation
                    ));
                } else {
                    // Check for examples with too few rows
                    for (Scenario.Example example : scenario.getExamples()) {
                        int rowCount = example.getRows().size();
                        if (rowCount < MIN_EXAMPLES_RECOMMENDED) {
                            List<String> remediation = Arrays.asList(
                                "Add more example rows to better test the scenario variations",
                                "Include both positive and negative test cases",
                                "Consider boundary values and edge cases"
                            );
                            
                            warnings.add(new Warning(
                                WarningType.TOO_FEW_EXAMPLES,
                                "Examples table '" + (example.getName() != null && !example.getName().isEmpty() ? example.getName() : "unnamed") + 
                                "' has only " + rowCount + " row(s) (recommended minimum: " + MIN_EXAMPLES_RECOMMENDED + ")",
                                location,
                                remediation
                            ));
                        }
                    }
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect naming issues like long scenario names or step text.
     */
    private List<Warning> detectNamingIssues() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                String location = feature.getFilename() + " - " + scenario.getName();
                
                // Check for long scenario names
                if (scenario.getName().length() > MAX_SCENARIO_NAME_LENGTH) {
                    List<String> remediation = Arrays.asList(
                        "Shorten the scenario name to be more concise",
                        "Focus on the key behavior being tested",
                        "Move details to the steps rather than the title"
                    );
                    
                    warnings.add(new Warning(
                        WarningType.LONG_SCENARIO_NAME,
                        "Scenario name is " + scenario.getName().length() + " characters long (recommended maximum: " + MAX_SCENARIO_NAME_LENGTH + ")",
                        location,
                        remediation
                    ));
                }
                
                // Check for long step text
                for (String step : scenario.getSteps()) {
                    if (step.length() > MAX_STEP_TEXT_LENGTH) {
                        List<String> remediation = Arrays.asList(
                            "Shorten the step text to be more concise",
                            "Break into multiple smaller steps if necessary",
                            "Move complex data to examples, DocString, or DataTable"
                        );
                        
                        warnings.add(new Warning(
                            WarningType.LONG_STEP_TEXT,
                            "Step text is " + step.length() + " characters long (recommended maximum: " + MAX_STEP_TEXT_LENGTH + "): \"" + 
                            (step.length() > 50 ? step.substring(0, 47) + "..." : step) + "\"",
                            location,
                            remediation
                        ));
                    }
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect issues with step ordering (Given/When/Then in wrong sequence).
     */
    private List<Warning> detectStepOrderIssues() {
        List<Warning> warnings = new ArrayList<>();
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                String location = feature.getFilename() + " - " + scenario.getName();
                
                // Track the current step type to detect improper sequences
                String currentStepType = null;
                List<String> stepSequenceIssues = new ArrayList<>();
                
                for (String step : scenario.getSteps()) {
                    String stepType = null;
                    
                    // Determine the step type
                    if (GIVEN_PATTERN.matcher(step).find()) {
                        stepType = "Given";
                    } else if (WHEN_PATTERN.matcher(step).find()) {
                        stepType = "When";
                    } else if (THEN_PATTERN.matcher(step).find()) {
                        stepType = "Then";
                    } else if (AND_PATTERN.matcher(step).find() || BUT_PATTERN.matcher(step).find()) {
                        // And/But steps inherit their type from the previous step
                        stepType = currentStepType;
                    }
                    
                    // Check for improper sequence
                    if (stepType != null) {
                        if ("When".equals(stepType) && "Then".equals(currentStepType)) {
                            stepSequenceIssues.add("When step after Then step: \"" + step + "\"");
                        } else if ("Given".equals(stepType) && ("When".equals(currentStepType) || "Then".equals(currentStepType))) {
                            stepSequenceIssues.add("Given step after " + currentStepType + " step: \"" + step + "\"");
                        }
                        
                        currentStepType = stepType;
                    }
                }
                
                // Report any sequence issues
                if (!stepSequenceIssues.isEmpty()) {
                    List<String> remediation = Arrays.asList(
                        "Follow the Given-When-Then sequence (setup, action, verification)",
                        "Given steps should come before When steps",
                        "When steps should come before Then steps",
                        "Consider restructuring the scenario if the current flow doesn't fit the pattern"
                    );
                    
                    for (String issue : stepSequenceIssues) {
                        warnings.add(new Warning(
                            WarningType.INCORRECT_STEP_ORDER,
                            issue,
                            location,
                            remediation
                        ));
                    }
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Detect ambiguous language in steps like pronouns or inconsistent tense.
     */
    private List<Warning> detectAmbiguousLanguage() {
        List<Warning> warnings = new ArrayList<>();
        
        // Pattern for ambiguous pronouns
        Pattern pronounPattern = Pattern.compile("\\b(it|they|them|this|that|these|those)\\b", Pattern.CASE_INSENSITIVE);
        
        // Patterns for detecting step tense
        Pattern presentTensePattern = Pattern.compile("\\b(I|user|we)\\s+(am|is|are|do|does|have|has|click|select|enter|navigate|see|view)\\b", Pattern.CASE_INSENSITIVE);
        Pattern pastTensePattern = Pattern.compile("\\b(I|user|we)\\s+(was|were|did|had|clicked|selected|entered|navigated|saw|viewed)\\b", Pattern.CASE_INSENSITIVE);
        
        // Pattern for detecting conjunctions in steps
        Pattern conjunctionPattern = Pattern.compile("\\b(and|or|but)\\b", Pattern.CASE_INSENSITIVE);
        
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                if (scenario.isBackground()) {
                    continue;
                }
                
                String location = feature.getFilename() + " - " + scenario.getName();
                boolean hasPresentTense = false;
                boolean hasPastTense = false;
                
                // Check each step for issues
                for (String step : scenario.getSteps()) {
                    // 1. Check for ambiguous pronouns
                    Matcher pronounMatcher = pronounPattern.matcher(step);
                    if (pronounMatcher.find()) {
                        List<String> remediation = Arrays.asList(
                            "Use specific nouns instead of pronouns for clarity",
                            "Pronouns can be ambiguous, especially in complex scenarios",
                            "Example: Instead of \"When I click it\", use \"When I click the button\""
                        );
                        
                        warnings.add(new Warning(
                            WarningType.AMBIGUOUS_PRONOUN,
                            "Step contains ambiguous pronoun '" + pronounMatcher.group() + "': \"" + step + "\"",
                            location,
                            remediation
                        ));
                    }
                    
                    // 2. Track tense usage
                    if (presentTensePattern.matcher(step).find()) {
                        hasPresentTense = true;
                    }
                    if (pastTensePattern.matcher(step).find()) {
                        hasPastTense = true;
                    }
                    
                    // 3. Check for conjunctions
                    Matcher conjunctionMatcher = conjunctionPattern.matcher(step);
                    if (conjunctionMatcher.find() && !AND_PATTERN.matcher(step).find() && !BUT_PATTERN.matcher(step).find()) {
                        List<String> remediation = Arrays.asList(
                            "Split steps with conjunctions into separate steps",
                            "Each step should test a single action or assertion",
                            "Example: Instead of \"When I login and navigate to dashboard\", use two separate steps"
                        );
                        
                        warnings.add(new Warning(
                            WarningType.CONJUNCTION_IN_STEP,
                            "Step contains conjunction '" + conjunctionMatcher.group() + "' suggesting it should be split: \"" + step + "\"",
                            location,
                            remediation
                        ));
                    }
                }
                
                // Report inconsistent tense if both present and past tense are used
                if (hasPresentTense && hasPastTense) {
                    List<String> remediation = Arrays.asList(
                        "Standardize on a single tense throughout the scenario",
                        "Present tense is generally preferred (\"I click\" rather than \"I clicked\")",
                        "Consistent tense makes scenarios easier to read and understand"
                    );
                    
                    warnings.add(new Warning(
                        WarningType.INCONSISTENT_TENSE,
                        "Scenario uses a mix of present and past tense",
                        location,
                        remediation
                    ));
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Generate a formatted report of all warnings.
     * 
     * @param warnings List of warnings to include in the report
     * @return Formatted string with warning details
     */
    public String generateWarningReport(List<Warning> warnings) {
        if (warnings.isEmpty()) {
            return "No anti-pattern issues detected.";
        }
        
        // Group warnings by type
        Map<WarningType, List<Warning>> warningsByType = new HashMap<>();
        for (Warning warning : warnings) {
            warningsByType.computeIfAbsent(warning.getType(), k -> new ArrayList<>()).add(warning);
        }
        
        StringBuilder report = new StringBuilder();
        report.append("FEATURE ANTI-PATTERN WARNINGS\n");
        report.append("============================\n\n");
        report.append("Found ").append(warnings.size()).append(" potential anti-pattern issues.\n\n");
        
        // Generate summary section
        report.append("SUMMARY\n");
        report.append("-------\n");
        
        for (WarningType type : WarningType.values()) {
            List<Warning> typeWarnings = warningsByType.getOrDefault(type, Collections.emptyList());
            if (!typeWarnings.isEmpty()) {
                report.append(String.format("%-25s: %d\n", type.getDescription(), typeWarnings.size()));
            }
        }
        
        report.append("\n");
        
        // Generate detailed section for each warning type
        for (Map.Entry<WarningType, List<Warning>> entry : warningsByType.entrySet()) {
            WarningType type = entry.getKey();
            List<Warning> typeWarnings = entry.getValue();
            
            report.append(type.getDescription().toUpperCase()).append("\n");
            report.append(String.join("", Collections.nCopies(type.getDescription().length(), "-"))).append("\n");
            
            // Include a general remediation suggestion for this warning type
            if (!typeWarnings.isEmpty()) {
                report.append("Remediation:\n");
                for (String remedy : typeWarnings.get(0).getRemediation()) {
                    report.append("- ").append(remedy).append("\n");
                }
                report.append("\n");
            }
            
            // List all instances of this warning type
            for (Warning warning : typeWarnings) {
                report.append("- ").append(warning.getMessage());
                if (warning.getLocation() != null && !warning.getLocation().isEmpty()) {
                    report.append(" (in ").append(warning.getLocation()).append(")");
                }
                report.append("\n");
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
}