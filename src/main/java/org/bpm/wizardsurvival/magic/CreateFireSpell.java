package org.bpm.wizardsurvival.magic;

import org.bpm.wizardsurvival.entities.Player;

public class CreateFireSpell extends Spell {
    public CreateFireSpell() {
        super("Create Fire", "Magically ignites a fire with 100% success rate", 20.0f, 24.0f);
    }

    @Override
    public boolean cast(Player caster) {
        if (isOnCooldown()) {
            return false;
        }

        // Check if player has enough mana/energy
        if (caster.getEnergy() < getManaCost()) {
            return false;
        }

        // Consume mana/energy
        caster.consumeEnergy(getManaCost());

        // Create fire (100% success)
        // Implementation details...

        // Reset cooldown
        resetCooldown();

        return true;
    }
}
