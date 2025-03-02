package org.bpm.wizardsurvival.world;

import java.util.Objects;

/**
 * Represents a position in the 2D game world.
 * Can be used to track locations of entities, calculate distances,
 * and handle movement throughout the game world.
 */
public class Position {
    private double x;
    private double y;

    /**
     * Creates a new position at the specified coordinates.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a copy of an existing position.
     *
     * @param position The position to copy
     */
    public Position(Position position) {
        this.x = position.x;
        this.y = position.y;
    }

    /**
     * Creates a position at the origin (0,0).
     */
    public Position() {
        this(0, 0);
    }

    /**
     * Gets the x-coordinate.
     *
     * @return The x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate.
     *
     * @return The y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the x-coordinate.
     *
     * @param x The new x-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate.
     *
     * @param y The new y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Sets both coordinates at once.
     *
     * @param x The new x-coordinate
     * @param y The new y-coordinate
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Moves this position by the specified amount.
     *
     * @param dx The amount to move along the x-axis
     * @param dy The amount to move along the y-axis
     * @return This position after moving (for method chaining)
     */
    public Position move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        return this;
    }

    /**
     * Creates a new position by adding the specified offsets to this position.
     *
     * @param dx The x-offset
     * @param dy The y-offset
     * @return A new position at (x+dx, y+dy)
     */
    public Position add(double dx, double dy) {
        return new Position(this.x + dx, this.y + dy);
    }

    /**
     * Creates a new position by adding another position to this one.
     *
     * @param other The other position to add
     * @return A new position at (x+other.x, y+other.y)
     */
    public Position add(Position other) {
        return new Position(this.x + other.x, this.y + other.y);
    }

    /**
     * Calculates the distance to another position.
     *
     * @param other The other position
     * @return The Euclidean distance between the positions
     */
    public double distanceTo(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculates the Manhattan distance to another position.
     * This is often used for grid-based movement.
     *
     * @param other The other position
     * @return The Manhattan distance (|x1-x2| + |y1-y2|)
     */
    public double manhattanDistanceTo(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /**
     * Creates a position by interpolating between this position and another.
     *
     * @param other The other position
     * @param t The interpolation factor (0.0 = this position, 1.0 = other position)
     * @return A new interpolated position
     */
    public Position interpolate(Position other, double t) {
        double newX = this.x + (other.x - this.x) * t;
        double newY = this.y + (other.y - this.y) * t;
        return new Position(newX, newY);
    }

    /**
     * Checks if this position is within range of another position.
     *
     * @param other The other position
     * @param range The maximum distance
     * @return true if the distance is less than or equal to the range
     */
    public boolean isWithinRange(Position other, double range) {
        return distanceTo(other) <= range;
    }

    /**
     * Gets the direction to another position as a normalized vector.
     *
     * @param other The target position
     * @return A new position representing the direction vector
     */
    public Position directionTo(Position other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            return new Position(dx / length, dy / length);
        } else {
            return new Position(0, 0);
        }
    }

    /**
     * Creates a position representing a grid cell location.
     *
     * @param gridX The grid x-coordinate
     * @param gridY The grid y-coordinate
     * @param cellSize The size of each grid cell
     * @return A new position at the center of the specified grid cell
     */
    public static Position fromGrid(int gridX, int gridY, double cellSize) {
        return new Position(gridX * cellSize + cellSize / 2, gridY * cellSize + cellSize / 2);
    }

    /**
     * Converts this position to grid coordinates.
     *
     * @param cellSize The size of each grid cell
     * @return An int array where [0] is the grid x and [1] is the grid y
     */
    public int[] toGrid(double cellSize) {
        int gridX = (int)(x / cellSize);
        int gridY = (int)(y / cellSize);
        return new int[] { gridX, gridY };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Double.compare(position.x, x) == 0 && Double.compare(position.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position(" + x + ", " + y + ")";
    }
}