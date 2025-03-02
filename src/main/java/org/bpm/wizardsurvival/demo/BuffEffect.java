package org.bpm.wizardsurvival.demo;

enum BuffEffect {
    FISHING_CHANCE("Fishing success chance"),
    FISHING_QUANTITY("Fish quantity"),
    BUILDING_COST("Building material cost"),
    BUILDING_STRENGTH("Raft durability"),
    WATER_QUALITY("Water purification"),
    WATER_QUANTITY("Water collection amount"),
    MATERIAL_CHANCE("Material finding chance"),
    MATERIAL_QUANTITY("Material quantity"),
    REST_RECOVERY("Rest recovery amount"),
    STORM_RESISTANCE("Storm damage resistance"),
    CARD_DRAW("Extra card draw"),
    INVENTORY_SPACE("Inventory capacity");

    private final String description;

    BuffEffect(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
