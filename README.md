# ftoc - Feature Table of Contents Utility

FTOC solves the challenge of managing large Cucumber test suites by automatically analyzing feature files to improve discoverability, ensure tag consistency, and maintain quality. It generates structured documentation, comprehensive tag analysis with visualizations, and quality metrics that help teams effectively organize their BDD tests. With cross-language support and CI/CD integration, FTOC bridges the gap between test authoring and maintainable, discoverable test documentation.

[![Version](https://img.shields.io/badge/version-0.9.1-brightgreen.svg)](https://github.com/heymumford/ftoc/releases/tag/v0.9.1)
[![Build](https://img.shields.io/badge/build-12-blue.svg)](https://github.com/heymumford/ftoc/actions)
[![License](https://img.shields.io/badge/license-MIT-purple.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/java-11+-orange.svg)](https://openjdk.java.net/)
[![Cucumber](https://img.shields.io/badge/cucumber-compatible-green.svg)](https://cucumber.io/)
[![Coverage](.github/badges/coverage.svg)](.github/badges/jacoco.csv)

## Quick Start

```bash
git clone https://github.com/heymumford/ftoc.git
cd ftoc
mvn clean package
java -jar target/ftoc-0.9.1-jar-with-dependencies.jar -d /path/to/features
```

## Overview

`ftoc` analyzes Cucumber feature files to create documentation and perform quality checks, helping development teams maintain consistent BDD practices. It provides sophisticated analytics to understand your test suite organization.

## Key Features

- **Table of Contents Generation:** Creates structured TOC of all scenarios and scenario outlines
- **Advanced Tag Analytics:**
  - Tag occurrence metrics and co-occurrence analysis
  - Trend detection for rising and declining tag usage
  - Statistical significance indicators
  - D3.js visualizations of tag relationships
- **Quality Assurance:** Flags missing tags or use of low-value generic tags
- **CI/CD Integration:** Generates JUnit XML reports for integration with CI systems
- **Multi-Format Output:** Plain text, Markdown, HTML, and JSON reporting

## Who Is This For?

- QA Engineers working with BDD/Cucumber frameworks
- Development teams practicing Behavior-Driven Development
- DevOps engineers integrating test reporting into CI/CD pipelines
- Project managers tracking test coverage and organization

## Installation

```bash
git clone https://github.com/heymumford/ftoc.git
cd ftoc
mvn clean package
```

## Usage

```bash
java -jar target/ftoc-<version>-jar-with-dependencies.jar [OPTIONS]
```

### Options

- `-d <directory>`: Specify the directory to analyze (default: current directory)
- `--version`, `-v`: Display version information
- `--help`: Display help message

## Example

```bash
# Analyze all feature files in a specific directory
java -jar target/ftoc-0.9.1-jar-with-dependencies.jar -d src/test/resources/features

# Get version information
java -jar target/ftoc-0.9.1-jar-with-dependencies.jar --version
```

## Programmatic Usage

### Core API (Recommended)

```java
import com.heymumford.ftoc.core.FeatureRepository;
import com.heymumford.ftoc.core.FeatureProcessor;
import com.heymumford.ftoc.core.Reporter;

// Wire up the core components
FeatureRepository repository = new FeatureRepository();
FeatureProcessor processor = new FeatureProcessor(repository);
Reporter reporter = new Reporter();

// Use them directly
List<Feature> features = repository.loadFeatures(Path.of("/path/to/feature/files"));
Map<String, Integer> concordance = processor.generateTagConcordance(features);
reporter.generateTableOfContents(features, Reporter.Format.MARKDOWN, List.of(), List.of());
```

### Legacy API (Deprecated)

```java
import com.heymumford.ftoc.FtocUtility;

// FtocUtility is deprecated and will be removed in a future release.
FtocUtility ftoc = new FtocUtility();
ftoc.initialize();
ftoc.processDirectory("/path/to/feature/files");
```

## Why ftoc?

Managing large suites of Cucumber tests presents challenges:

- **Discoverability:** Finding relevant scenarios becomes difficult as test suites grow
- **Tag Consistency:** Ensuring proper tagging for test selection and reporting
- **Documentation:** Generating up-to-date documentation from feature files
- **Quality Control:** Avoiding generic, unhelpful tagging patterns

`ftoc` addresses these challenges with automated analysis and reporting tools.

## Documentation

- [Architecture](docs/ARCHITECTURE.md) -- project structure, key patterns, how to extend
- [Contributing](CONTRIBUTING.md) -- development workflow and conventions

## Version Management

FTOC uses semantic versioning (MAJOR.MINOR.PATCH) combined with build numbers for precise tracking:

```bash
# Display version information
./version summary
```

## Project Structure

```
ftoc/
├── config/            # Configuration files and scripts
├── docs/              # Documentation
│   ├── adr/           # Architecture Decision Records
│   ├── c4/            # Architecture diagrams
│   ├── developer/     # Developer documentation
│   └── user/          # User documentation
├── src/               # Source code
│   ├── main/          # Application code
│   └── test/          # Test code
├── .github/           # GitHub templates and workflows
├── Makefile           # Build and development convenience commands
├── ftoc               # Main executable script
├── version            # Version management script
├── LICENSE            # MIT License
└── README.md          # This file
```

## License

MIT © [heymumford](https://github.com/heymumford)