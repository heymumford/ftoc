#!/bin/bash

# Script to validate feature files against best practices

# Display usage if no arguments provided
if [ $# -eq 0 ] || [ "$1" == "--help" ]; then
    echo "Usage: ftoc:validate <file-or-directory> [options]"
    echo "Options:"
    echo "  --strict             Enable strict validation"
    echo "  --fix                Attempt to fix issues automatically"
    echo "  --report <file>      Write validation report to file"
    exit 0
fi

# Default values
STRICT=false
FIX=false
REPORT_FILE=""
TARGET_PATH=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --strict)
            STRICT=true
            shift
            ;;
        --fix)
            FIX=true
            shift
            ;;
        --report)
            REPORT_FILE="$2"
            shift 2
            ;;
        *)
            # First non-option argument is the target path
            if [ -z "$TARGET_PATH" ]; then
                TARGET_PATH="$1"
            fi
            shift
            ;;
    esac
done

# Validate target path
if [ -z "$TARGET_PATH" ]; then
    echo "Error: No file or directory specified."
    exit 1
fi

if [ ! -e "$TARGET_PATH" ]; then
    echo "Error: File or directory does not exist: $TARGET_PATH"
    exit 1
fi

# Setup FTOC command
FTOC_CMD="java -jar target/ftoc-0.5.3-jar-with-dependencies.jar"

# Determine if target is a file or directory
if [ -d "$TARGET_PATH" ]; then
    echo "Validating all feature files in directory: $TARGET_PATH"
    
    # Run FTOC with appropriate options
    if [ "$STRICT" = true ]; then
        FTOC_CMD="$FTOC_CMD --detect-anti-patterns --analyze-tags"
    else
        FTOC_CMD="$FTOC_CMD --detect-anti-patterns"
    fi
    
    # Add report file if specified
    if [ -n "$REPORT_FILE" ]; then
        mkdir -p "$(dirname "$REPORT_FILE")"
        $FTOC_CMD -d "$TARGET_PATH" > "$REPORT_FILE"
        echo "Validation report written to: $REPORT_FILE"
    else
        $FTOC_CMD -d "$TARGET_PATH"
    fi
    
    # If fix mode is enabled, inform user (actual fixing would require additional implementation)
    if [ "$FIX" = true ]; then
        echo "Note: Automatic fixing is not yet implemented for directory validation."
    fi
else
    echo "Validating feature file: $TARGET_PATH"
    
    # For single file validation, analyze it in more detail
    if [ "${TARGET_PATH##*.}" = "feature" ]; then
        # Create temporary directory to hold the single file
        TEMP_DIR=$(mktemp -d)
        TEMP_FILE="$TEMP_DIR/$(basename "$TARGET_PATH")"
        cp "$TARGET_PATH" "$TEMP_FILE"
        
        # Run FTOC with appropriate options
        if [ "$STRICT" = true ]; then
            FTOC_CMD="$FTOC_CMD --detect-anti-patterns --analyze-tags"
        else
            FTOC_CMD="$FTOC_CMD --detect-anti-patterns"
        fi
        
        # Add report file if specified
        if [ -n "$REPORT_FILE" ]; then
            mkdir -p "$(dirname "$REPORT_FILE")"
            $FTOC_CMD -d "$TEMP_DIR" > "$REPORT_FILE"
            echo "Validation report written to: $REPORT_FILE"
        else
            $FTOC_CMD -d "$TEMP_DIR"
        fi
        
        # Clean up temp dir
        rm -rf "$TEMP_DIR"
        
        # If fix mode is enabled, provide guidance
        if [ "$FIX" = true ]; then
            echo
            echo "Fix mode is enabled. Common fixes for feature files include:"
            echo "1. Adding missing priority tags (@P0, @P1, etc.)"
            echo "2. Adding missing type tags (@UI, @API, etc.)"
            echo "3. Removing duplicate tags"
            echo "4. Replacing low-value tags with more specific ones"
            echo "5. Ensuring proper Gherkin syntax and structure"
            echo
            echo "Please review the validation report and make the necessary changes."
        fi
    else
        echo "Error: Not a feature file. File must have .feature extension."
        exit 1
    fi
fi