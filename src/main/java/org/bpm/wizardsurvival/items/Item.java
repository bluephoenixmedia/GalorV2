package org.bpm.wizardsurvival.items;

public abstract class Item {
    private String name;
    private String description;
    private float weight;
    private float durability;
    private float maxDurability;

    public Item(String name, String description, float weight, float durability) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.durability = durability;
        this.maxDurability = durability;
    }

    public void deteriorate(float amount) {
        durability -= amount;
        if (durability < 0) {
            durability = 0;
        }
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    public abstract void use();

    // Getters and setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getWeight() { return weight; }
    public float getDurability() { return durability; }
    public float getMaxDurability() { return maxDurability; }
}
