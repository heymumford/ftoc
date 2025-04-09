package com.heymumford.ftoc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a Cucumber feature file with its scenarios and metadata.
 */
public class Feature {
    private final String file;
    private String name;
    private String description;
    private final List<String> tags;
    private final List<Scenario> scenarios;
    private final java.util.Map<String, String> metadata;

    public Feature(String file) {
        this.file = file;
        this.tags = new ArrayList<>();
        this.scenarios = new ArrayList<>();
        this.metadata = new java.util.HashMap<>();
        this.name = "Unnamed Feature";
        this.description = "";
    }

    public String getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public void addScenario(Scenario scenario) {
        scenarios.add(scenario);
    }

    public List<Scenario> getScenarios() {
        return Collections.unmodifiableList(scenarios);
    }
    
    /**
     * Get scenarios filtered by tags.
     * 
     * @param includeTags Scenarios must have at least one of these tags to be included (empty list means include all)
     * @param excludeTags Scenarios with any of these tags will be excluded
     * @return A filtered list of scenarios
     */
    public List<Scenario> getFilteredScenarios(List<String> includeTags, List<String> excludeTags) {
        return scenarios.stream()
                .filter(scenario -> {
                    // Check exclude tags first - if any match, exclude the scenario
                    if (!excludeTags.isEmpty() && 
                        scenario.getTags().stream().anyMatch(excludeTags::contains)) {
                        return false;
                    }
                    
                    // If no include tags are specified, include all scenarios (that weren't excluded)
                    if (includeTags.isEmpty()) {
                        return true;
                    }
                    
                    // Include scenarios that have at least one of the include tags
                    return scenario.getTags().stream().anyMatch(includeTags::contains);
                })
                .collect(Collectors.toList());
    }

    public String getFilename() {
        return file.substring(file.lastIndexOf('/') + 1);
    }

    /**
     * Add metadata to the feature.
     * 
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    
    /**
     * Get a metadata value.
     * 
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Check if metadata exists for a key.
     * 
     * @param key The metadata key
     * @return True if metadata exists for the key, false otherwise
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    /**
     * Get all metadata as a map.
     * 
     * @return An unmodifiable map of metadata
     */
    public java.util.Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
    
    @Override
    public String toString() {
        return "Feature{" +
                "file='" + file + '\'' +
                ", name='" + name + '\'' +
                ", scenarios=" + scenarios.size() +
                ", tags=" + tags +
                ", metadata=" + metadata +
                '}';
    }
}