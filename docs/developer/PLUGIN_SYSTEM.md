# FTOC Plugin System

This document provides details on the FTOC plugin system, which allows users to extend FTOC functionality with custom components and event handlers.

## Overview

The FTOC plugin system provides a flexible way to extend and customize FTOC's functionality without modifying the core codebase. Plugins can:

1. Replace core components with custom implementations
2. Add event handlers for various stages of FTOC processing
3. Add pre- and post-processing for feature files
4. Modify command-line arguments and report generation

## Plugin Structure

A plugin is a Java class that implements the `FtocPlugin` interface. The simplest way to create a plugin is to extend the `AbstractFtocPlugin` class, which provides default implementations for many of the required methods.

### FtocPlugin Interface

```java
public interface FtocPlugin {
    // Identity methods
    String getName();
    String getVersion();
    String getDescription();
    
    // Lifecycle methods
    void initialize(PluginRegistry registry);
    void shutdown();
    
    // Optional component providers
    FeatureRepository getFeatureRepository();
    FeatureProcessor getFeatureProcessor();
    Reporter getReporter();
}
```

### Plugin Lifecycle

1. **Loading**: Plugins are loaded from JAR files in the plugin directory
2. **Initialization**: The `initialize` method is called with a reference to the `PluginRegistry`
3. **Registration**: Plugins can register event handlers for specific events
4. **Execution**: Plugin event handlers are called during FTOC processing
5. **Shutdown**: The `shutdown` method is called when FTOC is shutting down

## Plugin Directory

FTOC looks for plugins in these directories, in order:

1. `./plugins` (current directory)
2. `~/.ftoc/plugins` (user's home directory)
3. `/etc/ftoc/plugins` (system-wide plugins)

## Event System

The event system allows plugins to hook into various stages of FTOC processing. Events include:

- `STARTUP`: Triggered when FTOC starts up
- `SHUTDOWN`: Triggered when FTOC is shutting down
- `PRE_PROCESS_FEATURE`: Triggered before processing a feature file
- `POST_PROCESS_FEATURE`: Triggered after processing a feature file
- `FEATURES_LOADED`: Triggered after all feature files are loaded
- `PRE_GENERATE_REPORT`: Triggered before generating a report
- `POST_GENERATE_REPORT`: Triggered after generating a report
- `PRE_PARSE_ARGUMENTS`: Triggered before processing command line arguments
- `POST_PARSE_ARGUMENTS`: Triggered after processing command line arguments
- `ERROR_OCCURRED`: Triggered when an error occurs during processing

## Creating a Plugin

### Example Plugin

Here's a simple example plugin that logs information about feature files:

```java
public class ExamplePlugin extends AbstractFtocPlugin {
    
    @Override
    public String getName() {
        return "ExamplePlugin";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getDescription() {
        return "Example plugin that logs information about feature files";
    }
    
    @Override
    public void initialize(PluginRegistry registry) {
        super.initialize(registry);
        
        // Register event handler for FEATURES_LOADED event
        registerEventHandler(PluginEvent.FEATURES_LOADED, data -> {
            if (data instanceof List<?> && !((List<?>) data).isEmpty() 
                    && ((List<?>) data).get(0) instanceof Feature) {
                @SuppressWarnings("unchecked")
                List<Feature> features = (List<Feature>) data;
                logFeatureStats(features);
            }
        });
    }
    
    private void logFeatureStats(List<Feature> features) {
        int featureCount = features.size();
        int scenarioCount = features.stream()
                .mapToInt(f -> f.getScenarios().size())
                .sum();
        
        logger.info("Example plugin detected:");
        logger.info("  - {} feature files", featureCount);
        logger.info("  - {} scenarios", scenarioCount);
    }
}
```

### Packaging a Plugin

To package a plugin:

1. Create a JAR file containing your plugin class
2. Place the JAR in one of the plugin directories
3. FTOC will load the plugin automatically on startup

## Customizing Core Components

Plugins can provide custom implementations of core FTOC components:

```java
public class CustomComponentPlugin extends AbstractFtocPlugin {
    // ... identity methods ...
    
    @Override
    public FeatureRepository getFeatureRepository() {
        return new CustomFeatureRepository();
    }
    
    @Override
    public Reporter getReporter() {
        return new CustomReporter();
    }
}
```

## Command-Line Interactions

To list all loaded plugins, use the `--list-plugins` flag:

```
ftoc --list-plugins
```

## Plugin Development Guidelines

1. **Robustness**: Handle exceptions gracefully to avoid affecting FTOC operations
2. **Performance**: Minimize performance impact, especially for event handlers
3. **Resources**: Clean up resources in the `shutdown` method
4. **Dependencies**: Include all required dependencies in your plugin JAR
5. **Documentation**: Document your plugin thoroughly, especially configuration options

## Plugin API Stability

The plugin API is considered stable, but may evolve over time. Major changes will be documented in the FTOC release notes.