# FTOC Architecture

This document describes the architecture of the FTOC utility, focusing on the separation of concerns introduced in the recent refactoring.

## Overview

The FTOC utility is designed to analyze Cucumber/Gherkin feature files and generate various reports, including a table of contents, tag concordance, tag quality analysis, and anti-pattern detection.

The architecture follows these key principles:

- **Separation of concerns**: Clearly defined responsibilities for each component
- **Testability**: Components can be tested in isolation
- **Extensibility**: New implementations can be added without changing the core interfaces
- **Maintainability**: Reduced coupling between components

## Core Components

The FTOC architecture consists of three main components:

1. **Feature Repository**: Responsible for finding and loading feature files
2. **Feature Processor**: Responsible for processing and analyzing features
3. **Reporter**: Responsible for generating reports in various formats

### Component Relationships

```
┌───────────────┐       ┌───────────────┐       ┌───────────────┐
│               │       │               │       │               │
│ Feature       │──────▶│ Feature       │──────▶│ Reporter      │
│ Repository    │       │ Processor     │       │               │
│               │       │               │       │               │
└───────────────┘       └───────────────┘       └───────────────┘
        │                      │                       │
        ▼                      ▼                       ▼
┌───────────────┐       ┌───────────────┐       ┌───────────────┐
│               │       │               │       │               │
│ Default       │       │ Default       │       │ Default       │
│ Feature       │       │ Feature       │       │ Reporter      │
│ Repository    │       │ Processor     │       │               │
│               │       │               │       │               │
└───────────────┘       └───────────────┘       └───────────────┘
```

### Feature Repository

The `FeatureRepository` interface defines operations for finding and loading feature files:

```java
public interface FeatureRepository {
    List<Path> findFeatureFiles(Path directoryPath);
    Optional<Feature> loadFeature(Path path);
    List<Feature> loadFeatures(Path directoryPath);
    List<Feature> loadFeatures(List<Path> featurePaths);
}
```

The default implementation, `DefaultFeatureRepository`, handles file system operations and delegates to the appropriate parser based on file content.

### Feature Processor

The `FeatureProcessor` interface defines operations for analyzing and processing features:

```java
public interface FeatureProcessor {
    Map<String, Integer> generateTagConcordance(List<Feature> features);
    List<Feature> filterFeaturesByTags(List<Feature> features, List<String> includeTags, List<String> excludeTags);
    List<Feature> processFeatures(List<Path> features, boolean useParallel);
    boolean shouldUseParallelProcessing(int featureCount);
}
```

The default implementation, `DefaultFeatureProcessor`, handles feature processing, tag concordance generation, and performance optimizations through parallel processing.

### Reporter

The `Reporter` interface defines operations for generating various reports:

```java
public interface Reporter {
    enum Format { PLAIN_TEXT, MARKDOWN, HTML, JSON, JUNIT_XML }
    
    String generateTableOfContents(List<Feature> features, Format format, 
                                  List<String> includeTags, List<String> excludeTags);
    String generateConcordanceReport(Map<String, Integer> tagConcordance, 
                                    List<Feature> features, Format format);
    String generateTagQualityReport(List<Feature> features, 
                                   Map<String, Integer> tagConcordance,
                                   Format format);
    String generateAntiPatternReport(List<Feature> features, Format format);
    void setOutputHandler(Consumer<String> outputHandler);
}
```

The default implementation, `DefaultReporter`, delegates to specialized formatters for each report type and supports various output formats.

## Main Utility Class

The `FtocUtilityRefactored` class serves as the main entry point for the utility. It coordinates the work of the three core components and provides a convenient API for users.

```java
public class FtocUtilityRefactored {
    private final FeatureRepository repository;
    private final FeatureProcessor processor;
    private final Reporter reporter;
    
    // Methods to process directories and generate reports
    public void processDirectory(String directoryPath) { ... }
    
    // Methods to configure the utility
    public void setOutputFormat(Reporter.Format format) { ... }
    public void addIncludeTagFilter(String tag) { ... }
    // ...
}
```

## Migration from Old Architecture

The `ArchitectureMigration` utility class helps migrate from the old architecture to the new one:

```java
FtocUtility oldUtility = new FtocUtility();
FtocUtilityRefactored newUtility = ArchitectureMigration.migrateToNewArchitecture(oldUtility);
```

## Creating Custom Implementations

You can create custom implementations of any of the three core components and inject them into the `FtocUtilityRefactored` constructor:

```java
FeatureRepository customRepository = new CustomFeatureRepository();
FeatureProcessor customProcessor = new CustomFeatureProcessor(customRepository);
Reporter customReporter = new CustomReporter();

FtocUtilityRefactored ftoc = new FtocUtilityRefactored(
    customRepository, customProcessor, customReporter);
```

## Benefits of the New Architecture

1. **Better separation of concerns**: Each component has a clear, focused responsibility
2. **Improved testability**: Components can be tested in isolation using mocks
3. **Enhanced extensibility**: New implementations can be easily added without modifying existing code
4. **Simplified maintenance**: Issues can be isolated to specific components
5. **Better dependency management**: Dependencies are explicit and injected rather than implicitly created