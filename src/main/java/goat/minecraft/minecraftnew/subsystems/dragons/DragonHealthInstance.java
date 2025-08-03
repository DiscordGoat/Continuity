package goat.minecraft.minecraftnew.subsystems.dragons;

import org.bukkit.entity.EnderDragon;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks max and current health for a specific dragon. The instance is tied to
 * a dragon's UUID and stored statically for quick lookup during fights.
 */
public class DragonHealthInstance {

    private static final Map<UUID, DragonHealthInstance> INSTANCES = new ConcurrentHashMap<>();

    private final UUID dragonId;
    private final double maxHealth;
    private double currentHealth;

    private DragonHealthInstance(UUID dragonId, double maxHealth) {
        this.dragonId = dragonId;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    /**
     * Creates and stores a new health instance for the supplied dragon.
     */
    public static DragonHealthInstance create(EnderDragon dragon, double maxHealth) {
        DragonHealthInstance instance = new DragonHealthInstance(dragon.getUniqueId(), maxHealth);
        INSTANCES.put(dragon.getUniqueId(), instance);
        return instance;
    }

    /**
     * Fetches the health instance for the given dragon UUID.
     */
    public static DragonHealthInstance get(UUID id) {
        return INSTANCES.get(id);
    }

    /**
     * @return the remaining health percentage (0-1).
     */
    public double getHealthPercentage() {
        if (maxHealth <= 0) return 0;
        return currentHealth / maxHealth;
    }

    /**
     * Alias for {@link #getHealthPercentage()} to match external API naming.
     */
    public double getDragonHealthPercentage() {
        return getHealthPercentage();
    }

    public double getCurrentHealth() {
        return currentHealth;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    /**
     * Reduces health by the given amount, clamped to zero.
     */
    public void damage(double amount) {
        currentHealth = Math.max(0, currentHealth - amount);
    }

    /**
     * Removes the stored instance for this dragon.
     */
    public void remove() {
        INSTANCES.remove(dragonId);
    }
}
