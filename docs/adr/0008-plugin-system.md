# ADR-0008: Plugin System for Extensibility

## Status

Accepted

## Context

As FTOC grows, there's a need to allow users to extend its functionality without modifying the core codebase. Currently, customization is limited to:

1. Providing custom implementations of the core interfaces (FeatureRepository, FeatureProcessor, Reporter)
2. Using configuration files for warning customization
3. Using command-line options for behavior control

These options require either direct code modification or a limited set of pre-defined configurations. A more flexible extension mechanism is needed to:

- Allow third-party developers to extend FTOC
- Enable custom report formats, feature file sources, and processors
- Support integration with other tools and systems
- Provide lifecycle hooks for custom processing steps

## Decision

We will implement a plugin system with the following characteristics:

1. **Plugin Interface**: A simple interface that all plugins must implement
2. **Plugin Registry**: A central registry that manages plugin loading and lifecycle
3. **Event System**: A system for plugins to register handlers for specific events
4. **Component Injection**: A mechanism for plugins to provide custom implementations of core components
5. **Directory-Based Loading**: Automatic loading of plugins from predefined directories

The plugin system will use Java's standard class loading mechanism to load plugin classes from JAR files. Plugins will be able to hook into various stages of FTOC processing through an event system, and can provide custom implementations of core interfaces.

## Consequences

### Positive

- **Extensibility**: Users can extend FTOC functionality without modifying the core codebase
- **Modularity**: Core functionality can be kept separate from extensions
- **Flexibility**: Plugins can hook into various stages of processing
- **Standardization**: A consistent API for all extensions
- **Discoverability**: Automatic loading of plugins from standard locations
- **Isolation**: Plugin errors are contained and don't crash the main application

### Negative

- **Complexity**: Adds complexity to the codebase
- **Performance**: Potentially introduces performance overhead for event dispatch
- **Compatibility**: Need to maintain compatibility for plugins across versions
- **Security**: Need to consider security implications of loading external code

## Implementation Approach

1. Create a `plugin` package with the core plugin interfaces and registry
2. Define an event system for different stages of processing
3. Update the main FtocUtilityRefactored class to use the plugin system
4. Create documentation for plugin developers
5. Provide example plugins to demonstrate the API

## Usage Example

```java
// Creating a plugin
public class MyPlugin implements FtocPlugin {
    @Override
    public String getName() {
        return "MyPlugin";
    }
    
    @Override
    public void initialize(PluginRegistry registry) {
        registry.registerEventHandler(PluginEvent.FEATURES_LOADED, data -> {
            // Custom processing of loaded features
        });
    }
}

// Using plugins with FTOC
FtocUtilityRefactored ftoc = new FtocUtilityRefactored();
System.out.println(ftoc.getPluginSummary());  // Show loaded plugins
```

## Directory Structure

Plugins will be loaded from the following directories in order:

1. `./plugins` - Local plugins directory
2. `~/.ftoc/plugins` - User-specific plugins directory
3. `/etc/ftoc/plugins` - System-wide plugins directory