#!/bin/bash

# Script to generate specific FTOC reports

# Display usage if no arguments provided
if [ $# -eq 0 ]; then
    echo "Usage: ftoc:report <report-type> <directory> [options]"
    echo "Report types:"
    echo "  concordance          Generate tag concordance report"
    echo "  tags                 Generate tag quality analysis report"
    echo "  anti-patterns        Generate anti-pattern detection report"
    echo "Options:"
    echo "  --format <format>    Output format (text, md, html, json, junit)"
    exit 1
fi

# Get the report type and directory
REPORT_TYPE=$1
DIR=$2
shift 2

# Validate report type
if [[ ! "$REPORT_TYPE" =~ ^(concordance|tags|anti-patterns)$ ]]; then
    echo "Error: Invalid report type. Must be one of: concordance, tags, anti-patterns"
    exit 1
fi

# Check if the directory exists
if [ ! -d "$DIR" ]; then
    echo "Error: Directory '$DIR' not found."
    exit 1
fi

# Run FTOC with appropriate options based on report type
echo "Generating $REPORT_TYPE report for directory: $DIR"

case $REPORT_TYPE in
    concordance)
        java -jar target/ftoc-0.5.3-jar-with-dependencies.jar -d "$DIR" --concordance "$@"
        ;;
    tags)
        java -jar target/ftoc-0.5.3-jar-with-dependencies.jar -d "$DIR" --analyze-tags "$@"
        ;;
    anti-patterns)
        java -jar target/ftoc-0.5.3-jar-with-dependencies.jar -d "$DIR" --detect-anti-patterns "$@"
        ;;
esac