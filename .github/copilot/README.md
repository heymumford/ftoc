# GitHub Copilot Integration for FTOC

This directory contains resources and configurations to enhance the FTOC project with GitHub Copilot capabilities.

## Directory Structure

- `CODE_PATTERNS.md` - Documentation of code patterns and conventions
- `examples/` - Example code files demonstrating project patterns
- `prompts/` - Template prompts for common development tasks
- `workspace.json` - Copilot workspace configuration

## Using Copilot with FTOC

### IDE Integration

#### VS Code Integration

The project includes custom VS Code Copilot Chat commands and personalization:

- Located in `.vscode/copilot-chat/`
- Custom commands: `@explain-ftoc`, `@analyze-gherkin`, `@suggest-test`, `@optimize-code`
- Personalization: `@ftoc-expert`, `@bdd-advisor`, `@java-expert`

#### JetBrains IDEs Integration

For IntelliJ IDEA and other JetBrains IDEs, custom Copilot configurations are available:

- Located in `.idea/copilot/`
- Custom prompts in `prompts.json`
- Personalities in `personalities.json`
- Live templates in `live-templates.xml`
- Sample run configurations
- Detailed setup instructions in `.idea/copilot/README.md`

### Command Line Integration

GitHub Copilot CLI integration provides AI-assisted command-line experience:

- Located in `.github/copilot-cli/`
- Custom project aliases for common FTOC tasks
- Specialized scripts for feature file generation and validation
- Integration with GitHub Copilot CLI commands
- Setup script to configure your environment
- Detailed documentation in `.github/copilot-cli/README.md`

### Prompt Templates

The `prompts/` directory contains templates for common tasks:

- `add_feature.md` - Template for adding new features
- `fix_bug.md` - Template for fixing bugs
- `test_gen.md` - Template for generating tests

### Copilot Code Review

Pull requests are automatically reviewed by GitHub Copilot using:

- GitHub Actions workflow in `.github/workflows/copilot-review.yml`
- Optimized PR template in `.github/PULL_REQUEST_TEMPLATE/default.md`

## Extending Copilot Integration

To add new Copilot resources:

1. For new code patterns, update `CODE_PATTERNS.md`
2. For new example code, add to the `examples/` directory
3. For new prompt templates, add to the `prompts/` directory
4. For new VS Code Copilot Chat commands, edit `.vscode/copilot-chat/commands.json`