package com.heymumford.ftoc.plugin;

/**
 * Enum defining events that plugins can listen for.
 */
public enum PluginEvent {
    /**
     * Triggered when FTOC starts up.
     */
    STARTUP,
    
    /**
     * Triggered when FTOC is shutting down.
     */
    SHUTDOWN,
    
    /**
     * Triggered before processing a feature file.
     * Event data: Feature object
     */
    PRE_PROCESS_FEATURE,
    
    /**
     * Triggered after processing a feature file.
     * Event data: Feature object
     */
    POST_PROCESS_FEATURE,
    
    /**
     * Triggered after all feature files are loaded.
     * Event data: List<Feature>
     */
    FEATURES_LOADED,
    
    /**
     * Triggered before generating a report.
     * Event data: ReportContext object
     */
    PRE_GENERATE_REPORT,
    
    /**
     * Triggered after generating a report.
     * Event data: ReportContext object
     */
    POST_GENERATE_REPORT,
    
    /**
     * Triggered before processing command line arguments.
     * Event data: String[] args
     */
    PRE_PARSE_ARGUMENTS,
    
    /**
     * Triggered after processing command line arguments.
     * Event data: CommandLineContext object
     */
    POST_PARSE_ARGUMENTS,
    
    /**
     * Triggered when an error occurs during processing.
     * Event data: Exception object
     */
    ERROR_OCCURRED
}