#!/bin/bash

# Script to run FTOC performance benchmarks

# Display usage with options
if [ "$1" == "--help" ] || [ $# -eq 0 ]; then
    echo "Usage: ftoc:benchmark [options]"
    echo "Options:"
    echo "  --small              Run benchmarks on small repositories (10 files)"
    echo "  --medium             Run benchmarks on medium repositories (50 files)"
    echo "  --large              Run benchmarks on large repositories (200 files)"
    echo "  --very-large         Run benchmarks on very large repositories (500 files)"
    echo "  --all                Run benchmarks on all repository sizes"
    echo "  --report <file>      Specify report output file (default: benchmark-report.txt)"
    echo "  --no-cleanup         Do not delete temporary files after benchmark"
    exit 0
fi

# Build the jar if not already built
if [ ! -f "target/ftoc-0.5.3-jar-with-dependencies.jar" ]; then
    echo "Building FTOC jar..."
    mvn clean package -DskipTests
fi

# Run benchmark
echo "Running FTOC benchmarks..."
java -jar target/ftoc-0.5.3-jar-with-dependencies.jar --benchmark "$@"