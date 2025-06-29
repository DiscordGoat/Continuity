package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Simplified manager for determining player hostility tiers.
 * The tier now depends solely on Forestry notoriety and is no longer
 * persisted to disk.
 */
public class HostilityManager {

    private static HostilityManager instance;
    private final JavaPlugin plugin;

    private HostilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("[HostilityManager] Initialized.");
    }

    public static synchronized HostilityManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new HostilityManager(plugin);
        }
        return instance;
    }

    /**
     * Returns the existing instance without creating a new one.
     */
    public static synchronized HostilityManager getExistingInstance() {
        return instance;
    }

    /**
     * Calculates the player's hostility tier based solely on forestry notoriety.
     *
     * @param player the player to check
     * @return tier from 1 (lowest) to 5 (highest)
     */
    public int getPlayerDifficultyTier(Player player) {
        if (player == null) {
            return 1;
        }
        int notoriety = Forestry.getInstance().getNotoriety(player);
        if (notoriety <= 64) {
            return 1;
        } else if (notoriety <= 192) {
            return 2;
        } else if (notoriety <= 384) {
            return 3;
        } else if (notoriety <= 640) {
            return 4;
        } else {
            return 5;
        }
    }

    /**
     * Legacy method retained for API compatibility. No-op.
     */
    public void setPlayerTier(Player player, int tier) {
        // Hostility tiers now derive from notoriety.
    }

    /**
     * Legacy method retained for API compatibility. No-op.
     */
    public void saveConfig() {
        // No persistent configuration.
    }
}
