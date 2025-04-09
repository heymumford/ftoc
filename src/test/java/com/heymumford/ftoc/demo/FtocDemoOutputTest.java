package com.heymumford.ftoc.demo;

import com.heymumford.ftoc.FtocUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple test to capture the output of the FTOC utility for inspection.
 */
public class FtocDemoOutputTest {

    private FtocUtility ftoc;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private String demoDir = "src/test/resources/ftoc/demo";
    private String outputDir = demoDir + "/output";
    
    @BeforeEach
    public void setup() throws IOException {
        // Initialize FTOC utility
        ftoc = new FtocUtility();
        ftoc.initialize();
        
        // Set up output capture
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }
    
    @Test
    public void captureMarkdownOutput() throws IOException {
        // Run FTOC on the demo directory with markdown output
        ftoc.setOutputFormat(com.heymumford.ftoc.formatter.TocFormatter.Format.MARKDOWN);
        ftoc.processDirectory(demoDir);
        
        // Get the captured output
        String output = outputStream.toString();
        
        // Save the output to a file for inspection
        Path outputPath = Paths.get(outputDir, "markdown_output.md");
        Files.writeString(outputPath, output);
        
        // Restore original output and print the file path
        System.setOut(originalOut);
        System.out.println("Markdown output saved to: " + outputPath);
    }
    
    @Override
    protected void finalize() {
        // Restore the original System.out
        System.setOut(originalOut);
    }
}