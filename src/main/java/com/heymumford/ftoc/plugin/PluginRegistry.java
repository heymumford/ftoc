package com.heymumford.ftoc.plugin;

import com.heymumford.ftoc.core.FeatureProcessor;
import com.heymumford.ftoc.core.FeatureRepository;
import com.heymumford.ftoc.core.Reporter;
import com.heymumford.ftoc.model.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Registry for FTOC plugins.
 * The registry manages plugin lifecycle and provides hook points for plugins to extend FTOC.
 */
public class PluginRegistry {
    private static final Logger logger = LoggerFactory.getLogger(PluginRegistry.class);
    
    // Maps to store the registered plugins and event handlers
    private final Map<String, FtocPlugin> plugins = new ConcurrentHashMap<>();
    private final Map<PluginEvent, List<EventHandler>> eventHandlers = new ConcurrentHashMap<>();
    
    // Custom components provided by plugins
    private FeatureRepository featureRepository = null;
    private FeatureProcessor featureProcessor = null;
    private Reporter reporter = null;
    
    // Plugin directory
    private final String pluginDirectory;
    
    /**
     * Create a new plugin registry with the default plugin directory.
     */
    public PluginRegistry() {
        this(getDefaultPluginDirectory());
    }
    
    /**
     * Create a new plugin registry with a specific plugin directory.
     * 
     * @param pluginDirectory The directory containing plugin JAR files
     */
    public PluginRegistry(String pluginDirectory) {
        this.pluginDirectory = pluginDirectory;
        
        // Initialize event handler lists
        for (PluginEvent event : PluginEvent.values()) {
            eventHandlers.put(event, new ArrayList<>());
        }
    }
    
    /**
     * Get the default plugin directory.
     * Looks for plugins in the following locations:
     * 1. ./plugins
     * 2. ~/.ftoc/plugins
     * 3. /etc/ftoc/plugins
     * 
     * @return The default plugin directory
     */
    private static String getDefaultPluginDirectory() {
        // Check for ./plugins directory first
        Path localPlugins = Paths.get("plugins");
        if (localPlugins.toFile().exists() && localPlugins.toFile().isDirectory()) {
            return localPlugins.toAbsolutePath().toString();
        }
        
        // Check for ~/.ftoc/plugins directory
        Path userPlugins = Paths.get(System.getProperty("user.home"), ".ftoc", "plugins");
        if (userPlugins.toFile().exists() && userPlugins.toFile().isDirectory()) {
            return userPlugins.toAbsolutePath().toString();
        }
        
        // Use /etc/ftoc/plugins as fallback
        return "/etc/ftoc/plugins";
    }
    
    /**
     * Load all plugins from the plugin directory.
     */
    public void loadPlugins() {
        File pluginDir = new File(pluginDirectory);
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            logger.warn("Plugin directory does not exist: {}", pluginDirectory);
            return;
        }
        
        logger.info("Looking for plugins in: {}", pluginDirectory);
        
