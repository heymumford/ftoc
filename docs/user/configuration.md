# FTOC Configuration Guide

## Overview

FTOC supports customization through configuration files. This guide explains how to create and use these configuration files to tailor the behavior of FTOC to your project's specific needs.

## Configuration File

The configuration file uses YAML format and allows you to customize various aspects of FTOC, including:

- Warning types and severity levels
- Threshold values
- Custom tag lists

### Default Locations

FTOC looks for configuration files in the following locations (in order):

1. Path specified with `--config-file` option
2. `.ftoc/config.yml` in the current directory
3. `.ftoc.yml` in the current directory
4. `.config/ftoc/warnings.yml` in the current directory
5. `config/ftoc-warnings.yml` in the current directory
6. `.config/ftoc/warnings.yml` in the user's home directory

## Configuration Structure

The configuration file has three main sections:

1. `warnings`: Configure which warnings are enabled and their severity
2. `tags`: Define custom tag lists for different categories
3. `thresholds`: Set threshold values for numeric criteria

### Example Configuration

```yaml
# FTOC Warning Configuration
warnings:
  # Disable specific warnings across all types
  disabled:
    - DUPLICATE_TAG
    - AMBIGUOUS_TAG
  
  # Tag quality warnings configuration
  tagQuality:
    MISSING_PRIORITY_TAG:
      enabled: true
      severity: error
    MISSING_TYPE_TAG:
      enabled: true
      severity: warning
    LOW_VALUE_TAG:
      enabled: true
      severity: info
    EXCESSIVE_TAGS:
      enabled: true
      severity: error
  
  # Anti-pattern warnings configuration
  antiPatterns:
    LONG_SCENARIO:
      enabled: true
      severity: error
    MISSING_GIVEN:
      enabled: true
      severity: error
    MISSING_WHEN:
      enabled: true
      severity: error
    MISSING_THEN:
      enabled: true
      severity: error
    UI_FOCUSED_STEP:
      enabled: true
      severity: warning

# Custom thresholds
thresholds:
  maxSteps: 12           # Maximum steps in a scenario
  minSteps: 2            # Minimum steps in a scenario
  maxTags: 5             # Maximum tags on a feature/scenario
  maxScenarioNameLength: 80
  maxStepLength: 100

# Custom tag definitions
tags:
  # Priority tags
  priority:
    - "@P0"
    - "@P1"
    - "@P2"
    - "@P3"
    - "@Critical"
    - "@High"
    - "@Medium"
    - "@Low"
  
  # Type tags
  type:
    - "@UI"
    - "@API"
    - "@Backend"
    - "@Frontend"
    - "@Integration"
    - "@Unit"
  
  # Low-value tags to detect
  lowValue:
    - "@Test"
    - "@Temp"
    - "@ToDo"
    - "@TBD"
    - "@WIP"
```

## Warning Types

### Tag Quality Warnings

| Warning Type | Description |
|--------------|-------------|
| `MISSING_PRIORITY_TAG` | Scenario is missing a priority tag |
| `MISSING_TYPE_TAG` | Scenario is missing a type tag |
| `LOW_VALUE_TAG` | Tag provides little or no value |
| `INCONSISTENT_TAGGING` | Inconsistent tag usage across features |
| `EXCESSIVE_TAGS` | Too many tags on a single scenario/feature |
| `TAG_TYPO` | Possible typo in tag name |
| `ORPHANED_TAG` | Tag used only once across all features |
| `AMBIGUOUS_TAG` | Tag name is ambiguous or too short |
| `TOO_GENERIC_TAG` | Tag is too generic to be useful |
| `INCONSISTENT_NESTING` | Inconsistent tag nesting patterns |
| `DUPLICATE_TAG` | Same tag appears multiple times |

### Anti-Pattern Warnings

| Warning Type | Description |
|--------------|-------------|
| `LONG_SCENARIO` | Scenario has too many steps |
| `TOO_FEW_STEPS` | Scenario has too few steps |
| `MISSING_GIVEN` | Scenario is missing a Given step |
| `MISSING_WHEN` | Scenario is missing a When step |
| `MISSING_THEN` | Scenario is missing a Then step |
| `UI_FOCUSED_STEP` | Step contains UI-focused language |
| `IMPLEMENTATION_DETAIL` | Step contains implementation details |
| `MISSING_EXAMPLES` | Scenario Outline has no Examples tables |
| `TOO_FEW_EXAMPLES` | Examples table has too few rows |
| `LONG_SCENARIO_NAME` | Scenario name is too long |
| `LONG_STEP_TEXT` | Step text is too long |
| `INCORRECT_STEP_ORDER` | Steps are in incorrect order |
| `AMBIGUOUS_PRONOUN` | Step contains ambiguous pronouns |
| `INCONSISTENT_TENSE` | Inconsistent tense in steps |
| `CONJUNCTION_IN_STEP` | Step contains conjunctions |

## Severity Levels

Each warning can have one of three severity levels:

- `error`: Critical issue that should be fixed
- `warning`: Potential issue that may need attention
- `info`: Informational message about a minor issue

## Using Custom Configuration

To use a custom configuration file:

```bash
# Specify a custom configuration file
ftoc --config-file ./my-config.yml --analyze-tags -d ./features

# Display the current configuration
ftoc --config-file ./my-config.yml --show-config
```

## Configuration Tips

1. **Start with defaults**: Begin with the default configuration and customize as needed
2. **Project standards**: Align your configuration with your team's standards
3. **Severity levels**: Set severity appropriate to your team's maturity level
4. **Custom thresholds**: Adjust thresholds based on your project's complexity
5. **Custom tag lists**: Define tag lists that reflect your tagging taxonomy

## Conclusion

Custom configuration allows you to tailor FTOC to your specific project needs. By defining appropriate warning levels, thresholds, and tag lists, you can ensure that FTOC provides valuable guidance that aligns with your team's standards and practices.