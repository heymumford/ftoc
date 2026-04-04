package com.heymumford.ftoc.exception;

/**
 * Single exception type for all FTOC errors.
 * Use {@link ErrorCode} to distinguish failure modes.
 */
public class FtocException extends Exception {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public FtocException(String message) {
        this(message, null, ErrorCode.GENERAL_ERROR);
    }

    public FtocException(String message, Throwable cause) {
        this(message, cause, ErrorCode.GENERAL_ERROR);
    }

    public FtocException(String message, ErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public FtocException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetailedMessage() {
        String code = errorCode != null
            ? errorCode.getCode() : "UNKNOWN";
        return String.format("[%s] %s", code, getMessage());
    }
}
