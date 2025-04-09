#!/bin/bash

# Script to display FTOC version information

# Check for the verbose flag
if [ "$1" == "--verbose" ] || [ "$1" == "-v" ]; then
    VERBOSE=true
else
    VERBOSE=false
fi

# Get FTOC version
VERSION=$(java -jar target/ftoc-0.5.3-jar-with-dependencies.jar --version)

echo "$VERSION"

# If verbose, show additional information
if [ "$VERBOSE" == "true" ]; then
    echo
    echo "Environment Information:"
    echo "----------------------"
    echo "Java Version: $(java -version 2>&1 | head -n 1)"
    echo "Maven Version: $(mvn --version | head -n 1)"
    echo "Operating System: $(uname -a)"
    
    # Show git information if available
    if command -v git &> /dev/null && [ -d ".git" ]; then
        echo
        echo "Git Information:"
        echo "---------------"
        echo "Branch: $(git branch --show-current)"
        echo "Last Commit: $(git log -1 --pretty=%B | head -n 1)"
        echo "Commit Hash: $(git rev-parse HEAD)"
        echo "Commit Date: $(git log -1 --format=%cd --date=local)"
    fi
fi