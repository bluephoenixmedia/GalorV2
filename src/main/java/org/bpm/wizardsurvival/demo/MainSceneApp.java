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
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

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

    private java.util.List<ItemType> inventory = new java.util.ArrayList<>();
    private java.util.Map<ItemType, Integer> itemCounts = new java.util.HashMap<>();
    private Text dayTimeText;
    private Text weatherText;

    // UI elements
    private Text healthText;
    private Text hungerText;
    private Text thirstText;
    private Text materialsText;
    private Text foodText;
    private Text waterText;

    private VBox cardHandContainer;
    private HBox activeBuffsContainer;
    private Text cardsLeftText;
    private Text activeBuffsText;

    private int currentDay = 1;
    private int timeOfDay = 0; // 0-23 hours
    private WeatherType currentWeather = WeatherType.CLEAR;
    private int weatherDuration = 0;
    private Color skyColor = Color.SKYBLUE;
    private Rectangle skyOverlay;

    private java.util.List<Card> deck = new java.util.ArrayList<>();
    private java.util.List<Card> hand = new java.util.ArrayList<>();
    private java.util.List<Card> discardPile = new java.util.ArrayList<>();
    private java.util.List<Card> activeBuffs = new java.util.ArrayList<>();
    private int handSize = 5;
    private int maxHandSize = 7;
    private int deckSize = 0;
    private int cardsPlayed = 0;
    private int maxCardsPerTurn = 3;

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

        //should this be here?
        initUI();

        //initialize deck
        initializeDeck();

        skyOverlay = new Rectangle(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE, Color.TRANSPARENT);
        FXGL.addUINode(skyOverlay);


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

        // Add to the initInput method
// Action for drinking water
        input.addAction(new UserAction("Drink Water") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    drinkWater();
                }
            }
        }, KeyCode.Z);

// Action for eating food
        input.addAction(new UserAction("Eat Food") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    eatFood();
                }
            }
        }, KeyCode.E);

