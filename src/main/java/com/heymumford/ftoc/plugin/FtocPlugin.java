package com.heymumford.ftoc.plugin;

import com.heymumford.ftoc.core.FeatureProcessor;
import com.heymumford.ftoc.core.FeatureRepository;
import com.heymumford.ftoc.core.Reporter;

/**
 * Interface for FTOC plugins.
 * Plugins can extend FTOC functionality by providing custom components,
 * such as repositories, processors, or reporters, or by registering 
 * handlers for specific events in the processing lifecycle.
 */
public interface FtocPlugin {
    
    /**
     * Get the name of the plugin.
     * 
     * @return The plugin name
     */
    String getName();
    
    /**
     * Get the version of the plugin.
     * 
     * @return The plugin version
     */
    String getVersion();
    
    /**
     * Get the description of the plugin.
     * 
     * @return The plugin description
     */
    String getDescription();
    
    /**
     * Initialize the plugin.
     * This method is called when the plugin is loaded.
     * 
     * @param registry The plugin registry
     */
    void initialize(PluginRegistry registry);
    
    /**
     * Get a feature repository implementation provided by this plugin.
     * Return null if this plugin doesn't provide a repository.
     * 
     * @return A feature repository or null
     */
    default FeatureRepository getFeatureRepository() {
        return null;
    }
    
    /**
     * Get a feature processor implementation provided by this plugin.
     * Return null if this plugin doesn't provide a processor.
     * 
     * @return A feature processor or null
     */
    default FeatureProcessor getFeatureProcessor() {
        return null;
    }
    
    /**
     * Get a reporter implementation provided by this plugin.
     * Return null if this plugin doesn't provide a reporter.
     * 
     * @return A reporter or null
     */
    default Reporter getReporter() {
        return null;
    }
    
    /**
     * Called before the plugin is unloaded.
     * Use this method to clean up any resources.
     */
    default void shutdown() {
        // Default implementation does nothing
    }
}