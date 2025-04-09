@OutputFormatting @P1
Feature: Output formatting options in ftoc
  As a QA engineer
  I want different output formats from ftoc
  So that I can integrate the results into various documentation systems

  Background:
    Given the ftoc utility is initialized
    And test feature files are available in "src/test/resources/ftoc/test-feature-files"

  @TextOutput
  Scenario: Generate plain text output
    When I run the utility with output format "text" on "src/test/resources/ftoc/test-feature-files"
    Then the output should be formatted as plain text
    And the plain text should be properly indented
    And the plain text should include all scenarios

  @MarkdownOutput
  Scenario: Generate markdown output
    When I run the utility with output format "markdown" on "src/test/resources/ftoc/test-feature-files"
    Then the output should be formatted as markdown
    And the markdown should use proper headings
    And the markdown should include tables for examples
    And the markdown should format tags correctly

  @HTMLOutput
  Scenario: Generate HTML output
    When I run the utility with output format "html" on "src/test/resources/ftoc/test-feature-files"
    Then the output should be formatted as HTML
    And the HTML should include proper styling
    And the HTML should have a navigable structure
    And the HTML should include a tag filter

  @JSONOutput
  Scenario: Generate JSON output for integration
    When I run the utility with output format "json" on "src/test/resources/ftoc/test-feature-files"
    Then the output should be valid JSON
    And the JSON should include all feature information
    And the JSON should include all scenarios and tags
    And the JSON structure should be consistent
    
  @UnifiedFormat
  Scenario: Setting unified format with --format option
    When I run the utility with unified format "markdown" on "src/test/resources/ftoc/test-feature-files"
    Then the output should be formatted as markdown
    And the utility should use markdown format for all reports
    
  @ConsistentFormats
  Scenario: Setting different formats for different reports
    When I run the utility with mixed formats on "src/test/resources/ftoc/test-feature-files":
      | report_type  | format    |
      | toc          | html      |
      | concordance  | json      |
      | tag_quality  | markdown  |
    Then each report should use its specified format