# JUnit XML Report Integration

FTOC provides JUnit XML report format output that makes it easy to integrate with Continuous Integration (CI) systems like Jenkins, GitHub Actions, GitLab CI, and more.

## Overview

JUnit XML is a standard report format supported by most CI/CD systems for displaying test results. FTOC can generate its analysis reports in JUnit XML format, allowing:

- Integration with CI/CD dashboards
- Historical tracking of quality metrics
- Setting up quality gates based on report results
- Creating alerts when quality metrics don't meet expectations

## Generating JUnit Reports

There are several ways to generate JUnit XML reports with FTOC:

### Option 1: Use the `--junit-report` flag (Recommended)

```bash
ftoc --junit-report --analyze-tags --detect-anti-patterns -d ./features
```

This flag sets all report formats to JUnit XML.

### Option 2: Specify individual JUnit formats

```bash
ftoc --tag-quality-format junit --concordance-format junit -d ./features
```

### Option 3: Use `--format junit` to set all formats at once

```bash
ftoc --format junit --analyze-tags --detect-anti-patterns -d ./features
```

## JUnit Report Types

FTOC generates different JUnit test suites for each type of analysis:

1. **`FTOC Tag Quality Analysis`**: Reports tag quality issues as test failures
2. **`FTOC Anti-Pattern Analysis`**: Reports anti-pattern issues as test failures
3. **`FTOC Tag Concordance`**: Informational report with tag statistics
4. **`FTOC Table of Contents`**: Informational report with feature structure

## CI/CD Integration Examples

### GitHub Actions

```yaml
jobs:
  quality-checks:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'adopt'
      
      - name: Run FTOC Analysis
        run: |
          # Install FTOC (or use a Docker image)
          java -jar ftoc.jar --junit-report --analyze-tags --detect-anti-patterns -d ./features
          
      - name: Publish Test Report
        uses: mikepenz/action-junit-report@v3
        with:
          report_paths: 'target/junit-reports/*.xml'
          fail_on_failure: true
```

### Jenkins

Add to your Jenkinsfile:

```groovy
pipeline {
    agent any
    stages {
        stage('Feature Quality Analysis') {
            steps {
                sh 'java -jar ftoc.jar --junit-report --analyze-tags --detect-anti-patterns -d ./features'
            }
            post {
                always {
                    junit 'target/junit-reports/*.xml'
                }
            }
        }
    }
}
```

## JUnit Report Structure

The JUnit XML reports follow the standard format:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="FTOC Tag Quality Analysis" tests="5" failures="3" errors="0" skipped="0" timestamp="2023-09-10T12:34:56">
  <properties>
    <property name="analysis_type" value="tag_quality"/>
  </properties>
  <testcase name="tag_quality_check_missing_priority_tag" classname="com.heymumford.ftoc.TagQualityAnalysis">
    <failure type="MISSING_PRIORITY_TAG" message="Found 2 MISSING_PRIORITY_TAG issues">
      Missing priority tag in scenario "Login functionality" (in login.feature)
      Missing priority tag in scenario "User registration" (in registration.feature)
    </failure>
  </testcase>
  <!-- More testcases -->
</testsuite>
```

## Best Practices

1. **Run FTOC as part of your CI pipeline** for every pull request to maintain consistent feature quality

2. **Generate multiple report types** (use both `--analyze-tags` and `--detect-anti-patterns`)

3. **Set appropriate thresholds** in your CI system (e.g., fail build only on critical issues)

4. **Create a custom configuration file** for your project to customize the quality rules

   ```bash
   ftoc --junit-report --config-file .ftoc.yml --analyze-tags --detect-anti-patterns -d ./features
   ```

5. **Combine with other tests** like Cucumber test execution results for a comprehensive quality dashboard

## Common CI Integration Patterns

### Quality Gates

Configure your CI system to fail the build when specific conditions are met, such as:

- More than 5 high-priority tag issues
- Any anti-pattern related to missing priority tags
- Scenarios without appropriate tag categories

### Report Visualization

Most CI systems can display JUnit test results as visual dashboards, showing:

- Trend graphs of quality issues over time
- Specific details about each issue
- Pass/fail status for each quality check

### Notifications

Configure your CI system to send notifications when quality issues are detected:

- Slack/Teams messages for quality regressions
- Email alerts for critical issues
- Comments on pull requests with quality feedback