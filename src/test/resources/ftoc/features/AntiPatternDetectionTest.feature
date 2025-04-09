@AntiPatterns @P0
Feature: Testing anti-pattern detection functionality
  As a QA engineer
  I want to detect anti-patterns in my feature files
  So that I can improve the quality of my BDD scenarios

  Background:
    Given the ftoc utility is initialized
    And test feature files are available in "src/test/resources/ftoc/test-feature-files"

  @BasicAntiPatternDetection
  Scenario: Detecting basic anti-patterns in feature files
    When I enable anti-pattern detection
    And I run the utility with anti-pattern detection on "src/test/resources/ftoc/test-feature-files/anti_patterns.feature"
    Then an anti-pattern report should be generated
    And the anti-pattern report should detect long scenarios
    And the anti-pattern report should detect missing Given/When/Then steps
    And the anti-pattern report should detect UI-focused steps
    And the anti-pattern report should detect implementation details in steps

  @AntiPatternFormatting
  Scenario Outline: Anti-pattern reports in different formats
    When I enable anti-pattern detection
    And I set anti-pattern format to "<format>"
    And I run the utility with anti-pattern detection on "src/test/resources/ftoc/test-feature-files/anti_patterns.feature"
    Then an anti-pattern report should be generated
    And the anti-pattern report should be formatted as "<format>"

    Examples:
      | format   |
      | text     |
      | markdown |
      | html     |
      | json     |

  @ConfigurableAntiPatterns
  Scenario: Customizing anti-pattern detection with configuration
    When I set a custom configuration file path "src/test/resources/ftoc/test-configs/custom-anti-patterns.yml"
    And I enable anti-pattern detection
    And I run the utility with anti-pattern detection on "src/test/resources/ftoc/test-feature-files/anti_patterns.feature"
    Then an anti-pattern report should be generated
    And the anti-pattern report should respect the custom configuration settings
    And the anti-pattern report should not include disabled anti-pattern warnings

  @ComplexScenarioDetection
  Scenario: Detecting complexity issues in feature files
    When I enable anti-pattern detection
    And I run the utility with anti-pattern detection on "src/test/resources/ftoc/test-feature-files/complex_scenarios.feature"
    Then an anti-pattern report should be generated
    And the anti-pattern report should identify scenarios with too many steps
    And the anti-pattern report should identify scenarios with long names
    And the anti-pattern report should identify steps that are too long
    And the anti-pattern report should suggest refactoring for complex scenarios

  @ScenarioOutlineIssues
  Scenario: Detecting issues in scenario outlines
    When I enable anti-pattern detection
    And I run the utility with anti-pattern detection on "src/test/resources/ftoc/test-feature-files/scenario_outline.feature"
    Then an anti-pattern report should be generated
    And the anti-pattern report should identify scenario outlines with missing examples
    And the anti-pattern report should identify scenario outlines with too few examples