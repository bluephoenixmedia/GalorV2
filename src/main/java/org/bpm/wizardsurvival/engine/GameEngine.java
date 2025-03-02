package org.bpm.wizardsurvival.engine;

import org.bpm.wizardsurvival.entities.Player;
import org.bpm.wizardsurvival.items.Joker;
import org.bpm.wizardsurvival.world.OceanZone;
import org.bpm.wizardsurvival.world.Weather;
import org.bpm.wizardsurvival.world.WorldMap;
import org.bpm.wizardsurvival.world.WorldMapGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameEngine implements Runnable {
    // Singleton instance
    private static GameEngine instance;

    // Game state variables
    private Player player;
    private WorldMap worldMap;
    private Weather currentWeather;
    private Random rng;
    private List<Joker> activeJokers;
    private GameDifficulty difficulty;
    private int gameDay;

    // Game loop variables
    private boolean running;
    private boolean paused;
    private ScheduledExecutorService gameLoopExecutor;
    private long lastUpdateTime;
    private final int TICKS_PER_SECOND = 60;
    private final long TICK_DURATION_MS = 1000 / TICKS_PER_SECOND;

    // Input and rendering handlers
    private InputHandler inputHandler;
    private GameRenderer renderer;

    // Callback for game events
    private GameEventListener eventListener;

    private GameEngine() {
        this.rng = new Random();
        this.activeJokers = new ArrayList<>();
        this.gameDay = 1;
        this.difficulty = GameDifficulty.NORMAL;
        this.running = false;
        this.paused = false;
    }

    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    public void startNewGame(CharacterClass characterClass) {
        player = new Player(characterClass);
        worldMap = WorldMapGenerator.generateNewWorld();
        currentWeather = Weather.CLEAR;
        activeJokers.clear();
        gameDay = 1;

        if (!running) {
            startGameLoop();
        }
    }

    public void stopGame() {
        running = false;
        if (gameLoopExecutor != null) {
            gameLoopExecutor.shutdown();
        }
    }

    public void pauseGame() {
        paused = true;
        if (eventListener != null) {
            eventListener.onGamePaused();
        }
    }

    public void resumeGame() {
        paused = false;
        if (eventListener != null) {
            eventListener.onGameResumed();
        }
    }

    private void startGameLoop() {
        running = true;
        lastUpdateTime = System.currentTimeMillis();

        // Use a scheduled executor for consistent timing
        gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();
        gameLoopExecutor.scheduleAtFixedRate(this, 0, TICK_DURATION_MS, TimeUnit.MILLISECONDS);

        if (eventListener != null) {
            eventListener.onGameStarted();
        }
    }

    @Override
    public void run() {
        if (!running || paused) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        try {
            // Process input
            if (inputHandler != null) {
                inputHandler.processInput();
            }

            // Update game state
            update(deltaTime);

            // Render the game
            if (renderer != null) {
                renderer.render();
            }
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error in game loop: " + e.getMessage());
            e.printStackTrace();

            if (eventListener != null) {
                eventListener.onGameError(e);
            }
        }
    }

    private void update(float deltaTime) {
        // Update game components
        player.update(deltaTime);
        worldMap.update();
        updateJokers();

        // Check for game state changes
        checkGameConditions();
    }

    private void checkGameConditions() {
        // Check for win/loss conditions
        if (!player.isAlive()) {
            if (eventListener != null) {
                eventListener.onPlayerDeath();
            }
            stopGame();
        }

        if (worldMap.isAtWizardSchool(player.getPosition())) {
            if (eventListener != null) {
                eventListener.onReachedWizardSchool();
            }
        }
    }

    public float calculateSuccessRate(SurvivalActivity activity) {
        float baseRate = activity.getBaseSuccessRate();

        // Apply modifiers from jokers
        for (Joker joker : activeJokers) {
            baseRate = joker.modifySuccessRate(activity, baseRate);
        }

        // Apply skill modifiers
        baseRate += player.getRelevantSkillBonus(activity);

        // Apply weather effects
        baseRate = currentWeather.modifySuccessRate(activity, baseRate);

        // Apply difficulty modifier
        baseRate *= difficulty.getSuccessRateModifier();

        return Math.min(baseRate, 1.0f); // Cap at 100%
    }

    public boolean attemptActivity(SurvivalActivity activity) {
        float successRate = calculateSuccessRate(activity);
        float roll = rng.nextFloat();

        if (roll < successRate) {
            // Success! Apply results
            activity.applySuccess(player);
            player.increaseSkill(activity.getRelevantSkill(), 0.01f);
            return true;
        } else {
            // Failure
            activity.applyFailure(player);
            player.increaseSkill(activity.getRelevantSkill(), 0.005f); // Still learn a bit from failure
            return false;
        }
    }

    public void addJoker(Joker joker) {
        activeJokers.add(joker);
        joker.onActivation(player);
    }

    public void updateJokers() {
        List<Joker> expiredJokers = new ArrayList<>();

        for (Joker joker : activeJokers) {
            joker.decrementDuration();
            if (joker.isExpired()) {
                expiredJokers.add(joker);
                joker.onExpiration(player);
            }
        }

        activeJokers.removeAll(expiredJokers);
    }

    public void advanceDay() {
        gameDay++;
        updateJokers();
        updateWeather();
        worldMap.update();
        player.updateRaftCondition();

        if (eventListener != null) {
            eventListener.onDayAdvanced(gameDay);
        }
    }

    private void updateWeather() {
        // Random weather changes based on ocean zone
        OceanZone currentZone = worldMap.getZoneAt(player.getPosition());
        Weather previousWeather = currentWeather;
        currentWeather = currentZone.generateWeather(rng, currentWeather);

        if (previousWeather != currentWeather && eventListener != null) {
            eventListener.onWeatherChanged(currentWeather);
        }
    }

    public void retryTrial() {
        // Keep abilities but increase difficulty
        difficulty = difficulty.increaseLevel();
        startNewGame(player.getCharacterClass());
        player.retainAbilities();

        if (eventListener != null) {
            eventListener.onTrialRestarted(difficulty);
        }
    }

    public void attackSchool() {
        // Initiate wrath path
        if (eventListener != null) {
            eventListener.onAttackSchoolInitiated();
        }
    }

    // Getters and setters
    public Player getPlayer() { return player; }
    public WorldMap getWorldMap() { return worldMap; }
    public Weather getCurrentWeather() { return currentWeather; }
    public int getGameDay() { return gameDay; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }

    // Set handlers and listeners
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void setRenderer(GameRenderer renderer) {
        this.renderer = renderer;
    }

    public void setEventListener(GameEventListener eventListener) {
        this.eventListener = eventListener;
    }

    // Required interfaces
    public interface InputHandler {
        void processInput();
    }

    public interface GameRenderer {
        void render();
    }

    public interface GameEventListener {
        void onGameStarted();
        void onGamePaused();
        void onGameResumed();
        void onGameError(Exception e);
        void onPlayerDeath();
        void onReachedWizardSchool();
        void onDayAdvanced(int day);
        void onWeatherChanged(Weather newWeather);
        void onTrialRestarted(GameDifficulty newDifficulty);
        void onAttackSchoolInitiated();
    }
}