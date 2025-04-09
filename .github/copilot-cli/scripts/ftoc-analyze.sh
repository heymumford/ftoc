#!/bin/bash

# Script to run FTOC analysis on feature files

# Display usage if no arguments provided
if [ $# -eq 0 ]; then
    echo "Usage: ftoc:analyze <directory> [options]"
    echo "Options:"
    echo "  --performance        Enable performance monitoring"
    echo "  --tags <tags>        Filter by tags (comma-separated)"
    echo "  --exclude-tags <tags>  Exclude tags (comma-separated)"
    echo "  --format <format>    Output format (text, md, html, json)"
    exit 1
fi

# Get the directory to analyze
DIR=$1
shift

# Check if the directory exists
if [ ! -d "$DIR" ]; then
    echo "Error: Directory '$DIR' not found."
    exit 1
fi

# Run FTOC with the directory and any additional options
echo "Running FTOC analysis on directory: $DIR"
java -jar target/ftoc-0.5.3-jar-with-dependencies.jar -d "$DIR" "$@"