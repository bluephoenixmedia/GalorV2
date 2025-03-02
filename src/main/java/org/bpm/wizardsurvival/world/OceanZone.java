package org.bpm.wizardsurvival.world;

import java.util.Random;

public enum OceanZone {
    PEACEFUL,
    NEUTRAL,
    HAUNTED,
    STORMY,
    ABUNDANT,
    BARREN;

    public Weather generateWeather(Random rng, Weather current) {
        // Different zones have different weather patterns
        switch (this) {
            case PEACEFUL:
                return Weather.values()[rng.nextInt(3)]; // Only nice weather
            case STORMY:
                return Weather.values()[2 + rng.nextInt(3)]; // Only bad weather
            default:
                // 30% chance to change weather
                if (rng.nextFloat() < 0.3f) {
                    return Weather.values()[rng.nextInt(Weather.values().length)];
                }
                return current;
        }
    }
}