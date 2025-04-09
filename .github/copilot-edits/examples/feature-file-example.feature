@P1 @API @Regression
Feature: User Authentication API

  This feature covers authentication-related functionality
  including login, logout, and token validation.

  Background:
    Given the API service is running
    And the authentication endpoints are accessible

  Scenario: Successful login with valid credentials
    Given a user with valid credentials
    When a POST request is sent to "/auth/login" with username and password
    Then the response status code should be 200
    And the response should contain a valid access token
    And the response should contain a valid refresh token

  Scenario: Failed login with invalid credentials
    Given a user with invalid credentials
    When a POST request is sent to "/auth/login" with username and password
    Then the response status code should be 401
    And the response should contain an error message

  Scenario Outline: Token validation with different token types
    Given a user with a <token_type> token
    When a GET request is sent to "/auth/validate" with the token
    Then the response status code should be <status_code>
    And the response should contain <response_content>

    Examples:
      | token_type | status_code | response_content     |
      | valid      | 200         | user profile details |
      | expired    | 401         | error message        |
      | invalid    | 401         | error message        |
      | malformed  | 400         | error message        |

  Scenario: Successful logout
    Given a logged in user with a valid token
    When a POST request is sent to "/auth/logout" with the token
    Then the response status code should be 200
    And the token should be invalidated