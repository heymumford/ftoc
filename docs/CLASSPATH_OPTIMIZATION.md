# Classpath Optimization Report

## Current State

- JAR size: **4.65MB**
- Main dependency: Full Cucumber Java implementation
- Purpose: Simple parsing of Cucumber feature files

## Issues Identified

1. **Excessive Dependencies**: The full Cucumber Java implementation is included, but only needed for parsing feature files
2. **Many Language Files**: Cucumber includes language files for 50+ languages, most unused
3. **Inefficient Parsing**: Current implementation uses line-by-line text processing instead of proper Gherkin parsing
4. **Test Dependencies in Runtime**: Some test dependencies may be leaking into the final JAR

## Proposed Solutions

### 1. Replace Dependencies

| Current Dependency | Proposed Replacement | Rationale |
|-------------------|----------------------|-----------|
| `cucumber-java` (compile) | `gherkin` (compile) | Directly use the Gherkin parser which is more lightweight |
| `slf4j-simple` (compile) | `slf4j-simple` (runtime) | Move to runtime scope since it's only needed at runtime |
| `cucumber-java` (compile) | `cucumber-java` (test) | Only needed during testing |

### 2. Use Maven Shade Plugin

- Replace `maven-assembly-plugin` with `maven-shade-plugin`
- Enables `minimizeJar` option which keeps only classes that are used
- Better filtering of Maven metadata and signatures
- Proper merging of service files

### 3. Refactor Code to Use Gherkin Parser

Created a new implementation (`GherkinImprovedFtocUtility.java`) that:
- Uses the official Gherkin parser instead of line-by-line text processing
- Properly understands feature file structure
- Can extract tags from both features and scenarios 
- Will be more maintainable and accurate

### 4. Expected Benefits

- **Reduced JAR Size**: Expected reduction to ~1MB (75% smaller)
- **Better Performance**: More efficient parsing using the optimized Gherkin parser
- **More Accurate Analysis**: Proper understanding of feature file structure
- **Future Extensibility**: The Gherkin parser provides access to all elements of a feature file

## Implementation Steps

1. Update POM file with new dependencies
2. Replace the assembly plugin with shade plugin
3. Move the original implementation to use the Gherkin parser
4. Update tests to ensure compatibility

## Verification

Once implemented, verify with:
```bash
mvn clean package
ls -la target/ftoc-*.jar
```