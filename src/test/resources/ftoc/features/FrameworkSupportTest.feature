@FrameworkSupport @P1
Feature: Framework-specific support in ftoc
  As a QA engineer using different testing frameworks
  I want ftoc to understand various framework syntaxes
  So that I can analyze all my feature files

  Background:
    Given the ftoc utility is initialized
    And test feature files are available in "src/test/resources/ftoc/test-feature-files"

  @StandardCucumber
  Scenario: Analyze standard Cucumber syntax
    When I run the utility on "src/test/resources/ftoc/test-feature-files/basic.feature"
    Then the utility should correctly parse standard Cucumber syntax
    And recognize all standard Gherkin keywords
    And extract all scenarios and steps correctly

  @KarateFramework
  Scenario: Analyze Karate framework syntax
    When I run the utility on "src/test/resources/ftoc/test-feature-files/karate_style.feature"
    Then the utility should correctly parse Karate syntax
    And recognize Karate-specific symbols like "*"
    And handle embedded JSON/JavaScript correctly
    And extract API-specific information

  @ComplexGherkin
  Scenario: Analyze complex Gherkin structures
    When I run the utility on "src/test/resources/ftoc/test-feature-files/complex_structure.feature"
    Then the utility should correctly parse rules and nested backgrounds
    And recognize all scenarios within each rule
    And maintain the hierarchical structure in the output
    And correctly report the total scenario count