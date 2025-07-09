package goat.minecraft.minecraftnew.other.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
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
 * Applies the Fathmic Iron full set bonus while the player is wearing the full set.
 * Reduces sea creature chance by 20% but prevents Common or Uncommon sea creatures from being considered.
 */
public class FathmicIronSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> applied = new HashMap<>();

    public FathmicIronSetBonus(JavaPlugin plugin) {
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
     * Returns the sea creature chance modifier for the given player.
     * Should be called by the fishing system when calculating sea creature spawn chance.
     */
    public static double getSeaCreatureChanceModifier(Player player) {
        FathmicIronSetBonus instance = getInstance();
        if (instance != null && instance.applied.getOrDefault(player.getUniqueId(), false)) {
            return -0.2; // -20% sea creature chance
        }
        return 0.0;
    }

    /**
     * Returns whether low-tier sea creatures should be filtered out for the given player.
     * Should be called by the fishing system when selecting sea creatures.
     */
    public static boolean shouldFilterLowTierSeaCreatures(Player player) {
        FathmicIronSetBonus instance = getInstance();
        if (instance != null && instance.applied.getOrDefault(player.getUniqueId(), false)) {
            return true; // Filter out Common and Uncommon
        }
        return false;
    }

    /**
     * Returns whether the given rarity should be filtered out.
     */
    public static boolean isRarityFiltered(Rarity rarity) {
        return rarity == Rarity.COMMON || rarity == Rarity.UNCOMMON;
    }

    private static FathmicIronSetBonus instance;
    
    private static FathmicIronSetBonus getInstance() {
        return instance;
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Fathmic Iron")) {
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