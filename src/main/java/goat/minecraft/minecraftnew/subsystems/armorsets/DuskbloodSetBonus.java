package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies the Duskblood full set bonus while the player is wearing the full set.
 * Adds 60 stacks of Warp to the Warp Ultimate Enchantment.
 */
public class DuskbloodSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> applied = new HashMap<>();

    public DuskbloodSetBonus(JavaPlugin plugin) {
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

    /**
     * Returns the warp stacks bonus for the given player.
     * Should be called by the Warp Ultimate Enchantment system.
     */
    public static int getWarpStacksBonus(Player player) {
        DuskbloodSetBonus instance = getInstance();
        if (instance != null && instance.applied.getOrDefault(player.getUniqueId(), false)) {
            return 60; // 60 warp stacks bonus
        }
        return 0;
    }

    private static DuskbloodSetBonus instance;
    
    private static DuskbloodSetBonus getInstance() {
        return instance;
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Duskblood")) {
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