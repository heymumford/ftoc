#!/bin/bash
#
# FTOC Docker Wrapper
# Simplifies running FTOC using Docker
#

set -e

# Configuration
IMAGE_NAME="ftoc"
DEFAULT_FORMAT="markdown"
DEFAULT_CONFIG="config/ftoc-warnings.yml"

# Make sure we have a local output directory
mkdir -p target/docker-output

# Display usage
function show_help {
    echo "FTOC Docker Wrapper"
    echo ""
    echo "Usage: $0 [options] [command]"
    echo ""
    echo "Options:"
    echo "  --build                 Build the Docker image"
    echo "  --directory DIR         Directory to analyze (default: src/test/resources/ftoc/features)"
    echo "  --format FORMAT         Output format (default: $DEFAULT_FORMAT)"
    echo "  --help                  Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --build              Build the Docker image"
    echo "  $0 --directory ./features     Analyze features in ./features directory"
    echo "  $0 --format html              Generate output in HTML format"
    echo "  $0 --help                     Show help"
    echo ""
}

# Build the Docker image
function build_image {
    echo "Building the FTOC Docker image..."
    docker build -t $IMAGE_NAME .
    echo "Done!"
}

# Process command line arguments
DIRECTORY="src/test/resources/ftoc/features"
FORMAT="$DEFAULT_FORMAT"
BUILD=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --help)
            show_help
            exit 0
            ;;
        --build)
            BUILD=true
            shift
            ;;
        --directory)
            DIRECTORY="$2"
            shift 2
            ;;
        --format)
            FORMAT="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Build the image if requested
if $BUILD; then
    build_image
fi

# Check if the image exists
if [[ "$(docker images -q $IMAGE_NAME 2> /dev/null)" == "" ]]; then
    echo "FTOC Docker image not found. Building it..."
    build_image
fi

# Convert directory path to absolute path
DIRECTORY=$(realpath "$DIRECTORY")

# Run FTOC in Docker
echo "Running FTOC on $DIRECTORY with format: $FORMAT"
docker run --rm -v "$DIRECTORY:/data" -v "$(pwd)/target/docker-output:/output" $IMAGE_NAME --directory /data --output-directory /output --format $FORMAT

echo "Done! Output is in target/docker-output/"