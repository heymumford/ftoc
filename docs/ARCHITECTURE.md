# FTOC Architecture

FTOC (Feature Table of Contents) is a CLI tool that analyzes Cucumber and Karate feature files. It generates tables of contents, tag concordance reports, tag quality analysis, and anti-pattern detection. Output formats include plain text, Markdown, HTML, JSON, and JUnit XML.

## Project Structure

```
src/main/java/com/heymumford/ftoc/
├── FtocUtility.java           # CLI entry point and argument parsing
├── core/                      # Interfaces
│   ├── FeatureRepository.java #   find and load feature files
│   ├── FeatureProcessor.java  #   filter, tag concordance, parallel processing
│   └── Reporter.java          #   generate reports in multiple formats
├── model/                     # Domain objects
│   ├── Feature.java           #   parsed feature file
│   ├── Scenario.java          #   individual scenario
│   ├── Tag.java               #   tag metadata
│   └── TagConcordance.java    #   tag frequency data
├── parser/                    # File parsing
│   ├── FeatureParser.java     #   standard Gherkin parser
│   ├── KarateParser.java      #   Karate-specific parser
│   └── FeatureParserFactory.java  # selects parser by file content
├── analyzer/                  # Quality analysis
│   ├── TagQualityAnalyzer.java
│   ├── TagFrequencyAnalyzer.java
│   ├── TagTypoAnalyzer.java
│   ├── LowValueTagAnalyzer.java
│   └── FeatureAntiPatternAnalyzer.java
├── formatter/                 # Output formatting
│   ├── TocFormatter.java
│   ├── ConcordanceFormatter.java
│   ├── ConcordanceAnalyzer.java
│   ├── TagQualityFormatter.java
│   ├── AntiPatternFormatter.java
│   └── JUnitFormatter.java
├── config/
│   └── WarningConfiguration.java
└── exception/
    ├── FtocException.java
    └── ErrorCode.java
```

## Key Patterns

**Three-layer pipeline.** Every run follows: Repository (find/parse files) -> Processor (filter, analyze) -> Reporter (format output). Each layer is an interface with a default implementation. Custom implementations can be injected via the constructor.

**Parser selection.** `FeatureParserFactory` inspects file content to choose between the standard Gherkin parser and the Karate parser. This keeps parser-specific logic out of the repository.

**Analyzer chain.** Quality analysis runs multiple independent analyzers (typo detection, frequency analysis, low-value tag detection, anti-pattern detection). Each analyzer operates on the parsed model and produces findings that the formatter renders.

**Format strategy.** Each report type has a dedicated formatter that handles all output formats (plain text, Markdown, HTML, JSON, JUnit XML). JUnit XML output allows integration with CI reporting tools.

## How to Extend

**Add an output format.** Modify the relevant formatter class in `formatter/` to handle the new format. Add the format to the `Reporter.Format` enum.

**Add a new analyzer.** Create a class in `analyzer/`, wire it into the processing pipeline in `FeatureProcessor`, and add formatting support in the appropriate formatter.

**Add a parser.** Implement parsing logic in `parser/`, register it in `FeatureParserFactory` with a detection heuristic.

**Plugin system.** FTOC supports runtime plugins loaded from `./plugins`, `~/.ftoc/plugins`, or `/etc/ftoc/plugins`. A plugin implements `FtocPlugin` (or extends `AbstractFtocPlugin`) and can replace core components or register event handlers for processing stages.

## Build and Test

```bash
mvn package          # build + unit tests
mvn verify           # build + unit + Karate integration tests
mvn jacoco:report    # generate coverage report at target/site/jacoco/
```

## CI/CD

- `ci-cd.yml` -- build, test, coverage check, version consistency (every push/PR)
- `release.yml` -- build and publish GitHub release (on version tags)
- `docker-publish.yml` -- build and push Docker image
- `dependabot-auto-merge.yml` -- auto-merge patch dependency updates
