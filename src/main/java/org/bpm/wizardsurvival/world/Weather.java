package org.bpm.wizardsurvival.world;

import org.bpm.wizardsurvival.engine.SurvivalActivity;

public enum Weather {
    CLEAR,
    SUNNY,
    CLOUDY,
    RAIN,
    STORM;

    public float modifySuccessRate(SurvivalActivity activity, float baseRate) {
        switch (this) {
            case CLEAR:
                return baseRate * 1.1f;
            case SUNNY:
                return baseRate * 1.2f;
            case CLOUDY:
                return baseRate * 0.9f;
            case RAIN:
                return baseRate * 0.7f;
            case STORM:
                return baseRate * 0.5f;
            default:
                return baseRate;
        }
    }
}