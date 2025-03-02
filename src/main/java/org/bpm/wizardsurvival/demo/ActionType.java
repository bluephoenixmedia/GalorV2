package org.bpm.wizardsurvival.demo;

public enum ActionType {
    FISH("Fish for food"),
    BUILD("Build raft extension"),
    COLLECT_WATER("Collect water"),
    SCAVENGE("Scavenge for materials"),
    REST("Rest and recover"),
    REPAIR("Repair raft"),
    CRAFT("Craft items"),
    NAVIGATE("Change raft direction");

    private final String description;

    ActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
