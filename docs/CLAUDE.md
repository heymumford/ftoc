# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands
- Build project: `mvn clean package`
- Run tests: `mvn test`
- Run a single test: `mvn test -Dcucumber.filter.tags="@tagName"`
- Run the tool: `java -jar target/ftoc-<version>-jar-with-dependencies.jar [OPTIONS]`
- Print help: `java -jar target/ftoc-<version>-jar-with-dependencies.jar --help`

## Style Guidelines
- Java 11 compatible code
- Consistent 4-space indentation
- Use slf4j for logging
- Organize imports alphabetically
- Method names: camelCase
- Class names: PascalCase
- Constants: UPPER_SNAKE_CASE
- Tag test methods with appropriate annotations (@InitialTest, @P0, etc.)
- Use explicit exception handling with appropriate logging
- Follow Cucumber best practices for feature files