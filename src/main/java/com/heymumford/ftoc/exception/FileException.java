package com.heymumford.ftoc.exception;

/**
 * Exception for file and I/O related errors in FTOC.
 * Used when there are problems with reading or writing files.
 */
public class FileException extends FtocException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new FileException with the given message.
     * 
     * @param message The error message
     */
    public FileException(String message) {
        super(message, ErrorCode.FILE_READ_ERROR);
    }
    
    /**
     * Creates a new FileException with the given message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public FileException(String message, Throwable cause) {
        super(message, cause, ErrorCode.FILE_READ_ERROR);
    }
    
    /**
     * Creates a new FileException with the given message and error code.
     * 
     * @param message The error message
     * @param errorCode The specific error code
     */
    public FileException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
    
    /**
     * Creates a new FileException with the given message, cause, and error code.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     */
    public FileException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
    
    /**
     * Creates a new FileException with full details.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     * @param severity The severity level
     */
    public FileException(String message, Throwable cause, ErrorCode errorCode, ExceptionSeverity severity) {
        super(message, cause, errorCode, severity);
    }
}