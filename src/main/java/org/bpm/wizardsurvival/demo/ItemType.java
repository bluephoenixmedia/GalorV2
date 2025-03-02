package org.bpm.wizardsurvival.demo;

enum ItemType {
    FISHING_ROD("Fishing Rod", "Increases fishing success rate by 20%"),
    WATER_PURIFIER("Water Purifier", "Allows purifying water for safe drinking"),
    COMPASS("Compass", "Shows the direction to the nearest island"),
    SAIL("Sail", "Lets you control raft movement direction"),
    HARPOON("Harpoon", "Allows hunting larger sea creatures"),
    METAL_DETECTOR("Metal Detector", "Increases chance of finding materials"),
    RAIN_COLLECTOR("Rain Collector", "Automatically collects water during rain"),
    GRILL("Grill", "Cook food for better nutrition"),
    SPYGLASS("Spyglass", "Spot islands and events from further away"),
    STORAGE_BOX("Storage Box", "Increases your inventory capacity");

    private final String name;
    private final String description;

    ItemType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
