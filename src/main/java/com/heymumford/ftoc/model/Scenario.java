package com.heymumford.ftoc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Cucumber scenario with its steps and metadata.
 */
public class Scenario {
    private final String name;
    private final String type; // "Scenario", "Scenario Outline", "Background", etc.
    private String description;
    private final List<String> tags;
    private final List<String> steps;
    private final int lineNumber;
    private final List<Example> examples;

    public Scenario(String name, String type, int lineNumber) {
        this.name = name;
        this.type = type;
        this.lineNumber = lineNumber;
        this.tags = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.examples = new ArrayList<>();
        this.description = "";
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
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

    public void addStep(String step) {
        steps.add(step);
    }

    public List<String> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public void addExample(Example example) {
        examples.add(example);
    }

    public List<Example> getExamples() {
        return Collections.unmodifiableList(examples);
    }

    public boolean isOutline() {
        return "Scenario Outline".equals(type);
    }

    public boolean isBackground() {
        return "Background".equals(type);
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", lineNumber=" + lineNumber +
                ", tags=" + tags +
                ", steps=" + steps.size() +
                ", examples=" + examples.size() +
                '}';
    }

    /**
     * Static inner class representing Examples in a Scenario Outline
     */
    public static class Example {
        private final String name;
        private final List<String> headers;
        private final List<List<String>> rows;

        public Example(String name) {
            this.name = name;
            this.headers = new ArrayList<>();
            this.rows = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setHeaders(List<String> headers) {
            this.headers.clear();
            this.headers.addAll(headers);
        }

        public List<String> getHeaders() {
            return Collections.unmodifiableList(headers);
        }

        public void addRow(List<String> row) {
            rows.add(new ArrayList<>(row));
        }

        public List<List<String>> getRows() {
            return Collections.unmodifiableList(rows);
        }

        @Override
        public String toString() {
            return "Example{" +
                    "name='" + name + '\'' +
                    ", headers=" + headers +
                    ", rows=" + rows.size() +
                    '}';
        }
    }
}