package org.bpm.wizardsurvival.items;

import java.util.ArrayList;
import java.util.List;

public class Raft {
    private float integrity;
    private int size;
    private List<RaftUpgrade> upgrades;

    public Raft() {
        this.integrity = 100.0f;
        this.size = 1;
        this.upgrades = new ArrayList<>();
    }

    public void deteriorate(float amount) {
        integrity -= amount;
        if (integrity < 0) {
            integrity = 0;
        }
    }

    public void repair(float amount) {
        integrity += amount;
        if (integrity > 100.0f) {
            integrity = 100.0f;
        }
    }

    public void addUpgrade(RaftUpgrade upgrade) {
        upgrades.add(upgrade);
        upgrade.applyToRaft(this);
    }

    public void increaseSize() {
        size++;
    }

    // Getters
    public float getIntegrity() { return integrity; }
    public int getSize() { return size; }
    public List<RaftUpgrade> getUpgrades() { return upgrades; }
}
