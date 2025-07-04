package goat.minecraft.minecraftnew.subsystems.warpgate;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles placement of Warp Gates. When the custom Warp Gate item is placed,
 * the block is replaced with an Ender Chest and the player is prompted to name
 * the new instance. If the placement is cancelled or the block is broken before
 * naming completes, the original block is restored and the item refunded.
 */
public class WarpGateManager implements Listener {

    private final JavaPlugin plugin;

    /** Data about a pending warp gate placement. */
    private static class PendingGate {
        final BlockState oldState;
        final UUID playerId;
        PendingGate(BlockState oldState, UUID playerId) {
            this.oldState = oldState;
            this.playerId = playerId;
        }
    }

    // Map of location key -> pending gate data
    private final Map<String, PendingGate> pending = new HashMap<>();
    // Player waiting to name their placed gate
    private final Map<UUID, String> naming = new HashMap<>();

    public WarpGateManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private String toKey(Location loc) {
        return loc.getWorld().getName()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
    }

    private boolean isWarpGateItem(ItemStack stack) {
        if (stack == null) return false;
        ItemStack gate = ItemRegistry.getWarpGate();
        if (!stack.hasItemMeta() || !gate.hasItemMeta()) return false;
        ItemMeta sMeta = stack.getItemMeta();
        ItemMeta gMeta = gate.getItemMeta();
        return stack.getType() == gate.getType()
                && sMeta.hasDisplayName()
                && gMeta.hasDisplayName()
                && sMeta.getDisplayName().equals(gMeta.getDisplayName());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!isWarpGateItem(event.getItemInHand())) return;

        Block placed = event.getBlockPlaced();
        BlockState oldState = event.getBlockReplacedState();
        Player player = event.getPlayer();

        // Replace with Ender Chest
        placed.setType(Material.ENDER_CHEST);

        String key = toKey(placed.getLocation());
        pending.put(key, new PendingGate(oldState, player.getUniqueId()));
        naming.put(player.getUniqueId(), key);

        player.sendMessage(ChatColor.AQUA + "Name this instance in chat. Type 'cancel' to abort.");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        String key = naming.remove(id);
        if (key == null) return;
        event.setCancelled(true);
        PendingGate data = pending.remove(key);
        if (data == null) return;

        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase("cancel")) {
            // Restore original block and refund item
            data.oldState.update(true, false);
            event.getPlayer().getInventory().addItem(ItemRegistry.getWarpGate());
            event.getPlayer().sendMessage(ChatColor.RED + "Warp Gate placement cancelled.");
            return;
        }

        // For now we simply acknowledge the name.
        event.getPlayer().sendMessage(ChatColor.GREEN + "Created instance '" + msg + "'.");
        // Further instance creation logic would go here.
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        String key = toKey(event.getBlock().getLocation());
        PendingGate data = pending.remove(key);
        if (data == null) return;

        // Prevent drops and restore original block
        event.setDropItems(false);
        data.oldState.update(true, false);

        // Refund item to the player if they're the placer
        if (data.playerId != null) {
            Player p = Bukkit.getPlayer(data.playerId);
            if (p != null) {
                p.getInventory().addItem(ItemRegistry.getWarpGate());
            }
        }
        naming.values().remove(key);
    }
}
