# XML Standards and Cleanup

This document describes the XML formatting standards used in the FTOC project and the automated cleanup process that ensures consistent formatting across all XML files in the repository.

## XML Standards

All XML files in the project follow these standards:

1. **Proper XML Declaration**: All XML files should include an XML declaration at the top of the file.
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   ```

2. **Consistent Indentation**: 2-space indentation is used throughout XML files.

3. **Well-formed XML**: All XML files must be well-formed, with proper nesting of elements and closing tags.

4. **Explicit Plugin Versions**: In the `pom.xml` file, all Maven plugins must have explicitly defined versions for build reproducibility.

5. **Property-based Versioning**: Where possible, all dependency versions in the `pom.xml` file should use Maven properties for easy management.

6. **No Duplicate Dependencies**: The `pom.xml` file should not contain duplicate dependencies.

## Automated Cleanup Process

FTOC includes an automated XML cleanup process that is triggered during the build process. This ensures that all XML files in the repository maintain consistent formatting and structure.

### How It Works

1. A build counter tracks the number of builds that have been executed.

2. Every 10 builds, the XML cleanup script is run automatically.

3. The script does the following:
   - Validates all XML files in the repository
   - Formats them according to the project's standards (2-space indentation, etc.)
   - Checks for issues in the `pom.xml` such as duplicate dependencies or missing plugin versions
   - Generates a report of all the changes made

### Manual Execution

You can also run the XML cleanup process manually at any time:

```bash
./config/scripts/xml-cleanup.sh --force
```

### Build Integration

The XML cleanup process is integrated into the Maven build process via the `maven-antrun-plugin` in the `validate` phase. This ensures that the XML files are checked and formatted early in the build process.

## Configuration Files

The XML cleanup script has a few configuration points:

1. **Build Counter**: Located at `config/.build-count`, this file tracks the number of builds since the last cleanup.

2. **Log File**: Located at `config/xml-cleanup.log`, this file contains the results of the last cleanup run.

## Tools Used

The XML cleanup process uses the following tools:

- **XMLStarlet**: A command-line tool for validating and transforming XML files.

## Benefits

The automated XML cleanup process provides several benefits:

1. **Consistency**: All XML files in the project follow the same formatting style.

2. **Early Detection of Issues**: Problems in XML files are detected early in the build process.

3. **Improved Code Review**: Consistent formatting makes it easier to review changes to XML files.

4. **Reduced Merge Conflicts**: Standardized formatting reduces the likelihood of merge conflicts in XML files.

## Future Enhancements

Potential future enhancements to the XML cleanup process include:

1. **Schema Validation**: Add support for validating XML files against their respective schemas.

2. **Dependency Analysis**: Analyze dependencies for security vulnerabilities or newer versions.

3. **Integration with CI/CD**: Add more detailed reporting in CI/CD environments.