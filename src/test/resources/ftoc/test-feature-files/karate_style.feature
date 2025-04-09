@KarateTest @API
Feature: Karate framework style feature file
  This feature demonstrates the Karate framework style of Gherkin
  which includes embedded JavaScript/JSON and direct API calls

  Background:
    * url 'https://api.example.com'
    * header Accept = 'application/json'
    * def authToken = call read('classpath:auth.js')
    * header Authorization = 'Bearer ' + authToken

  @Smoke @P0
  Scenario: Get user by ID
    Given path 'users', '1'
    When method GET
    Then status 200
    And match response == { id: 1, name: '#string', email: '#regex[.+@.+]' }
    And match response.name == '#present'

  @Regression @P1
  Scenario Outline: Create multiple users with different roles
    Given path 'users'
    And request { name: '<name>', email: '<email>', role: '<role>' }
    When method POST
    Then status 201
    And match response.id == '#number'
    And match response.role == '<role>'

    Examples:
      | name      | email             | role      |
      | Admin User| admin@example.com | admin     |
      | Test User | test@example.com  | user      |
      | Guest User| guest@example.com | guest     |

  @Security @P0
  Scenario: Attempt access with invalid token
    * def invalidToken = 'invalid-token-value'
    Given path 'users'
    And header Authorization = 'Bearer ' + invalidToken
    When method GET
    Then status 401

  @Performance @P2
  Scenario: Measure API response time
    Given path 'users'
    When method GET
    Then status 200
    And assert responseTime < 500