@JunitReport @P0
Feature: JUnit XML Report Generation

  @P1
  Scenario: Generate tag quality report in JUnit XML format
    Given the ftoc utility is initialized
    When I enable tag quality analysis
    And I set tag quality format to "junit"
    And I run the utility with tag quality analysis on "src/test/resources/ftoc/test-feature-files" directory
    Then a tag quality report should be generated
    And the report should be in JUnit XML format

  @P1
  Scenario: Generate anti-pattern report in JUnit XML format
    Given the ftoc utility is initialized
    When I detect anti-patterns
    And I set anti-pattern format to "junit"
    And I run the utility on the "src/test/resources/ftoc/test-feature-files" directory
    Then an anti-pattern report should be generated
    And the report should be in JUnit XML format

  @P1
  Scenario: Generate all reports in JUnit XML format using shorthand
    Given the ftoc utility is initialized
    When I enable tag quality analysis
    And I detect anti-patterns
    And I set JUnit output format for all reports
    And I run the utility on the "src/test/resources/ftoc/test-feature-files" directory
    Then all reports should be in JUnit XML format

  @P1
  Scenario: Use command line for JUnit reports
    Given the ftoc utility is initialized
    When I run the utility with parameters "--junit-report --analyze-tags --detect-anti-patterns -d src/test/resources/ftoc/test-feature-files"
    Then all reports should be in JUnit XML format