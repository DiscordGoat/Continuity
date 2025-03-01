package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Preservation implements Listener {

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        ItemStack item = event.getBrokenItem();
        Player player = event.getPlayer();

        // Only proceed if the item has the "Preservation" enchantment
        if (!CustomEnchantmentManager.hasEnchantment(item, "Preservation")) return;

        // Instead of letting the item break, "save" it by resetting its durability.
        item.setDurability((short) (item.getType().getMaxDurability() - 1));

        // If the item was worn (armor), remove it from its slot
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Your " + item.getType().toString() + " was saved from breaking!");
        removeIfWorn(player, item);

        // Try to add the saved item back to the player's main inventory.
        // If inventory is full, drop it at the player's location.
        if (!addToInventory(player, item)) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        // Play a sound and notify the player that the item was saved.

    }

    /**
     * Remove the given item from the player's armor slots if it is currently worn.
     */
    private void removeIfWorn(Player player, ItemStack item) {
        PlayerInventory inv = player.getInventory();
        if (inv.getHelmet() != null && inv.getHelmet().equals(item)) {
            inv.setHelmet(null);
        } else if (inv.getChestplate() != null && inv.getChestplate().equals(item)) {
            inv.setChestplate(null);
        } else if (inv.getLeggings() != null && inv.getLeggings().equals(item)) {
            inv.setLeggings(null);
        } else if (inv.getBoots() != null && inv.getBoots().equals(item)) {
            inv.setBoots(null);
        }
    }

    /**
     * Attempts to put the item in the player's main inventory.
     * @return true if successful, false if inventory is full.
     */
    private boolean addToInventory(Player player, ItemStack item) {
        // Clone the item for safe transfer since the broken item might be partially "removed"
        ItemStack clone = item.clone();
        item.setAmount(0); // Remove it from its current slot

        if (player.getInventory().addItem(clone).isEmpty()) {
            return true;
        } else {
            // If adding fails, restore the item amount and return false.
            item.setAmount(1);
            return false;
        }
    }

    /**
     * Placeholder for a backpack system.
     * Attempts to put the item in the player's "backpack".
     * @return true if successful, false otherwise.
     */
    private boolean addToBackpack(Player player, ItemStack item) {
        // Implement your custom backpack logic here.
        return false;
    }
}
