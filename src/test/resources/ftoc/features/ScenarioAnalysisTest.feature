@ScenarioAnalysis @P0
Feature: Scenario analysis capabilities of ftoc
  As a QA engineer
  I want to analyze scenario structure and quality
  So that I can improve test coverage and maintainability

  Background:
    Given the ftoc utility is initialized
    And test feature files are available in "src/test/resources/ftoc/test-feature-files"

  @TOC
  Scenario: Generate table of contents for scenarios
    When I run the utility on the "src/test/resources/ftoc/test-feature-files" directory
    Then the output should contain a structured table of contents
    And the TOC should list all scenarios and scenario outlines
    And the TOC should be organized by feature file
    And the TOC should include tags for each scenario

  @PairwiseCoverage
  Scenario: Analyze pairwise parameter coverage
    When I run the utility with pairwise analysis on "src/test/resources/ftoc/test-feature-files/scenario_outline.feature"
    Then the output should assess pairwise parameter coverage
    And the output should identify missing parameter combinations
    And the output should suggest additional examples for better coverage

  @PositiveNegativeBalance
  Scenario: Analyze positive vs negative test balance
    When I run the utility with test coverage analysis on "src/test/resources/ftoc/test-feature-files/positive_negative.feature"
    Then the output should report the positive to negative test ratio
    And the output should assess if the balance is appropriate
    And the output should suggest additional test cases if needed

  @ComplexityAnalysis
  Scenario: Analyze scenario complexity
    When I run the utility with complexity analysis on "src/test/resources/ftoc/test-feature-files/complex_structure.feature"
    Then the output should report scenario complexity metrics
    And the output should identify overly complex scenarios
    And the output should suggest refactoring for scenarios above complexity threshold