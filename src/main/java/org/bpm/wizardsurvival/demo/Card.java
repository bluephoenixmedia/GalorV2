package org.bpm.wizardsurvival.demo;

public class Card {
    private String name;
    private String description;
    private CardType type;
    private ActionType actionType; // Only for ACTION cards
    private BuffEffect buffEffect; // Only for PERMANENT cards
    private int value; // Magnitude of effect for both types

    // Constructor for ACTION cards
    public Card(String name, String description, ActionType actionType, int value) {
        this.name = name;
        this.description = description;
        this.type = CardType.ACTION;
        this.actionType = actionType;
        this.value = value;
    }

    // Constructor for PERMANENT cards
    public Card(String name, String description, BuffEffect buffEffect, int value) {
        this.name = name;
        this.description = description;
        this.type = CardType.PERMANENT;
        this.buffEffect = buffEffect;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CardType getType() {
        return type;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public BuffEffect getBuffEffect() {
        return buffEffect;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (type == CardType.ACTION) {
            return name + " (" + value + ")";
        } else {
            return name + " [" + value + "% " + buffEffect.getDescription() + "]";
        }
    }
}