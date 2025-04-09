@API @BalancedTesting
Feature: Balanced positive and negative testing feature
  This feature demonstrates both positive and negative test cases
  to test ftoc's ability to analyze test coverage balance

  @Positive @P0
  Scenario: Successful user registration with valid data
    Given a new user with valid information
    When the user submits the registration form
    Then the user should be registered successfully
    And receive a confirmation email

  @Negative @P1 @Validation
  Scenario: User registration fails with invalid email
    Given a new user with an invalid email "user@invalid"
    When the user submits the registration form
    Then the registration should fail
    And an error message "Invalid email format" should be displayed

  @Negative @P1 @Validation
  Scenario: User registration fails with password too short
    Given a new user with password "short"
    When the user submits the registration form
    Then the registration should fail
    And an error message "Password must be at least 8 characters" should be displayed

  @Negative @P1 @Security
  Scenario: User registration fails with common password
    Given a new user with password "password123"
    When the user submits the registration form
    Then the registration should fail
    And an error message "Password is too common" should be displayed

  @Positive @P2
  Scenario: User registration with minimum required fields
    Given a new user with only required fields filled
    When the user submits the registration form
    Then the user should be registered successfully
    But profile should be marked as incomplete

  @Negative @P3 @Performance
  Scenario: Registration system handles high load
    Given the registration system is under heavy load
    When 1000 registration requests are submitted simultaneously
    Then all valid registrations should be processed
    And the system response time should remain under 2 seconds