#!/bin/bash

# Script to build the FTOC project

# Display usage with options
if [ "$1" == "--help" ]; then
    echo "Usage: ftoc:build [options]"
    echo "Options:"
    echo "  --clean              Clean and rebuild"
    echo "  --test               Include tests"
    echo "  --package            Create distributable package"
    echo "  --skip-tests         Skip running tests"
    exit 0
fi

# Parse build options
MAVEN_ARGS="compile"

for arg in "$@"; do
    case $arg in
        --clean)
            MAVEN_ARGS="clean $MAVEN_ARGS"
            ;;
        --test)
            MAVEN_ARGS="$MAVEN_ARGS test"
            ;;
        --package)
            MAVEN_ARGS="$MAVEN_ARGS package"
            ;;
        --skip-tests)
            MAVEN_ARGS="$MAVEN_ARGS -DskipTests"
            ;;
    esac
done

# Run Maven build
echo "Building FTOC with options: $MAVEN_ARGS"
mvn $MAVEN_ARGS

if [ $? -eq 0 ]; then
    echo "Build successful!"
else
    echo "Build failed."
    exit 1
fi