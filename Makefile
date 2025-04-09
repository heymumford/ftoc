# FTOC Makefile - Convenience targets for development
# All build operations use the centralized build orchestrator

.PHONY: build clean install test unit-test integration-test cucumber-test release package deploy docs version-show version-set version-patch version-minor version-major help

# Default target
all: build

# Use the build orchestrator for all operations
build:
	./util/build-orchestrator.sh build

# Clean build artifacts
clean:
	./util/build-orchestrator.sh clean

# Run all tests
test:
	./util/build-orchestrator.sh test

# Run unit tests only
unit-test:
	./util/build-orchestrator.sh unit-test

# Run integration tests only
integration-test:
	./util/build-orchestrator.sh integration-test

# Run cucumber tests only
cucumber-test:
	./util/build-orchestrator.sh cucumber-test

# Install to local Maven repository
install: build
	mvn install

# Package without running tests
package:
	./util/build-orchestrator.sh build --skip-tests

# Run FTOC on a specified directory (use as: make run DIR=./path/to/features)
run: build
	java -jar target/ftoc-*-jar-with-dependencies.jar -d $(DIR)

# Generate project documentation
docs:
	./util/build-orchestrator.sh docs

# Release operations
release-patch:
	./util/build-orchestrator.sh release patch

release-minor:
	./util/build-orchestrator.sh release minor

release-major:
	./util/build-orchestrator.sh release major

# Version operations
version-show:
	./util/build-orchestrator.sh version show

version-set:
	@if [ -z "$(V)" ]; then \
		echo "Error: Version not specified. Use 'make version-set V=x.y.z'"; \
		exit 1; \
	fi
	./util/build-orchestrator.sh version set $(V)

# Deploy artifacts
deploy: build
	./util/build-orchestrator.sh deploy

# Help target
help:
	@echo "FTOC Development Targets"
	@echo "-----------------------"
	@echo "make build              - Build the project"
	@echo "make clean              - Clean build artifacts"
	@echo "make test               - Run all tests"
	@echo "make unit-test          - Run unit tests only"
	@echo "make integration-test   - Run integration tests only"
	@echo "make cucumber-test      - Run cucumber tests only"
	@echo "make package            - Package without running tests"
	@echo "make install            - Install to local Maven repository"
	@echo "make run DIR=path       - Run FTOC on specified directory"
	@echo "make docs               - Generate documentation"
	@echo "make release-patch      - Create a patch release (0.0.X)"
	@echo "make release-minor      - Create a minor release (0.X.0)"
	@echo "make release-major      - Create a major release (X.0.0)"
	@echo "make version-show       - Show current version"
	@echo "make version-set V=x.y.z - Set specific version"
	@echo "make deploy             - Deploy build artifacts"
	@echo "make help               - Show this help message"