# FTOC Core Architecture

This directory contains the core interfaces for the FTOC utility's refactored architecture, which focuses on proper separation of concerns.

## Core Interfaces

### `FeatureRepository`

Responsible for finding and loading feature files:
- `findFeatureFiles(Path directoryPath)`: Find all feature files in a directory
- `loadFeature(Path path)`: Load a single feature file
- `loadFeatures(Path directoryPath)`: Load all feature files in a directory
- `loadFeatures(List<Path> featurePaths)`: Load a list of feature files

### `FeatureProcessor`

Responsible for processing and analyzing features:
- `generateTagConcordance(List<Feature> features)`: Generate tag concordance information
- `filterFeaturesByTags(List<Feature> features, List<String> includeTags, List<String> excludeTags)`: Filter features by tags
- `processFeatures(List<Path> features, boolean useParallel)`: Process features with optional parallel execution
- `shouldUseParallelProcessing(int featureCount)`: Determine if parallel processing should be used

### `Reporter`

Responsible for generating various reports:
- `generateTableOfContents(List<Feature> features, Format format, List<String> includeTags, List<String> excludeTags)`: Generate TOC report
- `generateConcordanceReport(Map<String, Integer> tagConcordance, List<Feature> features, Format format)`: Generate concordance report
- `generateTagQualityReport(List<Feature> features, Map<String, Integer> tagConcordance, Format format)`: Generate tag quality report
- `generateAntiPatternReport(List<Feature> features, Format format)`: Generate anti-pattern report
- `setOutputHandler(Consumer<String> outputHandler)`: Configure output handler

## Default Implementations

The `impl` directory contains default implementations of the core interfaces:

### `DefaultFeatureRepository`

Default implementation of `FeatureRepository` that handles file system operations and delegates to the appropriate parser based on file content.

### `DefaultFeatureProcessor`

Default implementation of `FeatureProcessor` that handles feature processing, tag concordance generation, and performance optimizations through parallel processing.

### `DefaultReporter`

Default implementation of `Reporter` that delegates to specialized formatters for each report type and supports various output formats.

## Usage

The new architecture can be used through the `FtocUtilityRefactored` class, which provides a convenient API that is similar to the original `FtocUtility` class:

```java
// Using default implementation
FtocUtilityRefactored ftoc = new FtocUtilityRefactored();
ftoc.initialize();
ftoc.processDirectory("/path/to/features");

// Or with custom components
FeatureRepository repository = new CustomFeatureRepository();
FeatureProcessor processor = new CustomFeatureProcessor(repository);
Reporter reporter = new CustomReporter();

FtocUtilityRefactored ftoc = new FtocUtilityRefactored(repository, processor, reporter);
ftoc.initialize();
ftoc.processDirectory("/path/to/features");
```