# ADR 0004: Tag Analysis Approach

## Status

Accepted

## Context

One of the core features of ftoc is analyzing tags in Cucumber feature files. This includes:

1. **Tag concordance**: Counting occurrences of each tag
2. **Low-value tag detection**: Identifying generic tags with limited utility
3. **Missing tag detection**: Finding scenarios without required tag types
4. **Tag consistency analysis**: Checking for inconsistent tag usage across files

We need a structured approach to implementing these features that is both extensible and maintainable.

## Decision

We will implement a tag analysis system with the following components:

1. **TagAnalyzer**: Core class that coordinates tag analysis
   - Collects tags from parsed features
   - Maintains tag statistics
   - Delegates to specialized analyzers for specific checks

2. **Specialized Analyzers**:
   - **TagConcordanceAnalyzer**: Counts occurrences of tags
   - **LowValueTagDetector**: Identifies generic tags using configurable rules
   - **MissingTagDetector**: Checks for required tag categories
   - **TagConsistencyAnalyzer**: Checks for inconsistent tag usage patterns

3. **Rule-based Configuration**:
   - Allow configuration of tag rules via properties file
   - Define standard tag categories (priority, type, etc.)
   - Specify patterns for low-value tags

## Consequences

### Positive

- Modular design allows adding new tag analysis features
- Rule-based approach provides flexibility without code changes
- Clear separation of different analysis concerns
- Testable in isolation

### Negative

- More complex than a monolithic approach
- Requires coordination between analyzers
- Rule configuration adds complexity

### Risks

- Too many rules could make the system difficult to understand
- Different projects may have different tag conventions

### Mitigations

- Provide sensible defaults that work in most cases
- Document the rule configuration clearly
- Create a plug-in system for project-specific analyzers