package com.heymumford.ftoc.exception;

/**
 * Base exception class for all FTOC-specific exceptions.
 * This is the root of the FTOC exception hierarchy.
 */
public class FtocException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Severity of the exception, indicating how it should be handled.
     */
    private final ExceptionSeverity severity;
    
    /**
     * Code indicating the specific type of exception.
     */
    private final ErrorCode errorCode;
    
    /**
     * Creates a new FtocException with the given message.
     * 
     * @param message The error message
     */
    public FtocException(String message) {
        this(message, null, ErrorCode.GENERAL_ERROR, ExceptionSeverity.ERROR);
    }
    
    /**
     * Creates a new FtocException with the given message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public FtocException(String message, Throwable cause) {
        this(message, cause, ErrorCode.GENERAL_ERROR, ExceptionSeverity.ERROR);
    }
    
    /**
     * Creates a new FtocException with the given message and error code.
     * 
     * @param message The error message
     * @param errorCode The specific error code
     */
    public FtocException(String message, ErrorCode errorCode) {
        this(message, null, errorCode, ExceptionSeverity.ERROR);
    }
    
    /**
     * Creates a new FtocException with the given message, error code, and severity.
     * 
     * @param message The error message
     * @param errorCode The specific error code
     * @param severity The severity level
     */
    public FtocException(String message, ErrorCode errorCode, ExceptionSeverity severity) {
        this(message, null, errorCode, severity);
    }
    
    /**
     * Creates a new FtocException with the given message, cause, and error code.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     */
    public FtocException(String message, Throwable cause, ErrorCode errorCode) {
        this(message, cause, errorCode, ExceptionSeverity.ERROR);
    }
    
    /**
     * Creates a new FtocException with full details.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     * @param severity The severity level
     */
    public FtocException(String message, Throwable cause, ErrorCode errorCode, ExceptionSeverity severity) {
        super(message, cause);
        this.errorCode = errorCode;
        this.severity = severity;
    }
    
    /**
     * Get the severity of this exception.
     * 
     * @return The exception severity
     */
    public ExceptionSeverity getSeverity() {
        return severity;
    }
    
    /**
     * Get the error code for this exception.
     * 
     * @return The error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Get a detailed error message including error code and severity.
     * 
     * @return Formatted error message
     */
    public String getDetailedMessage() {
        return String.format("[%s-%s] %s", 
            severity.name(), 
            errorCode.getCode(),
            getMessage());
    }
}