        File[] jarFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            logger.info("No plugin JAR files found in {}", pluginDirectory);
            return;
        }
        
        for (File jarFile : jarFiles) {
            try {
                loadPlugin(jarFile);
            } catch (Exception e) {
                logger.error("Failed to load plugin from {}: {}", jarFile.getName(), e.getMessage());
            }
        }
        
        // Initialize all plugins
        for (FtocPlugin plugin : plugins.values()) {
            try {
                plugin.initialize(this);
                logger.info("Initialized plugin: {} v{}", plugin.getName(), plugin.getVersion());
            } catch (Exception e) {
                logger.error("Failed to initialize plugin {}: {}", plugin.getName(), e.getMessage());
            }
        }
    }
    
    /**
     * Load a plugin from a JAR file.
     * 
     * @param jarFile The JAR file
     * @throws Exception if loading fails
     */
    private void loadPlugin(File jarFile) throws Exception {
        logger.debug("Loading plugin from JAR: {}", jarFile.getName());
        
        try (JarFile jar = new JarFile(jarFile);
             URLClassLoader classLoader = new URLClassLoader(
                 new URL[] { jarFile.toURI().toURL() },
                 getClass().getClassLoader())) {
            
            // Look for plugin implementations in the JAR
            jar.stream()
               .filter(entry -> !entry.isDirectory() && 
                     entry.getName().endsWith(".class") && 
                     !entry.getName().contains("$"))
               .forEach(entry -> {
                   String className = entry.getName()
                           .replace('/', '.')
                           .replace(".class", "");
                   try {
                       Class<?> clazz = classLoader.loadClass(className);
                       if (FtocPlugin.class.isAssignableFrom(clazz) && 
                           !clazz.isInterface() && !clazz.isEnum()) {
                           FtocPlugin plugin = (FtocPlugin) clazz.getDeclaredConstructor().newInstance();
                           registerPlugin(plugin);
                       }
                   } catch (Exception e) {
                       logger.debug("Could not load class {}: {}", className, e.getMessage());
                   }
               });
        } catch (IOException e) {
            throw new Exception("Failed to load JAR file: " + e.getMessage());
        }
    }
    
    /**
     * Register a plugin.
     * 
     * @param plugin The plugin to register
     */
    public void registerPlugin(FtocPlugin plugin) {
        String pluginName = plugin.getName();
        
        if (plugins.containsKey(pluginName)) {
            logger.warn("Plugin {} is already registered. Overriding previous registration.", pluginName);
        }
        
        plugins.put(pluginName, plugin);
        logger.info("Registered plugin: {} v{} - {}", 
                   plugin.getName(), plugin.getVersion(), plugin.getDescription());
        
        // Check if the plugin provides any of the core components
        if (plugin.getFeatureRepository() != null) {
            this.featureRepository = plugin.getFeatureRepository();
            logger.info("Plugin {} provides a custom FeatureRepository", pluginName);
        }
        
        if (plugin.getFeatureProcessor() != null) {
            this.featureProcessor = plugin.getFeatureProcessor();
            logger.info("Plugin {} provides a custom FeatureProcessor", pluginName);
        }
        
        if (plugin.getReporter() != null) {
            this.reporter = plugin.getReporter();
            logger.info("Plugin {} provides a custom Reporter", pluginName);
        }
    }
    
    /**
     * Unregister a plugin.
     * 
     * @param pluginName The name of the plugin to unregister
     * @return true if the plugin was unregistered, false if it wasn't found
     */
    public boolean unregisterPlugin(String pluginName) {
        FtocPlugin plugin = plugins.remove(pluginName);
        
        if (plugin == null) {
            return false;
        }
        
        try {
            plugin.shutdown();
            logger.info("Unregistered plugin: {}", pluginName);
            return true;
        } catch (Exception e) {
            logger.error("Error shutting down plugin {}: {}", pluginName, e.getMessage());
            return true;
        }
    }
    
    /**
     * Get all registered plugins.
     * 
     * @return List of all registered plugins
     */
    public List<FtocPlugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    /**
     * Get a specific plugin by name.
     * 
     * @param name The plugin name
     * @return The plugin or null if not found
     */
    public FtocPlugin getPlugin(String name) {
        return plugins.get(name);
    }
    
    /**
     * Register an event handler for a specific event.
     * 
     * @param event The event to handle
     * @param handler The handler function
     */
    public void registerEventHandler(PluginEvent event, EventHandler handler) {
        eventHandlers.get(event).add(handler);
        logger.debug("Registered handler for event: {}", event);
    }
    
    /**
     * Trigger an event with no data.
     * 
     * @param event The event to trigger
     */
    public void triggerEvent(PluginEvent event) {
        triggerEvent(event, null);
    }
    
    /**
     * Trigger an event with data.
     * 
     * @param event The event to trigger
     * @param data The event data
     */
    public void triggerEvent(PluginEvent event, Object data) {
        logger.debug("Triggering event: {}", event);
        for (EventHandler handler : eventHandlers.get(event)) {
            try {
                handler.handle(data);
            } catch (Exception e) {
                logger.error("Error in event handler for {}: {}", event, e.getMessage());
            }
        }
    }
    
    /**
     * Get the custom feature repository provided by a plugin, or null if none.
     * 
     * @return Custom feature repository or null
     */
    public FeatureRepository getCustomFeatureRepository() {
        return featureRepository;
    }
    
    /**
     * Get the custom feature processor provided by a plugin, or null if none.
     * 
     * @return Custom feature processor or null
     */
    public FeatureProcessor getCustomFeatureProcessor() {
        return featureProcessor;
    }
    
    /**
     * Get the custom reporter provided by a plugin, or null if none.
     * 
     * @return Custom reporter or null
     */
    public Reporter getCustomReporter() {
        return reporter;
    }
    
    /**
     * Add a handler for feature files before processing.
     * 
     * @param handler The handler function that modifies feature files
     */
    public void addFeaturePreProcessor(Consumer<Feature> handler) {
        registerEventHandler(PluginEvent.PRE_PROCESS_FEATURE, data -> {
            if (data instanceof Feature) {
                handler.accept((Feature) data);
            }
        });
    }
    
    /**
     * Add a handler for feature files after processing.
     * 
     * @param handler The handler function that modifies feature files
     */
    public void addFeaturePostProcessor(Consumer<Feature> handler) {
        registerEventHandler(PluginEvent.POST_PROCESS_FEATURE, data -> {
            if (data instanceof Feature) {
                handler.accept((Feature) data);
            }
        });
    }
    
    /**
     * Add a handler for feature lists after loading.
     * 
     * @param handler The handler function that modifies feature lists
     */
    public void addFeaturesLoadedHandler(Consumer<List<Feature>> handler) {
        registerEventHandler(PluginEvent.FEATURES_LOADED, data -> {
            if (data instanceof List<?> && !((List<?>) data).isEmpty() && ((List<?>) data).get(0) instanceof Feature) {
                @SuppressWarnings("unchecked")
                List<Feature> features = (List<Feature>) data;
                handler.accept(features);
            }
        });
    }
    
    /**
     * Shutdown all plugins.
     */
    public void shutdown() {
        logger.info("Shutting down all plugins");
        for (FtocPlugin plugin : plugins.values()) {
            try {
                plugin.shutdown();
            } catch (Exception e) {
                logger.error("Error shutting down plugin {}: {}", plugin.getName(), e.getMessage());
            }
        }
        plugins.clear();
        
        // Clear event handlers
        for (PluginEvent event : PluginEvent.values()) {
            eventHandlers.get(event).clear();
        }
    }
    
    /**
     * Get a summary of all registered plugins.
     * 
     * @return A summary string
     */
    public String getPluginSummary() {
        if (plugins.isEmpty()) {
            return "No plugins loaded";
        }
        
        return plugins.values().stream()
            .map(p -> String.format("%s v%s - %s", p.getName(), p.getVersion(), p.getDescription()))
            .collect(Collectors.joining("\n"));
    }
}