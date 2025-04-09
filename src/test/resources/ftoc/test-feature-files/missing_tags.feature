Feature: Feature with missing or incomplete tags
  This feature is intentionally missing proper tags
  to test ftoc's ability to detect and recommend missing tags

  Scenario: Scenario without any tags
    Given I have a scenario without tags
    When ftoc analyzes this scenario
    Then it should recommend adding appropriate tags

  @test
  Scenario: Scenario with generic tag
    Given I have a scenario with a generic tag
    When ftoc analyzes this scenario
    Then it should recommend more specific tags

  @UI
  Scenario: Scenario with only a platform tag but no priority
    Given I have a scenario with platform tag
    When ftoc analyzes this scenario
    Then it should recommend adding priority tags

  @P1
  Scenario: Scenario with only priority tag but no type
    Given I have a scenario with priority tag
    When ftoc analyzes this scenario
    Then it should recommend adding type tags

  Scenario Outline: Scenario outline without tags
    Given I have value "<value>"
    When I perform action with "<value>"
    Then I should get result "<result>"

    Examples:
      | value | result |
      | A     | A-res  |
      | B     | B-res  |