/**
 * Parser utility for feature files
 * In a real implementation, this would be more robust and handle all Gherkin syntax
 */

// Feature file model
export interface Feature {
    name: string;
    description: string;
    tags: string[];
    scenarios: Scenario[];
    path?: string;
}

// Scenario model
export interface Scenario {
    name: string;
    type: 'Scenario' | 'Scenario Outline' | 'Background';
    description: string;
    tags: string[];
    steps: string[];
    examples?: Example[];
    lineNumber?: number;
}

// Example model for scenario outlines
export interface Example {
    name: string;
    headers: string[];
    rows: string[][];
}

/**
 * Parse a feature file content into a structured Feature object
 * This is a simplified parser for demonstration purposes
 */
export function parseFeatureFile(content: string): Feature {
    // Default feature structure
    const feature: Feature = {
        name: 'Unnamed Feature',
        description: '',
        tags: [],
        scenarios: []
    };
    
    // Split content into lines for parsing
    const lines = content.split('\n');
    let currentTags: string[] = [];
    let currentScenario: Scenario | null = null;
    let parsingFeature = false;
    let parsingDescription = false;
    let descriptionBuffer = '';
    
    // Parse line by line
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim();
        
        // Skip empty lines
        if (!line) {
            if (parsingDescription) {
                descriptionBuffer += '\n';
            }
            continue;
        }
        
        // Parse tags (lines starting with @)
        if (line.startsWith('@')) {
            currentTags = line.split(/\s+/).filter(tag => tag.startsWith('@'));
            continue;
        }
        
        // Parse Feature
        if (line.startsWith('Feature:')) {
            parsingFeature = true;
            parsingDescription = true;
            feature.name = line.substring('Feature:'.length).trim();
            feature.tags = [...currentTags];
            currentTags = [];
            continue;
        }
        
        // Parse Scenario
        if (line.startsWith('Scenario:') || line.startsWith('Scenario Outline:') || line.startsWith('Background:')) {
            // If we were parsing a description, assign it
            if (parsingDescription) {
                if (currentScenario) {
                    currentScenario.description = descriptionBuffer.trim();
                } else {
                    feature.description = descriptionBuffer.trim();
                }
                parsingDescription = false;
                descriptionBuffer = '';
            }
            
            // Determine scenario type
            let type: 'Scenario' | 'Scenario Outline' | 'Background' = 'Scenario';
            if (line.startsWith('Scenario Outline:')) {
                type = 'Scenario Outline';
            } else if (line.startsWith('Background:')) {
                type = 'Background';
            }
            
            // Extract scenario name
            let name = '';
            if (type === 'Scenario') {
                name = line.substring('Scenario:'.length).trim();
            } else if (type === 'Scenario Outline') {
                name = line.substring('Scenario Outline:'.length).trim();
            } else {
                name = line.substring('Background:'.length).trim();
            }
            
            // Create new scenario
            currentScenario = {
                name,
                type,
                description: '',
                tags: [...currentTags],
                steps: [],
                lineNumber: i + 1
            };
            
            // Add to feature
            feature.scenarios.push(currentScenario);
            
            // Reset tags
            currentTags = [];
            
            // Start parsing description
            parsingDescription = true;
            continue;
        }
        
        // Parse steps
        if (currentScenario && (line.startsWith('Given ') || line.startsWith('When ') || 
                               line.startsWith('Then ') || line.startsWith('And ') || 
                               line.startsWith('But ') || line.startsWith('* '))) {
            // If we were parsing a description, assign it
            if (parsingDescription) {
                currentScenario.description = descriptionBuffer.trim();
                parsingDescription = false;
                descriptionBuffer = '';
            }
            
            // Add step to current scenario
            currentScenario.steps.push(line);
            continue;
        }
        
        // Parse examples
        if (currentScenario && line.startsWith('Examples:')) {
            // If we were parsing a description, assign it
            if (parsingDescription) {
                currentScenario.description = descriptionBuffer.trim();
                parsingDescription = false;
                descriptionBuffer = '';
            }
            
            // Ensure examples array exists
            if (!currentScenario.examples) {
                currentScenario.examples = [];
            }
            
            // Create new example
            const example: Example = {
                name: line.substring('Examples:'.length).trim(),
                headers: [],
                rows: []
            };
            
            // Look ahead for table
            let tableIndex = i + 1;
            
            // Parse headers
            while (tableIndex < lines.length && !lines[tableIndex].trim().startsWith('|')) {
                tableIndex++;
            }
            
            if (tableIndex < lines.length) {
                const headerLine = lines[tableIndex].trim();
                if (headerLine.startsWith('|') && headerLine.endsWith('|')) {
                    example.headers = headerLine
                        .split('|')
                        .filter(cell => cell.trim() !== '')
                        .map(cell => cell.trim());
                    
                    // Parse rows
                    tableIndex++;
                    while (tableIndex < lines.length) {
                        const rowLine = lines[tableIndex].trim();
                        if (!rowLine.startsWith('|')) {
                            break;
                        }
                        
                        if (rowLine.startsWith('|') && rowLine.endsWith('|')) {
                            const row = rowLine
                                .split('|')
                                .filter(cell => cell.trim() !== '')
                                .map(cell => cell.trim());
                            
                            example.rows.push(row);
                        }
                        
                        tableIndex++;
                    }
                }
            }
            
            // Add example to scenario
            currentScenario.examples.push(example);
            
            // Skip to end of table
            i = tableIndex - 1;
            continue;
        }
        
        // Add to description buffer if we're parsing description
        if (parsingDescription) {
            if (descriptionBuffer) {
                descriptionBuffer += '\n';
            }
            descriptionBuffer += line;
        }
    }
    
    // Handle any final description
    if (parsingDescription) {
        if (currentScenario) {
            currentScenario.description = descriptionBuffer.trim();
        } else if (parsingFeature) {
            feature.description = descriptionBuffer.trim();
        }
    }
    
    return feature;
}

/**
 * Extract all tags from a feature file
 */
export function extractTags(feature: Feature): string[] {
    const tags = new Set<string>();
    
    // Add feature tags
    feature.tags.forEach(tag => tags.add(tag));
    
    // Add scenario tags
    feature.scenarios.forEach(scenario => {
        scenario.tags.forEach(tag => tags.add(tag));
    });
    
    return Array.from(tags);
}

/**
 * Group tags by category based on prefix
 */
export function categorizeTagsByPrefix(tags: string[]): Record<string, string[]> {
    const categories: Record<string, string[]> = {
        'Priority': [],
        'TestType': [],
        'Status': [],
        'Other': []
    };
    
    // Priority pattern: @P0, @P1, etc.
    const priorityPattern = /^@P[0-9]$/;
    
    // Test type pattern: @API, @UI, @Integration, etc.
    const testTypePattern = /^@(API|UI|Integration|Unit|Smoke|Regression)$/;
    
    // Status pattern: @WIP, @Ready, @Deprecated, etc.
    const statusPattern = /^@(WIP|Ready|Deprecated|Legacy|Bug|FixNeeded)$/;
    
    tags.forEach(tag => {
        if (priorityPattern.test(tag)) {
            categories['Priority'].push(tag);
        } else if (testTypePattern.test(tag)) {
            categories['TestType'].push(tag);
        } else if (statusPattern.test(tag)) {
            categories['Status'].push(tag);
        } else {
            categories['Other'].push(tag);
        }
    });
    
    return categories;
}