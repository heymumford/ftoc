package com.heymumford.ftoc.config;

import com.heymumford.ftoc.analyzer.FeatureAntiPatternAnalyzer;
import com.heymumford.ftoc.analyzer.TagQualityAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manages configuration for warning types in FTOC.
 * This class loads and provides access to warning configurations from a YAML file.
 */
public class WarningConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(WarningConfiguration.class);
    
    /**
     * Enumeration of warning severity levels.
     */
    public enum Severity {
        ERROR("ERROR"),
        WARNING("WARNING"),
        INFO("INFO"),
        HINT("HINT");
        
        private final String displayName;
        
        Severity(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Convert a string to a Severity enum value.
         * 
         * @param str The string representation
         * @return The corresponding Severity, or WARNING if not recognized
         */
        public static Severity fromString(String str) {
            if (str == null) {
                return WARNING;
            }
            
            try {
                return valueOf(str.toUpperCase());
            } catch (IllegalArgumentException e) {
                return WARNING;
            }
        }
    }
    
    // Default configuration file paths to check (in order)
    private static final String[] DEFAULT_CONFIG_PATHS = {
        ".ftoc/config.yml",
        ".ftoc.yml",
        ".config/ftoc/warnings.yml",
        "config/ftoc-warnings.yml"
    };
    
    // Default user home directory config
    private static final String USER_HOME_CONFIG = ".config/ftoc/warnings.yml";
    
    private final Map<String, WarningConfig> tagQualityWarnings = new HashMap<>();
    private final Map<String, WarningConfig> antiPatternWarnings = new HashMap<>();
    private final Set<String> disabledWarnings = new HashSet<>();
    private final Map<String, List<String>> customTags = new HashMap<>();
    private final Map<String, Object> thresholds = new HashMap<>();
    private final String configPath;
    
    /**
     * Creates a new configuration manager with default settings (all warnings enabled).
     */
    public WarningConfiguration() {
        this(null);
    }
    
    /**
     * Creates a new configuration manager and attempts to load settings from the specified file.
     * If the file doesn't exist or can't be read, defaults are used (all warnings enabled).
     *
     * @param configFilePath Path to the configuration file, or null to use defaults
     */
    public WarningConfiguration(String configFilePath) {
        // Initialize with default configuration (all warnings enabled)
        initializeDefaults();
        
        // Try to load configuration from file
        String loadedPath = loadConfiguration(configFilePath);
        this.configPath = loadedPath;
        
        if (loadedPath != null) {
            logger.info("Loaded warning configuration from: {}", loadedPath);
        } else {
            logger.info("Using default warning configuration (no config file found)");
        }
    }
    
    /**
     * Set up default configuration with all warnings enabled.
     */
    private void initializeDefaults() {
        // Tag quality warnings (all enabled by default)
        for (TagQualityAnalyzer.WarningType type : TagQualityAnalyzer.WarningType.values()) {
            tagQualityWarnings.put(type.name(), new WarningConfig(type.name(), true, type.getDescription()));
        }
        
        // Anti-pattern warnings (all enabled by default)
        for (FeatureAntiPatternAnalyzer.WarningType type : FeatureAntiPatternAnalyzer.WarningType.values()) {
            antiPatternWarnings.put(type.name(), new WarningConfig(type.name(), true, type.getDescription()));
        }
        
        // Default thresholds
        thresholds.put("maxSteps", 10);
        thresholds.put("minSteps", 2);
        thresholds.put("maxExamples", 2);
        thresholds.put("maxTags", 6);
        thresholds.put("maxScenarioNameLength", 100);
        thresholds.put("maxStepLength", 120);
    }
    
    /**
     * Attempt to load configuration from a file.
     *
     * @param configFilePath User-specified path, or null to check default locations
     * @return The path that was successfully loaded, or null if no configuration was loaded
     */
    private String loadConfiguration(String configFilePath) {
        // If a specific path was provided, try only that one
        if (configFilePath != null) {
            if (loadConfigurationFromFile(configFilePath)) {
                return configFilePath;
            }
            logger.warn("Could not load configuration from specified file: {}", configFilePath);
            return null;
        }
        
        // Try default paths in the current directory
        for (String defaultPath : DEFAULT_CONFIG_PATHS) {
            if (loadConfigurationFromFile(defaultPath)) {
                return defaultPath;
            }
        }
        
        // Try user home directory
        String userHome = System.getProperty("user.home");
        String userHomePath = Paths.get(userHome, USER_HOME_CONFIG).toString();
        if (loadConfigurationFromFile(userHomePath)) {
            return userHomePath;
        }
        
        return null;
    }
    
    /**
     * Loads configuration from a specific file path.
     *
     * @param filePath Path to the configuration file
     * @return true if the file was loaded successfully, false otherwise
     */
    private boolean loadConfigurationFromFile(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            logger.debug("Configuration file does not exist: {}", filePath);
            return false;
        }
        
        try (InputStream input = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(input);
            
            if (config == null) {
                logger.warn("Configuration file is empty or invalid: {}", filePath);
                return false;
            }
            
            // Process warnings section
            if (config.containsKey("warnings")) {
                processWarningsSection((Map<String, Object>) config.get("warnings"));
            }
            
            // Process tags section
            if (config.containsKey("tags")) {
                processTagsSection((Map<String, Object>) config.get("tags"));
            }
            
            // Process thresholds section
            if (config.containsKey("thresholds")) {
                processThresholdsSection((Map<String, Object>) config.get("thresholds"));
            }
            
            return true;
        } catch (IOException e) {
            logger.warn("Error reading configuration file: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error parsing configuration file: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Process the warnings section of the configuration file.
     *
     * @param warningsSection The warnings section from the YAML file
     */
    private void processWarningsSection(Map<String, Object> warningsSection) {
        // Process disabled warnings list
        if (warningsSection.containsKey("disabled")) {
            List<String> disabled = (List<String>) warningsSection.get("disabled");
            if (disabled != null) {
                for (String warning : disabled) {
                    disabledWarnings.add(warning);
                    
                    // Update the enabled status in the appropriate map
                    if (tagQualityWarnings.containsKey(warning)) {
                        tagQualityWarnings.get(warning).setEnabled(false);
                    } else if (antiPatternWarnings.containsKey(warning)) {
                        antiPatternWarnings.get(warning).setEnabled(false);
                    }
                }
            }
        }
        
        // Process tag quality warnings
        if (warningsSection.containsKey("tagQuality")) {
            Map<String, Object> tagQualitySection = (Map<String, Object>) warningsSection.get("tagQuality");
            for (Map.Entry<String, Object> entry : tagQualitySection.entrySet()) {
                String warningName = entry.getKey();
                if (entry.getValue() instanceof Boolean) {
                    boolean enabled = (Boolean) entry.getValue();
                    if (tagQualityWarnings.containsKey(warningName)) {
                        tagQualityWarnings.get(warningName).setEnabled(enabled);
                    }
                } else if (entry.getValue() instanceof Map) {
                    Map<String, Object> warningConfig = (Map<String, Object>) entry.getValue();
                    boolean enabled = warningConfig.containsKey("enabled") ? (Boolean) warningConfig.get("enabled") : true;
                    if (tagQualityWarnings.containsKey(warningName)) {
                        tagQualityWarnings.get(warningName).setEnabled(enabled);
                        
                        if (warningConfig.containsKey("severity")) {
                            tagQualityWarnings.get(warningName).setSeverity((String) warningConfig.get("severity"));
                        }
                        
                        if (warningConfig.containsKey("standardAlternatives")) {
                            List<String> alternatives = (List<String>) warningConfig.get("standardAlternatives");
                            if (alternatives != null) {
                                tagQualityWarnings.get(warningName).setStandardAlternatives(alternatives);
                            }
                        }
                    }
                }
            }
        }
        
        // Process anti-pattern warnings
        if (warningsSection.containsKey("antiPatterns")) {
            Map<String, Object> antiPatternSection = (Map<String, Object>) warningsSection.get("antiPatterns");
            for (Map.Entry<String, Object> entry : antiPatternSection.entrySet()) {
                String warningName = entry.getKey();
                if (entry.getValue() instanceof Boolean) {
                    boolean enabled = (Boolean) entry.getValue();
                    if (antiPatternWarnings.containsKey(warningName)) {
                        antiPatternWarnings.get(warningName).setEnabled(enabled);
                    }
                } else if (entry.getValue() instanceof Map) {
                    Map<String, Object> warningConfig = (Map<String, Object>) entry.getValue();
                    boolean enabled = warningConfig.containsKey("enabled") ? (Boolean) warningConfig.get("enabled") : true;
                    if (antiPatternWarnings.containsKey(warningName)) {
                        antiPatternWarnings.get(warningName).setEnabled(enabled);
                        
                        if (warningConfig.containsKey("severity")) {
                            antiPatternWarnings.get(warningName).setSeverity((String) warningConfig.get("severity"));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Process the tags section of the configuration file.
     *
     * @param tagsSection The tags section from the YAML file
     */
    private void processTagsSection(Map<String, Object> tagsSection) {
        for (Map.Entry<String, Object> entry : tagsSection.entrySet()) {
            String tagCategory = entry.getKey();
            if (entry.getValue() instanceof List) {
                List<String> tags = (List<String>) entry.getValue();
                customTags.put(tagCategory, tags);
            }
        }
    }
    
    /**
     * Process the thresholds section of the configuration file.
     *
     * @param thresholdsSection The thresholds section from the YAML file
     */
    private void processThresholdsSection(Map<String, Object> thresholdsSection) {
        for (Map.Entry<String, Object> entry : thresholdsSection.entrySet()) {
            String thresholdName = entry.getKey();
            Object value = entry.getValue();
            thresholds.put(thresholdName, value);
        }
    }
    
    /**
     * Get a map of warning configs for tag quality warnings.
     *
     * @return Map of warning configs keyed by warning name
     */
    public Map<String, WarningConfig> getTagQualityWarnings() {
        return tagQualityWarnings;
    }
    
    /**
     * Get a map of warning configs for anti-pattern warnings.
     *
     * @return Map of warning configs keyed by warning name
     */
    public Map<String, WarningConfig> getAntiPatternWarnings() {
        return antiPatternWarnings;
    }
    
    /**
     * Check if a specific warning is enabled.
     *
     * @param warningName The warning name
     * @return true if the warning is enabled, false otherwise
     */
    public boolean isWarningEnabled(String warningName) {
        if (tagQualityWarnings.containsKey(warningName)) {
            return tagQualityWarnings.get(warningName).isEnabled();
        } else if (antiPatternWarnings.containsKey(warningName)) {
            return antiPatternWarnings.get(warningName).isEnabled();
        }
        return true; // Default to enabled for unknown warnings
    }
    
    /**
     * Get custom tags for a specific category.
     *
     * @param category The tag category (e.g., "priority", "type", "status")
     * @return List of custom tags, or empty list if none defined
     */
    public List<String> getCustomTags(String category) {
        return customTags.getOrDefault(category, Collections.emptyList());
    }
    
    /**
     * Get a threshold value.
     *
     * @param thresholdName The threshold name
     * @return The threshold value, or null if not defined
     */
    public Object getThreshold(String thresholdName) {
        return thresholds.get(thresholdName);
    }
    
    /**
     * Get an integer threshold value with a default.
     *
     * @param thresholdName The threshold name
     * @param defaultValue Default value to return if threshold is not found
     * @return The threshold value, or the default if not defined
     */
    public int getIntThreshold(String thresholdName, int defaultValue) {
        Object value = thresholds.get(thresholdName);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /**
     * Get the path to the configuration file that was loaded.
     *
     * @return The configuration file path, or null if using defaults
     */
    public String getConfigPath() {
        return configPath;
    }
    
    /**
     * Get a summary of the current configuration.
     *
     * @return A string representation of the configuration
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Warning Configuration Summary:\n");
        
        if (configPath != null) {
            sb.append("Loaded from: ").append(configPath).append("\n\n");
        } else {
            sb.append("Using default configuration (no config file loaded)\n\n");
        }
        
        sb.append("Tag Quality Warnings:\n");
        for (WarningConfig config : tagQualityWarnings.values()) {
            sb.append("  - ").append(config.getName()).append(": ")
              .append(config.isEnabled() ? "enabled" : "disabled");
            if (config.getSeverity() != null) {
                sb.append(" (").append(config.getSeverity()).append(")");
            }
            sb.append("\n");
        }
        
        sb.append("\nAnti-Pattern Warnings:\n");
        for (WarningConfig config : antiPatternWarnings.values()) {
            sb.append("  - ").append(config.getName()).append(": ")
              .append(config.isEnabled() ? "enabled" : "disabled");
            if (config.getSeverity() != null) {
                sb.append(" (").append(config.getSeverity()).append(")");
            }
            sb.append("\n");
        }
        
        sb.append("\nThresholds:\n");
        for (Map.Entry<String, Object> entry : thresholds.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ")
              .append(entry.getValue()).append("\n");
        }
        
        if (!customTags.isEmpty()) {
            sb.append("\nCustom Tags:\n");
            for (Map.Entry<String, List<String>> entry : customTags.entrySet()) {
                sb.append("  - ").append(entry.getKey()).append(": ")
                  .append(String.join(", ", entry.getValue())).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Inner class representing a warning configuration.
     */
    public static class WarningConfig {
        private final String name;
        private boolean enabled;
        private final String description;
        private Severity severity;
        private List<String> standardAlternatives;
        
        public WarningConfig(String name, boolean enabled, String description) {
            this.name = name;
            this.enabled = enabled;
            this.description = description;
            this.severity = Severity.WARNING; // Default severity
            this.standardAlternatives = new ArrayList<>();
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public String getSeverityDisplayName() {
            return severity.getDisplayName();
        }
        
        public void setSeverity(Severity severity) {
            this.severity = severity;
        }
        
        public void setSeverity(String severityStr) {
            this.severity = Severity.fromString(severityStr);
        }
        
        public List<String> getStandardAlternatives() {
            return standardAlternatives;
        }
        
        public void setStandardAlternatives(List<String> standardAlternatives) {
            this.standardAlternatives = standardAlternatives;
        }
        
        public void addStandardAlternative(String alternative) {
            this.standardAlternatives.add(alternative);
        }
    }
}