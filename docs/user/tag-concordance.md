# Tag Concordance Report

The Tag Concordance Report in FTOC provides comprehensive analysis of tag usage patterns in your Cucumber feature files. Advanced analytics help you understand tag relationships, trends, and significance.

## Overview

The Tag Concordance functionality includes:

1. **Basic Tag Statistics**: Count and frequency of each tag in your feature files
2. **Tag Co-occurrence Metrics**: Analysis of which tags are commonly used together
3. **Tag Trend Analysis**: Identification of rising and declining tag usage patterns
4. **Tag Significance Indicators**: Statistical significance of tags using TF-IDF methodology
5. **Visualization**: Interactive visualization of tag relationships (HTML format only)

## Report Formats

Tag Concordance reports are available in multiple formats:

- **Plain Text**: Simple text report for command line viewing
- **Markdown**: Formatted report for documentation
- **HTML**: Interactive report with visualizations
- **JSON**: Structured data for programmatic analysis

## Features

### Tag Co-occurrence Metrics

Co-occurrence metrics measure how frequently tags appear together, using the Jaccard coefficient:

- **Coefficient**: Strength of relationship between two tags (0-1)
- **Count**: Number of times tags appear together
- **Visualization**: Visual representation of tag relationships

Example output (Markdown format):

```
## Tag Co-occurrence Metrics

| Tag 1      | Tag 2        | Count | Coefficient |
|------------|--------------|-------|-------------|
| `@API`     | `@P1`        | 12    | 0.857       |
| `@UI`      | `@Smoke`     | 8     | 0.722       |
| `@Backend` | `@Database`  | 6     | 0.667       |
```

### Tag Trend Analysis

Trend analysis identifies rising and declining tag usage patterns:

- **Growth Rate**: Rate of change in tag usage
- **Trend Classification**: Rising, Stable, or Declining
- **Associated Tags**: Tags commonly used with this tag

Example output (Markdown format):

```
## Tag Trend Analysis

### Rising Tags

| Tag        | Count | Growth Rate |
|------------|-------|-------------|
| `@API`     | 24    | 0.375       |
| `@Mobile`  | 12    | 0.250       |
| `@Cloud`   | 8     | 0.167       |

### Declining Tags

| Tag        | Count | Growth Rate |
|------------|-------|-------------|
| `@Legacy`  | 6     | -0.214      |
| `@Manual`  | 4     | -0.167      |
| `@Old`     | 2     | -0.125      |
```

### Tag Relationship Visualization

The HTML report includes an interactive D3.js visualization:

- **Force-directed Graph**: Shows relationships between tags
- **Node Size**: Represents tag frequency
- **Link Width**: Represents strength of tag relationship
- **Interactive**: Drag nodes to explore relationships
- **Tooltips**: Hover for detailed information

### Statistical Significance Indicators

Significance metrics help identify important tags:

- **TF-IDF Score**: Measures tag importance
- **Highlighted Significant Tags**: Tags with higher significance scores
- **Filtering**: Focus on statistically meaningful tags

Example output (Markdown format):

```
## Statistically Significant Tags

| Tag           | Significance Score |
|---------------|-------------------|
| `@Critical`   | 0.857             |
| `@Security`   | 0.722             |
| `@Performance`| 0.667             |
```

## Usage

To generate a tag concordance report:

```bash
# Generate plain text report
ftoc --tag-concordance --format plain

# Generate markdown report
ftoc --tag-concordance --format markdown

# Generate HTML report with visualization
ftoc --tag-concordance --format html --output report.html

# Generate JSON report for programmatic analysis
ftoc --tag-concordance --format json --output report.json
```

## Interpreting Results

1. **Co-occurrence Patterns**: Identify which tags are commonly used together
2. **Tag Trends**: Monitor changes in tag usage over time
3. **Tag Significance**: Focus on statistically important tags
4. **Visual Patterns**: Discover clusters of related tags
5. **Low-value Tags**: Identify tags that may be candidates for removal

## Advanced Analysis

For deeper analysis, the JSON output contains:

```json
{
  "tagConcordanceReport": {
    "summary": { ... },
    "tagFrequency": [ ... ],
    "coOccurrences": [ ... ],
    "tagTrends": { 
      "rising": [ ... ],
      "declining": [ ... ]
    },
    "significantTags": [ ... ],
    "visualization": { ... }
  }
}
```

This structured data enables integration with other analytics tools for custom dashboards and reports.

## See Also

- [Tag Best Practices](tag-best-practices.md)
- [Configuration Options](configuration.md)
- [FTOC Usage Guide](usage.md)