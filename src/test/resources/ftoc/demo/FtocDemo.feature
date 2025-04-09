@DemoFeature @Showcase @P0
Feature: Comprehensive demonstration of FTOC capabilities
  As a test engineer
  I want to explore FTOC's capabilities
  So that I can effectively organize and analyze my feature files

  Background:
    Given the application is running
    And all services are available

  @Smoke @UI @Fast
  Scenario: Basic search functionality
    When I search for "cucumber"
    Then I should see search results
    And the first result should contain "cucumber"

  @Regression @API @Medium
  Scenario: Advanced filtering with multiple criteria
    When I search with the following criteria:
      | Category | Price  | Rating |
      | Books    | 0-50   | 4+     |
    Then I should see filtered results
    And all results should match the selected criteria

  @Rule @Parametrized
  Rule: Payment processing follows business rules

    Background:
      Given I am logged in as a customer
      And I have items in my cart

    @P1 @Payment @Positive
    Scenario: Successful payment with credit card
      When I choose to pay with credit card
      And I enter valid credit card details
      Then my payment should be processed
      And I should receive an order confirmation

    @P1 @Payment @Negative
    Scenario: Failed payment with expired credit card
      When I choose to pay with credit card
      And I enter an expired credit card
      Then I should see an error message
      And my payment should not be processed

  @P2 @DataDriven
  Scenario Outline: Login with different user types
    Given I am on the login page
    When I login as a "<user_type>" user with "<permissions>"
    Then I should see the "<dashboard_type>" dashboard
    And I should have access to <feature_count> features

    Examples: Admin users
      | user_type | permissions | dashboard_type | feature_count |
      | Admin     | Full        | Admin          | 10            |
      | Manager   | Limited     | Management     | 7             |

    Examples: Regular users
      | user_type | permissions | dashboard_type | feature_count |
      | Customer  | Basic       | Customer       | 3             |
      | Guest     | Minimal     | Limited        | 1             |