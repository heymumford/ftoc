package com.heymumford.ftoc.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

public class ErrorHandlingTest {

    @Test
    public void testFtocExceptionCreation() {
        FtocException exception = new FtocException("Test error message");
        assertEquals("Test error message", exception.getMessage());
        assertEquals(ErrorCode.GENERAL_ERROR, exception.getErrorCode());
    }

    @Test
    public void testExceptionWithCause() {
        Exception cause = new RuntimeException("Original error");
        FtocException exception = new FtocException("Wrapped error", cause);
        assertEquals("Wrapped error", exception.getMessage());
        assertSame(cause, exception.getCause());
        assertEquals(ErrorCode.GENERAL_ERROR, exception.getErrorCode());
    }

    @Test
    public void testExceptionWithErrorCode() {
        FtocException exception = new FtocException("Configuration error", ErrorCode.CONFIGURATION_ERROR);
        assertEquals("Configuration error", exception.getMessage());
        assertEquals(ErrorCode.CONFIGURATION_ERROR, exception.getErrorCode());
    }

    @Test
    public void testDetailedErrorMessage() {
        FtocException exception = new FtocException("File not found", ErrorCode.FILE_NOT_FOUND);
        String detailedMessage = exception.getDetailedMessage();
        assertTrue(detailedMessage.contains(ErrorCode.FILE_NOT_FOUND.getCode()));
        assertTrue(detailedMessage.contains("File not found"));
    }

    @Test
    public void testExceptionWithFileReadErrorCode() {
        FtocException exception = new FtocException("File not found: test.feature", ErrorCode.FILE_READ_ERROR);
        assertEquals("File not found: test.feature", exception.getMessage());
        assertEquals(ErrorCode.FILE_READ_ERROR, exception.getErrorCode());
    }

    @Test
    public void testExceptionWithInvalidGherkinCode() {
        FtocException exception = new FtocException("Invalid Gherkin syntax", ErrorCode.INVALID_GHERKIN);
        assertEquals("Invalid Gherkin syntax", exception.getMessage());
        assertEquals(ErrorCode.INVALID_GHERKIN, exception.getErrorCode());
    }

    @Test
    public void testExceptionWithCauseAndErrorCode() {
        Exception cause = new RuntimeException("IO failure");
        FtocException exception = new FtocException("Read failed", cause, ErrorCode.FILE_READ_ERROR);
        assertEquals("Read failed", exception.getMessage());
        assertSame(cause, exception.getCause());
        assertEquals(ErrorCode.FILE_READ_ERROR, exception.getErrorCode());
    }

    @Test
    public void testErrorCodes() {
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getCode());
            assertFalse(code.getCode().isEmpty());
            assertNotNull(code.getDescription());
            assertFalse(code.getDescription().isEmpty());
        }
    }

    @Test
    public void testFileNotFoundError() {
        File nonExistentFile = new File("this-file-does-not-exist.feature");
        Throwable exception = assertThrows(FtocException.class, () -> {
            new com.heymumford.ftoc.parser.FeatureParser().parseFeatureFile(nonExistentFile);
        });
        assertTrue(exception.getMessage().contains("not found"));
        assertEquals(FtocException.class, exception.getClass());
    }
}
