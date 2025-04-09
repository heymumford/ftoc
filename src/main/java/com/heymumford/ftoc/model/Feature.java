package com.heymumford.ftoc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Cucumber feature file with its scenarios and metadata.
 */
public class Feature {
    private final String file;
    private String name;
    private String description;
    private final List<String> tags;
    private final List<Scenario> scenarios;

    public Feature(String file) {
        this.file = file;
        this.tags = new ArrayList<>();
        this.scenarios = new ArrayList<>();
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

    public String getFilename() {
        return file.substring(file.lastIndexOf('/') + 1);
    }

    @Override
    public String toString() {
        return "Feature{" +
                "file='" + file + '\'' +
                ", name='" + name + '\'' +
                ", scenarios=" + scenarios.size() +
                ", tags=" + tags +
                '}';
    }
}