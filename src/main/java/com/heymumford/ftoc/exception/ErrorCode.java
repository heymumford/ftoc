package com.heymumford.ftoc.exception;

/**
 * Enumeration of specific error codes used throughout the FTOC application.
 * Each error code represents a unique error condition with a specific code and category.
 */
public enum ErrorCode {

    // General errors (1000-1999)
    GENERAL_ERROR("1000", "An unexpected error occurred"),
    INITIALIZATION_ERROR("1001", "Failed to initialize FTOC"),
    CONFIGURATION_ERROR("1002", "Invalid configuration"),
    
    // File/IO errors (2000-2999)
    FILE_NOT_FOUND("2000", "Feature file not found"),
    DIRECTORY_NOT_FOUND("2001", "Feature directory not found"),
    FILE_READ_ERROR("2002", "Error reading feature file"),
    FILE_WRITE_ERROR("2003", "Error writing output file"),
    
    // Parsing errors (3000-3999)
    PARSE_ERROR("3000", "Error parsing feature file"),
    INVALID_GHERKIN("3001", "Invalid Gherkin syntax"),
    UNSUPPORTED_FORMAT("3002", "Unsupported file format"),
    
    // Plugin errors (4000-4999)
    PLUGIN_INITIALIZATION_ERROR("4000", "Error initializing plugin"),
    PLUGIN_EXECUTION_ERROR("4001", "Error during plugin execution"),
    PLUGIN_NOT_FOUND("4002", "Plugin not found"),
    
    // Analysis errors (5000-5999)
    ANALYSIS_ERROR("5000", "Error during feature analysis"),
    TAG_ANALYSIS_ERROR("5001", "Error analyzing tags"),
    ANTI_PATTERN_ANALYSIS_ERROR("5002", "Error during anti-pattern analysis"),
    
    // Formatter errors (6000-6999)
    FORMAT_ERROR("6000", "Error formatting output"),
    UNSUPPORTED_OUTPUT_FORMAT("6001", "Unsupported output format");
    
    private final String code;
    private final String description;
    
    /**
     * Constructor for error codes.
     * 
     * @param code The unique error code
     * @param description A brief description of the error
     */
    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get the unique code for this error.
     * 
     * @return The error code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Get a brief description of this error.
     * 
     * @return The error description
     */
    public String getDescription() {
        return description;
    }
}