#!/bin/bash

# Setup script for GitHub Copilot Edits modes
# This script sets up the necessary files for using GitHub Copilot Edits with FTOC

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

echo "Setting up GitHub Copilot Edits modes for FTOC..."

# Setup for VS Code
echo "Setting up VS Code integration..."
mkdir -p "${PROJECT_ROOT}/.vscode"
cp -r "${SCRIPT_DIR}/vscode/"* "${PROJECT_ROOT}/.vscode/"
echo "VS Code setup complete."

# Setup for JetBrains
echo "Setting up JetBrains IDE integration..."
mkdir -p "${PROJECT_ROOT}/.idea/copilot"
cp -r "${SCRIPT_DIR}/jetbrains/"* "${PROJECT_ROOT}/.idea/copilot/"
echo "JetBrains setup complete."

# Copy snippets to appropriate locations
echo "Setting up snippets..."
mkdir -p "${PROJECT_ROOT}/.vscode/snippets"
cp -r "${SCRIPT_DIR}/snippets/"* "${PROJECT_ROOT}/.vscode/snippets/"
echo "Snippets setup complete."

# Setup is complete
echo ""
echo "GitHub Copilot Edits modes have been set up successfully!"
echo ""
echo "To use Copilot Edits modes:"
echo "1. In VS Code: Use the /edit command in Copilot Chat"
echo "2. In JetBrains IDEs: Use the /edit command in Copilot tool window"
echo ""
echo "For more information, see:"
echo "- ${SCRIPT_DIR}/SETUP_INSTRUCTIONS.md"
echo "- ${SCRIPT_DIR}/IMPLEMENTATION_GUIDE.md"
echo ""
echo "Happy coding with GitHub Copilot and FTOC!"