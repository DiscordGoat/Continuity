package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerDataManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TacticalRetreat implements Listener {

    private final PlayerDataManager playerData;
    private final JavaPlugin plugin;
    private final Map<UUID, Double> playerHealthMap = new HashMap<>();
    private final long COOLDOWN_TIME = 30000; // 30 seconds cooldown
    private final Map<UUID, Long> lastActivationTime = new HashMap<>();

    public TacticalRetreat(JavaPlugin plugin, PlayerDataManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Only apply to players
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();

        // Check if player has the perk
        if (!playerData.hasPerk(playerId, "Tactical Retreat")) return;

        // Check if this is the first damage event for this player
        if (!playerHealthMap.containsKey(playerId)) {
            playerHealthMap.put(playerId, player.getHealth());
            return;
        }

        // Get player's health before this damage
        double previousHealth = playerHealthMap.get(playerId);

        // Calculate max health using the attribute system
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double healthThreshold = maxHealth * 0.3;

        // Calculate how much damage this event will cause
        double damageTaken = Math.min(previousHealth, event.getFinalDamage());

        // Check if the damage is at least 30% of the player's max health
        if (damageTaken >= healthThreshold) {
            // Check if the cooldown has expired
            long currentTime = System.currentTimeMillis();
            Long lastActivation = lastActivationTime.get(playerId);

            if (lastActivation == null || (currentTime - lastActivation) >= COOLDOWN_TIME) {
                // Activate Tactical Retreat effect
                activateTacticalRetreat(player);

                // Update last activation time
                lastActivationTime.put(playerId, currentTime);

                // Send message to player
                player.sendMessage("§b§lTACTICAL RETREAT ACTIVATED!");
            }
        }

        // Update player's health in our tracking map for next time
        playerHealthMap.put(playerId, Math.max(0, player.getHealth() - event.getFinalDamage()));
    }

    private void activateTacticalRetreat(Player player) {
        // Get nearby entities that might be targeting the player
        for (Entity entity : player.getNearbyEntities(16, 16, 16)) {
            if (entity instanceof Monster && !(entity instanceof Player)) {
                Monster monster = (Monster) entity;

                // Check if this entity has the player as its target
                if (monster.getTarget() == player) {
                    // Set the target to null to interrupt attack
                    monster.setTarget(null);
                }
            }
        }

        // Apply invisibility for 5 seconds
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                5 * 20, // 5 seconds
                0,      // Level 1
                false,  // No ambient particles
                false,  // No icon
                true    // Show particles
        ));

        // Apply Speed II for 5 seconds
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                5 * 20, // 5 seconds
                1,      // Speed II (0-indexed)
                false,  // No ambient
                true,   // Show icon
                true    // Show particles
        ));

        // Visual and sound effects
        player.getWorld().playSound(player.getLocation(), "entity.enderman.teleport", 1.0f, 1.0f);
        player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);

        // Remove the invisibility after 5 seconds to ensure proper cleanup
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("§b§lTactical retreat effect ended.");
            }
        }.runTaskLater(plugin, 5 * 20); // 5 seconds in ticks
    }
}