package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import goat.minecraft.minecraftnew.other.health.HealthManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BeaconPassiveEffects implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> swiftApplied = new HashMap<>();

    public BeaconPassiveEffects(JavaPlugin plugin) {
        this.plugin = plugin;
        startPassiveEffectTask();
    }

    private void startPassiveEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updatePassiveEffects(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private void updatePassiveEffects(Player player) {
        boolean hasBeaconPassives = BeaconPassivesGUI.hasBeaconPassives(player);
        
        // Trigger health updates for Mending passive
        HealthManager.updateHealth(player);
        
        // Apply/remove Swift effect (+20% walk speed, -50% fall damage)
        if (hasBeaconPassives && BeaconPassivesGUI.hasPassiveEnabled(player, "swift")) {
            applySwiftEffect(player);
        } else {
            removeSwiftEffect(player);
        }
    }


    private void applySwiftEffect(Player player) {
        UUID playerId = player.getUniqueId();
        if (!swiftApplied.getOrDefault(playerId, false)) {
            // Apply speed effect (20% speed increase)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            swiftApplied.put(playerId, true);
        }
    }

    private void removeSwiftEffect(Player player) {
        UUID playerId = player.getUniqueId();
        if (swiftApplied.getOrDefault(playerId, false)) {
            player.removePotionEffect(PotionEffectType.SPEED);
            swiftApplied.put(playerId, false);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        
        if (!BeaconPassivesGUI.hasBeaconPassives(player)) return;
        
        // Sturdy passive: +15% damage reduction, knockback immunity
        if (BeaconPassivesGUI.hasPassiveEnabled(player, "sturdy")) {
            // Reduce damage by 15%
            double damage = event.getDamage();
            event.setDamage(damage * 0.85);
            
            // Prevent knockback for certain damage types
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK ||
                event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                // Schedule to reset velocity after damage is applied
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.setVelocity(player.getVelocity().multiply(0));
                }, 1L);
            }
        }
        
        // Swift passive: -50% fall damage
        if (BeaconPassivesGUI.hasPassiveEnabled(player, "swift") && 
            event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            double damage = event.getDamage();
            event.setDamage(damage * 0.5);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Clean up effects when player no longer has beacon
        if (!BeaconPassivesGUI.hasBeaconPassives(player)) {
            removeSwiftEffect(player);
            HealthManager.updateHealth(player);
        }
    }

    /**
     * Removes all beacon passive effects from all online players
     * Called during server shutdown to prevent effect stacking
     */
    public void removeAllPassiveEffects() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removeSwiftEffect(player);
            HealthManager.updateHealth(player);
        }
        swiftApplied.clear();
    }

    /**
     * Reapplies beacon passive effects to all online players who should have them
     * Called during server startup to restore proper effects
     */
    public void reapplyAllPassiveEffects() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePassiveEffects(player);
        }
    }
}