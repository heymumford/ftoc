Feature: Tag Quality Issues Demonstration

  # This scenario has no tags at all - should be flagged for missing priority and type tags
  Scenario: Missing all tags
    Given I have no tags
    When I run a test
    Then I should be flagged by tag quality analyzer

  # This scenario has only a low-value tag - should still be flagged
  @test
  Scenario: Just a low-value tag
    Given I have only a low-value tag
    When I run a test
    Then I should be flagged for missing priority

  # This scenario has priority but no type tag
  @P1
  Scenario: Missing type tag
    Given I have a priority tag
    When I run a test
    Then I should be flagged for missing type tag

  # This scenario has type but no priority tag
  @UI
  Scenario: Missing priority tag
    Given I have a type tag
    When I run a test
    Then I should be flagged for missing priority tag

  # This scenario has a potential typo in tag
  @Regressionn
  Scenario: Tag with typo
    Given I have a tag with typo
    When I run a test
    Then I should be flagged for tag typo

  # This scenario has duplicate tags
  @P0 @UI @Smoke @P0
  Scenario: Duplicate tags
    Given I have duplicate tags
    When I run a test
    Then I should be flagged for duplicate tags

  # This scenario has excessive tags
  @P0 @UI @Smoke @Regression @API @Backend @Frontend @Integration @Flaky @Debug @Security
  Scenario: Excessive tags
    Given I have too many tags
    When I run a test
    Then I should be flagged for excessive tags