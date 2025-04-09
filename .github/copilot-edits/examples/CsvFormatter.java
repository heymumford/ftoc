package com.heymumford.ftoc.formatter;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formatter for CSV output format.
 * This formatter generates a CSV representation of features and scenarios.
 */
public class CsvFormatter implements Formatter {
    private static final Logger logger = LoggerFactory.getLogger(CsvFormatter.class);
    
    // Default CSV column headers
    private static final String DEFAULT_HEADERS = "Feature,Scenario,Tags,File,Line";
    
    // Customizable column settings
    private boolean includeFeatureName = true;
    private boolean includeScenarioName = true;
    private boolean includeTags = true;
    private boolean includeFileName = true;
    private boolean includeLineNumber = true;
    
    /**
     * Generate CSV output for the provided features.
     * 
     * @param features List of features to include in the output
     * @return CSV string representing the features
     */
    @Override
    public String generate(List<Feature> features) {
        logger.debug("Generating CSV output for {} features", features.size());
        
        StringBuilder sb = new StringBuilder();
        
        // Add headers
        sb.append(getHeaderRow()).append("\n");
        
        // Add data rows
        for (Feature feature : features) {
            for (Scenario scenario : feature.getScenarios()) {
                sb.append(formatRow(feature, scenario)).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generate the header row based on configured columns.
     * 
     * @return CSV header row
     */
    private String getHeaderRow() {
        StringBuilder sb = new StringBuilder();
        
        if (includeFeatureName) sb.append("Feature,");
        if (includeScenarioName) sb.append("Scenario,");
        if (includeTags) sb.append("Tags,");
        if (includeFileName) sb.append("File,");
        if (includeLineNumber) sb.append("Line,");
        
        // Remove trailing comma if present
        String result = sb.toString();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        
        return result.isEmpty() ? DEFAULT_HEADERS : result;
    }
    
    /**
     * Format a data row for a feature-scenario combination.
     * 
     * @param feature The feature
     * @param scenario The scenario within the feature
     * @return CSV data row
     */
    private String formatRow(Feature feature, Scenario scenario) {
        StringBuilder sb = new StringBuilder();
        
        if (includeFeatureName) sb.append(escapeCsv(feature.getName())).append(",");
        if (includeScenarioName) sb.append(escapeCsv(scenario.getName())).append(",");
        if (includeTags) sb.append(escapeCsv(String.join(" ", scenario.getTags()))).append(",");
        if (includeFileName) sb.append(escapeCsv(feature.getFile())).append(",");
        if (includeLineNumber) sb.append(scenario.getLine()).append(",");
        
        // Remove trailing comma if present
        String result = sb.toString();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        
        return result;
    }
    
    /**
     * Escape special characters for CSV format.
     * 
     * @param value The string to escape
     * @return Escaped string
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // If the value contains commas, quotes, or newlines, wrap it in quotes
        // and escape any existing quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Configure which columns to include in the CSV output.
     * 
     * @param includeFeatureName Whether to include feature names
     * @param includeScenarioName Whether to include scenario names
     * @param includeTags Whether to include tags
     * @param includeFileName Whether to include file names
     * @param includeLineNumber Whether to include line numbers
     * @return This formatter instance for method chaining
     */
    public CsvFormatter configure(boolean includeFeatureName, boolean includeScenarioName, 
                                 boolean includeTags, boolean includeFileName, 
                                 boolean includeLineNumber) {
        this.includeFeatureName = includeFeatureName;
        this.includeScenarioName = includeScenarioName;
        this.includeTags = includeTags;
        this.includeFileName = includeFileName;
        this.includeLineNumber = includeLineNumber;
        
        return this;
    }
}