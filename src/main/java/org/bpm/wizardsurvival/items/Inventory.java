package org.bpm.wizardsurvival.items;

import java.util.List;
import java.util.ArrayList;

public class Inventory {
    private List<Item> items;
    private int capacity;

    public Inventory(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>(capacity);
    }

    public boolean addItem(Item item) {
        if (items.size() < capacity) {
            return items.add(item);
        }
        return false;
    }

    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    public Item getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }

    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    public int getCapacity() {
        return capacity;
    }

    public void increaseCapacity(int amount) {
        this.capacity += amount;
    }
}