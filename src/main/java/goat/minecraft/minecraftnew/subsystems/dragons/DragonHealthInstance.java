package goat.minecraft.minecraftnew.subsystems.dragons;

import java.util.UUID;

/**
 * Simple container for tracking a dragon's custom health.  All damage to the
 * dragon is redirected here, allowing us to control death and boss bar updates
 * independent from the vanilla Ender Dragon health system.
 */
public class DragonHealthInstance {

    private final UUID dragonId;
    private final double maxHealth;
    private double currentHealth;

    public DragonHealthInstance(UUID dragonId, double maxHealth) {
        this.dragonId = dragonId;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public UUID getDragonId() {
        return dragonId;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getCurrentHealth() {
        return currentHealth;
    }

    public void damage(double amount) {
        currentHealth = Math.max(0, currentHealth - amount);
    }

    public double getHealthPercentage() {
        return currentHealth / maxHealth;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }
}
