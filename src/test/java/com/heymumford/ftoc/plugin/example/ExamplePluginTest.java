package com.heymumford.ftoc.plugin.example;

import com.heymumford.ftoc.model.Feature;
import com.heymumford.ftoc.model.Scenario;
import com.heymumford.ftoc.plugin.PluginEvent;
import com.heymumford.ftoc.plugin.PluginRegistry;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("Temporary disabled until JUnit issues are fixed")
    public void testPluginIdentity() {
        ExamplePlugin plugin = new ExamplePlugin();
        
        assertEquals("ExamplePlugin", plugin.getName());
        assertEquals("1.0.0", plugin.getVersion());
        assertNotNull(plugin.getDescription());
    }
    
    @Test
    @Disabled("Temporary disabled until JUnit issues are fixed")
    public void testPluginInitialization() {
        ExamplePlugin plugin = new ExamplePlugin();
        PluginRegistry registry = new PluginRegistry();
        
        // Should not throw any exceptions
        plugin.initialize(registry);
    }
    
    @Test
    @Disabled("Temporary disabled until JUnit issues are fixed")
    public void testFeatureProcessing() {
        ExamplePlugin plugin = new ExamplePlugin();
        PluginRegistry registry = new PluginRegistry();
        plugin.initialize(registry);
        
        // Create test features
        List<Feature> features = new ArrayList<>();
        
        Feature feature = new Feature("test.feature");
        feature.setName("Test Feature");
        Scenario scenario = new Scenario("Test Scenario", "Scenario", 1);
        scenario.addTag("@Tag1");
        scenario.addTag("@Tag2");
        feature.addScenario(scenario);
        
        features.add(feature);
        
        // Trigger FEATURES_LOADED event
        registry.triggerEvent(PluginEvent.FEATURES_LOADED, features);
        
        // No assertions, just verify it doesn't throw an exception
        assertTrue(true);
    }
}