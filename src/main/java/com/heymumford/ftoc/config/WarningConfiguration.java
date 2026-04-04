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
    private final List<String> validationErrors = new ArrayList<>();
    private final String configPath;

    private static final Set<String> VALID_TOP_LEVEL_KEYS = new HashSet<>(Arrays.asList(
        "warnings", "tags", "thresholds"
    ));

    private static final Set<String> VALID_SEVERITIES = new HashSet<>(Arrays.asList(
        "ERROR", "WARNING", "INFO", "HINT"
    ));
    
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

            // Validate top-level keys
            for (String key : config.keySet()) {
                if (!VALID_TOP_LEVEL_KEYS.contains(key)) {
                    validationErrors.add("Unknown top-level configuration key: " + key);
                }
            }

            // Process and validate warnings section
            if (config.containsKey("warnings")) {
                Object warningsObj = config.get("warnings");
                if (warningsObj instanceof Map) {
                    validateWarningsSection(
                        (Map<String, Object>) warningsObj);
                    processWarningsSection(
                        (Map<String, Object>) warningsObj);
                } else if (warningsObj != null) {
                    validationErrors.add(
                        "'warnings' must be a map, got: "
                        + warningsObj.getClass().getSimpleName());
                }
            }

            // Process tags section
            if (config.containsKey("tags")) {
                processTagsSection(
                    (Map<String, Object>) config.get("tags"));
            }

            // Process and validate thresholds section
            if (config.containsKey("thresholds")) {
                Object thresholdsObj = config.get("thresholds");
                if (thresholdsObj instanceof Map) {
                    validateThresholdsSection(
                        (Map<String, Object>) thresholdsObj);
                    processThresholdsSection(
                        (Map<String, Object>) thresholdsObj);
                } else if (thresholdsObj != null) {
                    validationErrors.add(
                        "'thresholds' must be a map, got: "
                        + thresholdsObj.getClass()
                            .getSimpleName());
                }
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
     * Validate the warnings section for unknown names, bad types, and invalid severities.
     */
    @SuppressWarnings("unchecked")
    private void validateWarningsSection(Map<String, Object> warningsSection) {
        // Validate disabled list
        if (warningsSection.containsKey("disabled")) {
            Object disabled = warningsSection.get("disabled");
            if (disabled == null) {
                // null is OK -- means the key exists but the list is empty/commented out
            } else if (!(disabled instanceof List)) {
                validationErrors.add("'warnings.disabled' must be a list, got: "
                    + disabled.getClass().getSimpleName());
            } else {
                for (Object item : (List<?>) disabled) {
                    String warningName = String.valueOf(item);
                    if (!isKnownWarning(warningName)) {
                        validationErrors.add("Unknown warning in disabled list: " + warningName);
                    }
                }
            }
        }

        // Validate severity map
        if (warningsSection.containsKey("severity")) {
            Object severityObj = warningsSection.get("severity");
            if (severityObj instanceof Map) {
                Map<String, Object> severityMap =
                    (Map<String, Object>) severityObj;
                for (Map.Entry<String, Object> entry
                        : severityMap.entrySet()) {
                    if (!isKnownWarning(entry.getKey())) {
                        validationErrors.add(
                            "Unknown warning name in severity"
                            + " config: " + entry.getKey());
                    }
                    String severityValue = String.valueOf(
                        entry.getValue()).toUpperCase();
                    if (!VALID_SEVERITIES.contains(
                            severityValue)) {
                        validationErrors.add(
                            "Invalid severity value '"
                            + entry.getValue()
                            + "' for warning " + entry.getKey()
                            + ". Valid values: ERROR, WARNING,"
                            + " INFO, HINT");
                    }
                }
            } else if (severityObj != null) {
                validationErrors.add(
                    "'warnings.severity' must be a map, got: "
                    + severityObj.getClass().getSimpleName());
            }
        }

        // Validate standardAlternatives map
        if (warningsSection.containsKey("standardAlternatives")) {
            Object altObj = warningsSection.get("standardAlternatives");
            if (altObj instanceof Map) {
                Map<String, Object> altMap = (Map<String, Object>) altObj;
                for (String warningName : altMap.keySet()) {
                    if (!isKnownWarning(warningName)) {
                        validationErrors.add("Unknown warning name in standardAlternatives: " + warningName);
                    }
                }
            }
        }
    }

    /**
     * Validate the thresholds section for non-numeric and negative values.
     */
    private void validateThresholdsSection(
            Map<String, Object> thresholdsSection) {
        for (Map.Entry<String, Object> entry
                : thresholdsSection.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof Number)) {
                validationErrors.add("Threshold '"
                    + entry.getKey()
                    + "' must be a number, got: " + value);
            } else if (((Number) value).doubleValue() < 0) {
                validationErrors.add("Threshold '"
                    + entry.getKey()
                    + "' must not be negative, got: " + value);
            }
        }
    }

    /**
     * Check if a warning name is recognized in either category.
     */
    private boolean isKnownWarning(String warningName) {
        return tagQualityWarnings.containsKey(warningName)
            || antiPatternWarnings.containsKey(warningName);
    }

    /**
     * Process the warnings section of the configuration file.
     * Supports both the simplified flat format (severity/standardAlternatives maps)
     * and the legacy nested format (tagQuality/antiPatterns sub-sections).
     *
     * @param warningsSection The warnings section from the YAML file
     */
    @SuppressWarnings("unchecked")
    private void processWarningsSection(Map<String, Object> warningsSection) {
        // Process disabled warnings list (shared by both formats)
        if (warningsSection.containsKey("disabled")) {
            List<String> disabled =
                    (List<String>) warningsSection.get("disabled");
            if (disabled != null) {
                for (String warning : disabled) {
                    disabledWarnings.add(warning);
                    if (tagQualityWarnings.containsKey(warning)) {
                        tagQualityWarnings.get(warning).setEnabled(false);
                    } else if (antiPatternWarnings.containsKey(warning)) {
                        antiPatternWarnings.get(warning).setEnabled(false);
                    }
                }
            }
        }

        // Flat format: severity map (WARNING_NAME -> severity string)
        if (warningsSection.containsKey("severity")) {
            Map<String, Object> severityMap =
                    (Map<String, Object>) warningsSection.get("severity");
            if (severityMap != null) {
                for (Map.Entry<String, Object> entry
                        : severityMap.entrySet()) {
                    WarningConfig cfg =
                            findWarningConfig(entry.getKey());
                    if (cfg != null) {
                        cfg.setSeverity(
                                String.valueOf(entry.getValue()));
                    }
                }
            }
        }

        // Flat format: standardAlternatives (WARNING_NAME -> list)
        if (warningsSection.containsKey("standardAlternatives")) {
            Map<String, Object> altMap = (Map<String, Object>)
                    warningsSection.get("standardAlternatives");
            if (altMap != null) {
                for (Map.Entry<String, Object> entry
                        : altMap.entrySet()) {
                    if (entry.getValue() instanceof List) {
                        WarningConfig cfg =
                                findWarningConfig(entry.getKey());
                        if (cfg != null) {
                            cfg.setStandardAlternatives(
                                    (List<String>) entry.getValue());
                        }
                    }
                }
            }
        }

        // Legacy format: nested tagQuality section
        if (warningsSection.containsKey("tagQuality")) {
            processLegacyWarningCategory(
                    (Map<String, Object>) warningsSection
                            .get("tagQuality"),
                    tagQualityWarnings);
        }

        // Legacy format: nested antiPatterns section
        if (warningsSection.containsKey("antiPatterns")) {
            processLegacyWarningCategory(
                    (Map<String, Object>) warningsSection
                            .get("antiPatterns"),
                    antiPatternWarnings);
        }
    }

    /**
     * Look up a WarningConfig by name in either category map.
     */
    private WarningConfig findWarningConfig(String warningName) {
        WarningConfig cfg = tagQualityWarnings.get(warningName);
        return cfg != null ? cfg : antiPatternWarnings.get(warningName);
    }

    /**
     * Process a legacy nested warning category (tagQuality or antiPatterns).
     */
    @SuppressWarnings("unchecked")
    private void processLegacyWarningCategory(
            Map<String, Object> section,
            Map<String, WarningConfig> targetMap) {
        if (section == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            String warningName = entry.getKey();
            if (entry.getValue() instanceof Boolean) {
                if (targetMap.containsKey(warningName)) {
                    targetMap.get(warningName)
                            .setEnabled((Boolean) entry.getValue());
                }
            } else if (entry.getValue() instanceof Map) {
                Map<String, Object> wc =
                        (Map<String, Object>) entry.getValue();
                if (targetMap.containsKey(warningName)) {
                    WarningConfig cfg = targetMap.get(warningName);
                    cfg.setEnabled(wc.containsKey("enabled")
                            ? (Boolean) wc.get("enabled") : true);
                    if (wc.containsKey("severity")) {
                        cfg.setSeverity(
                                (String) wc.get("severity"));
                    }
                    if (wc.containsKey("standardAlternatives")) {
                        List<String> alts = (List<String>)
                                wc.get("standardAlternatives");
                        if (alts != null) {
                            cfg.setStandardAlternatives(alts);
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
     * Get validation errors found when loading the configuration.
     *
     * @return List of validation error messages, empty if config is valid
     */
    public List<String> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
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