@Configuration @P0
Feature: Warning configuration file support
  As a QA engineer
  I want to customize warning settings via configuration files
  So that I can tailor the tool to my team's specific needs and standards

  Background:
    Given the ftoc utility is initialized
    And test feature files are available in "src/test/resources/ftoc/test-feature-files"

  @ConfigDefaults
  Scenario: Using default configuration values
    When I enable tag quality analysis
    And I run the utility with tag quality analysis on "src/test/resources/ftoc/test-feature-files/tag_issues.feature" directory
    Then a tag quality report should be generated
    And the tag quality report should detect missing priority tags
    And the tag quality report should detect excessive tags
    And the tag quality report should detect tag typos

  @CustomConfigFile
  Scenario: Using a custom configuration file
    When I set a custom configuration file path "src/test/resources/ftoc/test-configs/custom-warnings.yml"
    And I enable tag quality analysis
    And I run the utility with tag quality analysis on "src/test/resources/ftoc/test-feature-files/tag_issues.feature" directory
    Then a tag quality report should be generated
    And the tag quality report should reflect the custom configuration settings
    And the tag quality report should respect enabled/disabled warnings
    And the tag quality report should apply custom thresholds

  @DisableWarnings
  Scenario: Disabling specific warnings in configuration
    When I set a custom configuration file path "src/test/resources/ftoc/test-configs/disabled-warnings.yml"
    And I enable tag quality analysis
    And I run the utility with tag quality analysis on "src/test/resources/ftoc/test-feature-files/tag_issues.feature" directory
    Then a tag quality report should be generated
    And the tag quality report should not contain disabled warnings
    And the tag quality report should still contain enabled warnings

  @CustomThresholds
  Scenario: Using custom thresholds in configuration
    When I set a custom configuration file path "src/test/resources/ftoc/test-configs/custom-thresholds.yml"
    And I enable tag quality analysis
    And I run the utility with tag quality analysis on "src/test/resources/ftoc/test-feature-files/tag_issues.feature" directory
    Then a tag quality report should be generated
    And the tag quality report should apply the custom max tags threshold
    And the tag quality report should apply the custom scenario name length threshold

  @CustomTagLists
  Scenario: Using custom tag lists in configuration
    When I set a custom configuration file path "src/test/resources/ftoc/test-configs/custom-tags.yml"
    And I enable tag quality analysis
    And I run the utility with tag quality analysis on "src/test/resources/ftoc/test-feature-files/tag_issues.feature" directory
    Then a tag quality report should be generated
    And the tag quality report should use the custom priority tag list
    And the tag quality report should use the custom type tag list
    And the tag quality report should use the custom low-value tag list

  @ShowConfig
  Scenario: Displaying the current configuration
    When I set a custom configuration file path "src/test/resources/ftoc/test-configs/custom-warnings.yml"
    And I display the configuration summary
    Then the configuration summary should show the correct settings
    And the configuration summary should include the loaded file path
    And the configuration summary should list all warning types and their status