package com.heymumford.ftoc.exception;

/**
 * Exception for errors related to parsing feature files.
 * Used when there are problems with the syntax or structure of feature files.
 */
public class ParsingException extends FtocException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new ParsingException with the given message.
     * 
     * @param message The error message
     */
    public ParsingException(String message) {
        super(message, ErrorCode.PARSE_ERROR);
    }
    
    /**
     * Creates a new ParsingException with the given message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause, ErrorCode.PARSE_ERROR);
    }
    
    /**
     * Creates a new ParsingException with the given message and error code.
     * 
     * @param message The error message
     * @param errorCode The specific error code
     */
    public ParsingException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
    
    /**
     * Creates a new ParsingException with the given message, cause, and error code.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     */
    public ParsingException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
    
    /**
     * Creates a new ParsingException with full details.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     * @param severity The severity level
     */
    public ParsingException(String message, Throwable cause, ErrorCode errorCode, ExceptionSeverity severity) {
        super(message, cause, errorCode, severity);
    }
}