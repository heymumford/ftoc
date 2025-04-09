package com.heymumford.ftoc.plugin.example;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.plugin.AbstractFtocPlugin;
import com.heymumford.ftoc.plugin.PluginEvent;
import com.heymumford.ftoc.plugin.PluginRegistry;

import java.util.List;

/**
 * Example plugin for FTOC.
 * This plugin demonstrates how to create a plugin and use the plugin API.
 */
public class ExamplePlugin extends AbstractFtocPlugin {
    
    @Override
    public String getName() {
        return "ExamplePlugin";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getDescription() {
        return "Example plugin that logs information about feature files";
    }
    
    @Override
    public void initialize(PluginRegistry registry) {
        super.initialize(registry);
        
        // Register event handlers
        registerEventHandler(PluginEvent.STARTUP, data -> {
            logger.info("Example plugin started");
        });
        
        registerEventHandler(PluginEvent.FEATURES_LOADED, data -> {
            if (data instanceof List<?> && !((List<?>) data).isEmpty() && ((List<?>) data).get(0) instanceof Feature) {
                @SuppressWarnings("unchecked")
                List<Feature> features = (List<Feature>) data;
                logFeatureStats(features);
            }
        });
        
        registerEventHandler(PluginEvent.SHUTDOWN, data -> {
            logger.info("Example plugin shutting down");
        });
    }
    
    /**
     * Log statistics about the loaded features.
     * 
     * @param features List of features
     */
    private void logFeatureStats(List<Feature> features) {
        int featureCount = features.size();
        int scenarioCount = features.stream()
                .mapToInt(f -> f.getScenarios().size())
                .sum();
        int tagCount = (int) features.stream()
                .flatMap(f -> f.getScenarios().stream())
                .flatMap(s -> s.getTags().stream())
                .distinct()
                .count();
        
        logger.info("Example plugin detected:");
        logger.info("  - {} feature files", featureCount);
        logger.info("  - {} scenarios", scenarioCount);
        logger.info("  - {} unique tags", tagCount);
        
        // Print to console as well
        System.out.println("\n[ExamplePlugin] Feature Statistics:");
        System.out.println("  - " + featureCount + " feature files");
        System.out.println("  - " + scenarioCount + " scenarios");
        System.out.println("  - " + tagCount + " unique tags\n");
    }
}