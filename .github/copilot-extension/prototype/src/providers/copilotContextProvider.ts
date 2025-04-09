import * as vscode from 'vscode';
import { parseFeatureFile } from '../utils/parser';

/**
 * Provides FTOC-specific context to GitHub Copilot.
 * This enhances Copilot's understanding of feature files and FTOC best practices.
 */
export class FtocCopilotContext {
    /**
     * Provides context about the current document to GitHub Copilot
     * @param document The current document
     * @returns Context object with FTOC-specific information
     */
    public provideContext(document: vscode.TextDocument): any {
        if (document.languageId !== 'feature') {
            return {};
        }
        
        try {
            // Parse the feature file
            const feature = parseFeatureFile(document.getText());
            
            // Create context object with FTOC-specific information
            return {
                // Feature metadata
                featureFile: {
                    name: feature.name,
                    tags: feature.tags,
                    scenarioCount: feature.scenarios.length,
                    filePath: document.fileName
                },
                
                // FTOC best practices
                ftocBestPractices: [
                    "Use hierarchical tags (@P0, @P1, etc.) for priority designation",
                    "Include @API, @UI, or @Integration tags for test type",
                    "Add @Smoke for critical test paths",
                    "Avoid generic tags like @test or @feature",
                    "Use snake_case for step descriptions",
                    "Keep scenarios focused and concise"
                ],
                
                // Common tag patterns used in this project
                tagPatterns: this.getProjectTagPatterns(),
                
                // FTOC tag quality guidelines
                tagQualityGuidelines: {
                    requiredTags: ["@P0", "@P1", "@P2", "@P3"],
                    functionTags: ["@API", "@UI", "@Integration", "@Unit"],
                    statusTags: ["@WIP", "@Ready", "@Deprecated"],
                    frameworkTags: ["@Karate", "@Cucumber", "@JUnit"]
                },
                
                // Anti-patterns to avoid
                antiPatterns: [
                    "Scenarios without tags",
                    "Overly complex scenarios with many steps",
                    "Inconsistent tag formatting",
                    "Vague scenario descriptions",
                    "Missing Given-When-Then structure"
                ]
            };
        } catch (error) {
            console.error('Error providing Copilot context:', error);
            return {};
        }
    }
    
    /**
     * Gets common tag patterns from the project
     * In a real implementation, this would analyze the workspace
     */
    private getProjectTagPatterns(): object {
        return {
            // Priority tags
            priority: ["@P0", "@P1", "@P2", "@P3"],
            
            // Function tags
            function: ["@API", "@UI", "@Integration", "@Unit"],
            
            // Status tags
            status: ["@WIP", "@Ready", "@Deprecated", "@Legacy"],
            
            // Common combinations
            commonCombinations: [
                ["@P0", "@Smoke", "@API"],
                ["@P1", "@Regression", "@API"],
                ["@P2", "@Integration"],
                ["@P3", "@UI"]
            ]
        };
    }
}