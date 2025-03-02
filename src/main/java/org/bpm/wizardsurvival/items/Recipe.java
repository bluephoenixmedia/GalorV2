package org.bpm.wizardsurvival.items;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Recipe {
    private String name;
    private Map<String, Integer> ingredients;
    private Item result;
    private float difficulty;

    public Recipe(String name, Item result, float difficulty) {
        this.name = name;
        this.ingredients = new HashMap<>();
        this.result = result;
        this.difficulty = difficulty;
    }

    public void addIngredient(String itemName, int count) {
        ingredients.put(itemName, count);
    }

    public boolean canCraft(List<Item> availableItems) {
        Map<String, Integer> inventory = new HashMap<>();

        // Count available items
        for (Item item : availableItems) {
            String itemName = item.getName();
            inventory.put(itemName, inventory.getOrDefault(itemName, 0) + 1);
        }

        // Check if we have all required ingredients
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            String ingredient = entry.getKey();
            int required = entry.getValue();
            int available = inventory.getOrDefault(ingredient, 0);

            if (available < required) {
                return false;
            }
        }

        return true;
    }

    // Getters
    public String getName() { return name; }
    public Map<String, Integer> getIngredients() { return ingredients; }
    public Item getResult() { return result; }
    public float getDifficulty() { return difficulty; }
}
