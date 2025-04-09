@DataDriven @RegressionTest
Feature: Scenario outline with examples
  This feature demonstrates the use of scenario outlines with various examples
  to test data-driven scenarios and pairwise combinations

  @AllBrowsers
  Scenario Outline: User login with multiple browsers and credentials
    Given I am on the login page in "<browser>"
    When I enter username "<username>" and password "<password>"
    And I click on the login button
    Then I should see the dashboard with greeting for "<username>"
    And my account type should be "<account_type>"

    @P0 @Smoke
    Examples: Admin users - high priority
      | browser | username     | password    | account_type |
      | Chrome  | admin        | admin123!   | Admin        |
      | Firefox | super_admin  | super456!   | SuperAdmin   |
    
    @P1 @Regression
    Examples: Regular users - medium priority
      | browser | username   | password   | account_type |
      | Chrome  | user1      | pass123    | Standard     |
      | Firefox | user2      | pass456    | Standard     |
      | Safari  | user3      | pass789    | Premium      |
      | Edge    | user4      | passabc    | Premium      |

  @PairwiseTest @P2
  Scenario Outline: Product search with multiple parameters
    Given I am on the product search page
    When I select category "<category>"
    And I select price range "<price_range>"
    And I select brand "<brand>"
    And I select color "<color>"
    Then I should see products matching all selected criteria

    Examples: Common search combinations
      | category   | price_range | brand   | color  |
      | Electronics| 0-100       | Brand A | Black  |
      | Electronics| 101-500     | Brand B | White  |
      | Electronics| 500+        | Brand C | Red    |
      | Clothing   | 0-100       | Brand B | Blue   |
      | Clothing   | 101-500     | Brand C | Green  |
      | Clothing   | 500+        | Brand A | Yellow |
      | Home       | 0-100       | Brand C | White  |
      | Home       | 101-500     | Brand A | Black  |
      | Home       | 500+        | Brand B | Red    |