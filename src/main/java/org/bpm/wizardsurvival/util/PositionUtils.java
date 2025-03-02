package org.bpm.wizardsurvival.util;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import org.bpm.wizardsurvival.world.Position;

import java.awt.*;

/**
     * Utility class for converting between game positions and FXGL positions.
     */
    public class PositionUtils {
        // Default scale factor between game units and screen pixels
        private static final double DEFAULT_SCALE = 32.0;

        // Private constructor to prevent instantiation
        private PositionUtils() {}

        /**
         * Converts a game position to an FXGL Point2D using the default scale.
         *
         * @param position The game position
         * @return An FXGL Point2D
         */
        public static Point2D toFXGLPoint(Point position) {
            return toFXGLPoint(position, DEFAULT_SCALE);
        }

        /**
         * Converts a game position to an FXGL Point2D with a custom scale.
         *
         * @param point The game position
         * @param scale The scale factor (pixels per game unit)
         * @return An FXGL Point2D
         */
        public static Point2D toFXGLPoint(Point point, double scale) {
            return new Point2D(point.getX() * scale, point.getY() * scale);
        }

        /**
         * Converts an FXGL Point2D to a game position using the default scale.
         *
         * @param point The FXGL point
         * @return A game position
         */
        public static Position toGamePosition(Point2D point) {
            return toGamePosition(point, DEFAULT_SCALE);
        }

        /**
         * Converts an FXGL Point2D to a game position with a custom scale.
         *
         * @param point The FXGL point
         * @param scale The scale factor (pixels per game unit)
         * @return A game position
         */
        public static Position toGamePosition(Point2D point, double scale) {
            return new Position(point.getX() / scale, point.getY() / scale);
        }

        /**
         * Converts screen coordinates to game world coordinates.
         *
         * @param screenX Screen X coordinate
         * @param screenY Screen Y coordinate
         * @return A position in the game world
         */
        public static Position screenToWorld(double screenX, double screenY) {
            Point2D screenPoint = new Point2D(screenX, screenY);
            Point2D worldPoint = screenPoint;
            return toGamePosition(worldPoint);
        }

        /**
         * Gets the current view visible in the game viewport.
         *
         * @return A 4-element array with [minX, minY, maxX, maxY] of the visible area
         */
        public static double[] getVisibleArea() {
            double viewportWidth = FXGL.getAppWidth();
            double viewportHeight = FXGL.getAppHeight();

            Point2D topLeft = FXGL.getGameScene().getViewport().getOrigin();
            Position topLeftPos = toGamePosition(topLeft);

            return new double[] {
                    topLeftPos.getX(),
                    topLeftPos.getY(),
                    topLeftPos.getX() + viewportWidth / DEFAULT_SCALE,
                    topLeftPos.getY() + viewportHeight / DEFAULT_SCALE
            };
        }

        /**
         * Centers the viewport on a game position.
         *
         * @param position The position to center on
         */
        public static void centerOn(Point position) {
            Point2D point = toFXGLPoint(position);
            FXGL.getGameScene().getViewport().bindToEntity(
                    FXGL.entityBuilder()
                            .at(point)
                            .buildAndAttach(),
                    FXGL.getAppWidth() / 2,
                    FXGL.getAppHeight() / 2
            );
        }
    }

