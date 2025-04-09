# FTOC Makefile - Convenience targets for development
# See docs/developer/VERSION_MANAGEMENT.md for detailed versioning info

.PHONY: build clean install test run doc version-show version-patch version-minor version-major

# Default target
all: clean build

# Build with Maven
build:
	mvn clean package

# Clean build artifacts
clean:
	mvn clean

# Run tests
test:
	mvn test

# Install to local Maven repository
install: build
	mvn install

# Run FTOC on a specified directory (use as: make run DIR=./path/to/features)
run: build
	java -jar target/ftoc-*-jar-with-dependencies.jar -d $(DIR)

# Generate project documentation
doc:
	@echo "Generating documentation"
	mvn javadoc:javadoc
	@echo "Documentation available at: target/site/apidocs/index.html"

# Show current version
version-show:
	./version get

# Bump patch version (0.0.X)
version-patch:
	./version patch

# Bump minor version (0.X.0)
version-minor:
	./version minor

# Bump major version (X.0.0)
version-major:
	./version major

# Help target
help:
	@echo "FTOC Development Targets"
	@echo "-----------------------"
	@echo "make build        - Build the project"
	@echo "make clean        - Clean build artifacts"
	@echo "make test         - Run tests"
	@echo "make run DIR=path - Run FTOC on specified directory"
	@echo "make doc          - Generate JavaDoc documentation"
	@echo "make version-show - Show current version"
	@echo "make version-patch - Increment patch version"
	@echo "make version-minor - Increment minor version"
	@echo "make version-major - Increment major version"
	@echo "make help         - Show this help message"