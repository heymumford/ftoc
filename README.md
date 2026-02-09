# ftoc - Feature Table of Contents Utility

FTOC solves the challenge of managing large Cucumber test suites by automatically analyzing feature files to improve discoverability, ensure tag consistency, and maintain quality. It generates structured documentation, comprehensive tag analysis with visualizations, and quality metrics that help teams effectively organize their BDD tests. With cross-language support and CI/CD integration, FTOC bridges the gap between test authoring and maintainable, discoverable test documentation.

[![Version](https://img.shields.io/badge/version-0.5.4-brightgreen.svg)](https://github.com/heymumford/ftoc/releases/tag/v0.5.4)
[![Build](https://img.shields.io/badge/build-12-blue.svg)](https://github.com/heymumford/ftoc/actions)
[![License](https://img.shields.io/badge/license-MIT-purple.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/java-11+-orange.svg)](https://openjdk.java.net/)
[![Cucumber](https://img.shields.io/badge/cucumber-compatible-green.svg)](https://cucumber.io/)
[![Coverage](.github/badges/coverage.svg)](.github/badges/jacoco.csv)

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
java -jar target/ftoc-0.5.1-jar-with-dependencies.jar -d src/test/resources/features

# Get version information
java -jar target/ftoc-0.5.1-jar-with-dependencies.jar --version
```

## Programmatic Usage

### Legacy API

```java
import com.heymumford.ftoc.FtocUtility;

FtocUtility ftoc = new FtocUtility();
ftoc.initialize();
ftoc.processDirectory("/path/to/feature/files");
```

### New Architecture (Recommended)

```java
import com.heymumford.ftoc.FtocUtilityRefactored;
import com.heymumford.ftoc.core.impl.DefaultFeatureRepository;
import com.heymumford.ftoc.core.impl.DefaultFeatureProcessor;
import com.heymumford.ftoc.core.impl.DefaultReporter;

// Using default implementation
FtocUtilityRefactored ftoc = new FtocUtilityRefactored();
ftoc.initialize();
ftoc.processDirectory("/path/to/feature/files");

// Or with custom components
FeatureRepository repository = new DefaultFeatureRepository();
FeatureProcessor processor = new DefaultFeatureProcessor(repository);
Reporter reporter = new DefaultReporter();

FtocUtilityRefactored ftoc = new FtocUtilityRefactored(repository, processor, reporter);
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

### User Documentation

- [Usage Guide](docs/user/usage.md)
- [Tag Best Practices](docs/user/tag-best-practices.md)
- [Tag Concordance Report](docs/user/tag-concordance.md)
- [JUnit Report Integration](docs/user/junit-report-integration.md)
- [Docker Usage](docs/user/docker-usage.md)
- [Cross-Platform Usage](docs/user/cross-platform.md)
- [Karate Syntax Guide](docs/user/KARATE_SYNTAX.md)

### Developer Documentation

- [Architecture Documentation](docs/developer/ARCHITECTURE.md)
- [Architecture Decision Records (ADRs)](docs/adr/README.md)
- [C4 Architecture Diagrams](docs/c4/README.md)
- [Project Kanban Board](docs/developer/KANBAN.md)
- [Version Management Guide](docs/developer/VERSION_MANAGEMENT.md)
- [Classpath Optimization Report](docs/developer/CLASSPATH_OPTIMIZATION.md)
- [XML Standards and Cleanup](docs/developer/XML_STANDARDS.md)
- [Security Practices](docs/developer/SECURITY.md)
- [Code Coverage](docs/developer/CODE_COVERAGE.md)
- [Claude AI Assistant Guide](docs/developer/CLAUDE.md)
- [Conventional Commits Guide](docs/developer/CONVENTIONAL_COMMITS.md)
- [Release Process](docs/developer/RELEASE_PROCESS.md)
- [Changelog Usage](docs/developer/CHANGELOG_USAGE.md)
- [Performance Optimization](docs/developer/PERFORMANCE.md)
- [TOC Formatting Guide](docs/developer/TOC_FORMATTING.md)

For all documentation, see the [docs directory](docs/README.md).

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