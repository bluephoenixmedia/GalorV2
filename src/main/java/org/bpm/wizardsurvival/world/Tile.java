package org.bpm.wizardsurvival.world;

import org.bpm.wizardsurvival.items.FloatingDebris;

import java.util.Random;

public class Tile {
    private TileType type;
    private OceanZone zone;
    private int depth;
    private java.util.List<FloatingDebris> debris;

    public Tile(TileType type, OceanZone zone) {
        this.type = type;
        this.zone = zone;
        this.depth = type == TileType.WATER ? 5 + new Random().nextInt(20) : 0;
        this.debris = new java.util.ArrayList<>();
    }

    public boolean isNavigable() {
        return type != TileType.BARRIER;
    }

    public boolean isDiveable() {
        return type == TileType.WATER && depth > 0;
    }

    public OceanZone getZone() {
        return zone;
    }

    // Getters and setters
}