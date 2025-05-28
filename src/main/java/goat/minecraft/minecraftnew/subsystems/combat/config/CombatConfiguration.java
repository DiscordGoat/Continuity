package goat.minecraft.minecraftnew.subsystems.combat.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration management for the combat subsystem.
 * Handles loading and accessing all combat-related configuration values.
 */
public class CombatConfiguration {
    
    private static final String CONFIG_FILE_NAME = "combat.yml";
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private YamlConfiguration config;
    private File configFile;
    
    // Cached configuration values for performance
    private DamageConfig damageConfig;
    private NotificationConfig notificationConfig;
    private HostilityConfig hostilityConfig;
    private SoundConfig soundConfig;
    private BuffConfig buffConfig;
    
    public CombatConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadConfiguration();
    }
    
    /**
     * Loads the combat configuration from the config file.
     * Creates the file with defaults if it doesn't exist.
     */
    public void loadConfiguration() {
        try {
            configFile = new File(plugin.getDataFolder(), CONFIG_FILE_NAME);
            
            if (!configFile.exists()) {
                createDefaultConfig();
            }
            
            config = YamlConfiguration.loadConfiguration(configFile);
            cacheConfigValues();
            
            logger.info("Combat configuration loaded successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load combat configuration", e);
            throw new CombatConfigurationException("Could not load combat configuration", e);
        }
    }
    
    /**
     * Creates the default configuration file from the resource in the JAR.
     * Only creates if the file doesn't already exist.
     */
    private void createDefaultConfig() throws IOException {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Only create if file doesn't exist - preserve existing configs
        if (configFile.exists()) {
            logger.info("Combat configuration file already exists, preserving existing settings");
            return;
        }
        
        try (InputStream defaultConfig = plugin.getResource(CONFIG_FILE_NAME)) {
            if (defaultConfig != null) {
                Files.copy(defaultConfig, configFile.toPath());
                logger.info("Created default combat configuration file");
            } else {
                logger.warning("Default combat configuration not found in JAR, creating empty file");
                configFile.createNewFile();
            }
        }
    }
    
    /**
     * Caches configuration values for better performance.
     */
    private void cacheConfigValues() {
        damageConfig = new DamageConfig(config);
        notificationConfig = new NotificationConfig(config);
        hostilityConfig = new HostilityConfig(config);
        soundConfig = new SoundConfig(config);
        buffConfig = new BuffConfig(config);
    }
    
    /**
     * Reloads the configuration from disk.
     */
    public void reload() {
        loadConfiguration();
    }
    
    /**
     * Saves the current configuration to disk.
     */
    public void save() {
        try {
            config.save(configFile);
            logger.info("Combat configuration saved successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save combat configuration", e);
            throw new CombatConfigurationException("Could not save combat configuration", e);
        }
    }
    
    // Getters for configuration sections
    public DamageConfig getDamageConfig() { return damageConfig; }
    public NotificationConfig getNotificationConfig() { return notificationConfig; }
    public HostilityConfig getHostilityConfig() { return hostilityConfig; }
    public SoundConfig getSoundConfig() { return soundConfig; }
    public BuffConfig getBuffConfig() { return buffConfig; }
    
    /**
     * Damage calculation configuration
     */
    public static class DamageConfig {
        private final double meleePerLevel;
        private final double rangedPerLevel;
        private final double monsterPerLevel;
        private final int maxSkillLevel;
        private final double recurveDamageBonus;
        
        public DamageConfig(YamlConfiguration config) {
            this.meleePerLevel = config.getDouble("damage.multipliers.melee_per_level", 0.03);
            this.rangedPerLevel = config.getDouble("damage.multipliers.ranged_per_level", 0.04);
            this.monsterPerLevel = config.getDouble("damage.multipliers.monster_per_level", 0.06);
            this.maxSkillLevel = config.getInt("damage.multipliers.max_skill_level", 100);
            this.recurveDamageBonus = config.getDouble("damage.potions.recurve_damage_bonus", 1.25);
        }
        
        public double getMeleePerLevel() { return meleePerLevel; }
        public double getRangedPerLevel() { return rangedPerLevel; }
        public double getMonsterPerLevel() { return monsterPerLevel; }
        public int getMaxSkillLevel() { return maxSkillLevel; }
        public double getRecurveDamageBonus() { return recurveDamageBonus; }
    }
    
    /**
     * Damage notification configuration
     */
    public static class NotificationConfig {
        private final boolean enabled;
        private final int displayDuration;
        private final double upwardMovement;
        private final double fadeStartPercentage;
        private final double randomOffset;
        private final double verticalOffsetBase;
        private final double verticalOffsetRandom;
        private final double lowThreshold;
        private final double mediumThreshold;
        private final double highThreshold;
        private final double criticalThreshold;
        
        public NotificationConfig(YamlConfiguration config) {
            this.enabled = config.getBoolean("notifications.enabled", true);
            this.displayDuration = config.getInt("notifications.display_duration", 20);
            this.upwardMovement = config.getDouble("notifications.animation.upward_movement_per_tick", 0.05);
            this.fadeStartPercentage = config.getDouble("notifications.animation.fade_start_percentage", 0.7);
            this.randomOffset = config.getDouble("notifications.animation.random_position_offset", 0.5);
            this.verticalOffsetBase = config.getDouble("notifications.animation.vertical_offset_base", 1.0);
            this.verticalOffsetRandom = config.getDouble("notifications.animation.vertical_offset_random", 0.5);
            this.lowThreshold = config.getDouble("notifications.colors.low_damage_threshold", 20.0);
            this.mediumThreshold = config.getDouble("notifications.colors.medium_damage_threshold", 40.0);
            this.highThreshold = config.getDouble("notifications.colors.high_damage_threshold", 80.0);
            this.criticalThreshold = config.getDouble("notifications.colors.critical_damage_threshold", 140.0);
        }
        
        public boolean isEnabled() { return enabled; }
        public int getDisplayDuration() { return displayDuration; }
        public double getUpwardMovement() { return upwardMovement; }
        public double getFadeStartPercentage() { return fadeStartPercentage; }
        public double getRandomOffset() { return randomOffset; }
        public double getVerticalOffsetBase() { return verticalOffsetBase; }
        public double getVerticalOffsetRandom() { return verticalOffsetRandom; }
        public double getLowThreshold() { return lowThreshold; }
        public double getMediumThreshold() { return mediumThreshold; }
        public double getHighThreshold() { return highThreshold; }
        public double getCriticalThreshold() { return criticalThreshold; }
    }
    
    /**
     * Hostility system configuration
     */
    public static class HostilityConfig {
        private final int levelPerTier;
        private final int maxTier;
        private final int defaultTier;
        private final String guiTitle;
        private final int guiSize;
        private final String unlockedMaterial;
        private final String lockedMaterial;
        private final String borderMaterial;
        private final String closeMaterial;
        
        public HostilityConfig(YamlConfiguration config) {
            this.levelPerTier = config.getInt("hostility.level_per_tier", 10);
            this.maxTier = config.getInt("hostility.max_tier", 10);
            this.defaultTier = config.getInt("hostility.default_tier", 1);
            this.guiTitle = config.getString("hostility.gui.title", "§4Select Hostility Tier");
            this.guiSize = config.getInt("hostility.gui.size", 54);
            this.unlockedMaterial = config.getString("hostility.gui.unlocked_material", "RED_STAINED_GLASS_PANE");
            this.lockedMaterial = config.getString("hostility.gui.locked_material", "GRAY_STAINED_GLASS_PANE");
            this.borderMaterial = config.getString("hostility.gui.border_material", "BLACK_STAINED_GLASS_PANE");
            this.closeMaterial = config.getString("hostility.gui.close_material", "BARRIER");
        }
        
        public int getLevelPerTier() { return levelPerTier; }
        public int getMaxTier() { return maxTier; }
        public int getDefaultTier() { return defaultTier; }
        public String getGuiTitle() { return guiTitle; }
        public int getGuiSize() { return guiSize; }
        public String getUnlockedMaterial() { return unlockedMaterial; }
        public String getLockedMaterial() { return lockedMaterial; }
        public String getBorderMaterial() { return borderMaterial; }
        public String getCloseMaterial() { return closeMaterial; }
    }
    
    /**
     * Sound configuration
     */
    public static class SoundConfig {
        private final String damageBoostSound;
        private final float volume;
        private final float pitch;
        
        public SoundConfig(YamlConfiguration config) {
            this.damageBoostSound = config.getString("sounds.damage_boost", "ENTITY_ARROW_HIT_PLAYER");
            this.volume = (float) config.getDouble("sounds.volume", 1.0);
            this.pitch = (float) config.getDouble("sounds.pitch", 1.0);
        }
        
        public String getDamageBoostSound() { return damageBoostSound; }
        public float getVolume() { return volume; }
        public float getPitch() { return pitch; }
    }
    
    /**
     * Combat buffs configuration
     */
    public static class BuffConfig {
        private final boolean skillDamageScaling;
        private final boolean potionInteractions;
        private final boolean monsterLevelScaling;
        private final boolean projectileBonuses;
        
        public BuffConfig(YamlConfiguration config) {
            this.skillDamageScaling = config.getBoolean("buffs.skill_damage_scaling", true);
            this.potionInteractions = config.getBoolean("buffs.potion_interactions", true);
            this.monsterLevelScaling = config.getBoolean("buffs.monster_level_scaling", true);
            this.projectileBonuses = config.getBoolean("buffs.projectile_bonuses", true);
        }
        
        public boolean isSkillDamageScaling() { return skillDamageScaling; }
        public boolean isPotionInteractions() { return potionInteractions; }
        public boolean isMonsterLevelScaling() { return monsterLevelScaling; }
        public boolean isProjectileBonuses() { return projectileBonuses; }
    }
    
    /**
     * Exception thrown when configuration operations fail
     */
    public static class CombatConfigurationException extends RuntimeException {
        public CombatConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}