package com.heymumford.ftoc.exception;

/**
 * Exception for errors related to plugins in FTOC.
 * Used when there are problems with loading, initializing, or executing plugins.
 */
public class PluginException extends FtocException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new PluginException with the given message.
     * 
     * @param message The error message
     */
    public PluginException(String message) {
        super(message, ErrorCode.PLUGIN_EXECUTION_ERROR);
    }
    
    /**
     * Creates a new PluginException with the given message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public PluginException(String message, Throwable cause) {
        super(message, cause, ErrorCode.PLUGIN_EXECUTION_ERROR);
    }
    
    /**
     * Creates a new PluginException with the given message and error code.
     * 
     * @param message The error message
     * @param errorCode The specific error code
     */
    public PluginException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
    
    /**
     * Creates a new PluginException with the given message, cause, and error code.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     */
    public PluginException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
    
    /**
     * Creates a new PluginException with full details.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     * @param severity The severity level
     */
    public PluginException(String message, Throwable cause, ErrorCode errorCode, ExceptionSeverity severity) {
        super(message, cause, errorCode, severity);
    }
}