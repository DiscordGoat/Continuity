package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.armorsets.FlowManager;
import goat.minecraft.minecraftnew.subsystems.armorsets.FlowType;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies the Lost Legion full set bonus while the player is wearing the full set.
 * Provides +25% arrow damage bonus.
 */
public class LostLegionSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final FlowManager flowManager;
    private final Map<UUID, Boolean> applied = new HashMap<>();

    public LostLegionSetBonus(JavaPlugin plugin) {
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
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }

        if (!(arrow.getShooter() instanceof Player shooter)) {
            return;
        }

        if (!applied.getOrDefault(shooter.getUniqueId(), false)) {
            return;
        }

        // Apply 25% bonus damage to arrows
        double damage = event.getDamage();
        double bonusDamage = damage * 0.25;
        event.setDamage(damage + bonusDamage);

        // Add flow when dealing damage
        flowManager.addFlow(shooter, FlowType.LOST_LEGION, 1);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!applied.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Add flow when shooting arrows
        flowManager.addFlow(player, FlowType.LOST_LEGION, 1);
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Lost Legion")) {
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