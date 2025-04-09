@ComplexScenarios
Feature: Examples of complex scenarios with common issues
  This feature file contains scenarios that exhibit complexity issues
  such as long names, too many steps, and other anti-patterns that
  make scenarios harder to maintain and understand

  @LongName @P1
  Scenario: This is an extremely long scenario name that exceeds the recommended maximum length and makes it difficult to quickly understand what this scenario is testing because it's so verbose and contains unnecessary details
    Given I am logged in
    When I navigate to the dashboard
    Then I should see my account summary

  @TooManySteps @P2
  Scenario: User completes a complex workflow
    Given I am logged in as an administrator
    When I navigate to the user management section
    And I click "Add New User"
    And I enter "John Doe" as the name
    And I enter "john.doe@example.com" as the email
    And I select "Administrator" as the role
    And I check the "Active" checkbox
    And I uncheck the "Require password change" checkbox
    And I enter "Password123!" as the password
    And I enter "Password123!" as the password confirmation
    And I select "Sales" as the department
    And I select "US" as the region
    And I enter "John's Manager" as the manager
    And I upload a profile picture
    And I click the "Create User" button
    Then I should see a success message
    And the user "John Doe" should appear in the user list
    And an email should be sent to "john.doe@example.com"
    And the audit log should show that I created the user
    And the user should be able to log in with "Password123!"

  @LongSteps @P1
  Scenario: User searches for products with filters
    Given I am on the product search page with a large catalog of items from multiple categories including electronics, clothing, home goods, and outdoor equipment
    When I filter the results by selecting multiple complex criteria including price range between $100 and $500, only 4-star and above ratings, items that are currently in stock, and limited to the electronics category with gaming subcategory
    Then I should see a properly filtered list of products that match all of my selected criteria with the most relevant items appearing at the top of the search results page

  @NestedScenarioOutline @P2
  Scenario Outline: Processing orders with different payment methods and shipping options
    Given I have a shopping cart with the following items:
      | Product     | Quantity | Price  |
      | Smartphone  | 1        | $999   |
      | Phone Case  | 2        | $19.99 |
      | Screen Protector | 3   | $9.99  |
    When I proceed to checkout
    And I choose "<payment_method>" as my payment method
    And I enter my payment details:
      | Field       | Value                 |
      | Card Number | <card_number>         |
      | Expiry      | <expiry>              |
      | CVV         | <cvv>                 |
      | Name        | <cardholder_name>     |
    And I select "<shipping_option>" as my shipping option
    And I confirm my order
    Then my order should be processed successfully
    And I should receive an order confirmation email
    And my order status should be "Processing"
    And my payment method should show as "<payment_method>"
    And my shipping option should be "<shipping_option>"
    And my order total should include the correct shipping cost

    Examples:
      | payment_method | card_number       | expiry    | cvv | cardholder_name | shipping_option      |
      | Credit Card    | 4111111111111111  | 12/2025   | 123 | John Doe        | Standard Shipping    |
      | Credit Card    | 5555555555554444  | 10/2024   | 456 | Jane Smith      | Express Shipping     |
      | PayPal         | N/A               | N/A       | N/A | N/A             | Standard Shipping    |
      | Debit Card     | 3700000000000002  | 08/2026   | 789 | Bob Johnson     | Next-Day Shipping    |