package org.bpm.wizardsurvival.demo;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class OceanGameFactory implements EntityFactory {

    private static final int TILE_SIZE = 32;

    @Spawns("player")
    public Entity spawnPlayer(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.PLAYER)
                .viewWithBBox(new Rectangle(TILE_SIZE, TILE_SIZE, Color.DARKRED))
                .buildAndAttach();
    }

    @Spawns("raftTile")
    public Entity spawnRaftTile(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.RAFT_TILE)
                .viewWithBBox(new Rectangle(TILE_SIZE, TILE_SIZE, Color.BROWN))
                .buildAndAttach();
    }

    @Spawns("waterTile")
    public Entity spawnWaterTile(SpawnData data) {
        int variation = data.get("variation");
        Color waterColor;

        // Different shades of blue for water variations
        switch (variation) {
            case 0:
                waterColor = Color.rgb(0, 119, 190);
                break;
            case 1:
                waterColor = Color.rgb(0, 105, 175);
                break;
            case 2:
                waterColor = Color.rgb(0, 90, 160);
                break;
            default:
                waterColor = Color.BLUE;
        }

        return FXGL.entityBuilder(data)
                .type(EntityType.WATER_TILE)
                .viewWithBBox(new Rectangle(TILE_SIZE, TILE_SIZE, waterColor))
                .buildAndAttach();
    }
}