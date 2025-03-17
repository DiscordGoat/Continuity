package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class QuickSwap implements Listener {

    private final PlayerMeritManager playerData;
    private final JavaPlugin plugin;

    public QuickSwap(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check if player has the perk
        if (!playerData.hasPerk(playerId, "QuickSwap")) return;

        // Get the item in the player's hand (the one they used to place the block)
        ItemStack handItem = event.getItemInHand();

        // Check if the block was successfully placed
        if (event.isCancelled()) return;

        // If the player just placed their last block, we need to find more in inventory
        if (handItem.getAmount() == 1) {
            // Get item type
            Material blockType = handItem.getType();

            // Check if the item has enchantments - we only swap unenchanted blocks
            if (handItem.getEnchantments().isEmpty()) {
                // Try to find the same block in the inventory
                PlayerInventory inventory = player.getInventory();
                int heldItemSlot = player.getInventory().getHeldItemSlot();

                // Look for the same material in the player's inventory
                for (int i = 0; i < inventory.getSize(); i++) {
                    // Skip the hotbar slots to avoid potential infinite loops
                    if (i >= 0 && i <= 8) continue;

                    ItemStack stack = inventory.getItem(i);
                    if (stack != null && stack.getType() == blockType && stack.getEnchantments().isEmpty()) {
                        // Found a matching stack of the same block type

                        // Get the amount from the found stack
                        int amount = stack.getAmount();

                        // Move the entire stack to the hotbar
                        inventory.setItem(i, null); // Remove from original slot
                        inventory.setItem(heldItemSlot, stack); // Put in hotbar slot

                        // Notify player (optional, can be removed if too spammy)
                        player.sendMessage("§a§lQuick Swap: §r§aRefilled " + amount + " blocks of " +
                                formatMaterialName(blockType.name()));

                        return; // Exit after finding and moving one stack
                    }
                }
            }
        }
    }

    /**
     * Formats the material name to be more readable
     * Example: STONE_BRICKS -> Stone Bricks
     */
    private String formatMaterialName(String materialName) {
        String[] words = materialName.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}