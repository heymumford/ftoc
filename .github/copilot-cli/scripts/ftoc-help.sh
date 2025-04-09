#!/bin/bash

# Script to show help for FTOC commands and options

# Display general help if no arguments
if [ $# -eq 0 ]; then
    echo "FTOC - Feature Table of Contents Utility"
    echo "========================================="
    echo
    echo "Available Copilot CLI Commands:"
    echo
    echo "  ftoc:analyze <directory> [options]     Run FTOC analysis on feature files"
    echo "  ftoc:report <type> <directory>         Generate a specific report type"
    echo "  ftoc:build [options]                   Build the FTOC project"
    echo "  ftoc:test [options]                    Run tests for the FTOC project"
    echo "  ftoc:benchmark [options]               Run performance benchmarks"
    echo "  ftoc:version [--verbose|-v]            Display version information"
    echo "  ftoc:setup [options]                   Set up FTOC environment"
    echo "  ftoc:help [command]                    Show help for a specific command"
    echo "  ftoc:generate [options]                Generate sample feature file"
    echo "  ftoc:validate <file> [options]         Validate feature files"
    echo
    echo "For help on a specific command, run: ftoc:help <command>"
    exit 0
fi

# Show help for a specific command
COMMAND=$1

case $COMMAND in
    analyze)
        java -jar target/ftoc-0.5.3-jar-with-dependencies.jar --help
        ;;
        
    report)
        echo "ftoc:report - Generate a specific report type"
        echo
        echo "Usage: ftoc:report <report-type> <directory> [options]"
        echo
        echo "Report types:"
        echo "  concordance          Generate tag concordance report"
        echo "  tags                 Generate tag quality analysis report"
        echo "  anti-patterns        Generate anti-pattern detection report"
        echo
        echo "Options:"
        echo "  --format <format>    Output format (text, md, html, json, junit)"
        ;;
        
    build)
        echo "ftoc:build - Build the FTOC project"
        echo
        echo "Usage: ftoc:build [options]"
        echo
        echo "Options:"
        echo "  --clean              Clean and rebuild"
        echo "  --test               Include tests"
        echo "  --package            Create distributable package"
        echo "  --skip-tests         Skip running tests"
        ;;
        
    test)
        echo "ftoc:test - Run tests for the FTOC project"
        echo
        echo "Usage: ftoc:test [options] [test-name]"
        echo
        echo "Options:"
        echo "  --unit               Run unit tests only"
        echo "  --integration        Run integration tests only"
        echo "  --cucumber           Run cucumber tests only"
        echo "  --karate             Run karate tests only"
        echo "  --benchmark          Run benchmark tests"
        echo "  --coverage           Generate code coverage report"
        ;;
        
    benchmark)
        echo "ftoc:benchmark - Run performance benchmarks"
        echo
        echo "Usage: ftoc:benchmark [options]"
        echo
        echo "Options:"
        echo "  --small              Run benchmarks on small repositories (10 files)"
        echo "  --medium             Run benchmarks on medium repositories (50 files)"
        echo "  --large              Run benchmarks on large repositories (200 files)"
        echo "  --very-large         Run benchmarks on very large repositories (500 files)"
        echo "  --all                Run benchmarks on all repository sizes"
        echo "  --report <file>      Specify report output file (default: benchmark-report.txt)"
        echo "  --no-cleanup         Do not delete temporary files after benchmark"
        ;;
        
    version)
        echo "ftoc:version - Display FTOC version information"
        echo
        echo "Usage: ftoc:version [--verbose|-v]"
        echo
        echo "Options:"
        echo "  --verbose, -v        Show detailed version and environment information"
        ;;
        
    setup)
        echo "ftoc:setup - Set up FTOC environment and dependencies"
        echo
        echo "Usage: ftoc:setup [options]"
        echo
        echo "Options:"
        echo "  --dev                Set up development environment"
        echo "  --docker             Set up Docker environment"
        echo "  --ci                 Set up CI environment"
        ;;
        
    generate)
        echo "ftoc:generate - Generate sample feature file with best practices"
        echo
        echo "Usage: ftoc:generate [options] <output-file>"
        echo
        echo "Options:"
        echo "  --template <type>    Template type (basic, detailed, api-test, ui-test)"
        echo "  --tags <tags>        Tags to include (comma-separated)"
        echo "  --scenarios <num>    Number of scenarios to generate (default: 3)"
        ;;
        
    validate)
        echo "ftoc:validate - Validate feature files against best practices"
        echo
        echo "Usage: ftoc:validate <file-or-directory> [options]"
        echo
        echo "Options:"
        echo "  --strict             Enable strict validation"
        echo "  --fix                Attempt to fix issues automatically"
        echo "  --report <file>      Write validation report to file"
        ;;
        
    *)
        echo "Unknown command: $COMMAND"
        echo "Run 'ftoc:help' for a list of available commands."
        exit 1
        ;;
esac