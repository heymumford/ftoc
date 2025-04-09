package com.heymumford.ftoc.util;

import com.heymumford.ftoc.FtocUtility;
import com.heymumford.ftoc.FtocUtilityRefactored;
import com.heymumford.ftoc.core.Reporter;

/**
 * Utility class with example code to help migrate from the old architecture to the new one.
 * Note: This class is for demonstration purposes and won't compile without modification
 * because the old FtocUtility class doesn't expose the necessary accessor methods.
 */
public class ArchitectureMigration {
    
    /**
     * Create a new refactored utility from an existing one.
     * This is a demonstration method that shows the concept but won't compile
     * without modifications to the FtocUtility class.
     * 
     * @param oldUtility The existing FtocUtility instance
     * @return A new FtocUtilityRefactored instance
     */
    public static FtocUtilityRefactored migrateFromOldToNew() {
        // Create a new instance of the refactored utility
        FtocUtilityRefactored newUtility = new FtocUtilityRefactored();
        
        // If you want to migrate settings from an existing utility,
        // you would need to extend FtocUtility to expose its private fields
        // or add accessor methods.
        
        return newUtility;
    }
    
    /**
     * Example of recommended migration approach for users.
     */
    public static void migrationExample() {
        // Existing code using old architecture
        FtocUtility oldUtility = new FtocUtility();
        oldUtility.initialize();
        oldUtility.processDirectory("/path/to/features");
        
        // New code using refactored architecture
        FtocUtilityRefactored newUtility = new FtocUtilityRefactored();
        newUtility.initialize();
        newUtility.processDirectory("/path/to/features");
        
        // The API is intentionally similar to make migration easy
    }
}