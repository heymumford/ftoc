@TagAnalysis @P0
Feature: Tag analysis capabilities of ftoc
  As a QA engineer
  I want to analyze tags in feature files
  So that I can improve test organization and consistency

  Background:
    Given the ftoc utility is initialized
    And test feature files are available in "src/test/resources/ftoc/test-feature-files"

  @Concordance
  Scenario: Generate tag concordance across feature files
    When I run the utility on the "src/test/resources/ftoc/test-feature-files" directory
    Then the output should contain a valid concordance summary
    And the tag concordance should list all tags from the feature files
    And the tag count for "@P0" should be at least 5
    And the tag count for "@P1" should be at least 5

  @LowValueTags
  Scenario: Detect low-value generic tags
    When I run the utility with tag analysis on "src/test/resources/ftoc/test-feature-files/basic.feature"
    Then the output should report "@Debug" as a low-value tag
    And the output should report "@Flaky" as a low-value tag
    And the output should report "@test" as a low-value tag when analyzing "missing_tags.feature"

  @MissingTags
  Scenario: Detect missing required tags
    When I run the utility with tag validation on "src/test/resources/ftoc/test-feature-files/missing_tags.feature"
    Then the output should flag scenarios without priority tags
    And the output should flag scenarios without type tags
    And the output should flag scenario outlines without any tags

  @TagConsistency
  Scenario: Analyze tag consistency across features
    When I run the utility with consistency check on "src/test/resources/ftoc/test-feature-files"
    Then the output should report inconsistent usage of priority tags
    And the output should identify features with insufficient tagging
    And the output should suggest a standard tag taxonomy