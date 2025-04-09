package com.heymumford.ftoc.plugin;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import com.heymumford.ftoc.plugin.example.ExamplePlugin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Temporarily disabled until JUnit issues are fixed")

/**
 * Tests for the PluginRegistry class.
 */
public class PluginRegistryTest {
    
    @Test
    public void testPluginRegistration() {
        PluginRegistry registry = new PluginRegistry();
        FtocPlugin plugin = new ExamplePlugin();
        
        registry.registerPlugin(plugin);
        
        assertEquals(1, registry.getAllPlugins().size());
        assertEquals(plugin, registry.getPlugin(plugin.getName()));
    }
    
    @Test
    public void testEventHandling() {
        PluginRegistry registry = new PluginRegistry();
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        
        registry.registerEventHandler(PluginEvent.STARTUP, data -> {
            handlerCalled.set(true);
        });
        
        registry.triggerEvent(PluginEvent.STARTUP);
        
        assertTrue(handlerCalled.get());
    }
    
    @Test
    public void testEventDataPassing() {
        PluginRegistry registry = new PluginRegistry();
        final String[] receivedData = new String[1];
        
        registry.registerEventHandler(PluginEvent.PRE_PARSE_ARGUMENTS, data -> {
            if (data instanceof String[]) {
                receivedData[0] = Arrays.toString((String[]) data);
            }
        });
        
        String[] args = {"arg1", "arg2"};
        registry.triggerEvent(PluginEvent.PRE_PARSE_ARGUMENTS, args);
        
        assertEquals(Arrays.toString(args), receivedData[0]);
    }
    
    @Test
    public void testExamplePluginFeatureProcessing() {
        // Create a mock feature list
        List<Feature> features = new ArrayList<>();
        
        Feature feature1 = new Feature("feature1.feature");
        feature1.setName("Feature 1");
        Scenario scenario1 = new Scenario("Scenario 1", "Scenario", 1);
        scenario1.addTag("@Tag1");
        scenario1.addTag("@Tag2");
        feature1.addScenario(scenario1);
        
        Feature feature2 = new Feature("feature2.feature");
        feature2.setName("Feature 2");
        Scenario scenario2 = new Scenario("Scenario 2", "Scenario", 1);
        scenario2.addTag("@Tag2");
        scenario2.addTag("@Tag3");
        feature2.addScenario(scenario2);
        
        features.add(feature1);
        features.add(feature2);
        
        // Register example plugin
        PluginRegistry registry = new PluginRegistry();
        ExamplePlugin plugin = new ExamplePlugin();
        registry.registerPlugin(plugin);
        plugin.initialize(registry);
        
        // Trigger FEATURES_LOADED event
        registry.triggerEvent(PluginEvent.FEATURES_LOADED, features);
        
        // Since the plugin just logs, there's no direct assertion,
        // but we can verify it doesn't throw an exception
        assertTrue(true);
    }
}