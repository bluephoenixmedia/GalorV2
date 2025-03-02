package org.bpm.wizardsurvival;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import org.bpm.wizardsurvival.demo.BasicGameApp;
import org.bpm.wizardsurvival.engine.*;

import java.util.Map;

public class WizardSurvivalGame extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(600);
        settings.setHeight(600);
        settings.setTitle("Basic Game App");
        settings.setVersion("0.1");
    }

    private Entity player;

    public enum EntityType {
        PLAYER, COIN
    }

    @Override
    protected void initGame() {
        player = FXGL.entityBuilder()
                .type(WizardSurvivalFXGL.EntityType.PLAYER)
                .at(300, 300)
                .viewWithBBox("brick.png")
                //.with(new CollidableComponent(true))
                .buildAndAttach();
    }

    @Override
    protected void initInput() {
        /* animation example
        FXGL.getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                player.getComponent(AnimationComponent.class).moveRight();
            }
        }, KeyCode.D);

        FXGL.getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                player.getComponent(AnimationComponent.class).moveLeft();
            }
        }, KeyCode.A);

        /*
         */
        FXGL.onKey(KeyCode.D, () -> {
            player.translateX(5); // move right 5 pixels
            FXGL.inc("pixelsMoved", +5);
        });

        FXGL.onKey(KeyCode.A, () -> {
            player.translateX(-5); // move left 5 pixels
            FXGL.inc("pixelsMoved", +5);
        });

        FXGL.onKey(KeyCode.W, () -> {
            player.translateY(-5); // move up 5 pixels
            FXGL.inc("pixelsMoved", +5);
        });

        FXGL.onKey(KeyCode.S, () -> {
            player.translateY(5); // move down 5 pixels
            FXGL.inc("pixelsMoved", +5);
        });

        FXGL.onKeyDown(KeyCode.F, () -> {
            FXGL.play("drop.wav");
        });
    }

    @Override
    protected void initUI() {
        Text textPixels = new Text();
        textPixels.setTranslateX(50); // x = 50
        textPixels.setTranslateY(100); // y = 100

        textPixels.textProperty().bind(FXGL.getWorldProperties().intProperty("pixelsMoved").asString());
        FXGL.getGameScene().addUINode(textPixels); // add to the scene graph

        Texture brickTexture = FXGL.getAssetLoader().loadTexture("brick.png");
        brickTexture.setTranslateX(50);
        brickTexture.setTranslateY(450);

       // FXGL.getGameScene().addUINode(brickTexture);
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(BasicGameApp.EntityType.PLAYER, BasicGameApp.EntityType.COIN) {

            // order of types is the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity player, Entity coin) {
                coin.removeFromWorld();
                FXGL.play("drop.wav");
            }
        });
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("pixelsMoved", 0);
    }

    public static void main(String[] args) {
        /*
        // Get the engine instance
        GameEngine engine = GameEngine.getInstance();

        // Set up handlers
        engine.setInputHandler(new KeyboardInputHandler());
        engine.setRenderer(new JavaFXRenderer());
        engine.setEventListener(new GameEventHandler());

        // Start a new game
        engine.startNewGame(CharacterClass.WARRIOR);
        */
        launch(args);
        // The game loop is now running automatically
    }
}
