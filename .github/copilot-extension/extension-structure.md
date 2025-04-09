# FTOC VS Code Extension with Copilot Integration - Structure

This document outlines the structure and components of the planned VS Code extension for FTOC that integrates with GitHub Copilot.

## Directory Structure

```
ftoc-vscode-extension/
├── .vscode/                  # VS Code configuration
├── node_modules/             # Dependencies (git-ignored)
├── src/
│   ├── extension.ts          # Main extension entry point
│   ├── providers/
│   │   ├── completionProvider.ts      # Tag and step autocompletion
│   │   ├── diagnosticProvider.ts      # Feature file validation
│   │   ├── formattingProvider.ts      # Feature file formatting
│   │   └── copilotContextProvider.ts  # Enhance Copilot with FTOC context
│   ├── analyzers/
│   │   ├── tagAnalyzer.ts     # Tag analysis utilities
│   │   ├── scenarioAnalyzer.ts # Scenario structure analysis
│   │   └── featureAnalyzer.ts # Overall feature file analysis
│   ├── commands/
│   │   ├── analyzeCommand.ts  # Run FTOC analysis
│   │   ├── reportCommand.ts   # Generate FTOC reports
│   │   └── generateCommand.ts # Generate new feature files
│   ├── views/
│   │   ├── tagExplorer.ts     # UI for exploring tags
│   │   ├── reportView.ts      # UI for viewing reports
│   │   └── dashboardView.ts   # Overview dashboard
│   ├── models/
│   │   ├── feature.ts         # Feature file model
│   │   ├── scenario.ts        # Scenario model
│   │   └── tag.ts             # Tag model
│   └── utils/
│       ├── parser.ts          # Feature file parser
│       ├── copilotUtils.ts    # Copilot integration utilities
│       └── ftocRunner.ts      # Integration with FTOC CLI
├── package.json              # Extension manifest
├── tsconfig.json             # TypeScript configuration
├── .gitignore                # Git ignore file
└── README.md                 # Extension documentation
```

## Key Components

### 1. Completion Provider

Provides intelligent autocompletions for:
- Tags based on existing project tags
- Steps based on existing step definitions
- Scenario templates following best practices

```typescript
export class FTOCCompletionProvider implements vscode.CompletionItemProvider {
    // Implementation for providing completions within feature files
    public provideCompletionItems(
        document: vscode.TextDocument,
        position: vscode.Position,
        token: vscode.CancellationToken
    ): vscode.CompletionItem[] | Thenable<vscode.CompletionItem[]> {
        // Check if we're in a tag context
        // If so, provide tag completions
        // Otherwise check for step contexts
        // Return appropriate completion items
    }
}
```

### 2. Copilot Context Provider

Enhances GitHub Copilot with FTOC-specific context:

```typescript
export class FTOCCopilotContextProvider {
    // Register with the Copilot API
    public register(): void {
        // Integration with Copilot API
    }
    
    // Provide context to Copilot when editing feature files
    public provideContext(document: vscode.TextDocument): object {
        // Extract relevant context from the document
        // Add project-specific tag patterns
        // Include best practices guidance
        // Return context object
    }
}
```

### 3. Diagnostic Provider

Validates feature files against FTOC best practices:

```typescript
export class FTOCDiagnosticProvider {
    // Validate a feature file and report issues
    public provideDiagnostics(document: vscode.TextDocument): vscode.Diagnostic[] {
        // Parse the feature file
        // Check for anti-patterns
        // Validate tag usage
        // Verify scenario structure
        // Return diagnostics
    }
}
```

### 4. Command Handlers

Implements VS Code commands for FTOC functionality:

```typescript
// Analyze Command
export function registerAnalyzeCommand(context: vscode.ExtensionContext): void {
    let disposable = vscode.commands.registerCommand('ftoc.analyze', async () => {
        // Get current file or directory
        // Run FTOC analysis
        // Display results
    });
    context.subscriptions.push(disposable);
}

// Report Command
export function registerReportCommand(context: vscode.ExtensionContext): void {
    let disposable = vscode.commands.registerCommand('ftoc.report', async () => {
        // Prompt for report type
        // Run FTOC report generation
        // Display report in webview
    });
    context.subscriptions.push(disposable);
}
```

### 5. Views

Custom VS Code views for FTOC functionality:

```typescript
// Tag Explorer View
export class TagExplorerProvider implements vscode.TreeDataProvider<TagItem> {
    // Implementation for TreeDataProvider interface
    // Display tag hierarchy with counts and quality indicators
}

// Report View (WebView)
export class ReportViewProvider implements vscode.WebviewViewProvider {
    // Implementation for WebviewViewProvider interface
    // Display interactive reports
}
```

## Copilot Integration Points

The extension will integrate with GitHub Copilot at these key points:

1. **Enhanced Context**: Provide FTOC-specific context to Copilot when editing feature files
2. **Custom Commands**: Add FTOC-specific commands to Copilot Chat
3. **Smart Completions**: Enhance Copilot's completions with FTOC best practices
4. **Intelligent Fixes**: Suggest FTOC-optimized fixes for issues
5. **Template Generation**: Generate feature file templates with Copilot

## Extension Settings

The extension will provide the following configurable settings:

```json
{
    "ftoc.enableCopilotIntegration": {
        "type": "boolean",
        "default": true,
        "description": "Enable integration with GitHub Copilot"
    },
    "ftoc.analysisOnSave": {
        "type": "boolean",
        "default": true,
        "description": "Run FTOC analysis on save"
    },
    "ftoc.tagRecommendations": {
        "type": "boolean",
        "default": true,
        "description": "Show tag recommendations"
    },
    "ftoc.ftocPath": {
        "type": "string",
        "default": "",
        "description": "Path to FTOC executable"
    }
}
```

## Activation Events

The extension will activate on these events:

```json
"activationEvents": [
    "onLanguage:feature",
    "onCommand:ftoc.analyze",
    "onCommand:ftoc.report",
    "onCommand:ftoc.generate",
    "onView:ftocTagExplorer",
    "onView:ftocReportView"
]
```

## Development Roadmap

1. **Phase 1: Basic Extension**
   - Feature file language support
   - Basic diagnostics
   - Simple command integration

2. **Phase 2: Copilot Integration**
   - Context providers for Copilot
   - Enhanced completions
   - Command integration

3. **Phase 3: Advanced Features**
   - Interactive report views
   - Tag explorer
   - Feature file generation

4. **Phase 4: Packaging and Distribution**
   - Publishing to VS Code Marketplace
   - Documentation
   - User guides