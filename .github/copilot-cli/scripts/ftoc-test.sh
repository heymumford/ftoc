#!/bin/bash

# Script to run tests for the FTOC project

# Display usage with options
if [ "$1" == "--help" ]; then
    echo "Usage: ftoc:test [options] [test-name]"
    echo "Options:"
    echo "  --unit               Run unit tests only"
    echo "  --integration        Run integration tests only"
    echo "  --cucumber           Run cucumber tests only"
    echo "  --karate             Run karate tests only"
    echo "  --benchmark          Run benchmark tests"
    echo "  --coverage           Generate code coverage report"
    exit 0
fi

# Parse test options
TEST_ARGS=""
TEST_TYPE=""

for arg in "$@"; do
    if [[ $arg == --* ]]; then
        case $arg in
            --unit)
                TEST_TYPE="unit"
                ;;
            --integration)
                TEST_TYPE="integration"
                ;;
            --cucumber)
                TEST_TYPE="cucumber"
                ;;
            --karate)
                TEST_TYPE="karate"
                ;;
            --benchmark)
                TEST_TYPE="benchmark"
                ;;
            --coverage)
                TEST_ARGS="$TEST_ARGS jacoco:report"
                ;;
        esac
    else
        # If not an option, it's a test name
        TEST_NAME=$arg
    fi
done

# Run tests based on type
case $TEST_TYPE in
    unit)
        echo "Running unit tests..."
        if [ -n "$TEST_NAME" ]; then
            mvn test -Dtest=$TEST_NAME $TEST_ARGS
        else
            mvn test -Dtest="*Test" $TEST_ARGS
        fi
        ;;
    integration)
        echo "Running integration tests..."
        mvn failsafe:integration-test $TEST_ARGS
        ;;
    cucumber)
        echo "Running cucumber tests..."
        mvn test -Dtest=RunCucumberTest $TEST_ARGS
        ;;
    karate)
        echo "Running karate tests..."
        mvn test -Dtest=KarateRunner $TEST_ARGS
        ;;
    benchmark)
        echo "Running benchmark tests..."
        java -jar target/ftoc-0.5.3-jar-with-dependencies.jar --benchmark --small --medium
        ;;
    *)
        # Default: run all tests
        echo "Running all tests..."
        if [ -n "$TEST_NAME" ]; then
            mvn test -Dtest=$TEST_NAME $TEST_ARGS
        else
            mvn test $TEST_ARGS
        fi
        ;;
esac