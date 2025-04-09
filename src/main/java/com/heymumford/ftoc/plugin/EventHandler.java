package com.heymumford.ftoc.plugin;

/**
 * Interface for handling plugin events.
 */
@FunctionalInterface
public interface EventHandler {
    
    /**
     * Handle an event.
     * 
     * @param data The event data (may be null)
     */
    void handle(Object data);
}