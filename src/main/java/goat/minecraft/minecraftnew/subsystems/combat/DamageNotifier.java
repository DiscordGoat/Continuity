package goat.minecraft.minecraftnew.subsystems.combat;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.Random;

public class DamageNotifier implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final DecimalFormat df = new DecimalFormat("#.#");

    public DamageNotifier(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Skip if the damage was cancelled or is 0
        if (event.isCancelled() || event.getDamage() <= 0) {
            return;
        }

        // Get the entity that was damaged
        Entity damaged = event.getEntity();
        
        // Skip if the damaged entity is not a living entity (like an item frame)
        if (!(damaged instanceof LivingEntity)) {
            return;
        }

        // Get the damager
        Entity damager = event.getDamager();
        Player attacker = null;

        // Handle different types of damage sources
        if (damager instanceof Player) {
            // Direct player attack
            attacker = (Player) damager;
        } else if (damager instanceof Projectile) {
            // Projectile attack (arrows, tridents, etc.)
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        } else if (damager instanceof TNTPrimed && ((TNTPrimed) damager).getSource() instanceof Player) {
            // TNT placed by player
            attacker = (Player) ((TNTPrimed) damager).getSource();
        } else if (damager instanceof AreaEffectCloud && ((AreaEffectCloud) damager).getSource() instanceof Player) {
            // Area effect cloud created by player
            attacker = (Player) ((AreaEffectCloud) damager).getSource();
        }

        // Only show damage indicators for player-caused damage
        if (attacker != null) {
            double damage = event.getFinalDamage();
            spawnDamageIndicator(damaged.getLocation(), damage);
        }
    }

    // Handle special case for loyal sword or other custom projectiles
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() != null && event.getEntity().hasMetadata("loyalSword")) {
            // This is a special case for the loyal sword
            // The actual damage will be handled by the EntityDamageByEntityEvent
            // This is just here for extensibility if needed
        }
    }

    /**
     * Spawns a damage indicator at the specified location
     * 
     * @param location The location to spawn the indicator
     * @param damage The amount of damage to display
     */
    public void spawnDamageIndicator(Location location, double damage) {
        // Format the damage to one decimal place
        String damageText = df.format(damage);
        
        // Determine color based on damage amount
        ChatColor color;
        if (damage < 20) {
            color = ChatColor.WHITE;
        } else if (damage < 40) {
            color = ChatColor.GREEN;
        } else if (damage < 80) {
            color = ChatColor.BLUE;
        } else if (damage < 140) {
            color = ChatColor.LIGHT_PURPLE;
        } else {
            color = ChatColor.GOLD;
        }

        // Add a fancy format to the damage text
        String displayText = color + "✧ " + damageText + " ✧";
        
        // Create a slightly randomized location for the damage indicator
        Location spawnLoc = location.clone().add(
                (random.nextDouble() - 0.5) * 0.5,  // Random X offset
                random.nextDouble() * 0.5 + 1.0,    // Random Y offset (above entity)
                (random.nextDouble() - 0.5) * 0.5   // Random Z offset
        );

        // Spawn an invisible armor stand with the damage text
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setCustomName(displayText);
        armorStand.setCustomNameVisible(true);
        armorStand.setSmall(true);

        // Create animation for the damage indicator
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 20; // 1 second at 20 ticks per second
            
            @Override
            public void run() {
                if (ticks >= maxTicks || armorStand.isDead()) {
                    armorStand.remove();
                    this.cancel();
                    return;
                }
                
                // Move the armor stand upward slowly
                armorStand.teleport(armorStand.getLocation().add(0, 0.05, 0));
                
                // Make it fade out by changing its name visibility
                if (ticks > maxTicks * 0.7) {
                    // Only hide every other tick for a blinking effect
                    armorStand.setCustomNameVisible(ticks % 2 == 0);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Manually spawn a damage indicator at a location
     * Useful for custom damage sources not caught by events
     * 
     * @param location The location to spawn the indicator
     * @param damage The amount of damage to display
     */
    public void manualDamageIndicator(Location location, double damage) {
        spawnDamageIndicator(location, damage);
    }
}