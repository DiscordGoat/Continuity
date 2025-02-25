package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Preservation implements Listener {

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        // Only proceed if the item has "Preservation" enchantment
        if (!CustomEnchantmentManager.hasEnchantment(item, "Preservation")) return;

        // Check if this item is about to break
        int newDurability = item.getDurability() + event.getDamage();
        if (newDurability >= item.getType().getMaxDurability()) {
            // Cancel the standard durability reduction
            event.setCancelled(true);

            // Instead of breaking, set the durability to 1
            item.setDurability((short) (item.getType().getMaxDurability() - 1));

            // If it's armor, remove it from the armor slot
            removeIfWorn(player, item);

            // Try to give it back to the player’s main inventory
            if (!addToInventory(player, item)) {
                // If player's main inventory is full, try adding to the backpack
                player.getWorld().dropItemNaturally(player.getLocation(), item);

            }

            // Optional sound or message
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GREEN + "Your " + item.getType().toString() + " was saved from breaking!");
        }
    }

    /**
     * Remove the given item from the player's armor slots if it is currently worn.
     */
    private void removeIfWorn(Player player, ItemStack item) {
        PlayerInventory inv = player.getInventory();
        // Compare references to remove the exact item
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
        // Temporarily clone and set the original stack to 1, since we only want to move that single item stack
        // If your enchant only deals with single items and not stacks, you can skip the cloning logic.
        ItemStack clone = item.clone();
        item.setAmount(0); // effectively remove it from its current spot

        // Attempt to add the clone to the player's main inventory
        if (player.getInventory().addItem(clone).isEmpty()) {
            return true;  // success
        } else {
            // If cannot add, revert the item stack
            item.setAmount(1);
            return false;
        }
    }

    /**
     * Attempts to put the item in the player's "backpack".
     * Replace this with however your code handles a "backpack" system.
     * @return true if successful, false otherwise.
     */
    private boolean addToBackpack(Player player, ItemStack item) {
        // Pseudocode: if you have a custom backpack inventory, do something like:
        // BackpackInventory backpack = getBackpackForPlayer(player);
        // if (backpack.hasSpace()) {
        //     backpack.addItem(item);
        //     return true;
        // }
        // return false;

        // Placeholder for demonstration: simply return false
        return false;
    }
}
