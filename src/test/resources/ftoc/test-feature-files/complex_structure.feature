@ComplexFeature @E2E
Feature: Complex feature file with nested structures and rules
  This feature demonstrates complex Gherkin structures including
  rules, multiple backgrounds, and complex data tables

  Background: Common setup for all scenarios
    Given the application is running
    And the database is in a clean state

  @Setup @P0
  Scenario: Initial setup
    Given I initialize the test environment
    When I verify all components are available
    Then the system should be ready for testing

  Rule: User authentication rules

    Background: Auth-specific setup
      Given I am on the login page
      And the following users exist:
        | username | password | role  | status   |
        | admin    | pass123  | admin | active   |
        | user1    | pass456  | user  | active   |
        | user2    | pass789  | user  | inactive |

    @Auth @Security @P0
    Scenario: Successful login
      When I login with username "admin" and password "pass123"
      Then I should be redirected to the admin dashboard
      And I should see a welcome message

    @Auth @Security @Negative @P1
    Scenario: Failed login with incorrect password
      When I login with username "admin" and password "wrong"
      Then I should see an error message "Invalid credentials"
      And I should remain on the login page

    @Auth @Security @Negative @P1
    Scenario: Login attempt with inactive account
      When I login with username "user2" and password "pass789"
      Then I should see an error message "Account is inactive"
      And I should remain on the login page

  Rule: User management rules

    Background: Admin setup
      Given I am logged in as an admin
      And I am on the user management page

    @Admin @CRUD @P1
    Scenario: Create new user
      When I click on "Create User" button
      And I fill in the following details:
        | Field    | Value           |
        | Username | newuser         |
        | Email    | new@example.com |
        | Role     | user            |
        | Status   | active          |
      And I click on "Save" button
      Then a new user should be created
      And I should see a success message "User created successfully"

    @Admin @CRUD @P1
    Scenario: Update existing user
      When I select user "user1"
      And I update the following details:
        | Field  | Value     |
        | Role   | moderator |
        | Status | inactive  |
      And I click on "Save" button
      Then the user details should be updated
      And I should see a success message "User updated successfully"

    @Admin @CRUD @P1 @Documentation
    Scenario: View user audit trail
      When I select user "user1"
      And I click on "View Audit Trail" button
      Then I should see a table with the following columns:
        """
        | Timestamp | Action | Changed By | Details |
        """
      And the table should contain at least one row

  Rule: Advanced search functionality

    @Search @P2
    Scenario Outline: Search users with different criteria
      Given I am on the user search page
      When I search with the following criteria:
        | Field       | Value        |
        | Username    | <username>   |
        | Role        | <role>       |
        | Status      | <status>     |
        | Created After | <date>       |
      Then I should see users matching the criteria
      And the result count should be <count>

      Examples:
        | username | role  | status   | date        | count |
        | admin    | admin | active   | 2023-01-01  | 1     |
        | user     | user  | active   | 2023-01-01  | 2     |
        | user     | any   | inactive | 2023-01-01  | 1     |
        | any      | admin | any      | 2023-01-01  | 1     |