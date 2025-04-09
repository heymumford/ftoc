# FTOC Exception Handling System

This package provides a comprehensive error handling system for the FTOC application. It includes a hierarchy of exception classes, error codes, severity levels, and a centralized error handler.

## Core Components

### Exceptions

- **FtocException**: Base class for all FTOC-specific exceptions
- **FileException**: For file I/O related errors
- **ParsingException**: For feature file parsing errors
- **PluginException**: For plugin-related errors
- **ConfigurationException**: For configuration errors
- **AnalysisException**: For analysis-related errors

### Error Codes

The `ErrorCode` enum provides specific error codes organized by category:

- **General errors** (1000-1999)
- **File/IO errors** (2000-2999)
- **Parsing errors** (3000-3999)
- **Plugin errors** (4000-4999)
- **Analysis errors** (5000-5999)
- **Formatter errors** (6000-6999)

Each error code includes a unique identifier and a description.

### Severity Levels

The `ExceptionSeverity` enum defines different severity levels:

- **FATAL**: Severe errors that cause the application to terminate
- **ERROR**: Serious errors that prevent specific operations
- **WARNING**: Non-critical issues that should be reported
- **INFO**: Informational exceptions for logging

### Error Handler

The `ErrorHandler` class provides centralized error handling logic:

- Consistent logging based on severity
- Decision logic for application continuation
- User-friendly error messages
- Utility methods for direct error handling

## Usage Examples

### Throwing Exceptions

```java
// File not found
throw new FileException(
    "Feature file not found: " + filePath,
    ErrorCode.FILE_NOT_FOUND);

// Invalid Gherkin syntax
throw new ParsingException(
    "Invalid feature file: No 'Feature:' definition found in " + file.getName(),
    ErrorCode.INVALID_GHERKIN);

// Configuration error with WARNING severity
throw new ConfigurationException(
    "Unknown format specified, using default",
    ErrorCode.CONFIGURATION_ERROR,
    ExceptionSeverity.WARNING);
```

### Handling Exceptions

```java
try {
    // Code that might throw an exception
} catch (FtocException e) {
    // Let the error handler decide if processing should continue
    boolean canContinue = ErrorHandler.handleException(e);
    
    if (!canContinue) {
        // Fatal error, terminate processing
        return;
    }
    
    // Display user-friendly message
    System.err.println(ErrorHandler.getUserFriendlyMessage(e));
}
```

### Direct Error Handling

```java
// Create and handle an error in one step
boolean canContinue = ErrorHandler.handleError(
    "Failed to load configuration from file",
    previousException,
    ErrorCode.CONFIGURATION_ERROR,
    ExceptionSeverity.ERROR);
```

## Extending the System

To add new exception types:

1. Create a new class that extends `FtocException`
2. Implement appropriate constructors
3. Add specific error codes to the `ErrorCode` enum
4. Update the `ErrorHandler.getUserFriendlyMessage()` method to handle the new type

## Best Practices

1. Use the most specific exception type available
2. Include descriptive error messages with relevant details
3. Set appropriate error codes and severity levels
4. Always handle exceptions at the appropriate level
5. Use `ErrorHandler` for consistent handling across the application