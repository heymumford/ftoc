package com.heymumford.ftoc.exception;

/**
 * Error codes for distinguishing FTOC failure modes.
 */
public enum ErrorCode {

    GENERAL_ERROR("1000", "An unexpected error occurred"),
    CONFIGURATION_ERROR("1002", "Invalid configuration"),
    FILE_NOT_FOUND("2000", "Feature file not found"),
    FILE_READ_ERROR("2002", "Error reading feature file"),
    PARSE_ERROR("3000", "Error parsing feature file"),
    INVALID_GHERKIN("3001", "Invalid Gherkin syntax");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
