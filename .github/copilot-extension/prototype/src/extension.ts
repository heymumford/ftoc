// FTOC extension for VS Code with GitHub Copilot Integration
// This is a prototype implementation demonstrating the integration approach

import * as vscode from 'vscode';
import { FtocCompletionProvider } from './providers/completionProvider';
import { FtocDiagnosticProvider } from './providers/diagnosticProvider';
import { FtocCopilotContext } from './providers/copilotContextProvider';
import { registerAnalyzeCommand, registerReportCommand, registerGenerateCommand, registerSuggestTagsCommand } from './commands';
import { TagExplorerProvider } from './views/tagExplorerView';

export function activate(context: vscode.ExtensionContext) {
    // Log activation
    console.log('FTOC extension is now active');

    // Register the feature file completion provider
    const completionProvider = new FtocCompletionProvider();
    context.subscriptions.push(
        vscode.languages.registerCompletionItemProvider(
            { scheme: 'file', language: 'feature' },
            completionProvider
        )
    );

    // Initialize the diagnostic provider
    const diagnosticProvider = new FtocDiagnosticProvider();
    const diagnosticCollection = vscode.languages.createDiagnosticCollection('ftoc');
    context.subscriptions.push(diagnosticCollection);

    // Setup document listeners for diagnostics
    if (vscode.window.activeTextEditor) {
        updateDiagnostics(vscode.window.activeTextEditor.document, diagnosticProvider, diagnosticCollection);
    }
    
    context.subscriptions.push(
        vscode.workspace.onDidSaveTextDocument(document => {
            if (document.languageId === 'feature' && 
                vscode.workspace.getConfiguration('ftoc').get('analysisOnSave', true)) {
                updateDiagnostics(document, diagnosticProvider, diagnosticCollection);
            }
        })
    );

    // Register commands
    registerAnalyzeCommand(context);
    registerReportCommand(context);
    registerGenerateCommand(context);
    registerSuggestTagsCommand(context);

    // Setup tag explorer view
    const tagExplorerProvider = new TagExplorerProvider();
    vscode.window.registerTreeDataProvider('ftocTagExplorer', tagExplorerProvider);

    // Setup Copilot integration if available and enabled
    if (vscode.workspace.getConfiguration('ftoc').get('enableCopilotIntegration', true)) {
        setupCopilotIntegration(context);
    }
}

function updateDiagnostics(
    document: vscode.TextDocument, 
    diagnosticProvider: FtocDiagnosticProvider,
    diagnosticCollection: vscode.DiagnosticCollection
): void {
    if (document.languageId !== 'feature') {
        return;
    }
    
    // Clear previous diagnostics
    diagnosticCollection.delete(document.uri);
    
    // Get new diagnostics
    const diagnostics = diagnosticProvider.provideDiagnostics(document);
    
    // Set diagnostics
    diagnosticCollection.set(document.uri, diagnostics);
}

function setupCopilotIntegration(context: vscode.ExtensionContext): void {
    // Check if Copilot is installed
    const copilot = vscode.extensions.getExtension('github.copilot');
    
    if (!copilot) {
        console.log('GitHub Copilot extension not found, integration disabled');
        return;
    }
    
    console.log('Setting up GitHub Copilot integration');
    
    // Initialize Copilot context provider
    const copilotContextProvider = new FtocCopilotContext();
    
    try {
        // This is a conceptual example - the actual API may differ
        // Register with Copilot's API when it becomes available
        if (copilot.isActive && copilot.exports) {
            console.log('Registering FTOC context with Copilot');
            
            // Example of how this might work - subject to actual Copilot API
            if (copilot.exports.registerContext) {
                copilot.exports.registerContext({
                    name: 'ftoc',
                    provider: copilotContextProvider
                });
            }
            
            // Register custom Copilot chat commands when API is available
            if (copilot.exports.registerChatCommands) {
                console.log('Registering FTOC commands with Copilot Chat');
                copilot.exports.registerChatCommands([
                    {
                        name: 'ftoc.analyze',
                        description: 'Analyze feature files with FTOC',
                        handler: async (args: string) => {
                            // Implementation would invoke the analyze command
                            return "FTOC analysis results would appear here";
                        }
                    },
                    {
                        name: 'ftoc.suggest-tags',
                        description: 'Suggest tags for the current feature',
                        handler: async (args: string) => {
                            // Implementation would generate tag suggestions
                            return "Tag suggestions would appear here";
                        }
                    }
                ]);
            }
        }
    } catch (error) {
        console.error('Error integrating with Copilot:', error);
    }
}

export function deactivate() {
    // Cleanup when extension is deactivated
}