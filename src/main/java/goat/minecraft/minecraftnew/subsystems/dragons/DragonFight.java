package goat.minecraft.minecraftnew.subsystems.dragons;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.scheduler.BukkitTask;

/**
 * Stores runtime state for an active dragon fight.
 */
public class DragonFight {
    private final Dragon type;
    private final EnderDragon dragon;
    private final double maxHealth;
    private double currentHealth;
    private final int baseRage;
    private final int flightSpeed;
    private BossBar bossBar;
    private BukkitTask decisionTask;

    public DragonFight(Dragon type, EnderDragon dragon) {
        this.type = type;
        this.dragon = dragon;
        this.maxHealth = type.getMaxHealth();
        this.currentHealth = this.maxHealth;
        this.baseRage = type.getBaseRage();
        this.flightSpeed = type.getFlightSpeed();
    }

    public Dragon getType() { return type; }
    public EnderDragon getDragon() { return dragon; }
    public double getMaxHealth() { return maxHealth; }
    public double getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(double health) { this.currentHealth = health; }
    public int getBaseRage() { return baseRage; }
    public int getFlightSpeed() { return flightSpeed; }
    public BossBar getBossBar() { return bossBar; }
    public void setBossBar(BossBar bar) { this.bossBar = bar; }
    public BukkitTask getDecisionTask() { return decisionTask; }
    public void setDecisionTask(BukkitTask task) { this.decisionTask = task; }
}
