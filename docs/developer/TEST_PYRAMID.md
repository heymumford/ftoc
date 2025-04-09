# FTOC Test Pyramid Strategy

This document outlines the test pyramid approach for the FTOC project, focusing on the different layers of testing and the appropriate tools for each layer.

## Test Pyramid Overview

The FTOC project follows a classic test pyramid with:

1. **Unit Tests** (Base layer) - Fast and focused tests for individual components
2. **Integration Tests** (Middle layer) - Tests for interactions between components
3. **System Tests** (Top layer) - End-to-end tests for the entire application

## Tool Selection by Test Layer

### Unit Tests - JUnit 5

Unit tests focus on testing individual components in isolation. For FTOC, these tests are implemented using:

- **JUnit 5**: The core unit testing framework for Java components
- **Mocks and Stubs**: To isolate components from their dependencies

### Integration Tests - Cucumber

Integration tests verify that different components work together correctly. For FTOC, these tests are implemented using:

- **Cucumber**: For behavior-driven testing of component interactions
- **JUnit Platform**: For test execution and reporting

### System Tests - Karate (Proposed)

System tests verify the entire application works as expected from an external perspective. The proposed approach is to use:

- **Karate**: For API and CLI testing
- **Cucumber**: For end-to-end functional testing

## Proposed Karate Integration

### Why Karate?

Karate provides several benefits for FTOC:

1. **API Testing**: Karate excels at API testing with a concise DSL
2. **JSON and XML Validation**: Built-in support for complex response validation
3. **CLI Testing**: Can test command-line interfaces through the `karate.exec()` API
4. **BDD Approach**: Uses Gherkin syntax like Cucumber, maintaining consistency
5. **Parallel Execution**: Built-in parallel test execution
6. **Reporting**: Comprehensive HTML reports

### Integration Approach

#### Phase 1: CLI Testing

1. Add Karate as a test dependency
2. Create initial Karate test suite for FTOC CLI
3. Test basic FTOC commands and validate outputs
4. Integrate with Maven build

#### Phase 2: FTOC Parser for Karate Files

1. Extend FTOC to understand Karate-specific syntax
2. Add support for parsing Karate-specific elements:
   - API call syntax (`Given url`, `When method`, etc.)
   - JSON/JavaScript embedded code
   - Karate-specific keywords and symbols

#### Phase 3: Full Test Pyramid Integration

1. Create a cohesive testing strategy document
2. Define clear boundaries between test layers
3. Implement CI integration for all test layers
4. Establish test coverage goals for each layer

## Karate Feature Example

```gherkin
Feature: Test FTOC CLI with Karate

  Scenario: Generate TOC in markdown format
    Given def command = 'ftoc --format markdown --directory src/test/resources/ftoc/features'
    When def result = karate.exec(command)
    Then match result.exitCode == 0
    And match result.stdout contains '## TABLE OF CONTENTS'
    
  Scenario: Analyze tags with filtering
    Given def command = 'ftoc --analyze-tags --include-tags P0,P1 --directory src/test/resources/ftoc/features'
    When def result = karate.exec(command)
    Then match result.exitCode == 0
    And match result.stdout contains 'Include tags: @P0,@P1'
```

## Implementation Plan

1. **Add Karate Dependencies**:
   ```xml
   <dependency>
     <groupId>com.intuit.karate</groupId>
     <artifactId>karate-junit5</artifactId>
     <version>1.4.0</version>
     <scope>test</scope>
   </dependency>
   ```

2. **Create Karate Configuration**:
   - Setup `karate-config.js`
   - Configure Karate runners
   - Setup reporting

3. **Implement Initial Test Suite**:
   - FTOC CLI tests
   - Output validation tests

4. **Extend FTOC Parser**:
   - Add Karate syntax recognition
   - Support for embedded JSON/JavaScript
   - API call pattern detection

## Benefits to FTOC Project

1. **Complete Test Coverage**: Full test pyramid coverage
2. **API Testing Capability**: New capability for testing API interactions
3. **Improved CLI Testing**: More robust CLI testing through Karate's exec API
4. **Better Reporting**: Enhanced test reporting and visualization
5. **Karate File Support**: Enhanced FTOC capabilities to analyze Karate feature files

## Conclusion

Integrating Karate into the FTOC testing strategy will provide a robust approach for system-level testing while maintaining the existing Cucumber-based tests for acceptance and integration testing. This approach aligns with the project's goals of providing comprehensive feature file analysis and ensuring high-quality test coverage.