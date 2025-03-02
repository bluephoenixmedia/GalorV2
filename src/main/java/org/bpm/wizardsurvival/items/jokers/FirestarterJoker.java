package org.bpm.wizardsurvival.items.jokers;

import org.bpm.wizardsurvival.engine.SurvivalActivity;
import org.bpm.wizardsurvival.items.Joker;

public class FirestarterJoker extends Joker {
    public FirestarterJoker() {
        super("Firestarter", "Increases fire starting success rate by 300%", 3);
    }

    @Override
    public float modifySuccessRate(SurvivalActivity activity, float baseRate) {
        if (activity == SurvivalActivity.START_FIRE) {
            return baseRate * 4.0f; // +300% boost
        }
        return baseRate;
    }
}
