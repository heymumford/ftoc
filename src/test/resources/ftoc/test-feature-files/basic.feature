@BasicFeature @MVP
Feature: Basic feature file structure
  As a QA engineer
  I want to test a basic feature file structure
  So that I can validate ftoc's basic functionality

  Background:
    Given I have a basic feature file
    And the feature file has a background

  @Smoke @P1
  Scenario: Simple scenario with basic elements
    When I execute the scenario
    Then the scenario should pass

  @Regression @P2
  Scenario: Another simple scenario with different tags
    When I execute the second scenario
    Then it should also pass

  @Debug @Smoke @Flaky
  Scenario: Scenario with low-value tags
    When I execute a scenario with low-value tags
    Then ftoc should recommend better tags