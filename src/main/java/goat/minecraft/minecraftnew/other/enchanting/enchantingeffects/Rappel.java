package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Rappel implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        // Check if the player right-clicked air
        if (!event.getAction().toString().contains("RIGHT_CLICK_AIR")) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if the item has the "Rappel" enchantment and it's enabled
        if (!CustomEnchantmentManager.isEnchantmentActive(player, itemInHand, "Rappel")) {
            return;
        }

        // Get the world and current player location
        Block highestNonBedrockBlock = getHighestNonBedrockBlock(player);
        if (highestNonBedrockBlock != null) {
            // Teleport the player to the highest block that isn't bedrock
            player.teleport(highestNonBedrockBlock.getLocation().add(0.5, 1, 0.5));

            // Play teleport sound and apply durability usage
            player.setSaturation(0);
            player.setFoodLevel(0);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);

            int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(itemInHand, "Rappel");
            itemInHand.setDurability((short) (itemInHand.getDurability() + enchantmentLevel));
        }
    }

    /**
     * Gets the highest block at the player's current location that is not bedrock.
     *
     * @param player The player triggering the enchantment.
     * @return The highest non-bedrock block or null if none is found.
     */
    private Block getHighestNonBedrockBlock(Player player) {
        int x = player.getLocation().getBlockX();
        int z = player.getLocation().getBlockZ();
        Block highestBlock = player.getWorld().getHighestBlockAt(x, z);

        // Traverse downwards to ensure the block isn't bedrock
        while (highestBlock.getType() == Material.BEDROCK && highestBlock.getY() > 0) {
            highestBlock = highestBlock.getRelative(0, -1, 0);
        }

        // Return null if only bedrock is found
        return highestBlock.getType() != Material.BEDROCK ? highestBlock : null;
    }
}
