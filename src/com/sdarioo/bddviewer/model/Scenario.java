package com.sdarioo.bddviewer.model;


import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scenario implements LocationHolder {

    private final String name;
    private final Meta meta;
    private final Location location;
    private final List<Step> steps;
    private final Table examples;

    private Story story;

    Scenario(String name, Meta meta, Location location, List<Step> steps, Table examples) {
        this.name = name;
        this.meta = meta;
        this.location = location;
        this.steps = new ArrayList<>(steps);
        this.examples = examples;
    }

    public String getName() {
        return name;
    }

    public Meta getMeta() {
        return meta;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public Table getExamples() {
        return examples;
    }

    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public Story getStory() {
        return story;
    }

    void setStory(Story story) {
        this.story = story;
    }

    /**
     * @return whether this scenario can be automatically executed
     */
    public boolean isRunnable() {
        if (story == null) {
            return false;
        }
        return story.isRunnable() && !meta.isSkip();
    }

    /**
     * Read scenario content lines from story file
     * @return scenario lines
     */
    public List<String> readLines() throws IOException {
        List<String> allLines = Files.readAllLines(location.getPath());
        return allLines.subList(location.getStartLine() - 1, location.getEndLine());
    }

    @Override
    public String toString() {
        return name;
    }
}
