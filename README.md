# ftoc

Analyzes Cucumber feature files to generate structured documentation, tag concordance reports, and quality warnings.

[![Build](https://github.com/heymumford/ftoc/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/heymumford/ftoc/actions/workflows/ci-cd.yml)
[![Version](https://img.shields.io/badge/version-0.9.1-brightgreen.svg)](https://github.com/heymumford/ftoc/releases/tag/v0.9.1)
[![License](https://img.shields.io/badge/license-MIT-purple.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-11+-orange.svg)](https://openjdk.java.net/)

## Install

### Download JAR

Grab the latest release from [GitHub Releases](https://github.com/heymumford/ftoc/releases) and run directly:

```bash
java -jar ftoc-0.9.1-jar-with-dependencies.jar --help
```

### Docker

```bash
docker build -t ftoc .
docker run --rm -v /path/to/features:/features ftoc -d /features
```

### Build from source

```bash
git clone https://github.com/heymumford/ftoc.git
cd ftoc
mvn package
```

The JAR is at `target/ftoc-0.9.1-jar-with-dependencies.jar`.

## Quick start

```bash
# Generate table of contents for all feature files in a directory
java -jar ftoc.jar -d /path/to/features

# Tag concordance report in markdown
java -jar ftoc.jar -d /path/to/features --concordance -f md

# Full quality analysis with anti-pattern detection
java -jar ftoc.jar -d /path/to/features --analyze-tags --detect-anti-patterns
```

## Usage

```
ftoc [-d <dir>] [-f <format>] [OPTIONS]
```

### Flags

```
Flag                         Description
----                         -----------
-d <directory>               Directory to analyze (default: current directory)
-f, --format <format>        Output format: text, md, html, json, junit (default: text)
--tags <tags>                Include only scenarios matching these tags (comma-separated)
--exclude-tags <tags>        Exclude scenarios matching these tags (comma-separated)
--concordance                Generate tag concordance report instead of TOC
--concordance-format <fmt>   Override format for concordance report only
--analyze-tags               Run tag quality analysis
--tag-quality-format <fmt>   Override format for tag quality report only
--detect-anti-patterns       Detect feature file anti-patterns
--anti-pattern-format <fmt>  Override format for anti-pattern report only
--junit-report               Shorthand: set all outputs to JUnit XML
--config-file <file>         Custom warning configuration file
--show-config                Display current warning configuration and exit
--performance                Enable parallel processing for large repositories
--benchmark                  Run performance benchmarks (see Benchmark flags below)
--version, -v                Print version
--help                       Print help
```

### Benchmark flags

Used with `--benchmark`:

```
--small        10 files
--medium       50 files
--large        200 files
--very-large   500 files
--all          All sizes
--report <f>   Output file (default: benchmark-report.txt)
--no-cleanup   Keep temporary files after benchmark
```

## Output formats

**Text** -- Plain text, suitable for terminal output. Default.

**Markdown** -- Markdown tables and headers. Use `-f md`.

**HTML** -- Standalone HTML with inline styles for tag visualizations.

**JSON** -- Machine-readable output for downstream tooling.

**JUnit XML** -- CI integration. Use `--junit-report` to set all reports at once, or `-f junit` per report.

## Tag analysis

ftoc provides three levels of tag analysis:

**Concordance** (`--concordance`) -- Counts tag occurrences across all features and scenarios. Shows co-occurrence patterns and identifies which tags appear together.

**Quality analysis** (`--analyze-tags`) -- Flags missing priority/type tags, low-value tags (e.g., `@Test`, `@Temp`), orphaned tags, and naming issues. Severity levels: error, warning, info.

**Anti-pattern detection** (`--detect-anti-patterns`) -- Identifies structural problems in feature files: missing Given/When/Then steps, incorrect step ordering, oversized scenarios, and missing examples in scenario outlines.

## Configuration

Warning thresholds and tag definitions are configured in YAML. ftoc searches for configuration in this order:

1. `--config-file <path>` flag
2. `.ftoc/config.yml` in the working directory
3. `.ftoc.yml` in the working directory

See [`config/ftoc-warnings.yml`](config/ftoc-warnings.yml) for the full reference. Key sections:

```yaml
warnings:
  disabled: []              # Warning names to suppress
  severity:                 # Override severity per warning (error, warning, info)
    MISSING_PRIORITY_TAG: error
    LOW_VALUE_TAG: info

thresholds:
  maxSteps: 10              # Max steps per scenario
  minSteps: 2               # Min steps per scenario
  maxTags: 6                # Max tags per element
  maxScenarioNameLength: 100
  maxStepLength: 120
```

Use `--show-config` to display the resolved configuration.

## Documentation

- [Architecture](docs/ARCHITECTURE.md) -- project structure and key patterns
- [Contributing](CONTRIBUTING.md) -- development workflow and conventions

## License

MIT -- see [LICENSE](LICENSE).
