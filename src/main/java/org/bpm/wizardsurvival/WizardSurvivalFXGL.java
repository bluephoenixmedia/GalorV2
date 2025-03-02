package org.bpm.wizardsurvival;


import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.bpm.wizardsurvival.engine.CharacterClass;
import org.bpm.wizardsurvival.engine.GameDifficulty;
import org.bpm.wizardsurvival.engine.GameEngine;
import org.bpm.wizardsurvival.engine.SurvivalActivity;
import org.bpm.wizardsurvival.entities.Player;
import org.bpm.wizardsurvival.entities.PlayerComponent;
import org.bpm.wizardsurvival.util.PositionUtils;
import org.bpm.wizardsurvival.world.Position;
import org.bpm.wizardsurvival.world.Weather;
import org.bpm.wizardsurvival.world.WorldMap;

import java.awt.*;

import javafx.scene.paint.Color;

import static com.almasb.fxgl.core.math.FXGLMath.random;

/**
     * Main FXGL application class that integrates with GameEngine
     */
    public class WizardSurvivalFXGL extends GameApplication {

        // Game components
        private GameEngine gameEngine;
        private Entity playerEntity;
        private Text weatherText;
        private Text dayCounter;

        private WorldMap worldMap;
        // Entity types for FXGL
        public enum EntityType {
            PLAYER, OBSTACLE, ITEM, JOKER, SCHOOL
        }

        @Override
        protected void initSettings(GameSettings settings) {
            settings.setWidth(1280);
            settings.setHeight(720);
            settings.setTitle("Wizard Survival");
            settings.setVersion("0.1");
            settings.setIntroEnabled(false); // Disable the FXGL intro
            settings.setMainMenuEnabled(true);
            settings.setGameMenuEnabled(true);
            settings.setProfilingEnabled(false);
        }

        @Override
        protected void initGame() {
            // Initialize our game engine
            gameEngine = GameEngine.getInstance();

            // Connect our FXGL handlers to the engine
            gameEngine.setInputHandler(new FXGLInputHandler());
            gameEngine.setRenderer(new FXGLRenderer());
            gameEngine.setEventListener(new FXGLEventHandler());

            // Start a new game
            gameEngine.startNewGame(CharacterClass.WARRIOR);

            // Create player entity in FXGL
            playerEntity = createPlayerEntity();
            FXGL.getGameWorld().addEntity(playerEntity);

            // Generate world entities based on world map
            generateWorldEntities();

            // Add UI elements
            initUI();
        }

        @Override
        protected void initInput() {
            // Movement
            FXGL.getInput().addAction(new UserAction("Move Up") {
                @Override
                protected void onAction() {
                    // Just store the input - will be processed in the game engine
                    FXGLInputHandler.moveUp = true;
                }

                @Override
                protected void onActionEnd() {
                    FXGLInputHandler.moveUp = false;
                }
            }, KeyCode.W);

            FXGL.getInput().addAction(new UserAction("Move Left") {
                @Override
                protected void onAction() {
                    FXGLInputHandler.moveLeft = true;
                }

                @Override
                protected void onActionEnd() {
                    FXGLInputHandler.moveLeft = false;
                }
            }, KeyCode.A);

            // Similar actions for down/right

            // Survival activities
            FXGL.getInput().addAction(new UserAction("Forage") {
                @Override
                protected void onActionBegin() {

                    gameEngine.attemptActivity(SurvivalActivity.FORAGING);
                }
            }, KeyCode.F);

            FXGL.getInput().addAction(new UserAction("Fish") {
                @Override
                protected void onActionBegin() {
                    gameEngine.attemptActivity(SurvivalActivity.FISHING);
                }
            }, KeyCode.R);

            FXGL.getInput().addAction(new UserAction("Next Day") {
                @Override
                protected void onActionBegin() {
                    gameEngine.advanceDay();
                }
            }, KeyCode.N);
        }

        @Override
        protected void onUpdate(double tpf) {
            // The FXGL game loop automatically calls this
            // We don't need to do much here since our GameEngine handles most logic

            // Update player entity position based on GameEngine player position
            // Update player entity position based on GameEngine player position
            Player player = gameEngine.getPlayer();
            if (player != null && playerEntity != null) {
                Point gamePosition = player.getPosition();
                Point2D fxglPosition = PositionUtils.toFXGLPoint(gamePosition);
                playerEntity.setPosition(fxglPosition);
            }

            // Update UI elements
            if (dayCounter != null) {
                dayCounter.setText("Day: " + gameEngine.getGameDay());
            }

            if (weatherText != null) {
                weatherText.setText("Weather: " + gameEngine.getCurrentWeather().toString());
            }
        }

        private Entity createPlayerEntity() {
            return FXGL.entityBuilder()
                    .type(EntityType.PLAYER)
                    .at(100, 100)
                    .viewWithBBox("player.png") // Assumes you have this image in assets folder
                    .with("characterClass", gameEngine.getPlayer().getCharacterClass())
                    .collidable()
                    .build();
        }

    private void generateWorldEntities() {
        // Process the world map from the game engine and create corresponding FXGL entities
        worldMap = gameEngine.getWorldMap();

        // Example: Create school entity at its position
        Point schoolPos = (Point) worldMap.getWizardSchoolPosition();
        if (schoolPos != null) {
            Point2D fxglPosition = PositionUtils.toFXGLPoint(schoolPos);

            FXGL.entityBuilder()
                    .type(EntityType.SCHOOL)
                    .at(fxglPosition)
                    .viewWithBBox("school.png")
                    .collidable()
                    .build();
        }

        // Create player entity at starting position
        Point playerPos = gameEngine.getPlayer().getPosition();
        Point2D fxglPlayerPos = PositionUtils.toFXGLPoint(playerPos);

        playerEntity = FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(fxglPlayerPos)
                .with(new PlayerComponent(gameEngine.getPlayer().getCharacterClass()))
                .collidable()
                .build();

        FXGL.getGameWorld().addEntity(playerEntity);
    }

        public void initUI() {
            // Create day counter
            dayCounter = FXGL.addText("Day: " + gameEngine.getGameDay(), 20, 20);
            Color white = Color.WHITE;
            dayCounter.setFill(white);

            // Create weather display
            weatherText = FXGL.addText("Weather: " + gameEngine.getCurrentWeather(), 20, 50);
            weatherText.setFill(white);
        }

        public static void main(String[] args) {
            launch(args);
        }

        /**
         * FXGL implementation of the InputHandler interface
         */
        private static class FXGLInputHandler implements GameEngine.InputHandler {
            // Input state flags
            public static boolean moveUp = false;
            public static boolean moveDown = false;
            public static boolean moveLeft = false;
            public static boolean moveRight = false;

            @Override
            public void processInput() {
                Player player = GameEngine.getInstance().getPlayer();

                // Process movement based on input flags
                if (moveUp) player.moveNorth();
                if (moveDown) player.moveSouth();
                if (moveLeft) player.moveWest();
                if (moveRight) player.moveEast();
            }
        }

        /**
         * FXGL implementation of the GameRenderer interface
         */
        private class FXGLRenderer implements GameEngine.GameRenderer {
            @Override
            public void render() {
                // FXGL handles most rendering automatically
                // We just need to update entity positions in onUpdate()

                // For any custom rendering that isn't entity-based:
                updateWeatherEffects();
            }

            private void updateWeatherEffects() {
                Weather weather = gameEngine.getCurrentWeather();

                //TODO fix emitters and weather
                // Clear existing particles
                //FXGL.getGameScene().getParticleEmitters().clear();

                // Add weather-specific particle effects
                switch (weather) {
                    case RAIN:
                        ParticleEmitter emitter = ParticleEmitters.newExplosionEmitter(300);

                        emitter.setMaxEmissions(Integer.MAX_VALUE);
                        emitter.setNumParticles(50);
                        emitter.setEmissionRate(0.86);
                        emitter.setSize(1, 24);
                        emitter.setScaleFunction(i -> FXGLMath.randomPoint2D().multiply(0.01));
                        emitter.setExpireFunction(i -> Duration.seconds(random(0.25, 2.5)));
                        emitter.setAccelerationFunction(() -> Point2D.ZERO);
                        emitter.setVelocityFunction(i -> FXGLMath.randomPoint2D().multiply(random(1, 45)));

                        ParticleEmitter rainEmitter = ParticleEmitters.newRainEmitter(300);

                        rainEmitter.setSize(1, 5);
                        rainEmitter.setColor(Color.LIGHTBLUE);
                        //rainEmitter.build();

                        //FXGL.getGameScene().addParticleEmitter(rain);
                        break;

                    case STORM:
                        // Create storm particles with lightning effects

                        ParticleEmitter storm = ParticleEmitters.newRainEmitter(300);


                        //FXGL.getGameScene().addParticleEmitter(storm);

                        // Occasional lightning flash
                        if (Math.random() < 0.01) {
                            //TODO add lightning
                           // FXGL.getGameScene().getViewport().fade(Duration.seconds(0.1), () -> {});
                        }
                        break;

                    default:
                        // No particles for clear weather
                        break;
                }
            }
        }

        /**
         * FXGL implementation of the GameEventListener interface
         */
        private class FXGLEventHandler implements GameEngine.GameEventListener {
            @Override
            public void onGameStarted() {
                FXGL.play("game_start.wav");
            }

            @Override
            public void onGamePaused() {
                FXGL.getGameController().pauseEngine();
            }

            @Override
            public void onGameResumed() {
                FXGL.getGameController().resumeEngine();
            }

            @Override
            public void onGameError(Exception e) {
                //TODO implement error
                System.out.println("game error");
               // FXGL.getDialogService().showErrorBox("Game error: " + e.getMessage());
            }

            @Override
            public void onPlayerDeath() {
                FXGL.play("death.wav");
                FXGL.getDialogService().showConfirmationBox("You have died! Try again?",
                        yes -> {
                            if (yes) {
                                gameEngine.retryTrial();
                            } else {
                                FXGL.getGameController().gotoMainMenu();
                            }
                        });
            }

            @Override
            public void onReachedWizardSchool() {
                FXGL.play("victory.wav");
                FXGL.getDialogService().showConfirmationBox(
                        "You've reached the Wizard School! Attack it?",
                        attack -> {
                            if (attack) {
                                gameEngine.attackSchool();
                            } else {
                                FXGL.getGameController().gotoMainMenu();
                            }
                        });
            }

            @Override
            public void onDayAdvanced(int day) {
                FXGL.play("day_change.wav");

                // Show notification
                FXGL.getNotificationService().pushNotification("Day " + day + " has begun!");
            }

            @Override
            public void onWeatherChanged(Weather newWeather) {
                FXGL.play("weather_change.wav");

                // Show weather notification
                FXGL.getNotificationService().pushNotification("Weather changed to " + newWeather);
            }


            @Override
            public void onTrialRestarted(GameDifficulty newDifficulty) {
                FXGL.play("retry.wav");
            }

            @Override
            public void onAttackSchoolInitiated() {
                FXGL.play("battle.wav");
                // Switch to battle scene
                // Not implemented in this example
            }
        }
    }

