# ftoc - Feature Table of Contents Utility

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

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
java -jar target/ftoc-1.0.7-jar-with-dependencies.jar -d src/test/resources/features

# Get version information
java -jar target/ftoc-1.0.7-jar-with-dependencies.jar --version
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

## License

MIT © [heymumford](https://github.com/heymumford)