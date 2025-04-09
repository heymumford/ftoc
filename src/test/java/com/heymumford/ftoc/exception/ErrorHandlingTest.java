package com.heymumford.ftoc.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests for the error handling system.
 */
public class ErrorHandlingTest {
    
    private final ByteArrayOutputStream outputContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outputContent));
    }
    
    public void restoreStreams() {
        System.setOut(originalOut);
    }
    
    @Test
    public void testFtocExceptionCreation() {
        String errorMessage = "Test error message";
        FtocException exception = new FtocException(errorMessage);
        
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(ErrorCode.GENERAL_ERROR, exception.getErrorCode());
        assertEquals(ExceptionSeverity.ERROR, exception.getSeverity());
    }
    
    @Test
    public void testExceptionWithCause() {
        Exception cause = new RuntimeException("Original error");
        FtocException exception = new FtocException("Wrapped error", cause);
        
        assertEquals("Wrapped error", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
    
    @Test
    public void testExceptionWithErrorCode() {
        FtocException exception = new FtocException("Configuration error", ErrorCode.CONFIGURATION_ERROR);
        
        assertEquals("Configuration error", exception.getMessage());
        assertEquals(ErrorCode.CONFIGURATION_ERROR, exception.getErrorCode());
    }
    
    @Test
    public void testDetailedErrorMessage() {
        FtocException exception = new FtocException(
            "File not found",
            ErrorCode.FILE_NOT_FOUND, 
            ExceptionSeverity.ERROR
        );
        
        String detailedMessage = exception.getDetailedMessage();
        assertTrue(detailedMessage.contains("ERROR"));
        assertTrue(detailedMessage.contains(ErrorCode.FILE_NOT_FOUND.getCode()));
        assertTrue(detailedMessage.contains("File not found"));
    }
    
    @Test
    public void testFileExceptionCreation() {
        FileException exception = new FileException("File not found: test.feature");
        
        assertEquals("File not found: test.feature", exception.getMessage());
        assertEquals(ErrorCode.FILE_READ_ERROR, exception.getErrorCode());
    }
    
    @Test
    public void testParsingExceptionCreation() {
        ParsingException exception = new ParsingException(
            "Invalid Gherkin syntax", 
            ErrorCode.INVALID_GHERKIN
        );
        
        assertEquals("Invalid Gherkin syntax", exception.getMessage());
        assertEquals(ErrorCode.INVALID_GHERKIN, exception.getErrorCode());
    }
    
    @Test
    public void testErrorHandlerHandling() {
        FtocException fatalException = new FtocException(
            "Fatal error", 
            null, 
            ErrorCode.GENERAL_ERROR, 
            ExceptionSeverity.FATAL
        );
        
        FtocException warningException = new FtocException(
            "Warning message", 
            null, 
            ErrorCode.GENERAL_ERROR, 
            ExceptionSeverity.WARNING
        );
        
        // Fatal errors should return false (application should terminate)
        assertFalse(ErrorHandler.handleException(fatalException));
        
        // Warnings should return true (application can continue)
        assertTrue(ErrorHandler.handleException(warningException));
    }
    
    @Test
    public void testErrorCodes() {
        // All error codes should have a non-empty code and description
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getCode());
            assertFalse(code.getCode().isEmpty());
            assertNotNull(code.getDescription());
            assertFalse(code.getDescription().isEmpty());
        }
    }
    
    @Test
    public void testUserFriendlyMessages() {
        FtocException generalException = new FtocException("General error");
        FileException fileException = new FileException("Missing file");
        ParsingException parsingException = new ParsingException("Invalid syntax");
        
        String generalMessage = ErrorHandler.getUserFriendlyMessage(generalException);
        String fileMessage = ErrorHandler.getUserFriendlyMessage(fileException);
        String parsingMessage = ErrorHandler.getUserFriendlyMessage(parsingException);
        
        assertTrue(generalMessage.contains("Error"));
        assertTrue(fileMessage.contains("File error"));
        assertTrue(parsingMessage.contains("Parsing error"));
    }
    
    @Test
    public void testDirectErrorHandling() {
        boolean result = ErrorHandler.handleError(
            "Test direct error", 
            ErrorCode.CONFIGURATION_ERROR, 
            ExceptionSeverity.WARNING
        );
        
        // Warning level should allow continuation
        assertTrue(result);
    }
    
    @Test
    public void testFileNotFoundError() {
        // Try with a non-existent file
        File nonExistentFile = new File("this-file-does-not-exist.feature");
        Throwable fileException = assertThrows(FileException.class, () -> {
            new com.heymumford.ftoc.parser.FeatureParser().parseFeatureFile(nonExistentFile);
        });
        
        assertTrue(fileException.getMessage().contains("not found"));
        assertEquals(FileException.class, fileException.getClass());
    }
}