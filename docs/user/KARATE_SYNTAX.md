# Karate Test Syntax Overview

This guide provides a concise overview of Karate API testing syntax, focusing on elements most relevant to feature file analysis with FTOC.

## Basic Structure

A Karate test file is a Cucumber feature file with specialized syntax for API testing:

```gherkin
Feature: Login API tests

  Background:
    * url 'https://api.example.com'
    * header Content-Type = 'application/json'
    * def credentials = { username: 'test', password: 'password' }

  Scenario: Successful login
    Given path 'login'
    And request credentials
    When method POST
    Then status 200
    And match response == { token: '#notnull', user: '#object' }
```

## Key Syntax Elements

### The Asterisk (`*`)

The `*` prefix is a generic step definition that can be used for any Karate statement. It avoids having to use `Given`, `When`, `Then` prefixes for Karate-specific operations.

```gherkin
* def result = { success: true }    # Create a variable
* print 'Response:', response       # Print to console during test execution
* match response.status == 'active' # Assert a value in the response
```

### HTTP Methods

Karate provides built-in keywords for HTTP operations:

```gherkin
# URL and path construction
* url 'https://api.example.com'      # Base URL
* path 'users', userId               # Path components (will be joined with '/')
* param page = 1                     # Query parameter (?page=1)

# HTTP methods
* method GET                         # GET request
* method POST                        # POST request
* method PUT                         # PUT request
* method DELETE                      # DELETE request

# Request body
* request { "name": "John", "age": 30 } # JSON request body
* request read('payload.json')         # Load request from file
```

### Variables and Data

Karate provides ways to declare and use variables:

```gherkin
# Define variables
* def user = { name: 'John', id: 1 }  # JSON object 
* def users = ['John', 'Jane']        # Array
* def count = 5                       # Number
* def active = true                   # Boolean
* def username = 'test_user'          # String

# Use variables in requests
* path 'users', user.id
* param username = username
* request user
```

### Response Validation

The `match` keyword is Karate's powerful assertion mechanism:

```gherkin
# Basic response validation
* status 200                           # Assert HTTP status code
* match response.success == true       # Exact match
* match response contains { id: 1 }    # Partial match

# Schema validation
* match response == { id: '#number', name: '#string' }
* match response == { id: '#notnull', created: '#regex [0-9]{4}-[0-9]{2}-[0-9]{2}' }

# Common matchers
# #notnull - value must not be null
# #null - value must be null
# #present - key must exist (any value including null)
# #notpresent - key must not exist
# #string - must be a string
# #number - must be a number
# #boolean - must be true or false
# #array - must be an array
# #object - must be an object
# #regex - must match the regex pattern
```

### JavaScript Integration

Karate allows inline JavaScript for complex operations:

```gherkin
* def sum =
"""
function(a, b) {
  return a + b;
}
"""
* def result = call sum 5, 10
* match result == 15
```

## Tagging Best Practices

Tags help organize and run tests selectively:

```gherkin
@api @auth                 # Feature-level tags
Feature: Authentication API

  @smoke @critical         # Scenario-level tags
  Scenario: Successful login
    # ...

  @regression @negative    # Different scenario tags for test selection
  Scenario: Failed login with invalid credentials
    # ...
```

### Recommended Tag Conventions

* **Functionality tags**: `@auth`, `@users`, `@orders` - identify functional area
* **Priority tags**: `@P0`, `@P1`, `@P2` - set test priority (P0 = highest)
* **Test type tags**: `@smoke`, `@regression`, `@integration` - identify test type
* **Status tags**: `@wip`, `@skip` - indicate test status

## Build Integration

### Maven Integration

```xml
<!-- pom.xml dependency -->
<dependency>
    <groupId>com.intuit.karate</groupId>
    <artifactId>karate-junit5</artifactId>
    <version>1.4.0</version>
    <scope>test</scope>
</dependency>
```

### Runner Class

```java
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KarateTests {
    @Test
    void testParallel() {
        Results results = Runner.path("classpath:karate")
                                .outputCucumberJson(true)
                                .parallel(5); // Run up to 5 threads
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
```

### Command Line Execution

```bash
# Run all tests
mvn test -Dtest=KarateTests

# Run tests with specific tags
mvn test -Dkarate.options="--tags @smoke"

# Skip tests with specific tags
mvn test -Dkarate.options="--tags ~@skip"
```

### Environment-Specific Configuration

Create `karate-config.js` in the classpath:

```javascript
function fn() {
  var env = karate.env || 'dev'; // Default to 'dev' if not specified
  var config = {
    baseUrl: 'https://dev-api.example.com'
  };
  
  if (env === 'stage') {
    config.baseUrl = 'https://stage-api.example.com';
  } else if (env === 'prod') {
    config.baseUrl = 'https://api.example.com';
  }
  
  return config;
}
```

Set the environment with:

```bash
mvn test -Dkarate.env=stage
```

## FTOC Integration

FTOC detects and analyzes Karate-specific syntax in your feature files:

1. Identifies Karate-style files
2. Reports specific API testing features used:
   - API calls
   - JSON schema validation
   - JSON matching
   - Embedded JavaScript
   - API operations (GET, POST, etc.)

## Additional Resources

* [Official Karate Documentation](https://github.com/karatelabs/karate)
* [Karate GitHub Repository](https://github.com/karatelabs/karate)
* [Karate OpenAPI Generator](https://github.com/karatelabs/karate/tree/master/karate-openapi)
* [Karate Testing Guide](../developer/KARATE_TESTING.md) - Developer guide for using Karate with FTOC