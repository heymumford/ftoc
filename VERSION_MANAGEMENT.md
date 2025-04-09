# FTOC Version Management System

This document describes the version management system for the FTOC project.

## Overview

The FTOC project uses a semantic versioning system (MAJOR.MINOR.PATCH) with a centralized version management script to ensure consistency across all components.

## Key Features

- **Single Source of Truth**: The version is defined in exactly one place (pom.xml)
- **Semantic Versioning**: Following [SemVer](https://semver.org/) specification
- **Automated Updates**: Simple commands to increment versions
- **Git Integration**: Automatic tagging of new versions
- **Build Verification**: Runs tests and verifies version consistency
- **CI/CD Integration**: For minor and major updates, runs GitHub Actions locally

## Usage

The version management system is controlled through the `version.sh` script:

```bash
./version.sh [command]
```

### Commands

| Command | Description | Example |
|---------|-------------|---------|
| `get` | Display current version | `./version.sh get` |
| `patch` | Increment patch version (x.y.Z) | `./version.sh patch` |
| `minor` | Increment minor version (x.Y.0) | `./version.sh minor` |
| `major` | Increment major version (X.0.0) | `./version.sh major` |
| `set x.y.z` | Set specific version | `./version.sh set 1.2.3` |
| `help` | Display help message | `./version.sh help` |

## Implementation Details

The version management system:

1. **Updates**:
   - POM file (Maven project definition)
   - Properties file used for runtime version reporting

2. **Verifies**:
   - Runs all tests after version update
   - Validates that the JAR file reports the correct version when executed

3. **Git Integration**:
   - Creates git tags for each version
   - For minor and major version changes, verifies GitHub Actions pipeline

## Best Practices

- **Small Increments**: Patch versions for bug fixes, minor for features, major for breaking changes
- **Commit Before Versioning**: Commit all changes before running the version script
- **Test After Update**: Always verify application behavior after updating versions
- **Consistent Naming**: Version numbers in documentation should follow the same format

## Troubleshooting

If you encounter issues with version synchronization:

1. Check if the version is correctly set in pom.xml using xmlstarlet:
   ```bash
   xmlstarlet sel -N pom="http://maven.apache.org/POM/4.0.0" -t -v "/pom:project/pom:version" pom.xml
   ```

2. Ensure the properties file is generated correctly:
   ```bash
   cat target/classes/ftoc-version.properties
   ```

3. Verify git tags match the expected version:
   ```bash
   git tag -l
   ```