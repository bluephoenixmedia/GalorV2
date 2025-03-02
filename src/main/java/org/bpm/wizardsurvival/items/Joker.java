package org.bpm.wizardsurvival.items;

import org.bpm.wizardsurvival.engine.SurvivalActivity;
import org.bpm.wizardsurvival.entities.Player;

public abstract class Joker {
    private String name;
    private String description;
    private int duration; // In game days
    private int originalDuration;

    public Joker(String name, String description, int duration) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.originalDuration = duration;
    }

    public abstract float modifySuccessRate(SurvivalActivity activity, float baseRate);

    public void onActivation(Player player) {
        // Effects when joker is activated
    }

    public void onExpiration(Player player) {
        // Effects when joker expires
    }

    public void decrementDuration() {
        duration--;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDuration() { return duration; }
    public int getOriginalDuration() { return originalDuration; }
}
