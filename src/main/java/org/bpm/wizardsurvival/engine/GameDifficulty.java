package org.bpm.wizardsurvival.engine;

public enum GameDifficulty {
    EASY(1.5f),
    NORMAL(1.0f),
    HARD(0.7f),
    NIGHTMARE(0.4f);

    private final float successRateModifier;

    GameDifficulty(float modifier) {
        this.successRateModifier = modifier;
    }

    public float getSuccessRateModifier() {
        return successRateModifier;
    }

    public GameDifficulty increaseLevel() {
        switch (this) {
            case EASY: return NORMAL;
            case NORMAL: return HARD;
            case HARD: return NIGHTMARE;
            default: return NIGHTMARE;
        }
    }
}
