# FTOC Code Patterns for GitHub Copilot

This document describes the coding patterns and conventions used in the FTOC project to help GitHub Copilot generate more appropriate code suggestions.

## Package and Class Structure

### Model Classes
- Located in `com.heymumford.ftoc.model`
- Represent domain objects like Feature, Scenario, etc.
- Immutable where possible
- Properties initialized in constructor
- Getters but minimal setters

```java
public class Feature {
    private final String file;
    private String name;
    private final List<String> tags;
    private final List<Scenario> scenarios;

    public Feature(String file) {
        this.file = file;
        this.tags = new ArrayList<>();
        this.scenarios = new ArrayList<>();
    }

    // Getters and controlled mutators
}
```

### Parser Classes
- Located in `com.heymumford.ftoc.parser`
- Responsible for converting external formats to model objects
- Never modify global state
- Return parsed objects, don't mutate passed arguments

```java
public class FeatureParser {
    public Feature parseFeatureFile(File file) {
        Feature feature = new Feature(file.getPath());
        // Parsing logic
        return feature;
    }
}
```

### Formatter Classes
- Located in `com.heymumford.ftoc.formatter`
- Convert model objects to various output formats
- Use enum for format types
- Implement method per format

```java
public class TocFormatter {
    public enum Format {
        PLAIN_TEXT,
        MARKDOWN,
        HTML,
        JSON
    }

    public String generateToc(List<Feature> features, Format format) {
        switch (format) {
            case MARKDOWN:
                return generateMarkdownToc(features);
            case HTML:
                return generateHtmlToc(features);
            case JSON:
                return generateJsonToc(features);
            case PLAIN_TEXT:
            default:
                return generatePlainTextToc(features);
        }
    }
}
```

### Analyzer Classes
- Located in `com.heymumford.ftoc.analyzer`
- Perform analysis on model objects
- Return analysis results, don't modify input
- Use inner classes or enums for categorization

```java
public class TagQualityAnalyzer {
    public enum WarningType {
        MISSING_PRIORITY_TAG("Missing priority tag"),
        // Other warning types
    }

    public static class Warning {
        private final WarningType type;
        private final String message;
        // Other fields and methods
    }

    public List<Warning> analyzeTagQuality() {
        // Analysis logic
        return warnings;
    }
}
```

## Error Handling

- Use Optional for values that might not exist
- Throw specific exceptions with clear messages
- Log errors before throwing exceptions
- Use RuntimeExceptions for unrecoverable errors

```java
public Feature parseFeatureFile(File file) {
    if (!file.exists()) {
        logger.error("Feature file does not exist: {}", file.getPath());
        throw new IllegalArgumentException("Feature file does not exist: " + file.getPath());
    }
    // Parsing logic
}
```

## Testing

- Unit tests in same package as class under test
- Use descriptive method names
- Use AssertJ for assertions
- Test happy path and edge cases
- Mock external dependencies

```java
@Test
public void shouldParseFeatureWithMultipleScenarios() {
    // Test setup
    File testFile = new File("src/test/resources/features/multiple_scenarios.feature");
    
    // Test execution
    Feature feature = parser.parseFeatureFile(testFile);
    
    // Assertions
    assertThat(feature).isNotNull();
    assertThat(feature.getScenarios()).hasSize(3);
    // More assertions
}
```

## Logging

- Use SLF4J for logging
- Log at appropriate levels
- Include context in log messages
- Don't log sensitive information

```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

public void methodName() {
    logger.debug("Processing file: {}", filename);
    // Method logic
    logger.info("Successfully processed {} scenarios", count);
}
```

## Command-Line Arguments

- Use positional arguments sparingly
- Prefer named options with clear meanings
- Support help and version options
- Validate input early

```java
if ("-d".equals(args[i]) && i + 1 < args.length) {
    directoryPath = args[i + 1];
    i++; // Skip the next argument
} else if ("-f".equals(args[i]) && i + 1 < args.length) {
    // Format handling
}
```

## Documentation

- JavaDoc for all public classes and methods
- Include examples where appropriate
- Document parameters and return values
- Explain exceptions that may be thrown

```java
/**
 * Parses a Cucumber feature file and returns a Feature object.
 *
 * @param file The feature file to parse
 * @return A Feature object representing the parsed file
 * @throws IllegalArgumentException if the file does not exist or is not readable
 */
public Feature parseFeatureFile(File file) {
    // Implementation
}
```