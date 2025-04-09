package com.heymumford.ftoc.exception;

/**
 * Exception for errors related to the analysis of feature files.
 * Used when there are problems during the analysis of tags, steps, or other feature elements.
 */
public class AnalysisException extends FtocException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new AnalysisException with the given message.
     * 
     * @param message The error message
     */
    public AnalysisException(String message) {
        super(message, ErrorCode.ANALYSIS_ERROR);
    }
    
    /**
     * Creates a new AnalysisException with the given message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public AnalysisException(String message, Throwable cause) {
        super(message, cause, ErrorCode.ANALYSIS_ERROR);
    }
    
    /**
     * Creates a new AnalysisException with the given message and error code.
     * 
     * @param message The error message
     * @param errorCode The specific error code
     */
    public AnalysisException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
    
    /**
     * Creates a new AnalysisException with the given message, cause, and error code.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     */
    public AnalysisException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
    
    /**
     * Creates a new AnalysisException with full details.
     * 
     * @param message The error message
     * @param cause The underlying cause
     * @param errorCode The specific error code
     * @param severity The severity level
     */
    public AnalysisException(String message, Throwable cause, ErrorCode errorCode, ExceptionSeverity severity) {
        super(message, cause, errorCode, severity);
    }
}