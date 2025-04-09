# FTOC Usage Guide

## Overview

FTOC (Feature Table of Contents) is a utility for generating table of contents and analyzing Cucumber/Gherkin feature files. It helps teams organize, visualize, and improve their BDD test suites.

## Quick Start

```bash
# Generate TOC in plain text format for current directory
ftoc

# Generate TOC in markdown format for a specific directory
ftoc -d /path/to/features -f md

# Generate TOC with tag filtering
ftoc --tags "@P0,@Smoke" --exclude-tags "@Flaky"

# Generate tag concordance report
ftoc --concordance --concordance-format html

# Analyze tag quality
ftoc --analyze-tags --tag-quality-format md
```

## Command Line Options

| Option | Description |
|--------|-------------|
| `-d <directory>` | Specify the directory to analyze (default: current directory) |
| `-f <format>` | Output format: text, md, html, json (default: text) |
| `--tags <tags>` | Include only scenarios with at least one of these tags (comma-separated) |
| `--exclude-tags <tags>` | Exclude scenarios with any of these tags (comma-separated) |
| `--concordance` | Generate detailed tag concordance report instead of TOC |
| `--concordance-format <format>` | Concordance output format: text, md, html, json (default: text) |
| `--analyze-tags` | Perform tag quality analysis and generate warnings report |
| `--tag-quality-format <format>` | Tag quality report format: text, md, html, json (default: text) |
| `--version`, `-v` | Display version information |
| `--help` | Display help message |

## Output Formats

FTOC supports multiple output formats:

- **Plain Text**: Simple text format suitable for console output
- **Markdown**: Formatted markdown suitable for documentation
- **HTML**: Rich HTML format with styling for browser viewing
- **JSON**: Structured JSON format for programmatic use

## Examples

### Generate TOC in HTML format

```bash
ftoc -d ./src/test/resources/features -f html > features.html
```

### Generate Concordance Report with Filtering

```bash
ftoc -d ./features --tags "@API" --concordance --concordance-format md > api-tags.md
```

### Analyze Tag Quality Issues

```bash
ftoc -d ./features --analyze-tags > tag-issues.txt
```

## Integration with CI/CD

FTOC can be used in CI/CD pipelines to:

1. Generate documentation for test suites
2. Validate tagging practices
3. Monitor test coverage over time
4. Generate reports as part of build processes

Add to your CI/CD configuration to run FTOC as part of your workflow.