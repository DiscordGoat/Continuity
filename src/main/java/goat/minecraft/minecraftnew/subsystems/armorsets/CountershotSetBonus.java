package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.armorsets.FlowManager;
import goat.minecraft.minecraftnew.subsystems.armorsets.FlowType;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Applies the Countershot full set bonus while the player is wearing the full set.
 * Provides arrow deflection capability.
 */
public class CountershotSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final FlowManager flowManager;
    private final Map<UUID, Boolean> applied = new HashMap<>();
    private final Random random = new Random();

    public CountershotSetBonus(JavaPlugin plugin) {
        this.plugin = plugin;
        this.flowManager = FlowManager.getInstance(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // Reapply bonus for players already online (e.g. during reload)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkPlayer(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(player), 1L);
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(event.getPlayer()), 1L);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }

        if (!applied.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // 30% chance to deflect arrows
        if (random.nextDouble() < 0.30) {
            event.setCancelled(true);
            
            // Create visual and audio feedback
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 5);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.5f);
            
            // Deflect arrow back towards shooter
            if (arrow.getShooter() instanceof Player shooter && !shooter.equals(player)) {
                Vector direction = shooter.getLocation().subtract(player.getLocation()).toVector().normalize();
                direction.multiply(1.5); // Increase speed slightly
                arrow.setVelocity(direction);
                arrow.setShooter(player); // Player becomes the new shooter
                
                // Add flow when deflecting
                flowManager.addFlow(player, FlowType.COUNTERSHOT, 2);
            } else {
                // If no valid shooter, just remove the arrow
                arrow.remove();
            }
        }
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Countershot")) {
            applyBonus(player);
        } else {
            removeBonus(player);
        }
    }

    private void applyBonus(Player player) {
        UUID id = player.getUniqueId();
        if (applied.getOrDefault(id, false)) {
            return;
        }
        applied.put(id, true);
    }

    private void removeBonus(Player player) {
        UUID id = player.getUniqueId();
        if (!applied.getOrDefault(id, false)) {
            return;
        }
        applied.put(id, false);
    }

    /**
     * Removes all active bonuses. Called on plugin disable to clean up.
     */
    public void removeAllBonuses() {
        applied.clear();
    }
}