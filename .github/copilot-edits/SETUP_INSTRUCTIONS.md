# Setting Up GitHub Copilot Edits Modes for FTOC

This document provides step-by-step instructions for setting up and using the custom GitHub Copilot Edits modes for FTOC.

## Prerequisites

1. GitHub Copilot subscription
2. VS Code or JetBrains IDE with Copilot plugin installed
3. Access to the FTOC repository

## Setup for VS Code

1. **Copy Configuration Files:**
   Copy the contents of the `.github/copilot-edits/vscode` directory to your project's `.vscode` directory:

   ```bash
   cp -r .github/copilot-edits/vscode/* .vscode/
   ```

2. **Update Copilot Settings:**
   In VS Code, open settings (File > Preferences > Settings) and add:

   ```json
   "github.copilot.editor.edits.customizationPath": ".vscode/copilot-edits.json"
   ```

3. **Restart VS Code:**
   Close and reopen VS Code for the changes to take effect.

## Setup for JetBrains IDEs

1. **Copy Configuration Files:**
   Copy the contents of the `.github/copilot-edits/jetbrains` directory to your project's `.idea/copilot` directory:

   ```bash
   mkdir -p .idea/copilot
   cp -r .github/copilot-edits/jetbrains/* .idea/copilot/
   ```

2. **Enable Copilot Edits:**
   In your JetBrains IDE, go to Settings > Tools > GitHub Copilot and enable "Use custom edit modes"

3. **Restart IDE:**
   Restart your JetBrains IDE for the changes to take effect.

## Using Copilot Edits Modes

### In VS Code

1. Open Copilot Chat panel (Ctrl+Shift+I or Cmd+Shift+I on Mac)
2. Type `/edit` followed by the edit mode prompt:

   ```
   /edit Create a Cucumber feature file for User Registration with priority P1 and category Security.
   ```

3. Copilot will generate the feature file based on the template and your specifications

### In JetBrains IDEs

1. Open Copilot Tool Window (Alt+C or Option+C on Mac)
2. Type `/edit` followed by the edit mode prompt:

   ```
   /edit Create a formatter class for XML output format.
   ```

3. Copilot will generate the formatter class based on the template and your specifications

## Available Edit Modes

### Feature File Creation
```
/edit Create a Cucumber feature file for [feature name] with priority [P1/P2/P3] and category [API/UI/etc].
```

### Tag Refactoring
```
/edit Refactor the tags in this feature file to follow FTOC standards with priority [P1/P2/P3] and add appropriate [category] tags.
```

### Scenario Outline Creation
```
/edit Create a scenario outline for [functionality] with [number] examples.
```

### Report Configuration
```
/edit Create a configuration for generating FTOC reports in [format] format with [verbosity] verbosity.
```

### Formatter Creation
```
/edit Create a formatter class for [format name] output format.
```

## Customizing Edit Modes

To customize the edit modes or add new ones:

1. Edit the appropriate configuration file:
   - VS Code: `.vscode/copilot-edits.json`
   - JetBrains: `.idea/copilot/copilot-edits.json`

2. Add or modify the edit modes following the existing patterns

3. Restart your IDE for the changes to take effect

## Troubleshooting

If the edit modes are not working:

1. Make sure Copilot is properly configured and connected
2. Check that the configuration files are in the correct locations
3. Restart your IDE
4. Try using the exact prompts from the examples
5. Check the Copilot logs for any error messages

## Support

For additional help, please:

1. Refer to the examples in the `.github/copilot-edits/examples` directory
2. Check the GitHub Copilot documentation
3. File an issue in the FTOC repository