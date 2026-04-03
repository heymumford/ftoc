# FTOC Core Architecture

This directory contains the core classes for the FTOC utility, which focus on proper separation of concerns.

## Core Classes

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
- `processFeatures(List<Path> features)`: Process feature files sequentially

### `Reporter`

Responsible for generating various reports:
- `generateTableOfContents(List<Feature> features, Format format, List<String> includeTags, List<String> excludeTags)`: Generate TOC report
- `generateConcordanceReport(Map<String, Integer> tagConcordance, List<Feature> features, Format format)`: Generate concordance report
- `generateTagQualityReport(List<Feature> features, Map<String, Integer> tagConcordance, Format format)`: Generate tag quality report
- `generateAntiPatternReport(List<Feature> features, Format format)`: Generate anti-pattern report
- `setOutputHandler(Consumer<String> outputHandler)`: Configure output handler
