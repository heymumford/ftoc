package com.heymumford.ftoc.plugin.example;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import com.heymumford.ftoc.plugin.PluginEvent;
import com.heymumford.ftoc.plugin.PluginRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ExamplePlugin class.
 */
public class ExamplePluginTest {
    
    @Test
    public void testPluginIdentity() {
        ExamplePlugin plugin = new ExamplePlugin();
        
        assertEquals("ExamplePlugin", plugin.getName());
        assertEquals("1.0.0", plugin.getVersion());
        assertNotNull(plugin.getDescription());
    }
    
    @Test
    public void testPluginInitialization() {
        ExamplePlugin plugin = new ExamplePlugin();
        PluginRegistry registry = new PluginRegistry();
        
        // Should not throw any exceptions
        plugin.initialize(registry);
    }
    
    @Test
    public void testFeatureProcessing() {
        ExamplePlugin plugin = new ExamplePlugin();
        PluginRegistry registry = new PluginRegistry();
        plugin.initialize(registry);
        
        // Create test features
        List<Feature> features = new ArrayList<>();
        
        Feature feature = new Feature();
        feature.setName("Test Feature");
        List<Scenario> scenarios = new ArrayList<>();
        Scenario scenario = new Scenario();
        scenario.setName("Test Scenario");
        scenario.setTags(Arrays.asList("@Tag1", "@Tag2"));
        scenarios.add(scenario);
        feature.setScenarios(scenarios);
        
        features.add(feature);
        
        // Trigger FEATURES_LOADED event
        registry.triggerEvent(PluginEvent.FEATURES_LOADED, features);
        
        // No assertions, just verify it doesn't throw an exception
        assertTrue(true);
    }
}