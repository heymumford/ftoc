@InitialTest @P0
Feature: Testing FTOC utility using its own feature files

  Scenario: Validate initial FTOC behavior
    Given the ftoc utility is initialized
    When I run the utility on the "src/test/resources/ftoc/features" directory
    Then it should generate a table of contents
    And the output should contain a valid concordance summary
