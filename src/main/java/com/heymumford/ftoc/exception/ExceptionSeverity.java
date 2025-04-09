package com.heymumford.ftoc.exception;

/**
 * Enum representing different severity levels for FTOC exceptions.
 * This hierarchy helps with prioritizing and handling errors appropriately.
 */
public enum ExceptionSeverity {
    
    /**
     * Fatal errors that cause the application to terminate immediately.
     * These errors make it impossible to continue processing.
     */
    FATAL,
    
    /**
     * Serious errors that prevent specific operations from completing,
     * but may allow the application to continue with other tasks.
     */
    ERROR,
    
    /**
     * Issues that should be reported but don't prevent operation.
     * Warnings indicate potential problems or non-optimal conditions.
     */
    WARNING,
    
    /**
     * Informational exceptions that provide context but aren't errors.
     * These exceptions are used for logging or debugging purposes.
     */
    INFO
}