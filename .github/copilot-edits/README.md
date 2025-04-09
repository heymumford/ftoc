# GitHub Copilot Edits Modes for FTOC

This directory contains configurations, templates, and examples for setting up GitHub Copilot Edits modes for common FTOC tasks. These custom edit modes enhance the Copilot experience for developers working with FTOC by providing specialized editing capabilities for feature files and related components.

## What are Copilot Edits Modes?

GitHub Copilot Edits are specialized AI-powered editing modes that help developers transform and generate code more efficiently. Edits modes focus on specific editing tasks, providing a more targeted experience than general code suggestions.

FTOC-specific edit modes are designed to:

- Streamline creation of well-structured feature files
- Enforce consistent tagging practices
- Assist with report generation and interpretation
- Facilitate feature file refactoring
- Support common FTOC development patterns

## Directory Structure

- `snippets/` - Code snippets for various FTOC editing tasks
- `templates/` - Templates for feature files, reports, and configurations
- `examples/` - Example usage of FTOC edit modes
- `vscode/` - VS Code editor configuration for Copilot Edits
- `jetbrains/` - JetBrains IDEs configuration for Copilot Edits

## Available Edit Modes

1. **Feature File Creation Mode**
   - Generate properly structured Gherkin feature files
   - Include appropriate tags based on FTOC best practices
   - Set up scenario outlines with examples

2. **Tag Management Mode**
   - Refactor tags for consistency
   - Add appropriate priority and category tags
   - Remove redundant or low-value tags

3. **Report Generation Mode**
   - Configure FTOC report formats
   - Generate formatted markdown documentation
   - Create tag concordance reports

4. **FTOC Extension Mode**
   - Extend FTOC functionality with new features
   - Create custom formatters and processors
   - Add new command-line options

## Getting Started

The edit modes in this directory should be used with GitHub Copilot Chat in your IDE. To use them:

1. Open a file you want to work on in your IDE
2. Access Copilot Chat
3. Use the `/edit` command followed by the specific edit mode
4. Follow the prompts to apply the edit mode to your code

Example:
```
/edit Create a new feature file for login functionality with priority P1 and category Authentication
```

## Configuration

### VS Code

VS Code configurations for Copilot Edits are stored in the `vscode/` directory. These configurations extend the default Copilot behavior to include FTOC-specific edit modes.

### JetBrains IDEs

JetBrains configurations for Copilot Edits are stored in the `jetbrains/` directory. These configurations enhance the default Copilot experience with FTOC-specific edit modes.

## Examples

See the `examples/` directory for examples of using the FTOC edit modes for common tasks. These examples demonstrate the various ways Copilot Edits can assist with FTOC development.

## Contributing

To contribute new edit modes or improve existing ones:

1. Create a new template or snippet in the appropriate directory
2. Document the edit mode in this README
3. Add an example to the `examples/` directory
4. Test the edit mode in both VS Code and JetBrains IDEs
5. Create a pull request with your changes