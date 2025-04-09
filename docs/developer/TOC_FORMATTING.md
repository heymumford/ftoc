# TOC Formatting Guide

This document describes the formatting options and features available for the Table of Contents (TOC) generation in FTOC.

## Overview

FTOC can generate tables of contents in various formats, each with specific formatting features:

- **Plain Text**: Simple text format suitable for console output
- **Markdown**: Rich formatting for documentation and GitHub
- **HTML**: Interactive web-based format with navigation and collapsible sections
- **JSON**: Structured data format for programmatic consumption
- **JUnit XML**: Compatible with CI/CD systems for test reporting

## Plain Text Format

The plain text format produces a clean, console-friendly output with proper indentation and hierarchical structure.

### Features

- **Proper indentation** for nested structures (scenarios, examples)
- **Hierarchical representation** of features, scenarios, and examples
- **Concise summary** of examples and variations
- **Tag visibility** on features and scenarios

### Example

```
TABLE OF CONTENTS
=================

Login Feature (login.feature)
  Tags: @auth, @critical

  Scenario: Successful login with valid credentials
    Tags: @smoke, @p0

  Scenario Outline: Failed login with invalid credentials
    Tags: @negative
    Examples: 3 total
      - Invalid credentials: 2 variations
      - Empty fields: 1 variations
```

## Markdown Format

The Markdown format provides rich text formatting suitable for documentation websites, READMEs, and wikis.

### Features

- **Table of contents navigation** with links to features and scenarios
- **Collapsible sections** using HTML details/summary tags
- **Code blocks** for steps with syntax highlighting
- **Tables** for examples data
- **Anchor links** for deep linking to specific scenarios
- **Back-to-top navigation** at the end of each feature

### Example

```markdown
# Table of Contents

## Contents

- [Login Feature](#login-feature)
  - [Successful login with valid credentials](#login-feature-successful-login-with-valid-credentials)
  - [Failed login with invalid credentials](#login-feature-failed-login-with-invalid-credentials)

## Filters Applied

**Include tags:** `@smoke` 

<h2 id="login-feature">Login Feature</h2>

*File: login.feature*

**Tags:** `@auth` `@critical`

<h3 id="login-feature-successful-login-with-valid-credentials">Scenario: Successful login with valid credentials</h3>

**Tags:** `@smoke` `@p0`

<details>
<summary>Steps</summary>

```gherkin
Given I am on the login page
When I enter valid credentials
And I click the login button
Then I should be logged in
```
</details>

[Back to Contents](#contents)
```

## HTML Format

The HTML format provides an interactive, web-based view with rich features for navigation and exploration.

### Features

- **Fixed navigation sidebar** for quick access to features
- **Collapsible sections** for features, scenarios, steps, and examples
- **Pagination** for large feature sets (automatically activates when total scenarios > 20)
- **Syntax highlighting** for steps
- **Tabular display** of examples
- **Visual distinction** between scenarios and scenario outlines
- **Tag highlighting** with different colors for included/excluded tags
- **Responsive design** suitable for different screen sizes

### JavaScript Features

- **Dynamic pagination** with page controls
- **Collapsible content** that expands and collapses with animation
- **Navigation** that scrolls to the selected section

## JSON Format

The JSON format provides a structured, machine-readable representation of the TOC for integration with other tools.

### Features

- **Hierarchical structure** representing the complete feature set
- **Full metadata** including tags, descriptions, and file paths
- **Complete examples data** including headers and rows
- **Summary statistics** for total features, scenarios, outlines, and examples
- **Filter information** included when filters are applied

### Example Structure

```json
{
  "tableOfContents": {
    "filters": {
      "includeTags": ["@smoke", "@p0"],
      "excludeTags": []
    },
    "features": [
      {
        "name": "Login Feature",
        "file": "login.feature",
        "description": "Tests for the login functionality",
        "tags": ["@auth", "@critical"],
        "scenarios": [
          {
            "type": "Scenario",
            "name": "Successful login with valid credentials",
            "line": 10,
            "tags": ["@smoke", "@p0"],
            "steps": ["Given I am on the login page", "..."],
            "examples": []
          }
        ]
      }
    ],
    "summary": {
      "totalFeatures": 1,
      "totalScenarios": 1,
      "scenarioOutlines": 0,
      "totalExamples": 0
    }
  }
}
```

## JUnit XML Format

The JUnit XML format provides CI/CD integration for test reporting systems like Jenkins, GitHub Actions, etc.

### Features

- **Test suite hierarchy** matching feature structure
- **Test cases** representing scenarios
- **Properties** for tags and metadata
- **Compatible with** standard CI/CD reporting tools

## Usage

### Command Line

```bash
java -jar ftoc.jar -d /path/to/features --format md
```

Available format options:
- `text` (default): Plain text format
- `md`: Markdown format
- `html`: HTML format
- `json`: JSON format
- `junit`: JUnit XML format

### API Usage

```java
import com.heymumford.ftoc.FtocUtility;
import com.heymumford.ftoc.formatter.TocFormatter;

FtocUtility ftoc = new FtocUtility();
ftoc.initialize();
ftoc.setOutputFormat(TocFormatter.Format.HTML);
ftoc.processDirectory("/path/to/features");
```

## Customization

The TOC formatter can be customized by modifying the TocFormatter class. Key customization points include:

- HTML styles in the generateHtmlToc method
- Markdown structure in the generateMarkdownToc method
- JSON structure in the generateJsonToc method
- Plain text formatting in the generatePlainTextToc method