package org.bpm.wizardsurvival.entities;

import org.bpm.wizardsurvival.engine.CharacterClass;
import org.bpm.wizardsurvival.engine.Skill;
import org.bpm.wizardsurvival.engine.SurvivalActivity;
import org.bpm.wizardsurvival.items.*;
import org.bpm.wizardsurvival.magic.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;

public class Player {
    private String name;
    private CharacterClass characterClass;
    private Raft raft;
    private Inventory inventory;
    private Point position;
    private List<Spell> knownSpells;
    private List<Recipe> knownRecipes;

    // Skills
    private float strength;
    private float dexterity;
    private float intelligence;
    private float experience;
    private float magic;
    private float charisma;

    // Status
    private float health;
    private float hunger;
    private float thirst;
    private float energy;

    public Player(CharacterClass characterClass) {
        this.characterClass = characterClass;
        this.raft = new Raft();
        this.inventory = new Inventory(20); // Start with 20 slots
        this.position = new Point(0, 0); // Start at origin
        this.knownSpells = new ArrayList<>();
        this.knownRecipes = new ArrayList<>();

        // Initialize skills based on class
        initializeSkills();

        // Full status at start
        this.health = 100.0f;
        this.hunger = 100.0f;
        this.thirst = 100.0f;
        this.energy = 100.0f;
    }

    private void initializeSkills() {
        // Base values for all classes
        this.strength = 5.0f;
        this.dexterity = 5.0f;
        this.intelligence = 5.0f;
        this.experience = 0.0f;
        this.magic = 5.0f;
        this.charisma = 5.0f;

        // Apply class modifiers
        switch (characterClass) {
            case WARRIOR:
                this.strength += 3.0f;
                this.dexterity += 1.0f;
                break;
            case MAGE:
                this.intelligence += 3.0f;
                this.magic += 2.0f;
                break;
            case ROGUE:
                this.dexterity += 3.0f;
                this.charisma += 1.0f;
                break;
            // Add more classes as needed
        }
    }

    public void increaseSkill(Skill skill, float amount) {
        switch (skill) {
            case STRENGTH:
                this.strength += amount;
                break;
            case DEXTERITY:
                this.dexterity += amount;
                break;
            case INTELLIGENCE:
                this.intelligence += amount;
                break;
            case EXPERIENCE:
                this.experience += amount;
                break;
            case MAGIC:
                this.magic += amount;
                break;
            case CHARISMA:
                this.charisma += amount;
                break;
        }
    }

    public float getRelevantSkillBonus(SurvivalActivity activity) {
        Skill relevantSkill = activity.getRelevantSkill();
        float skillValue = getSkillValue(relevantSkill);

        // Convert skill to success rate bonus (diminishing returns)
        return (float) (0.05 * Math.log(skillValue + 1));
    }

    private float getSkillValue(Skill skill) {
        switch (skill) {
            case STRENGTH: return strength;
            case DEXTERITY: return dexterity;
            case INTELLIGENCE: return intelligence;
            case EXPERIENCE: return experience;
            case MAGIC: return magic;
            case CHARISMA: return charisma;
            default: return 0.0f;
        }
    }

    public void move(int dx, int dy) {
        position.translate(dx, dy);
        consumeEnergy(1.0f);
    }

    public void dive() {
        // Diving consumes more energy
        consumeEnergy(5.0f);
    }

    public void consumeEnergy(float amount) {
        energy -= amount;
        if (energy < 30.0f) {
            // Low energy affects other stats
            hunger -= amount * 0.5f;
            thirst -= amount * 0.5f;
        }
    }

    public void eat(Food food) {
        hunger += food.getNutritionValue();
        health += food.getHealthValue();

        // Cap values
        hunger = Math.min(hunger, 100.0f);
        health = Math.min(health, 100.0f);
    }

    public void drink(float amount) {
        thirst += amount;
        thirst = Math.min(thirst, 100.0f);
    }

    public void rest(float hours) {
        energy += hours * 10.0f;
        energy = Math.min(energy, 100.0f);
    }

    public void updateRaftCondition() {
        // Raft deteriorates over time
        raft.deteriorate(0.5f);
    }

    public void learnRecipe(Recipe recipe) {
        if (!knownRecipes.contains(recipe)) {
            knownRecipes.add(recipe);
        }
    }

    public void learnSpell(Spell spell) {
        if (!knownSpells.contains(spell)) {
            knownSpells.add(spell);
        }
    }

    public void retainAbilities() {
        // Called when restarting after trial failure
        // Keep spells, recipes, and skill levels
    }

    // Getters and setters
    public CharacterClass getCharacterClass() { return characterClass; }
    public Raft getRaft() { return raft; }
    public Inventory getInventory() { return inventory; }
    public Point getPosition() { return position; }
    public List<Spell> getKnownSpells() { return knownSpells; }
    public List<Recipe> getKnownRecipes() { return knownRecipes; }

    public float getEnergy() { return energy;
    }

    public void update(float deltaTime) {
    }

    public boolean isAlive() {
        return true;
    }

    public void moveNorth() {
    }

    public void moveSouth() {
    }

    public void moveWest() {
    }

    public void moveEast() {
    }
}
