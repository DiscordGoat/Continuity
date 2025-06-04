package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Restock merit perk.
 * <p>
 * When the player is holding a bow with no arrows in their inventory and has free space,
 * automatically grants them a single arrow. Costs 1 merit point.
 * A five second cooldown prevents continuous arrow generation.
 */
public class Restock implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;
    private final Map<UUID, Long> lastRestock = new HashMap<>();
    private static final long COOLDOWN_MS = 5000; // 5 second cooldown

    public Restock(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        // Ensure the player has purchased the perk
        if (!playerData.hasPerk(id, "Restock")) return;

        // Only proceed if the player is holding a bow in the main hand
        if (player.getInventory().getItemInMainHand().getType() != Material.BOW) return;

        // Check for existing arrows; if any found, do nothing
        if (player.getInventory().contains(Material.ARROW)) return;

        // Inventory must have at least one free slot
        if (player.getInventory().firstEmpty() == -1) return;

        // Check cooldown
        long now = System.currentTimeMillis();
        Long last = lastRestock.get(id);
        if (last != null && now - last < COOLDOWN_MS) return;

        // Give the player a single arrow
        player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
        lastRestock.put(id, now);
    }
}
