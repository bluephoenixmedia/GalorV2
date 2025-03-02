package org.bpm.wizardsurvival.items;

public class Food extends Item {
    private float nutritionValue;
    private float healthValue;
    private boolean isCooked;
    private boolean isPreserved;

    public Food(String name, String description, float weight, float nutritionValue) {
        super(name, description, weight, 100.0f);
        this.nutritionValue = nutritionValue;
        this.healthValue = nutritionValue * 0.5f;
        this.isCooked = false;
        this.isPreserved = false;
    }

    public void cook() {
        if (!isCooked) {
            isCooked = true;
            nutritionValue *= 1.5f;
            healthValue *= 2.0f;
        }
    }

    public void preserve() {
        if (!isPreserved) {
            isPreserved = true;
            // Preserved food deteriorates slower
            setMaxDurability(getMaxDurability() * 3);
        }
    }

    private void setMaxDurability(float v) {
    }

    @Override
    public void use() {
        // Food is consumed by the player
    }

    // Getters
    public float getNutritionValue() { return nutritionValue; }
    public float getHealthValue() { return healthValue; }
    public boolean isCooked() { return isCooked; }
    public boolean isPreserved() { return isPreserved; }
}