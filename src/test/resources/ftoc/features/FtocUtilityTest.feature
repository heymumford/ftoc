@InitialTest @P0
Feature: Testing FTOC utility using its own feature files

  Scenario: Validate initial FTOC behavior
    Given the ftoc utility is initialized
    When I run the utility on the "src/test/resources/ftoc/features" directory
    Then it should generate a table of contents
    And the output should contain a valid concordance summary
    
  @TagFiltering @P1
  Scenario: Generate TOC with tag filtering
    Given the ftoc utility is initialized
    When I set include tag filter "@P0"
    And I set output format to "text"
    And I run the utility on the "src/test/resources/ftoc/features" directory
    Then the TOC should only contain scenarios with tag "@P0"
    
  @TagFiltering @P1
  Scenario: Generate TOC with multiple include tag filters
    Given the ftoc utility is initialized
    When I set include tag filter "@P0,@P1"
    And I set output format to "markdown"
    And I run the utility on the "src/test/resources/ftoc/features" directory
    Then the TOC should only contain scenarios with tags "@P0" or "@P1"
    
  @TagFiltering @P1
  Scenario: Generate TOC with exclude tag filter
    Given the ftoc utility is initialized
    When I set exclude tag filter "@P0"
    And I set output format to "html"
    And I run the utility on the "src/test/resources/ftoc/features" directory
    Then the TOC should not contain scenarios with tag "@P0"
    
  @TagFiltering @P1
  Scenario: Generate TOC with both include and exclude tag filters
    Given the ftoc utility is initialized
    When I set include tag filter "@P0,@P1"
    And I set exclude tag filter "@InitialTest"
    And I set output format to "json"
    And I run the utility on the "src/test/resources/ftoc/features" directory
    Then the TOC should only contain scenarios with tags "@P0" or "@P1"
    And the TOC should not contain scenarios with tag "@InitialTest"
