package com.heymumford.ftoc.plugin;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import com.heymumford.ftoc.plugin.example.ExamplePlugin;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

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
        
        Feature feature1 = new Feature();
        feature1.setName("Feature 1");
        List<Scenario> scenarios1 = new ArrayList<>();
        Scenario scenario1 = new Scenario();
        scenario1.setName("Scenario 1");
        scenario1.setTags(Arrays.asList("@Tag1", "@Tag2"));
        scenarios1.add(scenario1);
        feature1.setScenarios(scenarios1);
        
        Feature feature2 = new Feature();
        feature2.setName("Feature 2");
        List<Scenario> scenarios2 = new ArrayList<>();
        Scenario scenario2 = new Scenario();
        scenario2.setName("Scenario 2");
        scenario2.setTags(Arrays.asList("@Tag2", "@Tag3"));
        scenarios2.add(scenario2);
        feature2.setScenarios(scenarios2);
        
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