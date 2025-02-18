package goat.minecraft.minecraftnew.utils;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Extra import (unchanged):
import java.util.ArrayList;
import java.util.List;

public class ShulkerBox implements Listener {

    public static final List<Material> ALL_SHULKERS = List.of(
            Material.SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX
    );

    // Existing map to track which shulker box item is open.
    private final Map<UUID, ItemStack> openShulkers = new HashMap<>();

    // NEW map to remember which inventory (e.g., backpack) the player was in when they clicked a shulker box.
    private final Map<UUID, Inventory> previousInventories = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // We only care about right-clicks on an item in a GUI
        if (e.getClick() != ClickType.RIGHT) {
            return;
        }

        ItemStack clickedItem = e.getCurrentItem();
        // If no item was clicked or it's not a shulker, ignore
        if (clickedItem == null || !ALL_SHULKERS.contains(clickedItem.getType())) {
            return;
        }

        // Cancel picking up the item
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        // Extract the ShulkerBox's internal inventory
        BlockStateMeta meta = (BlockStateMeta) clickedItem.getItemMeta();
        org.bukkit.block.ShulkerBox shulkerBox = (org.bukkit.block.ShulkerBox) meta.getBlockState();

        // Create a new inventory to display the contents (27 slots)
        Inventory shulkerInventory = Bukkit.createInventory(null, 27, "Shulker Box");
        // Copy over the contents
        shulkerInventory.setContents(shulkerBox.getInventory().getContents());

        // Store a reference so we know which item to save to later
        openShulkers.put(player.getUniqueId(), clickedItem);

        // [NEW] Remember the inventory the player is currently in (the “backpack” or whatever GUI this is).
        // This way, when they close the shulker, we can return them here.
        previousInventories.put(player.getUniqueId(), e.getView().getTopInventory());

        // Open the new inventory for the player
        player.openInventory(shulkerInventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory closedInv = e.getInventory();

        // Only act if the closed inventory has our "Shulker Box" title
        if (!"Shulker Box".equals(e.getView().getTitle())) {
            return;
        }

        // Check if the player was indeed viewing a Shulker Box we tracked
        ItemStack shulkerItem = openShulkers.remove(player.getUniqueId());
        if (shulkerItem == null) {
            return;
        }

        // Save contents back into the Shulker Box item
        BlockStateMeta meta = (BlockStateMeta) shulkerItem.getItemMeta();
        org.bukkit.block.ShulkerBox shulkerBox = (org.bukkit.block.ShulkerBox) meta.getBlockState();
        shulkerBox.getInventory().setContents(closedInv.getContents());
        meta.setBlockState(shulkerBox);
        shulkerItem.setItemMeta(meta);

        // Now re-open the "backpack" on the *next tick*, instead of immediately
        Inventory oldInv = previousInventories.remove(player.getUniqueId());
        if (oldInv != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.openInventory(oldInv);
                }
            }.runTask(MinecraftNew.getInstance());
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (ALL_SHULKERS.contains(item.getType())) {
                org.bukkit.block.ShulkerBox shulkerBox = (org.bukkit.block.ShulkerBox)
                        ((org.bukkit.inventory.meta.BlockStateMeta) item.getItemMeta()).getBlockState();

                p.openInventory(shulkerBox.getInventory());
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        Inventory inventory = e.getInventory();
        if (e.getView().getTitle().equals("Shulker Box")) {
            org.bukkit.inventory.meta.BlockStateMeta meta =
                    (org.bukkit.inventory.meta.BlockStateMeta) item.getItemMeta();
            org.bukkit.block.ShulkerBox shulkerBox =
                    (org.bukkit.block.ShulkerBox) meta.getBlockState();
            shulkerBox.getInventory().setContents(inventory.getContents());
            meta.setBlockState(shulkerBox);
            item.setItemMeta(meta);
        }
    }
}
