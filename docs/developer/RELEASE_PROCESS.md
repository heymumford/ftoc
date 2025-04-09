# Release Process

## Overview

This document describes the process for creating a new release of the FTOC utility.

## Prerequisites

- All changes for the release should be merged into the main branch
- All tests should be passing
- The version number in `pom.xml` should be updated to the new version

## Release Steps

1. Ensure you have the latest code from the main branch:
   ```
   git checkout main
   git pull
   ```

2. Make sure all your changes follow the [Conventional Commits](./CONVENTIONAL_COMMITS.md) format

3. Create a tag with the new version number:
   ```
   git tag -a v1.2.3 -m "Release v1.2.3"
   ```

4. Push the tag to GitHub:
   ```
   git push origin v1.2.3
   ```

5. The GitHub Actions release workflow will automatically:
   - Build the project
   - Generate a changelog entry based on commits since the last tag
   - Create a GitHub release with the changelog and artifacts
   - Update the main CHANGELOG.md file

6. Verify the release on the GitHub Releases page

## Version Numbering

This project follows [Semantic Versioning](https://semver.org/):

- **MAJOR** version for incompatible API changes
- **MINOR** version for backward-compatible functionality additions
- **PATCH** version for backward-compatible bug fixes

## Post-Release

After a release:

1. Update the version in `pom.xml` to the next development version (e.g., `1.2.4-SNAPSHOT`)
2. Commit this change to the main branch

## Hotfixes

For urgent fixes to a released version:

1. Create a branch from the release tag:
   ```
   git checkout -b hotfix/v1.2.3 v1.2.3
   ```

2. Make the necessary changes, following conventional commits

3. Update the version in `pom.xml` (e.g., to `1.2.4`)

4. Create a tag for the hotfix version:
   ```
   git tag -a v1.2.4 -m "Hotfix v1.2.4"
   ```

5. Push the tag to trigger the release process:
   ```
   git push origin v1.2.4
   ```

6. Create a PR to merge the hotfix back to the main branch
