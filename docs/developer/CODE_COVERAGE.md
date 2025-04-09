# Code Coverage in FTOC

This document outlines the code coverage setup for the FTOC project, including how to run coverage reports locally and interpret the results.

## Overview

FTOC uses JaCoCo (Java Code Coverage) to measure and track code coverage. Coverage reports are automatically generated during CI/CD builds and displayed in the following ways:

- **README Badge**: Shows current coverage percentage on the main README
- **PR Comments**: Detailed coverage information on pull requests
- **GitHub Actions**: Coverage summary in GitHub Actions job outputs
- **HTML Reports**: Detailed drill-down reports as build artifacts

## Current Coverage

[![Coverage](../../.github/badges/coverage.svg)](../../.github/badges/jacoco.csv)

## Running Coverage Locally

### Maven Command

To generate a coverage report locally:

```bash
mvn clean test jacoco:report
```

This will:
1. Run all tests
2. Generate coverage data
3. Create reports in `target/site/jacoco/`

### Viewing Reports

After running the command above, you can view the report:

```bash
# On Linux/macOS
open target/site/jacoco/index.html

# On Windows
start target/site/jacoco/index.html
```

## Coverage Requirements

The project is configured with the following coverage requirements:

```xml
<rule>
  <element>BUNDLE</element>
  <limits>
    <limit>
      <counter>COMPLEXITY</counter>
      <value>COVEREDRATIO</value>
      <minimum>0.30</minimum>
    </limit>
  </limits>
</rule>
```

This means:
- At least 30% of code complexity must be covered by tests
- Builds will fail if coverage drops below this threshold

## Interpreting Coverage Reports

JaCoCo provides several types of coverage metrics:

1. **Instruction Coverage**: Measures the percentage of bytecode instructions that have been executed
2. **Branch Coverage**: Measures the percentage of branches (if/else, switch cases) that have been executed
3. **Complexity Coverage**: Measures the percentage of code complexity (cyclomatic complexity) that has been tested
4. **Line Coverage**: Measures the percentage of lines of code that have been executed
5. **Method Coverage**: Measures the percentage of methods that have been called
6. **Class Coverage**: Measures the percentage of classes that have been loaded

In the HTML report, the colors indicate:
- **Green**: Fully covered
- **Yellow**: Partially covered
- **Red**: Not covered

## Coverage in CI/CD

The CI/CD pipeline is configured to:

1. Generate coverage reports during the build process
2. Create and upload badges showing current coverage
3. Comment on PRs with coverage changes
4. Store coverage reports as artifacts

### PR Integration

When you open a PR, the workflow will:

1. Calculate the coverage for your changes
2. Compare it with the coverage on the main branch
3. Add a comment showing:
   - Overall coverage summary
   - Coverage change from base branch
   - Files with coverage changes

## Improving Coverage

To improve code coverage:

1. Focus on covering high-complexity code first
2. Write tests for error handling paths
3. Use parameterized tests for testing multiple inputs
4. Use mocks for external dependencies
5. Run the coverage report locally to identify uncovered areas

### Most Important Areas to Cover

1. Core business logic
2. Error handling paths
3. Edge cases
4. Complex conditional logic

## Coverage Reports in Releases

For each release, a summary of code coverage is included in the release notes. This helps track coverage improvements over time.

## Exclusions

Certain types of code are excluded from coverage requirements:

1. Generated code
2. Simple getters and setters
3. Empty constructors
4. Test code itself

## Future Improvements

Planned improvements to coverage tracking:

1. Class-level and package-level coverage thresholds
2. Historical coverage trends
3. Coverage visualization in documentation