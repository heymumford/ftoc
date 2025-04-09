# 6. Separation of Concerns Architecture

Date: 2025-04-09

## Status

Accepted

## Context

As the FTOC utility has grown in complexity, the original monolithic architecture faced several challenges:

1. The FtocUtility class had too many responsibilities, leading to code that was difficult to maintain and test
2. Adding new features often required modifying multiple parts of the codebase
3. Testing was cumbersome because components were tightly coupled
4. Extending the functionality with custom components was not easy

We needed a cleaner architecture that would address these issues and provide a solid foundation for future enhancements.

## Decision

We have implemented a new architecture based on the separation of concerns principle, with clear interfaces and well-defined responsibilities:

1. **FeatureRepository Interface**: Responsible for finding and loading feature files
   - Encapsulates file system operations
   - Abstracts the parsing logic

2. **FeatureProcessor Interface**: Responsible for processing and analyzing features
   - Handles tag concordance generation
   - Manages parallel processing for performance
   - Applies tag filtering

3. **Reporter Interface**: Responsible for generating various reports
   - Produces table of contents, concordance, tag quality, and anti-pattern reports
   - Supports multiple output formats
   - Configurable output handling

Each interface has a default implementation that provides the core functionality, but custom implementations can be created to extend or modify behavior.

The main entry point is now the `FtocUtilityRefactored` class, which coordinates the three components and provides a simple API similar to the original `FtocUtility` class for backward compatibility.

## Consequences

### Positive

- **Improved maintainability**: Each component has a well-defined responsibility and can be modified independently
- **Better testability**: Components can be tested in isolation using mocks
- **Enhanced extensibility**: New implementations can be provided for any of the interfaces without modifying existing code
- **Clearer code organization**: The codebase is more logically structured and easier to navigate
- **Simplified development**: Adding new features is cleaner as developers know where changes should be made

### Negative

- **Increased complexity**: The architecture has more moving parts
- **Learning curve**: New developers need to understand the component interactions
- **Migration effort**: Existing code needs to be updated to use the new architecture

## Clarifying Notes

The main interfaces are:

- `FeatureRepository` in `com.heymumford.ftoc.core.FeatureRepository`
- `FeatureProcessor` in `com.heymumford.ftoc.core.FeatureProcessor`
- `Reporter` in `com.heymumford.ftoc.core.Reporter`

Default implementations are provided in the `com.heymumford.ftoc.core.impl` package:

- `DefaultFeatureRepository`
- `DefaultFeatureProcessor`
- `DefaultReporter`

## Future Considerations

This architectural change sets the stage for additional improvements:

1. Creating a plugin system for extensibility
2. Implementing more comprehensive error handling
3. Adding a strategy pattern for output formats
4. Supporting custom report templates
5. Implementing a proper dependency injection framework