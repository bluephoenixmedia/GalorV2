package org.bpm.wizardsurvival.demo;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Map;
import java.util.Random;

public class MainSceneApp extends GameApplication {

    // Game settings
    private static final int TILE_SIZE = 32;
    private static final int GRID_WIDTH = 25;
    private static final int GRID_HEIGHT = 20;
    private static final int UI_HEIGHT = 100;

    // Game state variables
    private Entity[][] grid = new Entity[GRID_WIDTH][GRID_HEIGHT];
    private Point2D raftCenter = new Point2D(GRID_WIDTH / 2, GRID_HEIGHT / 2);
    private Random random = new Random();
    private boolean isPlayerTurn = true;

    // Player stats
    private int health = 100;
    private int hunger = 100;
    private int thirst = 100;
    private int materials = 0;
    private int food = 0;
    private int water = 0;

    // UI elements
    private Text healthText;
    private Text hungerText;
    private Text thirstText;
    private Text materialsText;
    private Text foodText;
    private Text waterText;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(GRID_WIDTH * TILE_SIZE);
        settings.setHeight(GRID_HEIGHT * TILE_SIZE + UI_HEIGHT);
        settings.setTitle("Ocean Survival Roguelike");
        settings.setVersion("0.1");
    }

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new OceanGameFactory());

        // Initialize the grid with water tiles
        initializeGrid();

        // Create initial raft (3x3)
        createInitialRaft();
    }

    private void initializeGrid() {
        // Fill the grid with water tiles
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                spawnWaterTile(x, y);
            }
        }
    }

    private void createInitialRaft() {
        int centerX = (int) raftCenter.getX();
        int centerY = (int) raftCenter.getY();

        // Create a 3x3 raft centered at raftCenter
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY - 1; y <= centerY + 1; y++) {
                if (isValidGridPosition(x, y)) {
                    // Remove water tile if exists
                    if (grid[x][y] != null) {
                        grid[x][y].removeFromWorld();
                    }

                    // Spawn raft tile
                    grid[x][y] = FXGL.spawn("raftTile", new SpawnData(x * TILE_SIZE, y * TILE_SIZE));
                }
            }
        }

        // Spawn player at center
        FXGL.spawn("player", new SpawnData(centerX * TILE_SIZE, centerY * TILE_SIZE));
    }

    private void spawnWaterTile(int x, int y) {
        // Remove existing tile if any
        if (grid[x][y] != null) {
            grid[x][y].removeFromWorld();
        }

        // Create new water tile with random variation
        grid[x][y] = FXGL.spawn("waterTile", new SpawnData(x * TILE_SIZE, y * TILE_SIZE)
                .put("variation", random.nextInt(3)));
    }

    private boolean isValidGridPosition(int x, int y) {
        return x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT;
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        // Movement controls
        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    movePlayer(0, -1);
                }
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    movePlayer(0, 1);
                }
            }
        }, KeyCode.S);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    movePlayer(-1, 0);
                }
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    movePlayer(1, 0);
                }
            }
        }, KeyCode.D);

        // Action for resting/skipping turn
        input.addAction(new UserAction("Rest") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    endPlayerTurn();
                }
            }
        }, KeyCode.SPACE);

        // Action for building raft extension
        input.addAction(new UserAction("Build") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn && materials >= 5) {
                    buildRaftExtension();
                }
            }
        }, KeyCode.B);

        // Action for fishing
        input.addAction(new UserAction("Fish") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    fish();
                }
            }
        }, KeyCode.F);

        // Action for collecting water
        input.addAction(new UserAction("Collect Water") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    collectWater();
                }
            }
        }, KeyCode.C);
    }

    private void movePlayer(int dx, int dy) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        Point2D currentPos = player.getPosition();

        int gridX = (int) (currentPos.getX() / TILE_SIZE);
        int gridY = (int) (currentPos.getY() / TILE_SIZE);

        int newX = gridX + dx;
        int newY = gridY + dy;

        // Check if the new position is a valid raft tile
        if (isValidGridPosition(newX, newY) && isRaftTile(newX, newY)) {
            player.setPosition(newX * TILE_SIZE, newY * TILE_SIZE);
            endPlayerTurn();
        }
    }

    private boolean isRaftTile(int x, int y) {
        if (grid[x][y] == null) return false;
        return grid[x][y].getType() == EntityType.RAFT_TILE;
    }

    private void buildRaftExtension() {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        Point2D currentPos = player.getPosition();

        int gridX = (int) (currentPos.getX() / TILE_SIZE);
        int gridY = (int) (currentPos.getY() / TILE_SIZE);

        // Check adjacent tiles for building
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                // Skip current position
                if (dx == 0 && dy == 0) continue;

                int newX = gridX + dx;
                int newY = gridY + dy;

                // Check if position is valid and is a water tile
                if (isValidGridPosition(newX, newY) && !isRaftTile(newX, newY)) {
                    // Remove water tile
                    grid[newX][newY].removeFromWorld();

                    // Spawn raft tile
                    grid[newX][newY] = FXGL.spawn("raftTile",
                            new SpawnData(newX * TILE_SIZE, newY * TILE_SIZE));

                    // Deduct materials
                    materials -= 5;
                    updateUI();

                    endPlayerTurn();
                    return;
                }
            }
        }
    }

    private void fish() {
        // 40% chance to catch a fish
        if (random.nextDouble() < 0.4) {
            food += 1;
            FXGL.getNotificationService().pushNotification("Caught a fish!");
        } else {
            FXGL.getNotificationService().pushNotification("No fish caught...");
        }

        updateUI();
        endPlayerTurn();
    }

    private void collectWater() {
        water += 1;
        FXGL.getNotificationService().pushNotification("Collected water (needs purification)");
        updateUI();
        endPlayerTurn();
    }

    private void endPlayerTurn() {
        isPlayerTurn = false;

        // Update player stats
        hunger = Math.max(0, hunger - 2);
        thirst = Math.max(0, thirst - 3);

        if (hunger <= 0 || thirst <= 0) {
            health = Math.max(0, health - 5);
        }

        // Update ocean (move water tiles)
        updateOcean();

        // Check for random events
        checkRandomEvents();

        // Update UI
        updateUI();

        // Return to player turn
        isPlayerTurn = true;
    }

    private void updateOcean() {
        // Shift water tiles to create movement illusion
        int shiftDirection = random.nextInt(4); // 0=up, 1=right, 2=down, 3=left

        switch (shiftDirection) {
            case 0: // Up
                shiftOcean(0, -1);
                break;
            case 1: // Right
                shiftOcean(1, 0);
                break;
            case 2: // Down
                shiftOcean(0, 1);
                break;
            case 3: // Left
                shiftOcean(-1, 0);
                break;
        }
    }

    private void shiftOcean(int dx, int dy) {
        // Create new water tiles on the edge based on shift direction
        if (dx > 0) { // Right shift - new tiles on left edge
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (!isRaftTile(0, y)) {
                    spawnWaterTile(0, y);
                }
            }
        } else if (dx < 0) { // Left shift - new tiles on right edge
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (!isRaftTile(GRID_WIDTH - 1, y)) {
                    spawnWaterTile(GRID_WIDTH - 1, y);
                }
            }
        }

        if (dy > 0) { // Down shift - new tiles on top edge
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (!isRaftTile(x, 0)) {
                    spawnWaterTile(x, 0);
                }
            }
        } else if (dy < 0) { // Up shift - new tiles on bottom edge
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (!isRaftTile(x, GRID_HEIGHT - 1)) {
                    spawnWaterTile(x, GRID_HEIGHT - 1);
                }
            }
        }
    }

    private void checkRandomEvents() {
        double eventChance = 0.2; // 20% chance for random event

        if (random.nextDouble() < eventChance) {
            int eventType = random.nextInt(3);

            switch (eventType) {
                case 0: // Debris found (gain materials)
                    materials += 1 + random.nextInt(3);
                    FXGL.getNotificationService().pushNotification("Found some floating debris!");
                    break;
                case 1: // Storm (lose health)
                    int damage = 5 + random.nextInt(10);
                    health = Math.max(0, health - damage);
                    FXGL.getNotificationService().pushNotification("A storm hits your raft!");
                    break;
                case 2: // Seagull (chance to gain food)
                    if (random.nextBoolean()) {
                        food += 1;
                        FXGL.getNotificationService().pushNotification("A seagull landed on your raft. You caught it!");
                    } else {
                        FXGL.getNotificationService().pushNotification("A seagull flew by...");
                    }
                    break;
            }
        }
    }

    @Override
    protected void initUI() {
        // Create UI container at the bottom of the screen
        VBox uiContainer = new VBox(10);
        uiContainer.setTranslateY(GRID_HEIGHT * TILE_SIZE);
        uiContainer.setPrefWidth(GRID_WIDTH * TILE_SIZE);
        uiContainer.setPrefHeight(UI_HEIGHT);
        uiContainer.setStyle("-fx-background-color: #333333; -fx-padding: 10px;");

        // Create top row for stats
        HBox statsContainer = new HBox(20);

        // Create stats texts
        healthText = new Text("Health: " + health);
        healthText.setFill(Color.RED);
        healthText.setFont(Font.font(16));

        hungerText = new Text("Hunger: " + hunger);
        hungerText.setFill(Color.ORANGE);
        hungerText.setFont(Font.font(16));

        thirstText = new Text("Thirst: " + thirst);
        thirstText.setFill(Color.BLUE);
        thirstText.setFont(Font.font(16));

        materialsText = new Text("Materials: " + materials);
        materialsText.setFill(Color.BROWN);
        materialsText.setFont(Font.font(16));

        foodText = new Text("Food: " + food);
        foodText.setFill(Color.GREEN);
        foodText.setFont(Font.font(16));

        waterText = new Text("Water: " + water);
        waterText.setFill(Color.LIGHTBLUE);
        waterText.setFont(Font.font(16));

        statsContainer.getChildren().addAll(
                healthText, hungerText, thirstText, materialsText, foodText, waterText
        );

        // Create bottom row for buttons
        HBox buttonsContainer = new HBox(10);

        String[] actions = {"Move (WASD)", "Rest (Space)", "Build (B)", "Fish (F)", "Collect Water (C)"};
        for (String action : actions) {
            Rectangle buttonBg = new Rectangle(100, 30, Color.GRAY);
            Text buttonText = new Text(action);
            buttonText.setFill(Color.WHITE);
            buttonText.setTranslateX(5);
            buttonText.setTranslateY(20);

            Entity button = FXGL.entityBuilder()
                    .view(buttonBg)
                    .view(buttonText)
                    .buildAndAttach();

            buttonsContainer.getChildren().add(button.getViewComponent().getParent());
        }

        uiContainer.getChildren().addAll(statsContainer, buttonsContainer);
        FXGL.addUINode(uiContainer);
    }

    private void updateUI() {
        healthText.setText("Health: " + health);
        hungerText.setText("Hunger: " + hunger);
        thirstText.setText("Thirst: " + thirst);
        materialsText.setText("Materials: " + materials);
        foodText.setText("Food: " + food);
        waterText.setText("Water: " + water);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("health", health);
        vars.put("hunger", hunger);
        vars.put("thirst", thirst);
        vars.put("materials", materials);
        vars.put("food", food);
        vars.put("water", water);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
