@AntiPatternExamples
Feature: Examples of anti-patterns in feature files
  This feature file intentionally contains examples of common anti-patterns
  to facilitate testing of FTOC's anti-pattern detection capabilities

  @LongScenario @UI
  Scenario: Long scenario with too many steps
    Given I am on the login page
    When I enter my username
    And I enter my password
    And I click the login button
    And I wait for the dashboard to load
    And I click on the profile menu
    And I select settings
    And I wait for the settings page to load
    And I click on the account tab
    And I wait for the account settings to load
    And I enter my new name
    And I enter my new email
    And I click save changes
    And I wait for the confirmation
    Then I should see a success message
    And my profile should be updated
    And I should receive a confirmation email

  @MissingGiven
  Scenario: Missing Given step
    When I click the login button
    Then I should see an error message

  @MissingWhen
  Scenario: Missing When step
    Given I am on the login page
    Then I should see the login form

  @MissingThen
  Scenario: Missing Then step
    Given I am on the login page
    When I enter my credentials

  @UIFocused
  Scenario: UI-focused steps instead of business-oriented steps
    Given I am on the login page
    When I click the username field 
    And I type "admin" into the username field
    And I click the password field
    And I type "password123" into the password field
    And I click the login button
    Then I should see the dashboard page
    And I should see a welcome message in the top right corner

  @ImplementationDetails
  Scenario: Steps with implementation details
    Given the user authentication API endpoint is up
    When I send a POST request to "/api/auth" with username "admin" and password "admin123"
    And I set the authentication token in the HTTP header
    And I wait for 200 milliseconds for the JS on the page to initialize
    Then the response status code should be 200
    And the response JSON should contain an "access_token" field
    And the element with id "welcome-user" should be visible within 3 seconds

  @InconsistentTense
  Scenario: Steps with inconsistent tense usage
    Given I am on the login page
    When I entered my username
    And I have typed my password
    Then I will see the dashboard

  @AmbiguousPronoun
  Scenario: Steps with ambiguous pronouns
    Given I have a shopping cart with items
    When I remove it
    Then they should not be shown anymore

  @ConjunctionStep
  Scenario: Steps with conjunctions
    Given I am on the registration page
    When I fill out the form and submit it
    Then I should see a success message and receive an email