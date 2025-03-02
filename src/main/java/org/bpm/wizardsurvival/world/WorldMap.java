package org.bpm.wizardsurvival.world;

import java.util.Random;
import java.awt.Point;

public class WorldMap {
    private Tile[][] tiles;
    private int width;
    private int height;
    private Point schoolLocation;

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
    }

    public Tile getTileAt(Point position) {
        int x = Math.min(Math.max(position.x, 0), width - 1);
        int y = Math.min(Math.max(position.y, 0), height - 1);
        return tiles[x][y];
    }

    public OceanZone getZoneAt(Point position) {
        return getTileAt(position).getZone();
    }

    public void update() {
        // Update dynamic elements like floating debris
        generateDebris();
    }

    private void generateDebris() {
        // Random chance to spawn new debris
    }

    public boolean isAtWizardSchool(Point position) {
        return position.equals(schoolLocation);
    }

    public Object getWizardSchoolPosition() {
        Object position = null;
        return position;
    }

    // More methods as needed
}