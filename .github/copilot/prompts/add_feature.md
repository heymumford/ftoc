# Adding a New Feature to FTOC

Use this prompt template to help Copilot generate code for adding a new feature to FTOC.

## Template

```
@workspace
I need to implement a new [FEATURE_TYPE] for the FTOC project.

The feature should:
- [DESCRIBE_FUNCTIONALITY]
- [DESCRIBE_INPUTS]
- [DESCRIBE_OUTPUTS]

It should follow these FTOC patterns:
- Use existing [MODEL_TYPE] classes or create new ones
- Add proper logging with SLF4J
- Include comprehensive error handling
- Document with JavaDoc
- Follow the established package structure
- Add tests for the feature

Please:
1. Create any necessary model classes
2. Implement the feature in [PACKAGE_NAME]
3. Update the FtocUtility class to make the feature available
4. Add command-line options if needed
5. Create tests for the implementation

The feature should be implemented with Java 11 compatibility in mind.
```

## Example

```
@workspace
I need to implement a new formatter for the FTOC project.

The feature should:
- Add CSV output format support
- Take the list of features and scenarios as input
- Generate a CSV file with columns for Feature, Scenario, Tags, and Line Number
- Allow customization of which columns to include

It should follow these FTOC patterns:
- Use existing Feature and Scenario model classes
- Add proper logging with SLF4J
- Include comprehensive error handling
- Document with JavaDoc
- Follow the established package structure
- Add tests for the feature

Please:
1. Create any necessary model classes
2. Implement the feature in com.heymumford.ftoc.formatter
3. Update the FtocUtility class to make the feature available
4. Add command-line options if needed
5. Create tests for the implementation

The feature should be implemented with Java 11 compatibility in mind.
```