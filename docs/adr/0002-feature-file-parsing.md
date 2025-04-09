# ADR 0002: Feature File Parsing Approach

## Status

Accepted

## Context

The ftoc utility needs to parse Cucumber feature files to extract scenarios, tags, and other elements. There are multiple approaches to parsing feature files:

1. **Line-by-line text processing**: Simple but less robust
2. **Regular expressions**: More powerful but can become complex
3. **Using Cucumber's Gherkin parser**: More robust but adds dependencies
4. **Creating a custom parser**: Flexible but requires more development effort

Each approach has different trade-offs in terms of development effort, accuracy, and maintenance.

## Decision

We will use **Cucumber's Gherkin parser library** for parsing feature files. Specifically, we'll:

1. Add the Gherkin parser as a dependency
2. Create a wrapper class (`FeatureParser`) to isolate the dependency
3. Transform the parsed output into our own domain model

This will give us robust parsing with the ability to handle all valid Gherkin syntax while isolating the external dependency.

## Consequences

### Positive

- Robust parsing of all valid Gherkin syntax
- Handles edge cases and different Gherkin formats
- Maintainable as the parser is updated with new Gherkin features
- Allows focusing on analysis rather than parsing concerns

### Negative

- Adds external dependency
- Increases JAR size
- May include more functionality than needed

### Risks

- The Gherkin parser API could change in future versions
- The parser may be overly complex for our needs

### Mitigations

- Isolate the parser behind an interface
- Consider a simpler custom parser if the Gherkin parser becomes problematic
- Use dependency shading to reduce JAR size