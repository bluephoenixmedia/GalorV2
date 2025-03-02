package org.bpm.wizardsurvival.items;

public abstract class RaftUpgrade {
    private String name;
    private String description;

    public RaftUpgrade(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public abstract void applyToRaft(Raft raft);

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
}
