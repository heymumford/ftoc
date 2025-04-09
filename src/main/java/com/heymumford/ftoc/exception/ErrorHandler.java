package com.heymumford.ftoc.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized error handler for FTOC application.
 * Provides consistent handling of exceptions based on their severity.
 */
public class ErrorHandler {
    
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());
    
    /**
     * Handles an exception according to its severity.
     * 
     * @param exception The exception to handle
     * @return True if the application can continue, false if it should terminate
     */
    public static boolean handleException(FtocException exception) {
        // Log the exception with appropriate level
        logException(exception);
        
        // Take action based on severity
        switch (exception.getSeverity()) {
            case FATAL:
                // Fatal errors should terminate the application
                return false;
            case ERROR:
                // Errors may allow continuation, but the current operation should fail
                return true;
            case WARNING:
            case INFO:
                // Warnings and info exceptions allow continuation
                return true;
            default:
                // Unknown severity - treat as ERROR
                return true;
        }
    }
    
    /**
     * Logs an exception with appropriate level based on its severity.
     * 
     * @param exception The exception to log
     */
    private static void logException(FtocException exception) {
        String detailedMessage = exception.getDetailedMessage();
        
        switch (exception.getSeverity()) {
            case FATAL:
                LOGGER.log(Level.SEVERE, detailedMessage, exception);
                break;
            case ERROR:
                LOGGER.log(Level.SEVERE, detailedMessage, exception);
                break;
            case WARNING:
                LOGGER.log(Level.WARNING, detailedMessage, exception);
                break;
            case INFO:
                LOGGER.log(Level.INFO, detailedMessage, exception);
                break;
            default:
                LOGGER.log(Level.SEVERE, detailedMessage, exception);
                break;
        }
    }
    
    /**
     * Creates and handles an exception in one step.
     * 
     * @param message The error message
     * @param errorCode The specific error code
     * @param severity The severity level
     * @return True if the application can continue, false if it should terminate
     */
    public static boolean handleError(String message, ErrorCode errorCode, ExceptionSeverity severity) {
        return handleException(new FtocException(message, null, errorCode, severity));
    }
    
    /**
     * Creates and handles an exception in one step.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     * @param severity The severity level
     * @return True if the application can continue, false if it should terminate
     */
    public static boolean handleError(String message, Throwable cause, ErrorCode errorCode, ExceptionSeverity severity) {
        return handleException(new FtocException(message, cause, errorCode, severity));
    }
    
    /**
     * Provides a user-friendly message for an exception.
     * 
     * @param exception The exception to get a message for
     * @return A user-friendly error message
     */
    public static String getUserFriendlyMessage(FtocException exception) {
        // Base message includes error code and description
        String baseMessage = String.format("%s: %s", 
                exception.getErrorCode().getCode(),
                exception.getMessage());
        
        // Add additional context for certain types of errors
        if (exception instanceof FileException) {
            return "File error: " + baseMessage;
        } else if (exception instanceof ParsingException) {
            return "Parsing error: " + baseMessage;
        } else if (exception instanceof PluginException) {
            return "Plugin error: " + baseMessage;
        } else if (exception instanceof ConfigurationException) {
            return "Configuration error: " + baseMessage;
        } else if (exception instanceof AnalysisException) {
            return "Analysis error: " + baseMessage;
        } else {
            return "Error: " + baseMessage;
        }
    }
}