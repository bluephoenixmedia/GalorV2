package org.bpm.wizardsurvival.demo;

import javafx.scene.paint.Color;

enum WeatherType {
    CLEAR(Color.TRANSPARENT, 0.0, "Clear skies"),
    CLOUDY(Color.color(0.5, 0.5, 0.5, 0.3), 0.1, "Cloudy"),
    RAINY(Color.color(0.2, 0.2, 0.8, 0.4), 0.2, "Rainy"),
    STORMY(Color.color(0.1, 0.1, 0.3, 0.5), 0.4, "Stormy"),
    FOG(Color.color(0.8, 0.8, 0.8, 0.6), 0.3, "Foggy");

    private final Color overlay;
    private final double damageMultiplier;
    private final String description;

    WeatherType(Color overlay, double damageMultiplier, String description) {
        this.overlay = overlay;
        this.damageMultiplier = damageMultiplier;
        this.description = description;
    }

    public Color getOverlay() {
        return overlay;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public String getDescription() {
        return description;
    }
}
