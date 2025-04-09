# Generating Tests for FTOC

Use this prompt template to help Copilot generate comprehensive tests for FTOC components.

## Template

```
@workspace
I need to create tests for [CLASS_NAME] in the FTOC project.

The class has these key methods:
- [METHOD_1]: [BRIEF_DESCRIPTION]
- [METHOD_2]: [BRIEF_DESCRIPTION]
- [METHOD_3]: [BRIEF_DESCRIPTION]

The class functionality:
[DESCRIBE_CLASS_FUNCTIONALITY]

Please generate comprehensive tests that:
- Test happy paths for each method
- Test edge cases (empty input, null input, large input, etc.)
- Test error conditions and exception handling
- Follow FTOC test patterns using JUnit 5 and AssertJ
- Include descriptive test method names using the shouldXxxWhenYyy pattern
- Use appropriate test data and resources from test/resources if needed

Make sure the tests are compatible with Java 11.
```

## Example

```
@workspace
I need to create tests for TagQualityAnalyzer in the FTOC project.

The class has these key methods:
- analyzeTagQuality(): Analyzes tag usage and returns a list of warnings
- generateWarningReport(): Generates a formatted report from the warnings
- detectMissingPriorityTags(): Detects scenarios missing priority tags
- detectOrphanedTags(): Detects tags used only once in the codebase

The class functionality:
TagQualityAnalyzer analyzes Feature files to find potential issues with tags such as missing priority tags, typos in tags, duplicate tags, and excessive tagging. It generates warnings with remediation suggestions.

Please generate comprehensive tests that:
- Test happy paths for each method
- Test edge cases (empty input, null input, large input, etc.)
- Test error conditions and exception handling
- Follow FTOC test patterns using JUnit 5 and AssertJ
- Include descriptive test method names using the shouldXxxWhenYyy pattern
- Use appropriate test data and resources from test/resources if needed

Make sure the tests are compatible with Java 11.
```