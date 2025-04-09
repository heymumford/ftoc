# Karate Testing in FTOC

This document explains how to use Karate for system-level testing in the FTOC project.

> **Note:** For a concise overview of Karate syntax and best practices, see the [Karate Syntax Guide](../user/KARATE_SYNTAX.md).

## Overview

FTOC uses a multi-layered testing approach following the Test Pyramid:

1. **Unit Tests** (JUnit) - Testing individual components in isolation
2. **Integration Tests** (Cucumber) - Testing component interactions
3. **System Tests** (Karate) - End-to-end testing of the application, especially the CLI

Karate is used for system-level testing because:
- It provides a powerful DSL for testing CLI applications
- It supports parallel test execution for faster feedback
- It generates comprehensive HTML and JUnit XML reports
- It uses Gherkin syntax, consistent with our existing BDD approach

## Getting Started

### Running Karate Tests

To run the Karate tests:

```bash
# Run all Karate tests (single-threaded)
mvn test -Dtest=karate.KarateRunner

# Run all Karate tests in parallel with JUnit XML reporting
mvn test -Dtest=karate.KarateRunner#testParallel

# Run specific Karate tests
mvn test -Dtest=karate.KarateRunner#testCliCommands
```

### Test Reports

Karate generates comprehensive reports in the `target/karate-reports` directory:
- HTML reports with detailed test information
- JUnit XML reports for CI integration
- Cucumber JSON reports for compatibility with other tools

## Writing Karate Tests

### CLI Testing

CLI tests in Karate use the `karate.exec()` function to run shell commands and validate their output:

```gherkin
Feature: FTOC CLI Testing

  Scenario: Generate TOC in markdown format
    Given def command = 'java -jar ' + ftocPath + ' --format markdown --directory src/test/resources/ftoc/features'
    When def result = karate.exec(command)
    Then match result.exitCode == 0
    And match result.stdout contains '## TABLE OF CONTENTS'
```

### Configuration

Karate tests use the `karate-config.js` file for configuration, which provides:
- Environment-specific settings
- Path to the FTOC JAR file
- Paths to test resources
- Helper functions for common operations

## Parallel Execution

Karate supports parallel test execution to improve test performance:

```java
@Test
void testParallel() {
    Results results = Runner.path("classpath:karate")
            .outputJunitXml(true)
            .reportDir("target/karate-reports")
            .parallel(5); // Run up to 5 threads in parallel
    
    assertEquals(0, results.getFailCount(), 
        results.getFailCount() + " test(s) failed");
}
```

## CI/CD Integration

Karate tests are integrated into our GitHub Actions workflow:
- Tests run as part of the build job
- Results are published as GitHub Actions test reports
- Test artifacts (HTML reports) are uploaded for debugging

## Adding New Tests

To add new Karate tests:

1. Create a new `.feature` file in `src/test/java/karate/`
2. Add scenarios using Gherkin syntax with Karate's DSL
3. Use the `karate.exec()` function for CLI testing
4. Add assertions using Karate's `match` keyword

Example of a new test:

```gherkin
Feature: FTOC Command Line Options

  Scenario Outline: Basic command line flags work correctly
    Given def command = 'java -jar ' + ftocPath + ' <flag>'
    When def result = karate.exec(command)
    Then match result.exitCode == 0
    And match result.stdout contains '<expected>'

    Examples:
      | flag       | expected        |
      | --help     | Usage:          |
      | --version  | FTOC version    |
```

## Best Practices

- Use `Background` sections for common setup
- Organize tests by feature area
- Use descriptive scenario names
- Include detailed assertions to validate behavior
- Leverage Karate's powerful JSON/XML validation for complex outputs
- Use tags to categorize tests (@CLI, @Performance, etc.)
- Avoid depending on external services for tests