package goat.minecraft.minecraftnew.other.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies the Nature's Wrath full set bonus while the player is wearing the full set.
 * Increases Spirit chance by 4%, increases outgoing damage to spirits by 25%, 
 * and decreases incoming damage from spirits by 25%.
 */
public class NaturesWrathSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> applied = new HashMap<>();

    public NaturesWrathSetBonus(JavaPlugin plugin) {
        this.plugin = plugin;
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
        // Handle player attacking spirit (increase damage by 25%)
        if (event.getDamager() instanceof Player player && isForestSpirit(event.getEntity())) {
            if (applied.getOrDefault(player.getUniqueId(), false)) {
                event.setDamage(event.getDamage() * 1.25);
            }
        }
        
        // Handle spirit attacking player (reduce damage by 25%)
        if (isForestSpirit(event.getDamager()) && event.getEntity() instanceof Player player) {
            if (applied.getOrDefault(player.getUniqueId(), false)) {
                event.setDamage(event.getDamage() * 0.75);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isForestSpirit(event.getEntity())) {
            return;
        }
        
        Player killer = event.getEntity().getKiller();
        if (killer != null && applied.getOrDefault(killer.getUniqueId(), false)) {
            // Activate Flow on spirit kills (+4)
            FlowManager flowManager = FlowManager.getInstance(plugin);
            flowManager.addFlowStacks(killer, 4);
        }
    }

    private boolean isForestSpirit(Entity entity) {
        return entity instanceof Skeleton && entity.hasMetadata("forestSpirit");
    }

    /**
     * Returns the spirit chance bonus for the given player.
     * Should be called by the Forestry system when calculating spirit spawn chance.
     */
    public static double getSpiritChanceBonus(Player player) {
        NaturesWrathSetBonus instance = getInstance();
        if (instance != null && instance.applied.getOrDefault(player.getUniqueId(), false)) {
            return 0.04; // 4% bonus
        }
        return 0.0;
    }

    private static NaturesWrathSetBonus instance;
    
    private static NaturesWrathSetBonus getInstance() {
        return instance;
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Nature's Wrath")) {
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
        instance = this; // Set static instance for external access
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
        instance = null;
    }
}