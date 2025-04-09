# FTOC Build Orchestrator

The Build Orchestrator is a single point of entry and single point of truth for all build instructions and artifacts in the FTOC project. It centralizes build processes, version management, and reporting.

## Overview

The build orchestrator provides:

- Consistent build and release process
- Comprehensive version management
- Detailed build reports and summaries
- Unified test execution approach
- Consistent artifact generation

## Usage

### Direct Usage

```bash
./util/build-orchestrator.sh [command] [options]
```

### Via Make

```bash
make [target]
```

## Available Commands

| Command | Description |
|---------|-------------|
| `build` | Perform a full build including clean, compile, test, and package |
| `clean` | Clean build artifacts |
| `test` | Run all tests |
| `unit-test` | Run unit tests only |
| `integration-test` | Run integration tests only |
| `cucumber-test` | Run Cucumber tests only |
| `release [type]` | Create a release (patch, minor, or major) |
| `version [cmd]` | Version management (get, set, show) |
| `deploy` | Deploy build artifacts |
| `docs` | Generate documentation |
| `help` | Display usage information |

## Options

| Option | Description |
|--------|-------------|
| `--skip-tests` | Skip running tests |
| `--verbose` | Enable verbose output |
| `--dry-run` | Show what would be done without making changes |
| `--push` | Push changes to remote repository |

## Build Summary

After each build, the orchestrator generates a detailed summary report in `target/build-logs/build-summary.txt` containing:

1. Version information
   - Current version
   - Build number (git commit count)
   - Git branch, commit, and tag
   - Build timestamp

2. Test results
   - Count of tests run
   - Passed/failed/skipped counts
   - Breakdown by test type (unit, integration, cucumber)

3. Build artifacts
   - Generated artifacts with sizes and checksums
   - JAR file details

4. Changes
   - Summary of changes since the last release

## Version Management

The orchestrator manages versions following semantic versioning:

- **Patch version**: Increments the Z in X.Y.Z (for bug fixes)
- **Minor version**: Increments the Y in X.Y.Z and resets Z to 0 (for new features)
- **Major version**: Increments the X in X.Y.Z and resets Y and Z to 0 (for breaking changes)

When a version is updated:
1. The POM file is updated with the new version
2. A git commit is created for the version change
3. A git tag is created (vX.Y.Z)
4. Tests are run to validate the new version

## Integration with Maven

The orchestrator wraps Maven for all build operations, ensuring consistency across local development and CI environments.

## Examples

### Full Build

```bash
./util/build-orchestrator.sh build
# or
make build
```

### Creating a Release

```bash
./util/build-orchestrator.sh release patch
# or
make release-patch
```

### Running Specific Tests

```bash
./util/build-orchestrator.sh cucumber-test
# or
make cucumber-test
```

### Setting a Specific Version

```bash
./util/build-orchestrator.sh version set 1.2.3
# or
make version-set V=1.2.3
```