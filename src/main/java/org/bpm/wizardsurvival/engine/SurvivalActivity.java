package org.bpm.wizardsurvival.engine;

import org.bpm.wizardsurvival.entities.Player;

public enum SurvivalActivity {
    FISHING(1/6.0f, Skill.DEXTERITY),
    PRESERVE_MEAT(1/10.0f, Skill.INTELLIGENCE),
    COOK_FOOD(1/4.0f, Skill.INTELLIGENCE),
    GATHER_WOOD(1/2.0f, Skill.STRENGTH),
    GATHER_STONES(1/2.0f, Skill.STRENGTH),
    START_FIRE(1/30000.0f, Skill.INTELLIGENCE),
    CRAFT_ITEM(1/5.0f, Skill.DEXTERITY),
    UPGRADE_RAFT(1/8.0f, Skill.INTELLIGENCE),

    FORAGING(1/8.0f, Skill.INTELLIGENCE),
    DIVE(1/3.0f, Skill.STRENGTH);

    private final float baseSuccessRate;
    private final Skill relevantSkill;

    SurvivalActivity(float baseSuccessRate, Skill relevantSkill) {
        this.baseSuccessRate = baseSuccessRate;
        this.relevantSkill = relevantSkill;
    }

    public float getBaseSuccessRate() {
        return baseSuccessRate;
    }

    public Skill getRelevantSkill() {
        return relevantSkill;
    }

    public void applySuccess(Player player) {
        // Implement specific success effects
    }

    public void applyFailure(Player player) {
        // Implement specific failure effects
    }
}