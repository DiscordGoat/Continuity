package goat.minecraft.minecraftnew.subsystems.combat.notification;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing damage notification displays.
 * Creates floating damage indicators with proper resource management.
 */
public class DamageNotificationService {
    
    private static final Logger logger = Logger.getLogger(DamageNotificationService.class.getName());
    private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#.#");
    
    private final JavaPlugin plugin;
    private final CombatConfiguration.NotificationConfig config;
    private final Random random;
    private final Map<ArmorStand, BukkitTask> activeIndicators;
    
    public DamageNotificationService(JavaPlugin plugin, CombatConfiguration.NotificationConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.random = new Random();
        this.activeIndicators = new ConcurrentHashMap<>();
    }
    
    /**
     * Shows a damage indicator at the specified location.
     * 
     * @param location The location to display the indicator
     * @param damage The damage amount to display
     */
    public void showDamageIndicator(Location location, double damage) {
        if (!config.isEnabled() || location == null || damage <= 0) {
            return;
        }
        
        try {
            String damageText = DAMAGE_FORMAT.format(damage);
            ChatColor color = getDamageColor(damage);
            String displayText = color + "✧ " + damageText + " ✧";
            
            Location spawnLocation = calculateSpawnLocation(location);
            ArmorStand indicator = createDamageIndicator(spawnLocation, displayText);
            
            if (indicator != null) {
                startIndicatorAnimation(indicator);
                logger.finest(String.format("Created damage indicator: %.1f at %s", damage, location));
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to create damage indicator", e);
        }
    }
    
    /**
     * Manually creates a damage indicator (for custom damage sources).
     * 
     * @param location The location to display the indicator
     * @param damage The damage amount to display
     */
    public void createCustomDamageIndicator(Location location, double damage) {
        showDamageIndicator(location, damage);
    }
    
    /**
     * Cleans up all active damage indicators.
     * Should be called when the plugin is disabled.
     */
    public void cleanup() {
        logger.info(String.format("Cleaning up %d active damage indicators", activeIndicators.size()));
        
        activeIndicators.forEach((indicator, task) -> {
            try {
                if (task != null && !task.isCancelled()) {
                    task.cancel();
                }
                if (indicator != null && indicator.isValid()) {
                    indicator.remove();
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error cleaning up damage indicator", e);
            }
        });
        
        activeIndicators.clear();
    }
    
    /**
     * Gets the number of currently active indicators.
     * 
     * @return The count of active indicators
     */
    public int getActiveIndicatorCount() {
        return activeIndicators.size();
    }
    
    /**
     * Determines the color for damage text based on damage amount.
     */
    private ChatColor getDamageColor(double damage) {
        if (damage < config.getLowThreshold()) {
            return ChatColor.WHITE;
        } else if (damage < config.getMediumThreshold()) {
            return ChatColor.GREEN;
        } else if (damage < config.getHighThreshold()) {
            return ChatColor.BLUE;
        } else if (damage < config.getCriticalThreshold()) {
            return ChatColor.LIGHT_PURPLE;
        } else {
            return ChatColor.GOLD;
        }
    }
    
    /**
     * Calculates a randomized spawn location for the damage indicator.
     */
    private Location calculateSpawnLocation(Location baseLocation) {
        double xOffset = (random.nextDouble() - 0.5) * config.getRandomOffset();
        double yOffset = config.getVerticalOffsetBase() + (random.nextDouble() * config.getVerticalOffsetRandom());
        double zOffset = (random.nextDouble() - 0.5) * config.getRandomOffset();
        
        return baseLocation.clone().add(xOffset, yOffset, zOffset);
    }
    
    /**
     * Creates the armor stand entity for the damage indicator.
     */
    private ArmorStand createDamageIndicator(Location location, String displayText) {
        try {
            if (location.getWorld() == null) {
                logger.warning("Cannot create damage indicator: world is null");
                return null;
            }
            
            ArmorStand indicator = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            indicator.setVisible(false);
            indicator.setGravity(false);
            indicator.setMarker(true);
            indicator.setCustomName(displayText);
            indicator.setCustomNameVisible(true);
            indicator.setSmall(true);
            indicator.setBasePlate(false);
            indicator.setArms(false);
            
            return indicator;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to create armor stand for damage indicator", e);
            return null;
        }
    }
    
    /**
     * Starts the animation for a damage indicator.
     */
    private void startIndicatorAnimation(ArmorStand indicator) {
        BukkitTask animationTask = new DamageIndicatorAnimation(indicator).runTaskTimer(plugin, 0L, 1L);
        activeIndicators.put(indicator, animationTask);
    }
    
    /**
     * Animation runnable for damage indicators.
     */
    private class DamageIndicatorAnimation extends BukkitRunnable {
        
        private final ArmorStand indicator;
        private int ticks = 0;
        private final int maxTicks;
        private final int fadeStartTick;
        
        public DamageIndicatorAnimation(ArmorStand indicator) {
            this.indicator = indicator;
            this.maxTicks = config.getDisplayDuration();
            this.fadeStartTick = (int) (maxTicks * config.getFadeStartPercentage());
        }
        
        @Override
        public void run() {
            try {
                // Check if indicator is still valid
                if (indicator == null || indicator.isDead() || !indicator.isValid()) {
                    cleanup();
                    return;
                }
                
                // Check if animation should end
                if (ticks >= maxTicks) {
                    cleanup();
                    return;
                }
                
                // Move indicator upward
                Location currentLocation = indicator.getLocation();
                currentLocation.add(0, config.getUpwardMovement(), 0);
                indicator.teleport(currentLocation);
                
                // Apply fade effect
                if (ticks > fadeStartTick) {
                    boolean visible = ticks % 2 == 0; // Blinking effect
                    indicator.setCustomNameVisible(visible);
                }
                
                ticks++;
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error in damage indicator animation", e);
                cleanup();
            }
        }
        
        private void cleanup() {
            try {
                activeIndicators.remove(indicator);
                
                if (indicator != null && indicator.isValid()) {
                    indicator.remove();
                }
                
                this.cancel();
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error cleaning up damage indicator animation", e);
            }
        }
    }
}