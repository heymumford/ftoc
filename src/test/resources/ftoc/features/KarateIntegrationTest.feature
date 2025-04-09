@KarateIntegration @P0
Feature: Karate Integration with FTOC
  As a tester using Karate for API testing
  I want FTOC to support Karate-specific syntax
  So that I can analyze all my feature files including Karate files

  Background:
    Given the ftoc utility is initialized

  @P1
  Scenario: Detect and parse Karate-style feature files
    When I run the utility on the "src/test/resources/ftoc/test-feature-files/karate_style.feature" directory
    Then the output should identify it as a Karate-style feature file
    And the output should show Karate-specific metadata

  @P1
  Scenario: Include Karate-specific information in TOC 
    When I set output format to "text"
    And I run the utility on the "src/test/resources/ftoc/test-feature-files" directory
    Then the output should include Karate-specific information for Karate files
    And the output should not include Karate-specific information for non-Karate files

  @P1
  Scenario: Use Karate for CLI System Testing
    Given I have Karate tests for the FTOC CLI
    When I run the Karate tests
    Then they should validate the CLI behavior at the system level
    And report results in a JUnit-compatible format