package com.heymumford.ftoc.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for FTOC plugins.
 * This class provides a basic implementation of the FtocPlugin interface
 * to make it easier to create plugins.
 */
public abstract class AbstractFtocPlugin implements FtocPlugin {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected PluginRegistry registry;
    
    @Override
    public void initialize(PluginRegistry registry) {
        this.registry = registry;
        logger.info("Initializing plugin: {} v{}", getName(), getVersion());
    }
    
    @Override
    public void shutdown() {
        logger.info("Shutting down plugin: {}", getName());
    }
    
    /**
     * Register an event handler for a specific event.
     * 
     * @param event The event to handle
     * @param handler The handler function
     */
    protected void registerEventHandler(PluginEvent event, EventHandler handler) {
        if (registry != null) {
            registry.registerEventHandler(event, handler);
        } else {
            logger.warn("Cannot register event handler: plugin not initialized");
        }
    }
}