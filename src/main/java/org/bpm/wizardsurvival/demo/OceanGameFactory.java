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
                .viewWithBBox(new Rectangle(TILE_SIZE, TILE_SIZE, Color.WHITE))
                .buildAndAttach();
    }

    @Spawns("raftTile")
    public Entity spawnRaftTile(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.RAFT_TILE)
                .viewWithBBox(new Rectangle(TILE_SIZE, TILE_SIZE, Color.DARKORANGE))
                .buildAndAttach();
    }

    @Spawns("waterTile")
    public Entity spawnWaterTile(SpawnData data) {
        int variation = data.get("variation");
        Color waterColor;

        /*
          if (color.equals(Color.rgb(0, 119, 190))) return 0;
        else if (color.equals(Color.rgb(0, 105, 175))) return 1;
        else if (color.equals(Color.rgb(0, 90, 160))) return 2;
         */

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

    @Spawns("itemTile")
    public Entity spawnItemTile(SpawnData data) {
        ItemType itemType = data.get("itemType");
        Color itemColor;

        // Different colors for different items
        switch (itemType) {
            case FISHING_ROD:
                itemColor = Color.BROWN;
                break;
            case WATER_PURIFIER:
                itemColor = Color.LIGHTBLUE;
                break;
            case COMPASS:
                itemColor = Color.GOLD;
                break;
            case SAIL:
                itemColor = Color.WHITE;
                break;
            case HARPOON:
                itemColor = Color.SILVER;
                break;
            case METAL_DETECTOR:
                itemColor = Color.GRAY;
                break;
            case RAIN_COLLECTOR:
                itemColor = Color.TEAL;
                break;
            case GRILL:
                itemColor = Color.ORANGE;
                break;
            case SPYGLASS:
                itemColor = Color.DARKGOLDENROD;
                break;
            case STORAGE_BOX:
                itemColor = Color.SADDLEBROWN;
                break;
            default:
                itemColor = Color.PURPLE;
                break;
        }

        return FXGL.entityBuilder(data)
                .type(EntityType.ITEM)
                .viewWithBBox(new Rectangle(TILE_SIZE, TILE_SIZE, itemColor))
                .with("itemType", itemType)
                .buildAndAttach();
    }
}