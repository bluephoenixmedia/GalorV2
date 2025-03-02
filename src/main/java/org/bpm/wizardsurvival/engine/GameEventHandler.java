package org.bpm.wizardsurvival.engine;

import org.bpm.wizardsurvival.world.Weather;

public class GameEventHandler implements GameEngine.GameEventListener {
    @Override
    public void onGameStarted() { /* Implementation */ }

    @Override
    public void onGamePaused() { /* Implementation */ }

    @Override
    public void onGameResumed() {

    }

    @Override
    public void onGameError(Exception e) {

    }

    @Override
    public void onPlayerDeath() {

    }

    @Override
    public void onReachedWizardSchool() {

    }

    @Override
    public void onDayAdvanced(int day) {

    }

    @Override
    public void onWeatherChanged(Weather newWeather) {

    }

    @Override
    public void onTrialRestarted(GameDifficulty newDifficulty) {

    }

    @Override
    public void onAttackSchoolInitiated() {

    }

    // Implement other methods...
}
