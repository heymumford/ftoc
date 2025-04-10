# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build/Test Commands
- Build project: `mvn clean package` or `make build`
- Run tests: `mvn test` or `make test`
- Run single test class: `mvn test -Dtest=TestClassName`
- Run specific test method: `mvn test -Dtest=TestClassName#testMethodName`
- Run specific Cucumber feature: `mvn test -Dcucumber.filter.tags="@TagName"`
- Run the tool: `java -jar target/ftoc-<version>-jar-with-dependencies.jar [OPTIONS]`
- Print help: `java -jar target/ftoc-<version>-jar-with-dependencies.jar --help`
- Generate documentation: `make doc`

## Code Style Guidelines
- Use Java 11 features appropriately
- Organize imports alphabetically, no unused imports, no wildcards
- Follow existing package structure (model, parser, formatter, analyzer)
- Formatter classes use Format enum with PLAIN_TEXT, MARKDOWN, HTML, JSON
- Use SLF4J for logging with appropriate levels
- Model classes should be immutable where possible
- Error handling: log errors, use specific exceptions with clear messages
- Document public APIs with JavaDoc including @param and @return
- Indent with 4 spaces, put opening braces on same line
- Follow patterns in .github/copilot/CODE_PATTERNS.md

## Naming Conventions
- Method names: camelCase
- Class names: PascalCase
- Constants: UPPER_SNAKE_CASE
- Tag test methods with appropriate annotations (@InitialTest, @P0, etc.)

## Testing Practices
- Follow Cucumber best practices for feature files
- Provide clear, descriptive step definitions
- Use explicit assertions with helpful error messages
- Follow Test Pyramid guidelines (more unit tests, fewer integration tests)
- Mock external dependencies when appropriate