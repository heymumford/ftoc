# ftoc - Feature Table of Contents Utility

[![Version](https://img.shields.io/badge/version-0.5.1-brightgreen.svg)](https://github.com/heymumford/ftoc/releases/tag/v0.5.1)
[![Build](https://img.shields.io/badge/build-12-blue.svg)](https://github.com/heymumford/ftoc/actions)
[![License](https://img.shields.io/badge/license-MIT-purple.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/java-11+-orange.svg)](https://openjdk.java.net/)
[![Cucumber](https://img.shields.io/badge/cucumber-compatible-green.svg)](https://cucumber.io/)

A command-line utility for Cucumber feature file analysis, documentation, and quality assurance.

## Overview

`ftoc` analyzes Cucumber feature files to create documentation and perform quality checks, helping development teams maintain consistent BDD practices. It works with all Cucumber-compatible implementations (Java, JavaScript, Ruby, Karate).

## Key Features

- **Table of Contents Generation:** Creates structured TOC of all scenarios and scenario outlines
- **Tag Concordance:** Counts and analyzes tag usage across feature files
- **Quality Assurance:** Flags missing tags or use of low-value generic tags
- **Cross-Language Support:** Works with feature files in any Cucumber implementation

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

```java
import com.heymumford.ftoc.FtocUtility;

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

### User Documentation

- [Usage Guide](docs/user/usage.md)

### Developer Documentation

- [Architecture Decision Records (ADRs)](docs/adr/README.md)
- [C4 Architecture Diagrams](docs/c4/README.md)
- [Project Kanban Board](docs/developer/KANBAN.md)
- [Version Management Guide](docs/developer/VERSION_MANAGEMENT.md)
- [Classpath Optimization Report](docs/developer/CLASSPATH_OPTIMIZATION.md)
- [Claude AI Assistant Guide](docs/developer/CLAUDE.md)

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