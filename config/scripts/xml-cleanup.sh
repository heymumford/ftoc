#!/bin/bash
#
# XML Clean-up and Formatting Script
# This script formats all XML files in the repository to ensure consistent style and structure.
# It should be run periodically (every 10 builds) to maintain consistency.
#

set -e

REPO_ROOT="$(git rev-parse --show-toplevel)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_COUNT_FILE="${REPO_ROOT}/config/.build-count"
LOG_FILE="${REPO_ROOT}/config/xml-cleanup.log"

# Create the build count file if it doesn't exist
if [ ! -f "$BUILD_COUNT_FILE" ]; then
  echo "1" > "$BUILD_COUNT_FILE"
fi

# Increment build count
BUILD_COUNT=$(cat "$BUILD_COUNT_FILE")
NEW_BUILD_COUNT=$((BUILD_COUNT + 1))
echo "$NEW_BUILD_COUNT" > "$BUILD_COUNT_FILE"

# Check if xmlstarlet is installed
if ! command -v xmlstarlet &> /dev/null; then
  echo "XMLStarlet is not installed. Please install it first."
  echo "  Debian/Ubuntu: sudo apt-get install xmlstarlet"
  echo "  CentOS/RHEL: sudo yum install xmlstarlet"
  echo "  macOS: brew install xmlstarlet"
  exit 1
fi

echo "XML Cleanup - Build count: $NEW_BUILD_COUNT"

# Run every 10 builds or when forced
if [[ "$NEW_BUILD_COUNT" -ge 10 || "$1" == "--force" ]]; then
  echo "Running XML cleanup and formatting..."
  # Reset build count
  echo "1" > "$BUILD_COUNT_FILE"
  
  # Find all XML files in the repo (excluding target directories)
  echo "Finding all XML files..."
  XML_FILES=$(find "$REPO_ROOT" -name "*.xml" | grep -v "/target/" | grep -v "/\.git/")
  
  # Initialize log file
  echo "XML Cleanup - $(date)" > "$LOG_FILE"
  echo "Processed files:" >> "$LOG_FILE"
  
  # Process each XML file
  for XML_FILE in $XML_FILES; do
    FILENAME=$(basename "$XML_FILE")
    echo "Processing $FILENAME..."
    
    # Validate XML file
    if xmlstarlet val -q "$XML_FILE"; then
      # Format XML file to temporary file
      TEMP_FILE=$(mktemp)
      xmlstarlet fo -s 2 "$XML_FILE" > "$TEMP_FILE"
      
      # Check if files differ
      if ! diff -q "$XML_FILE" "$TEMP_FILE" &> /dev/null; then
        # Files differ, so update the original
        cp "$TEMP_FILE" "$XML_FILE"
        echo "  - $FILENAME: Reformatted" >> "$LOG_FILE"
      else
        echo "  - $FILENAME: Already properly formatted" >> "$LOG_FILE"
      fi
      
      # Clean up temp file
      rm "$TEMP_FILE"
    else
      echo "  - $FILENAME: INVALID XML! Skipping." >> "$LOG_FILE"
    fi
  done
  
  # Perform additional checks for pom.xml
  POM_FILE="$REPO_ROOT/pom.xml"
  if [ -f "$POM_FILE" ]; then
    echo "Checking POM dependencies and plugins..."
    
    # Check for duplicate dependencies
    echo "Checking for duplicate dependencies in pom.xml..." >> "$LOG_FILE"
    DUPLICATES=$(xmlstarlet sel -t -m "//dependency[following::dependency[artifactId=current()/artifactId and groupId=current()/groupId]]" -v "concat(groupId, ':', artifactId)" -n "$POM_FILE" | sort | uniq)
    
    if [ -n "$DUPLICATES" ]; then
      echo "  WARNING: Duplicate dependencies found:" >> "$LOG_FILE"
      echo "$DUPLICATES" | while read DEP; do
        echo "    - $DEP" >> "$LOG_FILE"
      done
    else
      echo "  No duplicate dependencies found." >> "$LOG_FILE"
    fi
    
    # Check for missing version properties
    echo "Checking for hard-coded versions instead of properties..." >> "$LOG_FILE"
    HARD_CODED=$(xmlstarlet sel -t -m "//dependency[not(version[contains(text(),'\${')])]" -v "concat(groupId, ':', artifactId, '=', version)" -n "$POM_FILE")
    
    if [ -n "$HARD_CODED" ]; then
      echo "  INFO: Dependencies with hard-coded versions (consider using properties):" >> "$LOG_FILE"
      echo "$HARD_CODED" | while read DEP; do
        echo "    - $DEP" >> "$LOG_FILE"
      done
    fi
    
    # Check for plugin versions
    echo "Checking for plugins without versions..." >> "$LOG_FILE"
    MISSING_VER=$(xmlstarlet sel -t -m "//plugin[not(version)]" -v "concat(groupId, ':', artifactId)" -n "$POM_FILE")
    
    if [ -n "$MISSING_VER" ]; then
      echo "  WARNING: Plugins without explicit versions:" >> "$LOG_FILE"
      echo "$MISSING_VER" | while read PLUG; do
        echo "    - $PLUG" >> "$LOG_FILE"
      done
    else
      echo "  All plugins have explicit versions." >> "$LOG_FILE"
    fi
  fi
  
  echo "XML cleanup complete! See $LOG_FILE for details."
else
  echo "Skipping XML cleanup. Will run after $((10 - NEW_BUILD_COUNT)) more builds."
  echo "Run with --force to execute immediately."
fi

exit 0