# GitHub Copilot Edits Modes Implementation Guide

This guide explains how to implement and use different Copilot Edits modes for common FTOC tasks.

## Mode 1: Feature File Creation

The Feature File Creation mode helps you generate well-structured Cucumber feature files that follow FTOC best practices.

### Implementation

This mode is implemented in both VS Code and JetBrains configurations with the name `feature-file-create`.

### Usage Examples

Basic usage:
```
/edit Create a Cucumber feature file for User Authentication with priority P1 and category Security.
```

This will generate a feature file with:
- Properly structured feature with background and scenarios
- Appropriate tags (@P1 @Security @Regression)
- Placeholder steps that can be easily customized

Advanced usage:
```
/edit Create a Cucumber feature file for API Performance Testing with priority P2 and category Performance. Include 3 scenarios for different load levels.
```

### Best Practices

- Always include a priority tag (P1, P2, P3)
- Always include a category tag (API, UI, Integration, etc.)
- Include the @Regression tag for tests that should be part of regression suites
- Use Background for common preconditions
- Use descriptive feature and scenario names

## Mode 2: Tag Management

The Tag Management mode helps you refactor and standardize tags in feature files.

### Implementation

This mode is implemented in both VS Code and JetBrains configurations with the name `tag-refactor`.

### Usage Examples

Basic usage:
```
/edit Refactor the tags in this feature file to follow FTOC standards with priority P1 and add appropriate API tags.
```

This will:
- Add missing priority tags
- Add missing category tags
- Remove redundant or invalid tags
- Organize tags in a consistent order

### Best Practices

- Run this mode on feature files that were created without following FTOC tag standards
- Use this mode after importing external feature files
- Ensure at least one priority tag and one category tag are present

## Mode 3: Scenario Outline Creation

The Scenario Outline Creation mode helps you generate scenario outlines with examples tables.

### Implementation

This mode is implemented in both VS Code and JetBrains configurations with the name `scenario-outline-create`.

### Usage Examples

Basic usage:
```
/edit Create a scenario outline for login validation with 3 examples.
```

Advanced usage:
```
/edit Create a scenario outline for data transformation with parameters input_format, output_format, and validation_level.
```

### Best Practices

- Use scenario outlines when testing the same functionality with different inputs
- Keep example tables clean and readable
- Include edge cases in your examples
- Use meaningful parameter names

## Mode 4: Report Configuration

The Report Configuration mode helps you generate configuration files for FTOC reports.

### Implementation

This mode is implemented in both VS Code and JetBrains configurations with the name `report-config-create`.

### Usage Examples

Basic usage:
```
/edit Create a configuration for generating FTOC reports in markdown format with detailed verbosity.
```

Advanced usage:
```
/edit Create a configuration for generating FTOC reports in json format with summary verbosity and filtering for only @API and @P1 tags.
```

### Best Practices

- Store report configurations in a dedicated directory
- Use descriptive file names for configurations
- Include appropriate filters to focus reports
- Set appropriate verbosity level for the intended audience

## Mode 5: Formatter Creation

The Formatter Creation mode helps you generate new formatter classes for FTOC.

### Implementation

This mode is implemented in both VS Code and JetBrains configurations with the name `formatter-create`.

### Usage Examples

Basic usage:
```
/edit Create a formatter class for CSV output format.
```

Advanced usage:
```
/edit Create a formatter class for XML output format with support for custom namespaces and schema validation.
```

### Best Practices

- Follow the established formatter interface
- Implement proper error handling
- Add comprehensive logging
- Format output according to established standards
- Add unit tests for the formatter

## Creating Custom Edit Modes

You can create custom edit modes by adding new entries to the configuration files.

### Steps to Create a New Edit Mode

1. Identify a common task that could benefit from a template
2. Create a template with placeholders
3. Define the prompt format and variables
4. Add examples
5. Test the edit mode
6. Add documentation

### Example Custom Edit Mode

Here's an example of a custom edit mode for creating step definition classes:

```json
{
  "name": "step-defs-create",
  "description": "Create a step definitions class for a feature file",
  "filePattern": "**/*.java",
  "prompt": "Create step definitions for ${1:feature name} feature.",
  "template": "package com.heymumford.ftoc.steps;\n\nimport io.cucumber.java.en.Given;\nimport io.cucumber.java.en.When;\nimport io.cucumber.java.en.Then;\n\n/**\n * Step definitions for ${1} feature.\n */\npublic class ${1}StepDefs {\n\n    @Given(\"${2:given step}\")\n    public void ${3:given_method}() {\n        // TODO: Implement step\n    }\n\n    @When(\"${4:when step}\")\n    public void ${5:when_method}() {\n        // TODO: Implement step\n    }\n\n    @Then(\"${6:then step}\")\n    public void ${7:then_method}() {\n        // TODO: Implement step\n    }\n}",
  "examples": [
    "Create step definitions for UserAuthentication feature.",
    "Create step definitions for DataProcessing feature."
  ]
}
```

## Tips for Using Copilot Edits Effectively

1. **Start with a clear goal** - Know what you want to create before using an edit mode
2. **Be specific in your prompts** - The more specific you are, the better the results
3. **Review and customize** - Always review the generated code and customize it as needed
4. **Combine edit modes** - Use multiple edit modes in sequence for complex tasks
5. **Save common patterns** - If you find yourself using the same pattern repeatedly, create a custom edit mode for it
6. **Share with team** - Share effective edit modes with your team to improve consistency