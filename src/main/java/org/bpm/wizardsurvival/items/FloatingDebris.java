package org.bpm.wizardsurvival.items;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FloatingDebris {
    private String name;
    private List<Item> containedItems;
    private Point position;
    private boolean isVisible;

    public FloatingDebris(String name, Point position) {
        this.name = name;
        this.position = position;
        this.containedItems = new ArrayList<>();
        this.isVisible = true;
    }

    public void addItem(Item item) {
        containedItems.add(item);
    }

    public List<Item> collect() {
        List<Item> items = new ArrayList<>(containedItems);
        containedItems.clear();
        isVisible = false;
        return items;
    }

    // Getters
    public String getName() { return name; }
    public Point getPosition() { return position; }
    public boolean isVisible() { return isVisible; }
}