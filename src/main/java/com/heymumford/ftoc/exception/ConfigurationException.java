package com.heymumford.ftoc.exception;

/**
 * Exception for configuration-related errors in FTOC.
 * Used when there are problems with configuration settings or validation.
 */
public class ConfigurationException extends FtocException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new ConfigurationException with the given message.
     * 
     * @param message The error message
     */
    public ConfigurationException(String message) {
        super(message, ErrorCode.CONFIGURATION_ERROR);
    }
    
    /**
     * Creates a new ConfigurationException with the given message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause, ErrorCode.CONFIGURATION_ERROR);
    }
    
    /**
     * Creates a new ConfigurationException with the given message and error code.
     * 
     * @param message The error message
     * @param errorCode The specific error code
     */
    public ConfigurationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
    
    /**
     * Creates a new ConfigurationException with the given message, cause, and error code.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     */
    public ConfigurationException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
    
    /**
     * Creates a new ConfigurationException with full details.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     * @param severity The severity level
     */
    public ConfigurationException(String message, Throwable cause, ErrorCode errorCode, ExceptionSeverity severity) {
        super(message, cause, errorCode, severity);
    }
}