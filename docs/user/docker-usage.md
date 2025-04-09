# Using FTOC with Docker

FTOC can be run as a Docker container, making it easy to use without installing Java or any dependencies directly on your system.

## Quick Start

The simplest way to use FTOC with Docker is with the provided wrapper script:

```bash
# Build the Docker image (first time only)
./docker-ftoc.sh --build

# Run FTOC on your feature files
./docker-ftoc.sh --directory ./path/to/feature/files

# Generate HTML output
./docker-ftoc.sh --format html
```

## Using Docker Directly

### Building the Image

```bash
docker build -t ftoc .
```

### Running FTOC

```bash
# Create a directory for the output
mkdir -p target/docker-output

# Run FTOC on a directory of feature files
docker run --rm \
  -v "/path/to/feature/files:/data" \
  -v "$(pwd)/target/docker-output:/output" \
  ftoc --directory /data --output-directory /output --format markdown
```

## Docker Compose

The repository includes a `docker-compose.yml` file for development and running FTOC:

```bash
# Run FTOC on the test feature files
docker-compose up ftoc

# Start a development environment
docker-compose up -d ftoc-dev

# Run commands in the development container
docker-compose exec ftoc-dev mvn test
```

## Environment Variables

The Docker image supports configuration through environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| FTOC_OUTPUT_FORMAT | Output format (text, markdown, html, json) | text |
| FTOC_CONFIG_FILE | Path to custom warning configuration file | /opt/ftoc/config/ftoc-warnings.yml |

Example:

```bash
docker run --rm \
  -v "/path/to/feature/files:/data" \
  -v "$(pwd)/target/docker-output:/output" \
  -e FTOC_OUTPUT_FORMAT=html \
  ftoc --directory /data --output-directory /output
```

## CI/CD Integration

The Docker image is published to GitHub Container Registry and can be used in your CI/CD pipelines:

```bash
# Pull the latest stable version
docker pull ghcr.io/heymumford/ftoc:latest

# Run FTOC in your CI/CD pipeline
docker run --rm \
  -v "$(pwd)/features:/data" \
  -v "$(pwd)/reports:/output" \
  ghcr.io/heymumford/ftoc:latest \
  --directory /data --output-directory /output --junit-report
```

## Advanced Configuration

### Custom Warnings Configuration

You can mount your own warnings configuration file:

```bash
docker run --rm \
  -v "/path/to/feature/files:/data" \
  -v "$(pwd)/target/docker-output:/output" \
  -v "$(pwd)/my-config.yml:/opt/ftoc/config/custom-config.yml" \
  -e FTOC_CONFIG_FILE=/opt/ftoc/config/custom-config.yml \
  ftoc --directory /data --output-directory /output --config-file /opt/ftoc/config/custom-config.yml
```

### Docker Multi-Stage Build

The Dockerfile uses a multi-stage build to minimize the image size:

1. **Builder Stage**: Uses Maven to build the application
2. **Runtime Stage**: Uses a minimal JRE image to run the application

This results in a smaller, more secure Docker image.

### Security Considerations

The Docker image:
- Runs as a non-root user
- Contains only the necessary runtime dependencies
- Follows Docker best practices for security