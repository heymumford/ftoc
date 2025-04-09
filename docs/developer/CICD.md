# CI/CD Pipeline Documentation

FTOC uses GitHub Actions to implement a comprehensive Continuous Integration and Continuous Deployment (CI/CD) pipeline. This document explains the workflow setup, key components, and how to extend or modify it.

## Overview

The CI/CD pipeline automatically builds, tests, analyzes, and deploys the FTOC project. It's triggered on pull requests, pushes to the main branch, and tagged releases. The workflow can also be triggered manually as needed.

## Workflow Configuration

The main workflow is defined in `.github/workflows/ci-cd.yml` and consists of the following jobs:

1. **Validate**: Checks code quality and XML formatting
2. **Build**: Compiles and packages the application
3. **Analyze**: Runs FTOC's own analysis tools on its own feature files
4. **Release**: Creates GitHub releases for tagged versions
5. **Publish**: Publishes artifacts to GitHub Packages
6. **Notify**: Sends notifications about build status

### Triggers

The workflow runs on:

- **Push events** to the `main` branch
- **Pull request events** targeting the `main` branch
- **Tag events** when a tag following the pattern `v*` is pushed
- **Manual triggers** through GitHub's UI with configurable parameters

## Key Features

### 1. Code Quality Validation

The `validate` job ensures code quality by:

- Running the XML cleanup script to validate and format XML files
- Checking for direct `System.out.println` usage (should use logging instead)

### 2. Build and Test

The `build` job:

- Compiles the code with Maven
- Runs unit and integration tests
- Performs a version consistency check to ensure the version in `pom.xml` matches the runtime version
- Produces executable JARs

### 3. Feature Analysis

The `analyze` job:

- Uses FTOC's own analysis capabilities on its feature files
- Generates JUnit XML reports for tag quality and anti-pattern detection
- Publishes analysis results to GitHub using the JUnit report format

### 4. GitHub Release Creation

The `release` job:

- Automatically creates GitHub releases for tagged versions
- Attaches JAR files to the release
- Generates release notes from commit messages

### 5. Package Publication

The `publish` job:

- Publishes artifacts to GitHub Packages for easy distribution
- Only runs on the main branch or for tagged releases

### 6. Notifications

The `notify` job:

- Provides build status notifications
- Has commented example for Slack integration
- Updates deployment status

## Manual Workflow Dispatch

You can trigger the workflow manually with additional options:

- **run_analysis**: Toggle to enable/disable feature file analysis
- **skip_tests**: Toggle to skip running tests (useful for quick builds)

## JUnit Report Integration

The CI/CD pipeline integrates with FTOC's JUnit report feature (added in v0.5.1), allowing for:

- Visualization of anti-pattern detection results
- Historical tracking of feature file quality
- Status checks for quality gates

## How to Extend the Workflow

### Adding a New Job

To add a new job, insert a new section in the `.github/workflows/ci-cd.yml` file:

```yaml
new-job-name:
  name: Human-Readable Job Name
  runs-on: ubuntu-latest
  # Specify job dependencies if needed
  needs: [build, analyze]
  steps:
    - name: Step Description
      # Step configuration
```

### Adding Quality Gates

You can modify the workflow to fail when quality issues exceed a threshold:

```yaml
- name: Publish Test Report
  uses: mikepenz/action-junit-report@v3
  with:
    report_paths: 'target/junit-reports/*.xml'
    check_name: 'Feature Quality Report'
    fail_on_failure: true  # Change to true to make the workflow fail on quality issues
```

### Integrating with Slack/Teams

Uncomment and modify the Slack integration in the `notify` job to receive build notifications.

## Future Enhancements

Potential enhancements to the CI/CD pipeline include:

1. **Docker Image Publication**: Automatically publish Docker images to Docker Hub or GitHub Container Registry

2. **Automated Documentation**: Generate and publish API documentation to GitHub Pages

3. **Performance Testing**: Add performance benchmarks for large feature file repositories

4. **Cross-Platform Builds**: Add build matrix for testing on multiple operating systems

5. **Dependency Scanning**: Scan dependencies for security vulnerabilities

## Troubleshooting

### Common Issues

1. **Workflow Failure in the Validate Step**: Usually means an XML validation error or code style issue. Check the job logs for details.

2. **Release Creation Failure**: Occurs if the tag doesn't match the version in `pom.xml`. Ensure they're aligned.

3. **Publication Failures**: Often related to permissions. Ensure the workflow has proper permissions to create packages.

### Debugging Workflows

1. Enable debug logging by adding the following secret to your repository: `ACTIONS_RUNNER_DEBUG` set to `true`.

2. Use the `workflow_dispatch` trigger to manually run the workflow with specific options.

3. Check the "Actions" tab in your GitHub repository for detailed logs of each job and step.

## Best Practices

1. **Run the XML Cleanup Script Locally**: Before committing, run `./config/scripts/xml-cleanup.sh --force` to validate XML files.

2. **Version Consistency**: Make sure version numbers in `pom.xml` match tag names when creating releases (follow `v{version}` format).

3. **Update Tests**: Always add or update tests when adding new features to ensure the CI pipeline catches regressions.

4. **Review Workflow Logs**: Regularly check the workflow run logs to identify potential improvements and issues.