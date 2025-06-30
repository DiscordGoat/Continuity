package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import goat.minecraft.minecraftnew.subsystems.combat.SpawnMonsters;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class Preservation implements Listener {

    private static final int COOLDOWN_DAYS = 7;
    private static final String COOLDOWN_PREFIX = "Preservation: This item will be unusable until Day ";

    private Integer getPreservedDay(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return null;
        for (String line : meta.getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith(COOLDOWN_PREFIX)) {
                try {
                    return Integer.parseInt(stripped.substring(COOLDOWN_PREFIX.length()).trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private void removeCooldownLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.removeIf(l -> ChatColor.stripColor(l).startsWith(COOLDOWN_PREFIX));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        ItemStack item = event.getBrokenItem();
        Player player = event.getPlayer();

        // Only proceed if the item has the "Preservation" enchantment
        if (!CustomEnchantmentManager.hasEnchantment(item, "Preservation")) return;

        // Instead of letting the item break, "save" it by resetting its durability.
        item.setDurability((short) (item.getType().getMaxDurability() - 1));
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta != null && meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if (getPreservedDay(item) == null) {
            int untilDay = SpawnMonsters.getDayCount(player) + COOLDOWN_DAYS;
            lore.add(ChatColor.DARK_RED + COOLDOWN_PREFIX + untilDay);
        }
        if (meta == null) meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);

        // If the item was worn (armor), remove it from its slot
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Your " + item.getType().toString() + " was saved from breaking!");
        removeIfWorn(player, item);

        // Attempt to send the item to the player's backpack. If that fails,
        // try the ender chest. As a last resort, drop it on the ground.
        if (!addToBackpack(player, item)) {
            if (!addToEnderChest(player, item)) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

        // Play a sound and notify the player that the item was saved.

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        Integer day = getPreservedDay(clicked);
        if (day == null) return;
        int current = SpawnMonsters.getDayCount(player);
        if (current >= day) {
            removeCooldownLore(clicked);
            event.setCurrentItem(clicked);
            return;
        }
        event.setCancelled(true);
        event.setCurrentItem(null);
        CustomBundleGUI.getInstance().addItemToBackpack(player, clicked);
        player.sendMessage(ChatColor.RED + "You cannot use items that are repairing themselves.");
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null || item.getType() == Material.AIR) return;
        Integer day = getPreservedDay(item);
        if (day == null) return;
        int current = SpawnMonsters.getDayCount(player);
        if (current >= day) {
            removeCooldownLore(item);
            player.getInventory().setItem(event.getNewSlot(), item);
            return;
        }
        CustomBundleGUI.getInstance().addItemToBackpack(player, item.clone());
        player.getInventory().setItem(event.getNewSlot(), null);
        player.updateInventory();
        player.sendMessage(ChatColor.RED + "You cannot use items that are repairing themselves.");
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (item == null || !CustomEnchantmentManager.hasEnchantment(item, "Preservation")) return;

        Integer day = getPreservedDay(item);
        if (day == null) return;

        int current = SpawnMonsters.getDayCount(player);
        if (current >= day) {
            removeCooldownLore(item);
            return;
        }

        event.setCancelled(true);
        int slot = player.getInventory().first(item);
        if (slot != -1) player.getInventory().setItem(slot, null);
        removeIfWorn(player, item);
        CustomBundleGUI.getInstance().addItemToBackpack(player, item.clone());
        player.updateInventory();
        player.sendMessage(ChatColor.RED + "You cannot use items that are repairing themselves.");
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
     * Attempts to store the item in the player's persistent backpack.
     * Returns true if the item fit completely.
     */
    private boolean addToBackpack(Player player, ItemStack item) {
        ItemStack clone = item.clone();
        item.setAmount(0);
        boolean success = CustomBundleGUI.getInstance().addItemToBackpack(player, clone);
        if (!success) {
            item.setAmount(1);
        }
        return success;
    }

    /**
     * Attempts to place the item in the player's ender chest.
     */
    private boolean addToEnderChest(Player player, ItemStack item) {
        ItemStack clone = item.clone();
        item.setAmount(0);
        if (player.getEnderChest().addItem(clone).isEmpty()) {
            return true;
        }
        item.setAmount(1);
        return false;
    }
}
