# C4 Component Diagram for FTOC

```
┌──────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                              │
│                              FTOC Core Engine - Component Diagram                            │
│                                                                                              │
├──────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                              │
│   ┌────────────────────────────────────────────────────────────────────────────────────────┐ │
│   │                                                                                        │ │
│   │                                 FtocUtility                                            │ │
│   │                                                                                        │ │
│   │  Main class responsible for coordinating the process and command-line interface        │ │
│   │                                                                                        │ │
│   └────────────┬─────────────────────────────┬─────────────────────────────┬──────────────┘ │
│                │                             │                             │                │
│                │                             │                             │                │
│                │                             │                             │                │
│                ▼                             ▼                             ▼                │
│   ┌────────────────────────┐  ┌─────────────────────────┐  ┌─────────────────────────────┐ │
│   │                        │  │                         │  │                             │ │
│   │   FeatureFileManager   │  │     FeatureParser      │  │      VersionManager         │ │
│   │                        │  │                         │  │                             │ │
│   │ - Finds feature files  │  │ - Parses feature files │  │ - Manages version info      │ │
│   │ - Reads file content   │  │ - Extracts elements    │  │ - Outputs version info      │ │
│   │                        │  │                         │  │                             │ │
│   └────────────┬───────────┘  └─────────────┬───────────┘  └─────────────────────────────┘ │
│                │                            │                                              │
│                │                            │                                              │
│                │                            │                                              │
│                ▼                            ▼                                              │
│   ┌────────────────────────┐  ┌─────────────────────────┐  ┌─────────────────────────────┐ │
│   │                        │  │                         │  │                             │ │
│   │      TagAnalyzer       │  │    ScenarioAnalyzer     │  │     OutputFormatter        │ │
│   │                        │  │                         │  │                             │ │
│   │ - Creates concordance  │  │ - Creates TOC           │  │ - Formats output based on  │ │
│   │ - Detects low value    │  │ - Analyzes quality      │  │   selected format          │ │
│   │ - Checks for missing   │  │ - Detects patterns      │  │                             │ │
│   │                        │  │                         │  │                             │ │
│   └────────────┬───────────┘  └─────────────┬───────────┘  └───────────┬─────────────────┘ │
│                │                            │                          │                   │
│                │                            │                          │                   │
│                │                            │                          │                   │
│                ▼                            ▼                          ▼                   │
│   ┌────────────────────────┐  ┌─────────────────────────┐  ┌─────────────────────────────┐ │
│   │                        │  │                         │  │                             │ │
│   │   TagPatternDetector   │  │ CoverageAnalyzer        │  │    Format Strategies       │ │
│   │                        │  │                         │  │                             │ │
│   │ - Detects tag patterns │  │ - Analyzes test coverage│  │ - TextFormatter            │ │
│   │ - Suggestion engine    │  │ - Pairwise analysis     │  │ - MarkdownFormatter        │ │
│   │                        │  │ - Positive/negative     │  │ - HtmlFormatter            │ │
│   │                        │  │                         │  │ - JsonFormatter            │ │
│   └────────────────────────┘  └─────────────────────────┘  └─────────────────────────────┘ │
│                                                                                            │
└────────────────────────────────────────────────────────────────────────────────────────────┘
```