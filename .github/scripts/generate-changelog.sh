#!/bin/bash

set -e

# This script generates a changelog based on conventional commits

# Get the version from the pom.xml
VERSION=$(grep -A 1 "<artifactId>ftoc</artifactId>" pom.xml | grep "<version>" | sed -e 's/<version>\(.*\)<\/version>/\1/')

# Check if version exists
if [ -z "$VERSION" ]; then
    echo "Error: Could not extract version from pom.xml"
    exit 1
fi

# Get the previous tag
PREVIOUS_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")

# If no previous tag, use the first commit
if [ -z "$PREVIOUS_TAG" ]; then
    RANGE=$(git rev-list --max-parents=0 HEAD)..HEAD
else
    RANGE="${PREVIOUS_TAG}..HEAD"
fi

# Generate changelog content
echo "## [${VERSION}] - $(date '+%Y-%m-%d')"

# Features
FEATURES=$(git log ${RANGE} --pretty=format:"- %s" --grep="^feat" | sed -e 's/feat(\(.*\)):/\1:/')
if [ ! -z "$FEATURES" ]; then
    echo "### Added"
    echo "$FEATURES"
fi

# Bug fixes
FIXES=$(git log ${RANGE} --pretty=format:"- %s" --grep="^fix" | sed -e 's/fix(\(.*\)):/\1:/')
if [ ! -z "$FIXES" ]; then
    echo "### Fixed"
    echo "$FIXES"
fi

# Improvements
IMPROVEMENTS=$(git log ${RANGE} --pretty=format:"- %s" --grep="^improve\|^refactor\|^perf" | sed -e 's/\(improve\|refactor\|perf\)(\(.*\)):/\2:/')
if [ ! -z "$IMPROVEMENTS" ]; then
    echo "### Changed"
    echo "$IMPROVEMENTS"
fi

# Documentation
DOCS=$(git log ${RANGE} --pretty=format:"- %s" --grep="^docs" | sed -e 's/docs(\(.*\)):/\1:/')
if [ ! -z "$DOCS" ]; then
    echo "### Documentation"
    echo "$DOCS"
fi

# Security
SECURITY=$(git log ${RANGE} --pretty=format:"- %s" --grep="^security" | sed -e 's/security(\(.*\)):/\1:/')
if [ ! -z "$SECURITY" ]; then
    echo "### Security"
    echo "$SECURITY"
fi
