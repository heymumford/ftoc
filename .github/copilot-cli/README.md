# GitHub Copilot CLI Integration for FTOC

This directory contains configuration files and scripts for integrating FTOC with GitHub Copilot CLI, providing AI-assisted command-line interaction for the FTOC project.

## What is GitHub Copilot CLI?

GitHub Copilot CLI is a command-line tool that brings AI assistance to your terminal. It helps you:

- Generate and explain shell commands
- Understand and fix command errors
- Analyze code and files in context
- Automate common development tasks

## Setup Instructions

1. Ensure you have GitHub CLI installed:
   - Follow installation instructions at https://cli.github.com/

2. Install GitHub Copilot CLI extension:
   ```bash
   gh extension install github/gh-copilot
   ```

3. Run the FTOC Copilot CLI setup script:
   ```bash
   chmod +x .github/copilot-cli/setup.sh
   ./.github/copilot-cli/setup.sh
   ```

4. Source the aliases in your current shell:
   ```bash
   source ~/.config/ftoc/copilot-cli/ftoc-aliases.sh
   ```

5. Verify installation:
   ```bash
   ftoc:help
   ```

## Available Commands

| Command | Description |
|---------|-------------|
| `ftoc:analyze` | Run FTOC analysis on feature files |
| `ftoc:report` | Generate specific reports (concordance, tags, anti-patterns) |
| `ftoc:build` | Build the FTOC project |
| `ftoc:test` | Run tests for the FTOC project |
| `ftoc:benchmark` | Run performance benchmarks |
| `ftoc:version` | Display FTOC version information |
| `ftoc:setup` | Set up FTOC environment and dependencies |
| `ftoc:help` | Show help for commands and options |
| `ftoc:generate` | Generate sample feature file with best practices |
| `ftoc:validate` | Validate feature files against best practices |

## Examples

### Analyzing Feature Files

```bash
# Basic analysis
ftoc:analyze ./src/test/resources/features

# With performance monitoring and tag filtering
ftoc:analyze ./src/test/resources/features --performance --tags @P1,@API
```

### Generating Reports

```bash
# Generate tag concordance report
ftoc:report concordance ./src/test/resources/features

# Generate tag quality analysis report in HTML format
ftoc:report tags ./src/test/resources/features --format html
```

### Running Tests

```bash
# Run all tests
ftoc:test

# Run specific type of tests
ftoc:test --cucumber

# Run specific test with coverage
ftoc:test --coverage MySpecificTest
```

### Generating Feature Files

```bash
# Generate a basic feature file
ftoc:generate ./src/test/resources/features/new-feature.feature

# Generate an API test feature file with custom tags
ftoc:generate --template api-test --tags @P0,@API,@Regression ./src/test/resources/features/api-test.feature
```

## Using with GitHub Copilot

GitHub Copilot CLI enhances these commands with AI assistance. Try:

```bash
# Get help with FTOC commands
gh copilot suggest "How do I run tag analysis on my feature files?"

# Explain a command
gh copilot explain "ftoc:analyze ./src/test/resources/features --performance --tags @P1,@API"

# Get shell command suggestions
gh copilot "Generate a report showing tag quality issues in HTML format"
```

## Customizing

You can customize the commands and aliases:

1. Edit scripts in `.github/copilot-cli/scripts/`
2. Update alias descriptions in `.github/copilot-cli/aliases.json`
3. Run the setup script again to apply changes

## Troubleshooting

If you encounter issues:

- Ensure GitHub CLI and Copilot extension are properly installed
- Check that your GitHub account has Copilot access
- Run `gh auth status` and `gh copilot auth status` to verify authentication
- Check script permissions with `chmod +x .github/copilot-cli/scripts/*.sh`