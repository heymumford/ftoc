@API @KarateTest
Feature: User API endpoints
  Karate-style API test for user management

  Background:
    * url 'http://localhost:8080'

  @GET @Positive
  Scenario: Get user by ID
    * path '/users/1'
    * method GET
    * status 200
    * match response.name == '#string'
    * match response.id == '#number'

  @POST @Positive
  Scenario: Create a new user
    * def payload = { name: 'Alice', email: 'alice@example.com' }
    * path '/users'
    * method POST
    * status 201
    * match response.id == '#notnull'
