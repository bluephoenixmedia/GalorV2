package org.bpm.wizardsurvival.magic;

import org.bpm.wizardsurvival.entities.Player;

public abstract class Spell {
    private String name;
    private String description;
    private float manaCost;
    private float cooldown; // In game hours
    private float remainingCooldown;

    public Spell(String name, String description, float manaCost, float cooldown) {
        this.name = name;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.remainingCooldown = 0;
    }

    public abstract boolean cast(Player caster);

    public void decrementCooldown(float hours) {
        remainingCooldown -= hours;
        if (remainingCooldown < 0) {
            remainingCooldown = 0;
        }
    }

    public boolean isOnCooldown() {
        return remainingCooldown > 0;
    }

    protected void resetCooldown() {
        remainingCooldown = cooldown;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getManaCost() { return manaCost; }
    public float getCooldown() { return cooldown; }
    public float getRemainingCooldown() { return remainingCooldown; }
}