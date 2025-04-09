# ADR 0003: Output Format Strategy

## Status

Accepted

## Context

The ftoc utility needs to output analysis results in different formats to support various integration scenarios. Initially, we need to support:

1. **Plain text**: For terminal output
2. **Markdown**: For documentation
3. **HTML**: For web viewing
4. **JSON**: For programmatic integration

Each format has different requirements and structures, and we need a flexible approach that allows adding new formats in the future.

## Decision

We will implement the Strategy pattern for output formatting:

1. Define an `OutputFormatter` interface with a `format(AnalysisResult result)` method
2. Create concrete implementations for each output format:
   - `TextOutputFormatter`
   - `MarkdownOutputFormatter` 
   - `HtmlOutputFormatter`
   - `JsonOutputFormatter`
3. Use a factory to create the appropriate formatter based on user selection
4. Make the output format configurable via command-line options

## Consequences

### Positive

- Clear separation between analysis and presentation
- Easy to add new output formats
- Users can select the most appropriate format for their needs
- Testable in isolation

### Negative

- More complex than a single output format
- Need to maintain multiple formatters
- May require additional libraries for some formats (like JSON)

### Risks

- Different formats may require different levels of detail, which could complicate the analysis result model
- Some formats may have specific requirements that are difficult to standardize

### Mitigations

- Design a flexible analysis result model that contains all necessary information
- Use adapter methods in formatters to transform data as needed
- Consider format-specific extensions to the base model