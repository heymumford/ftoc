
TAG CONCORDANCE REPORT
======================

SUMMARY
-------
Total Tags Used: 19
Unique Tags: 17
Features: 1
Average Tags Per Feature: 19.00

TAG FREQUENCY
-------------
Tag                            Count      Percent    Trend        Significance
------------------------------ ---------- ---------- ------------ ------------
@P1                            2          10.5%      Stable       -1.386      
@Payment                       2          10.5%      Stable       -1.386      
@P0                            1          5.3%       Stable       -0.693      
@P2                            1          5.3%       Stable       -0.693      
@API                           1          5.3%       Stable       -0.693      
@Medium                        1          5.3%       Stable       -0.693      
@Showcase                      1          5.3%       Stable       -0.693      
@Negative                      1          5.3%       Stable       -0.693      
@Parametrized                  1          5.3%       Stable       -0.693      
@DataDriven                    1          5.3%       Stable       -0.693      
@Rule                          1          5.3%       Stable       -0.693      
@DemoFeature                   1          5.3%       Stable       -0.693      
@Positive                      1          5.3%       Stable       -0.693      
@UI                            1          5.3%       Stable       -0.693      
@Smoke                         1          5.3%       Stable       -0.693      
@Regression                    1          5.3%       Stable       -0.693      
@Fast                          1          5.3%       Stable       -0.693      

TAG CATEGORIES
-------------
DataDriven Tags (1):
  @DataDriven                    1

DemoFeature Tags (1):
  @DemoFeature                   1

Fast Tags (1):
  @Fast                          1

Feature Type Tags (4):
  @API                           1
  @Regression                    1
  @Smoke                         1
  @UI                            1

Negative Tags (1):
  @Negative                      1

Parametrized Tags (1):
  @Parametrized                  1

Payment Tags (1):
  @Payment                       2

Positive Tags (1):
  @Positive                      1

Priority Tags (4):
  @Medium                        1
  @P0                            1
  @P1                            2
  @P2                            1

Rule Tags (1):
  @Rule                          1

Showcase Tags (1):
  @Showcase                      1

TAG CO-OCCURRENCE METRICS
------------------------
Tag 1                Tag 2                Count      Coefficient    
-------------------- -------------------- ---------- ---------------
@API                 @Regression          1          1.000          
@P2                  @DataDriven          1          1.000          
@DemoFeature         @P0                  8          1.000          
@Smoke               @UI                  1          1.000          
@Regression          @Medium              1          1.000          
@Rule                @Parametrized        1          1.000          
@UI                  @Fast                1          1.000          
@DemoFeature         @Showcase            8          1.000          
@API                 @Medium              1          1.000          
@P0                  @Showcase            8          1.000          
@P1                  @Payment             2          1.000          
@Smoke               @Fast                1          1.000          
@Payment             @Negative            1          0.500          
@Positive            @Payment             1          0.500          
@P1                  @Negative            1          0.500          

TAG TREND ANALYSIS
------------------
Tag                            Count      Trend        Growth Rate    
------------------------------ ---------- ------------ ---------------
@P0                            1          Stable       0.000          
@P1                            2          Stable       0.000          
@P2                            1          Stable       0.000          
@API                           1          Stable       0.000          
@Payment                       2          Stable       0.000          
@Medium                        1          Stable       0.000          
@Showcase                      1          Stable       0.000          
@Negative                      1          Stable       0.000          
@Parametrized                  1          Stable       0.000          
@DataDriven                    1          Stable       0.000          
@Rule                          1          Stable       0.000          
@DemoFeature                   1          Stable       0.000          
@Positive                      1          Stable       0.000          
@UI                            1          Stable       0.000          
@Smoke                         1          Stable       0.000          
@Regression                    1          Stable       0.000          
@Fast                          1          Stable       0.000          

POTENTIALLY LOW-VALUE TAGS
-------------------------
No low-value tags detected.

