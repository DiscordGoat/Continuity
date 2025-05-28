package goat.minecraft.minecraftnew.subsystems.combat.hostility;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing player hostility tiers.
 * Handles persistence, validation, and tier calculations.
 */
public class HostilityService {
    
    private static final Logger logger = Logger.getLogger(HostilityService.class.getName());
    private static final String CONFIG_KEY = "playerTiers";
    
    private final JavaPlugin plugin;
    private final CombatConfiguration.HostilityConfig config;
    private final File hostilityFile;
    private final YamlConfiguration hostilityConfig;
    
    public HostilityService(JavaPlugin plugin, CombatConfiguration.HostilityConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.hostilityFile = new File(plugin.getDataFolder(), "hostility.yml");
        this.hostilityConfig = YamlConfiguration.loadConfiguration(hostilityFile);
        
        initializeHostilityFile();
    }
    
    /**
     * Gets the current difficulty tier for a player.
     * 
     * @param player The player to check
     * @return The player's current tier (1-10)
     */
    public int getPlayerTier(Player player) {
        if (player == null) {
            return config.getDefaultTier();
        }
        
        UUID uuid = player.getUniqueId();
        String path = CONFIG_KEY + "." + uuid.toString();
        int tier = hostilityConfig.getInt(path, config.getDefaultTier());
        
        // Validate tier is within bounds
        return Math.max(1, Math.min(config.getMaxTier(), tier));
    }
    
    /**
     * Sets the difficulty tier for a player.
     * 
     * @param player The player to set the tier for
     * @param tier The tier to set (1-10)
     * @return true if the tier was set successfully, false otherwise
     */
    public boolean setPlayerTier(Player player, int tier) {
        if (player == null || !isValidTier(tier)) {
            return false;
        }
        
        try {
            UUID uuid = player.getUniqueId();
            String path = CONFIG_KEY + "." + uuid.toString();
            
            hostilityConfig.set(path, tier);
            saveConfig();
            
            logger.fine(String.format("Set hostility tier for %s to %d", player.getName(), tier));
            return true;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                      String.format("Failed to set hostility tier for %s", player.getName()), e);
            return false;
        }
    }
    
    /**
     * Checks if a tier is unlocked for a player based on their level.
     * 
     * @param player The player to check
     * @param tier The tier to check
     * @return true if the tier is unlocked, false otherwise
     */
    public boolean isTierUnlocked(Player player, int tier) {
        if (player == null || !isValidTier(tier)) {
            return false;
        }
        
        int requiredLevel = (tier - 1) * config.getLevelPerTier();
        return player.getLevel() >= requiredLevel;
    }
    
    /**
     * Gets the level requirement for a specific tier.
     * 
     * @param tier The tier to check
     * @return The required level for the tier
     */
    public int getRequiredLevel(int tier) {
        if (!isValidTier(tier)) {
            return 0;
        }
        return (tier - 1) * config.getLevelPerTier();
    }
    
    /**
     * Gets the maximum unlocked tier for a player.
     * 
     * @param player The player to check
     * @return The highest tier the player has unlocked
     */
    public int getMaxUnlockedTier(Player player) {
        if (player == null) {
            return 1;
        }
        
        int playerLevel = player.getLevel();
        int maxTier = 1 + (playerLevel / config.getLevelPerTier());
        
        return Math.min(maxTier, config.getMaxTier());
    }
    
    /**
     * Asynchronously saves the hostility configuration.
     * 
     * @return A CompletableFuture that completes when the save operation is done
     */
    public CompletableFuture<Boolean> saveConfigAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                hostilityConfig.save(hostilityFile);
                return true;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save hostility configuration", e);
                return false;
            }
        });
    }
    
    /**
     * Synchronously saves the hostility configuration.
     */
    public void saveConfig() {
        try {
            hostilityConfig.save(hostilityFile);
            logger.finest("Hostility configuration saved successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save hostility configuration", e);
            throw new HostilityServiceException("Could not save hostility configuration", e);
        }
    }
    
    /**
     * Reloads the hostility configuration from disk.
     */
    public void reloadConfig() {
        try {
            hostilityConfig.load(hostilityFile);
            logger.info("Hostility configuration reloaded successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reload hostility configuration", e);
            throw new HostilityServiceException("Could not reload hostility configuration", e);
        }
    }
    
    /**
     * Validates if a tier number is within acceptable bounds.
     */
    private boolean isValidTier(int tier) {
        return tier >= 1 && tier <= config.getMaxTier();
    }
    
    /**
     * Initializes the hostility configuration file if it doesn't exist.
     */
    private void initializeHostilityFile() {
        if (!hostilityFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                hostilityFile.createNewFile();
                logger.info("Created hostility configuration file");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not create hostility configuration file", e);
                throw new HostilityServiceException("Failed to initialize hostility file", e);
            }
        }
    }
    
    /**
     * Gets configuration values for external use.
     */
    public CombatConfiguration.HostilityConfig getConfig() {
        return config;
    }
    
    /**
     * Exception thrown when hostility service operations fail.
     */
    public static class HostilityServiceException extends RuntimeException {
        public HostilityServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}