// Action for collecting items
        input.addAction(new UserAction("Collect Item") {
            @Override
            protected void onActionBegin() {
                if (isPlayerTurn) {
                    Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
                    Point2D playerPos = player.getPosition();

                    int gridX = (int) (playerPos.getX() / TILE_SIZE);
                    int gridY = (int) (playerPos.getY() / TILE_SIZE);

                    // Check adjacent tiles for items
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int newX = gridX + dx;
                            int newY = gridY + dy;

                            if (isValidGridPosition(newX, newY) &&
                                    grid[newX][newY] != null &&
                                    grid[newX][newY].getType() == EntityType.ITEM) {
                                collectItem(newX, newY);
                                return;
                            }
                        }
                    }

                    FXGL.getNotificationService().pushNotification("No items nearby to collect.");
                }
            }
        }, KeyCode.G);

        input.addAction(new UserAction("Show Inventory") {
            @Override
            protected void onActionBegin() {
                showInventory();
            }
        }, KeyCode.I);
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
        // Higher chance with fishing rod
        double fishChance = hasItem(ItemType.FISHING_ROD) ? 0.6 : 0.4;

        // Higher chance during dawn/dusk
        if ((timeOfDay >= 5 && timeOfDay < 8) || (timeOfDay >= 18 && timeOfDay < 21)) {
            fishChance += 0.1;
        }

        if (random.nextDouble() < fishChance) {
            food += 1;
            FXGL.getNotificationService().pushNotification("Caught a fish!");

            // Chance for bonus materials with harpoon
            if (hasItem(ItemType.HARPOON) && random.nextDouble() < 0.3) {
                materials += 1;
                FXGL.getNotificationService().pushNotification("Harvested some materials from the catch!");
            }
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
        hunger = Math.max(0, hunger - 1);
        thirst = Math.max(0, thirst - 1);

        if (hunger <= 0 || thirst <= 0) {
            health = Math.max(0, health - 5);
        }

        // Update day/night cycle
        updateDayNightCycle();

        // Update weather
        updateWeather();

        // Update ocean (move water tiles)
        updateOcean();

        // Check for random events
        checkRandomEvents();

        // Check for items
        checkForItems();

        // Update UI
        updateUI();

        // Return to player turn
        isPlayerTurn = true;
    }

    private void updateOcean() {
        // Shift water tiles to create movement illusion
        // TODO make this dependent on wind/ storm
       // int shiftDirection = random.nextInt(4); // 0=up, 1=right, 2=down, 3=left
        int shiftDirection = 1;

        switch (shiftDirection) {
            case 0: // Up
                shiftOceanUp();
                break;
            case 1: // Right
                shiftOceanRight();
                break;
            case 2: // Down
                shiftOceanDown();
                break;
            case 3: // Left
                shiftOceanLeft();
                break;
        }
    }

    private void shiftOceanUp() {
        // Start from the top row and move downward
        // This way we don't overwrite tiles we haven't processed yet
        for (int y = 0; y < GRID_HEIGHT - 1; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                // Skip raft tiles
                if (!isRaftTile(x, y)) {
                    // Move the tile from below to current position
                    if (grid[x][y + 1] != null && !isRaftTile(x, y + 1)) {
                        // Get the water variation from the tile below
                        Entity belowTile = grid[x][y + 1];
                        int variation = 0;
                        if (belowTile.getType() == EntityType.WATER_TILE) {
                            Rectangle view = (Rectangle)belowTile.getViewComponent().getChildren().get(0);
                            Color color = (Color)view.getFill();
                            // Determine variation from color
                            if (color.equals(Color.rgb(0, 119, 190))) variation = 0;
                            else if (color.equals(Color.rgb(0, 105, 175))) variation = 1;
                            else if (color.equals(Color.rgb(0, 90, 160))) variation = 2;
                        }

                        // Remove current tile
                        if (grid[x][y] != null) {
                            grid[x][y].removeFromWorld();
                        }

                        // Spawn new water tile with variation
                        grid[x][y] = FXGL.spawn("waterTile", new SpawnData(x * TILE_SIZE, y * TILE_SIZE)
                                .put("variation", variation));
                    }
                }
            }
        }

        // Create new tiles at the bottom edge
        for (int x = 0; x < GRID_WIDTH; x++) {
            if (!isRaftTile(x, GRID_HEIGHT - 1)) {
                if (grid[x][GRID_HEIGHT - 1] != null) {
                    grid[x][GRID_HEIGHT - 1].removeFromWorld();
                }
                grid[x][GRID_HEIGHT - 1] = FXGL.spawn("waterTile",
                        new SpawnData(x * TILE_SIZE, (GRID_HEIGHT - 1) * TILE_SIZE)
                                .put("variation", random.nextInt(3)));
            }
        }
    }

    private void shiftOceanDown() {
        // Start from the bottom row and move upward
        for (int y = GRID_HEIGHT - 1; y > 0; y--) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                // Skip raft tiles
                if (!isRaftTile(x, y)) {
                    // Move the tile from above to current position
                    if (grid[x][y - 1] != null && !isRaftTile(x, y - 1)) {
                        // Get the water variation from the tile above
                        Entity aboveTile = grid[x][y - 1];
                        int variation = 0;
                        if (aboveTile.getType() == EntityType.WATER_TILE) {
                            Rectangle view = (Rectangle)aboveTile.getViewComponent().getChildren().get(0);
                            Color color = (Color)view.getFill();
                            // Determine variation from color
                            if (color.equals(Color.rgb(0, 119, 190))) variation = 0;
                            else if (color.equals(Color.rgb(0, 105, 175))) variation = 1;
                            else if (color.equals(Color.rgb(0, 90, 160))) variation = 2;
                        }

                        // Remove current tile
                        if (grid[x][y] != null) {
                            grid[x][y].removeFromWorld();
                        }

                        // Spawn new water tile with variation
                        grid[x][y] = FXGL.spawn("waterTile", new SpawnData(x * TILE_SIZE, y * TILE_SIZE)
                                .put("variation", variation));
                    }
                }
            }
        }

        // Create new tiles at the top edge
        for (int x = 0; x < GRID_WIDTH; x++) {
            if (!isRaftTile(x, 0)) {
                if (grid[x][0] != null) {
                    grid[x][0].removeFromWorld();
                }
                grid[x][0] = FXGL.spawn("waterTile",
                        new SpawnData(x * TILE_SIZE, 0)
                                .put("variation", random.nextInt(3)));
            }
        }
    }

    private void shiftOceanLeft() {
        // Start from the leftmost column and move right
        for (int x = 0; x < GRID_WIDTH - 1; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                // Skip raft tiles
                if (!isRaftTile(x, y)) {
                    // Move the tile from right to current position
                    if (grid[x + 1][y] != null && !isRaftTile(x + 1, y)) {
                        // Get the water variation from the tile to the right
                        Entity rightTile = grid[x + 1][y];
                        int variation = 0;
                        if (rightTile.getType() == EntityType.WATER_TILE) {
                            Rectangle view = (Rectangle)rightTile.getViewComponent().getChildren().get(0);
                            Color color = (Color)view.getFill();
                            // Determine variation from color
                            if (color.equals(Color.rgb(0, 119, 190))) variation = 0;
                            else if (color.equals(Color.rgb(0, 105, 175))) variation = 1;
                            else if (color.equals(Color.rgb(0, 90, 160))) variation = 2;
                        }

                        // Remove current tile
                        if (grid[x][y] != null) {
                            grid[x][y].removeFromWorld();
                        }

                        // Spawn new water tile with variation
                        grid[x][y] = FXGL.spawn("waterTile", new SpawnData(x * TILE_SIZE, y * TILE_SIZE)
                                .put("variation", variation));
                    }
                }
            }
        }

        // Create new tiles at the right edge
        for (int y = 0; y < GRID_HEIGHT; y++) {
            if (!isRaftTile(GRID_WIDTH - 1, y)) {
                if (grid[GRID_WIDTH - 1][y] != null) {
                    grid[GRID_WIDTH - 1][y].removeFromWorld();
                }
                grid[GRID_WIDTH - 1][y] = FXGL.spawn("waterTile",
                        new SpawnData((GRID_WIDTH - 1) * TILE_SIZE, y * TILE_SIZE)
                                .put("variation", random.nextInt(3)));
            }
        }
    }

    private void shiftOceanRight() {
        // Start from the rightmost column and move left
        for (int x = GRID_WIDTH - 1; x > 0; x--) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                // Skip raft tiles
                if (!isRaftTile(x, y)) {
                    // Move the tile from left to current position
                    if (grid[x - 1][y] != null && !isRaftTile(x - 1, y)) {
                        // Get the water variation from the tile to the left
                        Entity leftTile = grid[x - 1][y];
                        int variation = 0;
                        if (leftTile.getType() == EntityType.WATER_TILE) {
                            Rectangle view = (Rectangle)leftTile.getViewComponent().getChildren().get(0);
                            Color color = (Color)view.getFill();
                            // Determine variation from color
                            if (color.equals(Color.rgb(0, 119, 190))) variation = 0;
                            else if (color.equals(Color.rgb(0, 105, 175))) variation = 1;
                            else if (color.equals(Color.rgb(0, 90, 160))) variation = 2;
                        }

                        // Remove current tile
                        if (grid[x][y] != null) {
                            grid[x][y].removeFromWorld();
                        }

                        // Spawn new water tile with variation
                        grid[x][y] = FXGL.spawn("waterTile", new SpawnData(x * TILE_SIZE, y * TILE_SIZE)
                                .put("variation", variation));
                    }
                }
            }
        }

        // Create new tiles at the left edge
        for (int y = 0; y < GRID_HEIGHT; y++) {
            if (!isRaftTile(0, y)) {
                if (grid[0][y] != null) {
                    grid[0][y].removeFromWorld();
                }
                grid[0][y] = FXGL.spawn("waterTile",
                        new SpawnData(0, y * TILE_SIZE)
                                .put("variation", random.nextInt(3)));
            }
        }
    }

    // You may also want to add this helper method to extract the variation from a water tile
    private int getWaterVariation(Entity waterTile) {
        if (waterTile != null && waterTile.getType() == EntityType.WATER_TILE) {
            Rectangle view = (Rectangle)waterTile.getViewComponent().getChildren().get(0);
            Color color = (Color)view.getFill();

            if (color.equals(Color.rgb(0, 119, 190))) return 0;
            else if (color.equals(Color.rgb(0, 105, 175))) return 1;
            else if (color.equals(Color.rgb(0, 90, 160))) return 2;
        }
        return random.nextInt(3);
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
            int eventType = random.nextInt(4);

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
                case 3: //find card
            }       offerCardReward();
                    FXGL.getNotificationService().pushNotification("Found card!");
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

        initCardUI();

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

        dayTimeText = new Text("Day: " + currentDay + " | Time: " + formatTime(timeOfDay));
        dayTimeText.setFill(Color.WHITE);
        dayTimeText.setFont(Font.font(16));

        weatherText = new Text("Weather: " + currentWeather.getDescription());
        weatherText.setFill(Color.WHITE);
        weatherText.setFont(Font.font(16));

        statsContainer.getChildren().addAll(dayTimeText, weatherText);

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
        dayTimeText.setText("Day: " + currentDay + " | Time: " + formatTime(timeOfDay));
        weatherText.setText("Weather: " + currentWeather.getDescription());
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

    private void updateDayNightCycle() {
        timeOfDay = (timeOfDay + 1) % 24;

        // Update sky color based on time of day
        if (timeOfDay >= 5 && timeOfDay < 8) {
            // Dawn
            skyColor = Color.color(0.8, 0.6, 0.6, 0.2);
        } else if (timeOfDay >= 8 && timeOfDay < 18) {
            // Day
            skyColor = Color.TRANSPARENT;
        } else if (timeOfDay >= 18 && timeOfDay < 21) {
            // Dusk
            skyColor = Color.color(0.6, 0.4, 0.6, 0.3);
        } else {
            // Night
            skyColor = Color.color(0.1, 0.1, 0.3, 0.5);
        }

        // Apply weather overlay on top of sky color
        Color finalColor = currentWeather == WeatherType.CLEAR ?
                skyColor :
                Color.color(
                        (skyColor.getRed() + currentWeather.getOverlay().getRed()) / 2,
                        (skyColor.getGreen() + currentWeather.getOverlay().getGreen()) / 2,
                        (skyColor.getBlue() + currentWeather.getOverlay().getBlue()) / 2,
                        Math.max(skyColor.getOpacity(), currentWeather.getOverlay().getOpacity())
                );

        skyOverlay.setFill(finalColor);

        // New day at midnight
        if (timeOfDay == 0) {
            currentDay++;
            FXGL.getNotificationService().pushNotification("Day " + currentDay + " begins!");
        }
    }

    private String formatTime(int hour) {
        return (hour < 10 ? "0" : "") + hour + ":00";
    }

    private void updateWeather() {
        // Update weather duration
        if (weatherDuration > 0) {
            weatherDuration--;
        } else {
            // Change weather
            updateRandomWeather();
        }

        // Apply weather effects
        applyWeatherEffects();
    }

    private void updateRandomWeather() {
        // More likely to have clear weather
        int weatherRoll = random.nextInt(100);
        WeatherType newWeather;

        if (weatherRoll < 50) {
            newWeather = WeatherType.CLEAR;
        } else if (weatherRoll < 70) {
            newWeather = WeatherType.CLOUDY;
        } else if (weatherRoll < 85) {
            newWeather = WeatherType.RAINY;
        } else if (weatherRoll < 95) {
            newWeather = WeatherType.FOG;
        } else {
            newWeather = WeatherType.STORMY;
        }

        // Only notify if weather changed
        if (newWeather != currentWeather) {
            currentWeather = newWeather;
            FXGL.getNotificationService().pushNotification("Weather changed: " + currentWeather.getDescription());
        }

        // Set duration (2-8 hours)
        weatherDuration = 2 + random.nextInt(7);
    }

    private void applyWeatherEffects() {
        switch (currentWeather) {
            case RAINY:
                // Chance to collect water if rain collector is owned
                if (hasItem(ItemType.RAIN_COLLECTOR) && random.nextDouble() < 0.3) {
                    water += 1;
                    FXGL.getNotificationService().pushNotification("Rain collector gathered some water!");
                }
                break;
            case STORMY:
                // Storm damage to raft
                if (random.nextDouble() < 0.2) {
                    // Check for damage to raft
                    damageRandomRaftTile();
                }
                break;
            case FOG:
                // Nothing special for fog yet
                break;
            case CLOUDY:
                // Nothing special for cloudy weather yet
                break;
            default:
                // Clear weather
                break;
        }
    }

    private void damageRandomRaftTile() {
        // Get all raft tiles except the center
        java.util.List<Point2D> raftTiles = new java.util.ArrayList<>();
        int centerX = (int) raftCenter.getX();
        int centerY = (int) raftCenter.getY();

        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (isRaftTile(x, y) && (x != centerX || y != centerY)) {
                    raftTiles.add(new Point2D(x, y));
                }
            }
        }

        // If there are raft tiles to damage
        if (!raftTiles.isEmpty()) {
            Point2D tileToRemove = raftTiles.get(random.nextInt(raftTiles.size()));
            int x = (int) tileToRemove.getX();
            int y = (int) tileToRemove.getY();

            // Remove raft tile
            grid[x][y].removeFromWorld();

            // Replace with water
            spawnWaterTile(x, y);

            FXGL.getNotificationService().pushNotification("Storm damaged your raft!");

            // Add materials (you get some back)
            if (random.nextBoolean()) {
                materials += 1;
            }
        }
    }
    private void checkForItems() {
        // Only check occasionally
        if (random.nextDouble() < 0.15) {
            // Increased chance with metal detector
            double chance = hasItem(ItemType.METAL_DETECTOR) ? 0.6 : 0.3;

            if (random.nextDouble() < chance) {
                spawnRandomItem();
            }
        }
    }

    private void spawnRandomItem() {
        // Determine which item to spawn
        ItemType[] availableItems = ItemType.values();
        ItemType itemToSpawn = availableItems[random.nextInt(availableItems.length)];

        // Find a suitable water tile adjacent to the raft
        java.util.List<Point2D> adjacentWaterTiles = new java.util.ArrayList<>();

        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (isRaftTile(x, y)) {
                    // Check adjacent tiles
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int newX = x + dx;
                            int newY = y + dy;

                            if (isValidGridPosition(newX, newY) && !isRaftTile(newX, newY)) {
                                adjacentWaterTiles.add(new Point2D(newX, newY));
                            }
                        }
                    }
                }
            }
        }

        // Spawn item on a random adjacent water tile
        if (!adjacentWaterTiles.isEmpty()) {
            Point2D spawnPos = adjacentWaterTiles.get(random.nextInt(adjacentWaterTiles.size()));

            FXGL.getNotificationService().pushNotification("You spotted a " + itemToSpawn.getName() + " floating nearby!");

            // Convert to grid entity
            int itemX = (int) spawnPos.getX();
            int itemY = (int) spawnPos.getY();

            // Replace water tile with item
            Entity itemEntity = FXGL.spawn("itemTile",
                    new SpawnData(itemX * TILE_SIZE, itemY * TILE_SIZE)
                            .put("itemType", itemToSpawn));

            grid[itemX][itemY].removeFromWorld();
            grid[itemX][itemY] = itemEntity;
        }
    }

    private boolean hasItem(ItemType itemType) {
        return inventory.contains(itemType);
    }

    private void collectItem(int x, int y) {
        if (isValidGridPosition(x, y) && grid[x][y] != null && grid[x][y].getType() == EntityType.ITEM) {
            ItemType collectedItem = grid[x][y].getObject("itemType");

            // Add to inventory
            inventory.add(collectedItem);

            // Update count
            itemCounts.put(collectedItem, itemCounts.getOrDefault(collectedItem, 0) + 1);

            FXGL.getNotificationService().pushNotification("Collected: " + collectedItem.getName() + " - " + collectedItem.getDescription());

            // Apply immediate effects of certain items
            applyItemEffects(collectedItem);

            // Replace with water tile
            grid[x][y].removeFromWorld();
            spawnWaterTile(x, y);

            // Update UI
            updateUI();

            if (random.nextDouble() < 0.3) { // 30% chance
                offerCardReward();
            }
        }
    }

    private void applyItemEffects(ItemType itemType) {
        switch (itemType) {
            case FISHING_ROD:
                FXGL.getNotificationService().pushNotification("Fishing success rate increased!");
                break;
            case WATER_PURIFIER:
                FXGL.getNotificationService().pushNotification("You can now purify water!");
                if (water > 0) {
                    FXGL.getNotificationService().pushNotification("All your water has been purified!");
                }
                break;
            case STORAGE_BOX:
                FXGL.getNotificationService().pushNotification("Storage capacity increased!");
                break;
            // Other items have passive effects applied during relevant actions
        }
    }

    private void drinkWater() {
        if (water > 0) {
            water--;

            // Water restores more thirst if purified
            if (hasItem(ItemType.WATER_PURIFIER)) {
                thirst = Math.min(100, thirst + 30);
                FXGL.getNotificationService().pushNotification("Drank purified water. Thirst restored!");
            } else {
                thirst = Math.min(100, thirst + 20);
                // Chance of getting sick from unpurified water
                if (random.nextDouble() < 0.2) {
                    health -= 5;
                    FXGL.getNotificationService().pushNotification("The water wasn't clean. You feel sick.");
                } else {
                    FXGL.getNotificationService().pushNotification("Drank water. Thirst partially restored.");
                }
            }

            updateUI();
            endPlayerTurn();
        } else {
            FXGL.getNotificationService().pushNotification("No water to drink!");
        }
    }

    // Add a method to eat food
    private void eatFood() {
        if (food > 0) {
            food--;

            // Food restores more hunger if cooked
            if (hasItem(ItemType.GRILL)) {
                hunger = Math.min(100, hunger + 30);
                FXGL.getNotificationService().pushNotification("Ate cooked food. Hunger restored!");
            } else {
                hunger = Math.min(100, hunger + 20);
                FXGL.getNotificationService().pushNotification("Ate raw food. Hunger partially restored.");
            }

            updateUI();
            endPlayerTurn();
        } else {
            FXGL.getNotificationService().pushNotification("No food to eat!");
        }
    }

    private void showInventory() {
        StringBuilder sb = new StringBuilder("Inventory:\n");

        if (inventory.isEmpty()) {
            sb.append("Empty");
        } else {
            // Count occurrences of each item
            for (ItemType itemType : ItemType.values()) {
                int count = itemCounts.getOrDefault(itemType, 0);
                if (count > 0) {
                    sb.append(itemType.getName()).append(" (").append(count).append(")\n");
                }
            }
        }

        FXGL.getNotificationService().pushNotification(sb.toString());
    }

    private void initializeDeck() {
        // Add basic action cards
        addCardToDeck(new Card("Basic Fish", "Fish for food", ActionType.FISH, 1));
        addCardToDeck(new Card("Basic Fish", "Fish for food", ActionType.FISH, 1));
        addCardToDeck(new Card("Basic Fish", "Fish for food", ActionType.FISH, 1));

        addCardToDeck(new Card("Basic Build", "Build raft extension", ActionType.BUILD, 1));
        addCardToDeck(new Card("Basic Build", "Build raft extension", ActionType.BUILD, 1));

        addCardToDeck(new Card("Basic Water", "Collect water", ActionType.COLLECT_WATER, 1));
        addCardToDeck(new Card("Basic Water", "Collect water", ActionType.COLLECT_WATER, 1));

        addCardToDeck(new Card("Basic Scavenge", "Scavenge for materials", ActionType.SCAVENGE, 1));
        addCardToDeck(new Card("Basic Scavenge", "Scavenge for materials", ActionType.SCAVENGE, 1));
        addCardToDeck(new Card("Basic Scavenge", "Scavenge for materials", ActionType.SCAVENGE, 1));

        addCardToDeck(new Card("Basic Rest", "Rest and recover", ActionType.REST, 1));
        addCardToDeck(new Card("Basic Rest", "Rest and recover", ActionType.REST, 1));

        // Add a few starter permanent cards
        addCardToDeck(new Card("Fishing Hook", "Improves fishing success", BuffEffect.FISHING_CHANCE, 10));
        addCardToDeck(new Card("Sturdy Plank", "Improves raft durability", BuffEffect.BUILDING_STRENGTH, 15));

        // Shuffle the deck
        shuffleDeck();

        // Deal initial hand
        drawHand();
    }

    private void addCardToDeck(Card card) {
        deck.add(card);
        deckSize++;
    }

    private void shuffleDeck() {
        // Add discard pile back to deck
        deck.addAll(discardPile);
        discardPile.clear();

        // Shuffle
        java.util.Collections.shuffle(deck);

        //FXGL.getNotificationService().pushNotification("Deck shuffled!");
    }

    private void drawHand() {
        // Clear current hand
        hand.clear();

        // Draw up to hand size
        for (int i = 0; i < handSize; i++) {
            if (deck.isEmpty() && !discardPile.isEmpty()) {
                shuffleDeck();
            }

            if (!deck.isEmpty()) {
                hand.add(deck.remove(0));
            } else {
                // No cards left
                break;
            }
        }

        updateCardDisplay();
    }

    private void discardHand() {
        discardPile.addAll(hand);
        hand.clear();
    }

    private void updateCardDisplay() {
        // Clear previous cards

        cardHandContainer.getChildren().clear();

        // Add cards to display
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            // Create card visual
            Rectangle cardBg = new Rectangle(120, 160,
                    card.getType() == CardType.ACTION ? Color.LIGHTBLUE : Color.GOLD);
            cardBg.setArcWidth(15);
            cardBg.setArcHeight(15);
            cardBg.setStroke(Color.BLACK);
            cardBg.setStrokeWidth(2);

            Text cardName = new Text(card.getName());
            cardName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            cardName.setWrappingWidth(100);
            cardName.setTextAlignment(TextAlignment.CENTER);

            Text cardDesc = new Text(card.getDescription());
            cardDesc.setFont(Font.font("Arial", 12));
            cardDesc.setWrappingWidth(100);
            cardDesc.setTextAlignment(TextAlignment.CENTER);

            String valueText;
            if (card.getType() == CardType.ACTION) {
                valueText = "Value: " + card.getValue();
            } else {
                valueText = "+" + card.getValue() + "% " + card.getBuffEffect().getDescription();
            }

            Text cardValue = new Text(valueText);
            cardValue.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            cardValue.setWrappingWidth(100);
            cardValue.setTextAlignment(TextAlignment.CENTER);

            VBox cardContent = new VBox(10);
            cardContent.setPadding(new Insets(10));
            cardContent.getChildren().addAll(cardName, cardDesc, cardValue);

            // Make the card clickable
            final int cardIndex = i;
            cardBg.setOnMouseClicked(e -> playCard(cardIndex));

            StackPane cardPane = new StackPane(cardBg, cardContent);
            cardPane.setTranslateX(10);
            cardPane.setTranslateY(10 + (i * 10)); // Stagger cards slightly

            cardHandContainer.getChildren().add(cardPane);
        }

        // Update card count text
        cardsLeftText.setText("Deck: " + deck.size() + " | Discard: " + discardPile.size() +
                " | Played: " + cardsPlayed + "/" + maxCardsPerTurn);

        // Update active buffs display
        updateActiveBuffsDisplay();
    }

    private void updateActiveBuffsDisplay() {
        // Clear previous buffs
        activeBuffsContainer.getChildren().clear();

        // Create text summary
        StringBuilder sb = new StringBuilder("Active Buffs: ");

        if (activeBuffs.isEmpty()) {
            sb.append("None");
        } else {
            for (Card buff : activeBuffs) {
                // Create buff visual
                Rectangle buffBg = new Rectangle(80, 80, Color.GOLD);
                buffBg.setArcWidth(10);
                buffBg.setArcHeight(10);
                buffBg.setStroke(Color.BLACK);
                buffBg.setStrokeWidth(1);

                Text buffName = new Text(buff.getName());
                buffName.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                buffName.setWrappingWidth(70);
                buffName.setTextAlignment(TextAlignment.CENTER);

                Text buffEffect = new Text("+" + buff.getValue() + "% " +
                        buff.getBuffEffect().getDescription());
                buffEffect.setFont(Font.font("Arial", 9));
                buffEffect.setWrappingWidth(70);
                buffEffect.setTextAlignment(TextAlignment.CENTER);

                VBox buffContent = new VBox(5);
                buffContent.setPadding(new Insets(5));
                buffContent.getChildren().addAll(buffName, buffEffect);

                StackPane buffPane = new StackPane(buffBg, buffContent);

                activeBuffsContainer.getChildren().add(buffPane);

                // Add to text summary
                sb.append("\n").append(buff.getName()).append(" (+").append(buff.getValue())
                        .append("% ").append(buff.getBuffEffect().getDescription()).append(")");
            }
        }

        activeBuffsText.setText(sb.toString());
    }

    private void playCard(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= hand.size() || cardsPlayed >= maxCardsPerTurn) {
            return;
        }

        Card card = hand.get(cardIndex);

        FXGL.getNotificationService().pushNotification("Playing card: " + card.getName());

        // Handle card effect based on type
        if (card.getType() == CardType.ACTION) {
            executeActionCard(card);
        } else {
            executePermanentCard(card);
        }

        // Remove from hand and add to discard
        hand.remove(cardIndex);
        discardPile.add(card);

        // Increment cards played
        cardsPlayed++;

        // Update UI
        if (cardHandContainer != null) {
            updateCardDisplay();
        }

        // If max cards played, end turn
        if (cardsPlayed >= maxCardsPerTurn) {
            FXGL.getNotificationService().pushNotification("Max cards played. Turn ending...");
            endTurn();
        }
    }

    private void executeActionCard(Card card) {
        switch (card.getActionType()) {
            case FISH:
                fishWithCard(card);
                break;
            case BUILD:
                buildWithCard(card);
                break;
            case COLLECT_WATER:
                collectWaterWithCard(card);
                break;
            case SCAVENGE:
                scavengeWithCard(card);
                break;
            case REST:
                restWithCard(card);
                break;
            case REPAIR:
                repairWithCard(card);
                break;
            case CRAFT:
                craftWithCard(card);
                break;
            case NAVIGATE:
                navigateWithCard(card);
                break;
        }
    }

    private void executePermanentCard(Card card) {
        // Add to active buffs
        activeBuffs.add(card);
        FXGL.getNotificationService().pushNotification("Permanent effect activated: " +
                card.getName() + " - " +
                card.getDescription());

        // If it's a card draw buff, immediately increase hand size
        if (card.getBuffEffect() == BuffEffect.CARD_DRAW) {
            handSize = Math.min(maxHandSize, handSize + card.getValue() / 100);
            FXGL.getNotificationService().pushNotification("Hand size increased to " + handSize);
        }
    }

    private double getBuffModifier(BuffEffect effect) {
        double totalModifier = 1.0;

        for (Card buff : activeBuffs) {
            if (buff.getBuffEffect() == effect) {
                totalModifier += buff.getValue() / 100.0;
            }
        }

        return totalModifier;
    }

    // Implement action card methods
    private void fishWithCard(Card card) {
        double fishChance = 0.4 * getBuffModifier(BuffEffect.FISHING_CHANCE);

        // Higher chance during dawn/dusk
        if ((timeOfDay >= 5 && timeOfDay < 8) || (timeOfDay >= 18 && timeOfDay < 21)) {
            fishChance += 0.1;
        }

        if (random.nextDouble() < fishChance) {
            int fishAmount = (int)Math.ceil(card.getValue() * getBuffModifier(BuffEffect.FISHING_QUANTITY));
            food += fishAmount;
            FXGL.getNotificationService().pushNotification("Caught " + fishAmount + " fish!");
        } else {
            FXGL.getNotificationService().pushNotification("No fish caught...");
        }

        updateUI();
    }

    private void buildWithCard(Card card) {
        // Get building cost reduction from buffs
        double costModifier = 1.0 - Math.min(0.8, (getBuffModifier(BuffEffect.BUILDING_COST) - 1.0));

        int buildCost = (int)Math.ceil(5 * costModifier);

        if (materials >= buildCost) {
            buildRaftExtension();
            materials -= buildCost;
            FXGL.getNotificationService().pushNotification("Built raft extension! (Cost: " + buildCost + " materials)");
        } else {
            FXGL.getNotificationService().pushNotification("Not enough materials! Need " + buildCost);
        }

        updateUI();
    }

    private void collectWaterWithCard(Card card) {
        int waterAmount = (int)Math.ceil(card.getValue() * getBuffModifier(BuffEffect.WATER_QUANTITY));
        water += waterAmount;

        String message = "Collected " + waterAmount + " water";

        // Check if water is automatically purified
        double purifyChance = getBuffModifier(BuffEffect.WATER_QUALITY) - 1.0;
        if (purifyChance > 0.5 || hasItem(ItemType.WATER_PURIFIER)) {
            message += " (purified)";
        } else {
            message += " (needs purification)";
        }

        FXGL.getNotificationService().pushNotification(message);
        updateUI();
    }

    private void scavengeWithCard(Card card) {
        double findChance = 0.6 * getBuffModifier(BuffEffect.MATERIAL_CHANCE);

        if (random.nextDouble() < findChance) {
            int materialAmount = (int)Math.ceil(card.getValue() * getBuffModifier(BuffEffect.MATERIAL_QUANTITY));
            materials += materialAmount;
            FXGL.getNotificationService().pushNotification("Found " + materialAmount + " materials!");
        } else {
            FXGL.getNotificationService().pushNotification("Found nothing useful...");
        }

        updateUI();
    }

    private void restWithCard(Card card) {
        int recoveryAmount = (int)Math.ceil(card.getValue() * 5 * getBuffModifier(BuffEffect.REST_RECOVERY));

        health = Math.min(100, health + recoveryAmount);
        hunger = Math.min(100, hunger + recoveryAmount / 2);
        thirst = Math.min(100, thirst + recoveryAmount / 2);

        FXGL.getNotificationService().pushNotification("Rested and recovered " + recoveryAmount + " health!");
        updateUI();
    }

    private void repairWithCard(Card card) {
        // TODO: Implement repair mechanic for damaged raft
        FXGL.getNotificationService().pushNotification("Repair not yet implemented");
    }

    private void craftWithCard(Card card) {
        // TODO: Implement crafting system
        FXGL.getNotificationService().pushNotification("Crafting not yet implemented");
    }

    private void navigateWithCard(Card card) {
        // TODO: Implement navigation to control raft movement direction
        FXGL.getNotificationService().pushNotification("Navigation not yet implemented");
    }
    private void endTurn() {
        // Reset cards played counter
        cardsPlayed = 0;

        // Discard hand
        discardHand();

        // Draw new hand
        drawHand();

        // Process end of turn effects
        endPlayerTurn();
    }
    private void initCardUI() {
        // Create card display area
        cardHandContainer = new VBox(5);
        cardHandContainer.setTranslateX(GRID_WIDTH * TILE_SIZE - 150);
        cardHandContainer.setTranslateY(50);
        cardHandContainer.setPrefWidth(150);
        cardHandContainer.setPrefHeight(300);
        FXGL.addUINode(cardHandContainer);

        // Create buffs display area
        activeBuffsContainer = new HBox(5);
        activeBuffsContainer.setTranslateX(10);
        activeBuffsContainer.setTranslateY(10);
        activeBuffsContainer.setPrefHeight(100);
        FXGL.addUINode(activeBuffsContainer);

        // Create text for card counts
        cardsLeftText = new Text("Deck: 0 | Discard: 0 | Played: 0/3");
        cardsLeftText.setFill(Color.WHITE);
        cardsLeftText.setFont(Font.font(14));
        cardsLeftText.setTranslateX(GRID_WIDTH * TILE_SIZE - 250);
        cardsLeftText.setTranslateY(30);
        FXGL.addUINode(cardsLeftText);

        // Create text for active buffs
        activeBuffsText = new Text("Active Buffs: None");
        activeBuffsText.setFill(Color.WHITE);
        activeBuffsText.setFont(Font.font(14));
        activeBuffsText.setTranslateX(10);
        activeBuffsText.setTranslateY(GRID_HEIGHT * TILE_SIZE - 20);
        FXGL.addUINode(activeBuffsText);

        // Add end turn button
        Rectangle endTurnBg = new Rectangle(100, 30, Color.RED);
        Text endTurnText = new Text("End Turn");
        endTurnText.setFill(Color.WHITE);

        StackPane endTurnButton = new StackPane(endTurnBg, endTurnText);
        endTurnButton.setTranslateX(GRID_WIDTH * TILE_SIZE - 120);
        endTurnButton.setTranslateY(GRID_HEIGHT * TILE_SIZE - 40);

        endTurnBg.setOnMouseClicked(e -> endTurn());

        FXGL.addUINode(endTurnButton);
    }

    // Method to generate new card rewards
    private Card generateCardReward() {
        int roll = random.nextInt(100);

        if (roll < 70) {
            // Generate action card (70%)
            ActionType[] actionTypes = ActionType.values();
            ActionType actionType = actionTypes[random.nextInt(actionTypes.length)];

            int value = 1 + random.nextInt(3); // 1-3 value

            String adjective;
            if (value == 1) {
                adjective = "Basic";
            } else if (value == 2) {
                adjective = "Improved";
            } else {
                adjective = "Advanced";
            }

            return new Card(adjective + " " + actionType.name().charAt(0) +
                    actionType.name().substring(1).toLowerCase(),
                    actionType.getDescription(), actionType, value);
        } else {
            // Generate permanent card (30%)
            BuffEffect[] buffEffects = BuffEffect.values();
            BuffEffect buffEffect = buffEffects[random.nextInt(buffEffects.length)];

            // 10-30% buff value
            int value = 10 + random.nextInt(21);

            String name;
            switch (buffEffect) {
                case FISHING_CHANCE:
                    name = random.nextBoolean() ? "Lucky Hook" : "Master Angler";
                    break;
                case FISHING_QUANTITY:
                    name = random.nextBoolean() ? "Big Net" : "Fish Attractor";
                    break;
                case BUILDING_COST:
                    name = random.nextBoolean() ? "Efficient Builder" : "Resource Saver";
                    break;
                case BUILDING_STRENGTH:
                    name = random.nextBoolean() ? "Reinforced Hull" : "Sturdy Frame";
                    break;
                case WATER_QUALITY:
                    name = random.nextBoolean() ? "Clean Filter" : "Purification Crystal";
                    break;
                case WATER_QUANTITY:
                    name = random.nextBoolean() ? "Large Bucket" : "Water Collector";
                    break;
                case MATERIAL_CHANCE:
                    name = random.nextBoolean() ? "Keen Eye" : "Treasure Hunter";
                    break;
                case MATERIAL_QUANTITY:
                    name = random.nextBoolean() ? "Bounty Hunter" : "Resource Magnet";
                    break;
                case REST_RECOVERY:
                    name = random.nextBoolean() ? "Comfortable Hammock" : "Peaceful Dreams";
                    break;
                case STORM_RESISTANCE:
                    name = random.nextBoolean() ? "Weather Shield" : "Storm Breaker";
                    break;
                case CARD_DRAW:
                    name = random.nextBoolean() ? "Quick Thinking" : "Strategic Mind";
                    break;
                case INVENTORY_SPACE:
                    name = random.nextBoolean() ? "Large Backpack" : "Organizing Skills";
                    break;
                default:
                    name = "Mystery Buff";
            }

            return new Card(name, "Permanent " + buffEffect.getDescription() + " boost",
                    buffEffect, value);
        }
    }

    // Add card reward after events/fishing/etc
    private void offerCardReward() {
        java.util.List<Card> cardChoices = new java.util.ArrayList<>();

        // Generate 3 random card choices
        for (int i = 0; i < 3; i++) {
            cardChoices.add(generateCardReward());
        }

        // Create a UI for choosing cards
        VBox rewardContainer = new VBox(10);
        rewardContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 20px;");
        rewardContainer.setPrefWidth(GRID_WIDTH * TILE_SIZE);
        rewardContainer.setPrefHeight(GRID_HEIGHT * TILE_SIZE);
        rewardContainer.setAlignment(Pos.CENTER);

        Text choiceText = new Text("Choose a card to add to your deck:");
        choiceText.setFill(Color.WHITE);
        choiceText.setFont(Font.font(18));

        HBox cardsBox = new HBox(20);
        cardsBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < cardChoices.size(); i++) {
            Card card = cardChoices.get(i);

            // Create card visual
            Rectangle cardBg = new Rectangle(150, 200,
                    card.getType() == CardType.ACTION ? Color.LIGHTBLUE : Color.GOLD);
            cardBg.setArcWidth(15);
            cardBg.setArcHeight(15);
            cardBg.setStroke(Color.WHITE);
            cardBg.setStrokeWidth(2);

            Text cardName = new Text(card.getName());
            cardName.setFill(Color.BLACK);
            cardName.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            cardName.setWrappingWidth(130);
            cardName.setTextAlignment(TextAlignment.CENTER);

            Text cardDesc = new Text(card.getDescription());
            cardDesc.setFill(Color.BLACK);
            cardDesc.setFont(Font.font("Arial", 14));
            cardDesc.setWrappingWidth(130);
            cardDesc.setTextAlignment(TextAlignment.CENTER);

            String valueText;
            if (card.getType() == CardType.ACTION) {
                valueText = "Value: " + card.getValue();
            } else {
                valueText = "+" + card.getValue() + "% " + card.getBuffEffect().getDescription();
            }

            Text cardValue = new Text(valueText);
            cardValue.setFill(Color.BLACK);
            cardValue.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            cardValue.setWrappingWidth(130);
            cardValue.setTextAlignment(TextAlignment.CENTER);

            VBox cardContent = new VBox(15);
            cardContent.setPadding(new Insets(15));
            cardContent.getChildren().addAll(cardName, cardDesc, cardValue);

            StackPane cardPane = new StackPane(cardBg, cardContent);

            // Make the card clickable
            final int cardIndex = i;
            cardBg.setOnMouseClicked(e -> {
                addCardToDeck(cardChoices.get(cardIndex));
                FXGL.getNotificationService().pushNotification("Added " +
                        cardChoices.get(cardIndex).getName() + " to your deck!");
                FXGL.removeUINode(rewardContainer);
                shuffleDeck();
            });

            cardsBox.getChildren().add(cardPane);
        }

        // Add skip option
        Button skipButton = new Button("Skip");
        skipButton.setOnAction(e -> {
            FXGL.removeUINode(rewardContainer);
        });

        rewardContainer.getChildren().addAll(choiceText, cardsBox, skipButton);
        FXGL.addUINode(rewardContainer);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