STATISTICALLY SIGNIFICANT TAGS
-----------------------------
@P0 (Score: -0.693)
@P2 (Score: -0.693)
@API (Score: -0.693)
@Medium (Score: -0.693)
@Showcase (Score: -0.693)
@Negative (Score: -0.693)
@Parametrized (Score: -0.693)
@DataDriven (Score: -0.693)
@Rule (Score: -0.693)
@DemoFeature (Score: -0.693)



# Table of Contents

## Contents

- [Comprehensive demonstration of FTOC capabilities](#comprehensive-demonstration-of-ftoc-capabilities)
  - [Basic search functionality](#comprehensive-demonstration-of-ftoc-capabilities-basic-search-functionality)
  - [Advanced filtering with multiple criteria](#comprehensive-demonstration-of-ftoc-capabilities-advanced-filtering-with-multiple-criteria)
  - [Payment processing follows business rules](#comprehensive-demonstration-of-ftoc-capabilities-payment-processing-follows-business-rules)
  - [Successful payment with credit card](#comprehensive-demonstration-of-ftoc-capabilities-successful-payment-with-credit-card)
  - [Failed payment with expired credit card](#comprehensive-demonstration-of-ftoc-capabilities-failed-payment-with-expired-credit-card)
  - ... and 1 more

<h2 id="comprehensive-demonstration-of-ftoc-capabilities">Comprehensive demonstration of FTOC capabilities</h2>

*File: FtocDemo.feature*

**Tags:** `@DemoFeature` `@Showcase` `@P0` 

<h3 id="comprehensive-demonstration-of-ftoc-capabilities-basic-search-functionality">Scenario: Basic search functionality</h3>

**Tags:** `@Smoke` `@UI` `@Fast` 

<details>
<summary>Steps</summary>

```gherkin
When I search for "cucumber"
Then I should see search results
And the first result should contain "cucumber"
```
</details>

<h3 id="comprehensive-demonstration-of-ftoc-capabilities-advanced-filtering-with-multiple-criteria">Scenario: Advanced filtering with multiple criteria</h3>

**Tags:** `@Regression` `@API` `@Medium` 

<details>
<summary>Steps</summary>

```gherkin
When I search with the following criteria:
Then I should see filtered results
And all results should match the selected criteria
```
</details>

<h3 id="comprehensive-demonstration-of-ftoc-capabilities-payment-processing-follows-business-rules">Scenario: Payment processing follows business rules</h3>

**Tags:** `@Rule` `@Parametrized` 

<h3 id="comprehensive-demonstration-of-ftoc-capabilities-successful-payment-with-credit-card">Scenario: Successful payment with credit card</h3>

**Tags:** `@P1` `@Payment` `@Positive` 

<details>
<summary>Steps</summary>

```gherkin
When I choose to pay with credit card
And I enter valid credit card details
Then my payment should be processed
And I should receive an order confirmation
```
</details>

<h3 id="comprehensive-demonstration-of-ftoc-capabilities-failed-payment-with-expired-credit-card">Scenario: Failed payment with expired credit card</h3>

**Tags:** `@P1` `@Payment` `@Negative` 

<details>
<summary>Steps</summary>

```gherkin
When I choose to pay with credit card
And I enter an expired credit card
Then I should see an error message
And my payment should not be processed
```
</details>

<h3 id="comprehensive-demonstration-of-ftoc-capabilities-login-with-different-user-types">Scenario Outline: Login with different user types</h3>

**Tags:** `@P2` `@DataDriven` 

**Examples:** 4 total variations

<details>
<summary>Example Details</summary>

#### Admin users

| user_type | permissions | dashboard_type | feature_count | 
| --- | --- | --- | --- | 
| Admin | Full | Admin | 10 | 
| Manager | Limited | Management | 7 | 

#### Regular users

| user_type | permissions | dashboard_type | feature_count | 
| --- | --- | --- | --- | 
| Customer | Basic | Customer | 3 | 
| Guest | Minimal | Limited | 1 | 

</details>

<details>
<summary>Steps</summary>

```gherkin
Given I am on the login page
When I login as a "<user_type>" user with "<permissions>"
Then I should see the "<dashboard_type>" dashboard
And I should have access to <feature_count> features
```
</details>

[Back to Contents](#contents)

---


