#!/bin/bash

# Script to set up GitHub Copilot CLI for FTOC
# This script registers the FTOC aliases with GitHub Copilot CLI

# Check if GitHub Copilot CLI is installed
if ! command -v gh &> /dev/null; then
    echo "Error: GitHub CLI (gh) is not installed."
    echo "Please install GitHub CLI from https://cli.github.com/"
    exit 1
fi

if ! gh extension list | grep -q "github/gh-copilot"; then
    echo "GitHub Copilot CLI extension not found. Installing..."
    gh extension install github/gh-copilot
    
    if [ $? -ne 0 ]; then
        echo "Error: Failed to install GitHub Copilot CLI extension."
        echo "Please install it manually: gh extension install github/gh-copilot"
        exit 1
    fi
fi

# Authenticate with GitHub Copilot if needed
if ! gh copilot auth status &> /dev/null; then
    echo "Authenticating with GitHub Copilot..."
    gh auth login
    gh copilot auth
fi

# Make sure scripts are executable
chmod +x .github/copilot-cli/scripts/*.sh

# Create the FTOC aliases directory in the user's home directory
FTOC_ALIASES_DIR="$HOME/.config/ftoc/copilot-cli"
mkdir -p "$FTOC_ALIASES_DIR"

# Create a shell script that will source all FTOC aliases
ALIAS_SCRIPT="$FTOC_ALIASES_DIR/ftoc-aliases.sh"

echo "Creating FTOC aliases script at $ALIAS_SCRIPT..."

cat > "$ALIAS_SCRIPT" << 'EOF'
#!/bin/bash

# FTOC aliases for GitHub Copilot CLI
# This file is generated automatically - do not edit manually

# Base project directory
FTOC_PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"

# Define FTOC aliases
alias ftoc:analyze="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-analyze.sh"
alias ftoc:report="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-report.sh"
alias ftoc:build="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-build.sh"
alias ftoc:test="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-test.sh"
alias ftoc:benchmark="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-benchmark.sh"
alias ftoc:version="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-version.sh"
alias ftoc:setup="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-setup.sh"
alias ftoc:help="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-help.sh"
alias ftoc:generate="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-generate.sh"
alias ftoc:validate="$FTOC_PROJECT_DIR/.github/copilot-cli/scripts/ftoc-validate.sh"

# Initialize Copilot CLI aliases for FTOC
echo "GitHub Copilot CLI aliases for FTOC are ready!"
echo "Available commands:"
echo "  ftoc:analyze - Run FTOC analysis on feature files"
echo "  ftoc:report - Generate specific reports"
echo "  ftoc:build - Build the project"
echo "  ftoc:test - Run tests"
echo "  ftoc:benchmark - Run performance benchmarks"
echo "  ftoc:version - Display version information"
echo "  ftoc:setup - Set up the environment"
echo "  ftoc:help - Show help"
echo "  ftoc:generate - Generate sample feature files"
echo "  ftoc:validate - Validate feature files"
EOF

chmod +x "$ALIAS_SCRIPT"

# Check if the alias script is already sourced in shell config files
SOURCE_LINE="source $ALIAS_SCRIPT"

function add_to_shell_config() {
    local config_file="$1"
    
    if [ -f "$config_file" ]; then
        if ! grep -q "$ALIAS_SCRIPT" "$config_file"; then
            echo "" >> "$config_file"
            echo "# FTOC Copilot CLI aliases" >> "$config_file"
            echo "$SOURCE_LINE" >> "$config_file"
            echo "Added FTOC aliases to $config_file"
        else
            echo "FTOC aliases already configured in $config_file"
        fi
    fi
}

# Add to common shell config files
add_to_shell_config "$HOME/.bashrc"
add_to_shell_config "$HOME/.zshrc"

echo
echo "GitHub Copilot CLI integration for FTOC has been set up successfully!"
echo "Run 'source $ALIAS_SCRIPT' to activate the aliases in your current shell."
echo "After that, you can use 'ftoc:help' to see all available commands."