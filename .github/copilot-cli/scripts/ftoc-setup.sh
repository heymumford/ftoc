#!/bin/bash

# Script to set up FTOC environment and dependencies

# Display usage
if [ "$1" == "--help" ]; then
    echo "Usage: ftoc:setup [options]"
    echo "Options:"
    echo "  --dev                Set up development environment"
    echo "  --docker             Set up Docker environment"
    echo "  --ci                 Set up CI environment"
    exit 0
fi

# Parse setup options
SETUP_TYPE="standard"

for arg in "$@"; do
    case $arg in
        --dev)
            SETUP_TYPE="dev"
            ;;
        --docker)
            SETUP_TYPE="docker"
            ;;
        --ci)
            SETUP_TYPE="ci"
            ;;
    esac
done

# Validate Java installation
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH."
    echo "Please install Java 11 or later and try again."
    exit 1
fi

# Validate Maven installation
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH."
    echo "Please install Maven 3.6 or later and try again."
    exit 1
fi

# Setup based on type
case $SETUP_TYPE in
    dev)
        echo "Setting up FTOC development environment..."
        
        # Install dependencies
        echo "Installing dependencies..."
        mvn dependency:resolve
        
        # Create sample feature files directory for testing
        mkdir -p src/test/resources/features/samples
        
        # Create output directory
        mkdir -p output
        
        # Set up git hooks if git is available
        if command -v git &> /dev/null && [ -d ".git" ]; then
            echo "Setting up git hooks..."
            cp -f .github/hooks/* .git/hooks/ 2>/dev/null || true
            chmod +x .git/hooks/* 2>/dev/null || true
        fi
        
        echo "Development environment setup complete."
        ;;
        
    docker)
        echo "Setting up FTOC Docker environment..."
        
        # Check for Docker
        if ! command -v docker &> /dev/null; then
            echo "Error: Docker is not installed or not in PATH."
            exit 1
        fi
        
        # Build the Docker image
        echo "Building Docker image..."
        docker build -t ftoc .
        
        echo "Docker environment setup complete."
        echo "Run FTOC using: docker run -v $(pwd):/data ftoc [options]"
        ;;
        
    ci)
        echo "Setting up FTOC CI environment..."
        
        # Install dependencies needed for CI
        echo "Installing dependencies..."
        mvn dependency:resolve -P ci
        
        echo "CI environment setup complete."
        ;;
        
    *)
        # Standard setup
        echo "Setting up FTOC standard environment..."
        
        # Compile the project
        echo "Compiling FTOC..."
        mvn compile
        
        # Create necessary directories
        mkdir -p output
        
        echo "Standard environment setup complete."
        ;;
esac

# Final steps
echo "FTOC setup completed successfully."
echo "Run 'ftoc:build' to build the project."
echo "Run 'ftoc:test' to run tests."
echo "Run 'ftoc:analyze <directory>' to analyze feature files."