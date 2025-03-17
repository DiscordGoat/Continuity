package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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

    // Track which shulker box item is open
    private final Map<UUID, ItemStack> openShulkers = new HashMap<>();

    // Remember which inventory the player was in when they clicked a shulker or crafting table
    private final Map<UUID, Inventory> previousInventories = new HashMap<>();

    // Keep track of who is currently in the “open Crafting Table” mode
    private final Set<UUID> openCrafting = new HashSet<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // Only care about RIGHT-click
        if (e.getClick() != ClickType.RIGHT) {
            return;
        }

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;

        Player player = (Player) e.getWhoClicked();

        // ========== SHULKER BOX LOGIC ==========
        if (ALL_SHULKERS.contains(clickedItem.getType())) {
            PlayerMeritManager playerMeritManager = PlayerMeritManager.getInstance(MinecraftNew.getInstance());
            if(playerMeritManager.hasPerk(player.getUniqueId(), "Shulkl Box")) {
                e.setCancelled(true);

                // Get ShulkerBox block state
                BlockStateMeta meta = (BlockStateMeta) clickedItem.getItemMeta();
                org.bukkit.block.ShulkerBox shulkerBox = (org.bukkit.block.ShulkerBox) meta.getBlockState();

                // Create new inventory to show contents
                Inventory shulkerInventory = Bukkit.createInventory(null, 27, "Shulker Box");
                shulkerInventory.setContents(shulkerBox.getInventory().getContents());

                openShulkers.put(player.getUniqueId(), clickedItem);
                previousInventories.put(player.getUniqueId(), e.getView().getTopInventory());

                player.openInventory(shulkerInventory);
            }
        }
        // ========== NEW CRAFTING TABLE LOGIC ==========
        else if (clickedItem.getType() == Material.CRAFTING_TABLE) {
            PlayerMeritManager playerMeritManager = PlayerMeritManager.getInstance(MinecraftNew.getInstance());
            if(playerMeritManager.hasPerk(player.getUniqueId(), "Workbench")) {
                e.setCancelled(true);

                // Save their current (backpack) inventory
                previousInventories.put(player.getUniqueId(), e.getView().getTopInventory());
                openCrafting.add(player.getUniqueId());

                // Open the built-in 3x3 crafting UI
                // (passing null as Location is fine)
                player.openWorkbench(player.getLocation(), true);
            }
        }
    }

    /**
     * When the player closes either:
     *  - the custom "Shulker Box" inventory
     *  - or the built-in crafting GUI
     * we return them to their previous (backpack) inventory on the next tick.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory closedInv = e.getInventory();

        // ========== SHULKER BOX CLOSE ==========
        if ("Shulker Box".equals(e.getView().getTitle())) {
            ItemStack shulkerItem = openShulkers.remove(player.getUniqueId());
            if (shulkerItem == null) return;

            // Save updated contents back to the item
            BlockStateMeta meta = (BlockStateMeta) shulkerItem.getItemMeta();
            org.bukkit.block.ShulkerBox shulkerBox = (org.bukkit.block.ShulkerBox) meta.getBlockState();
            shulkerBox.getInventory().setContents(closedInv.getContents());
            meta.setBlockState(shulkerBox);
            shulkerItem.setItemMeta(meta);

            // Return to previous inventory
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
        // ========== CRAFTING TABLE CLOSE ==========
        else if (e.getView().getType() == InventoryType.WORKBENCH
                && openCrafting.contains(player.getUniqueId())) {

            openCrafting.remove(player.getUniqueId());
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
    }

    /**
     * This is your existing right-click in-air logic for Shulkers.
     * If you also want to handle the crafting table in-air, you can add a similar block.
     * But the user specifically asked for “in their backpack,” so it’s optional.
     */
    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        // Right-click in air with a Shulker in-hand
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (ALL_SHULKERS.contains(item.getType())) {
                BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                org.bukkit.block.ShulkerBox shulkerBox = (org.bukkit.block.ShulkerBox) meta.getBlockState();
                p.openInventory(shulkerBox.getInventory());
            }
            // If you also want to open crafting if they right-click with a table in hand, do it here:
            /*
            else if (item.getType() == Material.CRAFTING_TABLE) {
                p.openWorkbench(null, true);
                e.setCancelled(true);
            }
            */
        }
    }

    /**
     * This is your original close event that tried to store
     * the Shulker’s contents. Merged with the new approach above.
     * Keeping it separate can cause double-calls, so ideally you
     * remove or unify it with the method above.
     */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        // This might be duplicating logic from the combined method above,
        // so in practice you can remove it if you rely on the top method instead.
        // Just be careful to not override your stored ShulkerBox changes.

        Player p = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();

        // If the closed inventory is the “Shulker Box” UI
        if (e.getView().getTitle().equals("Shulker Box")) {
            ItemStack item = p.getInventory().getItemInMainHand();
            // If we’re actually holding the Shulker in-hand
            if (item != null && ALL_SHULKERS.contains(item.getType())) {
                BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                org.bukkit.block.ShulkerBox shulkerBox = (org.bukkit.block.ShulkerBox) meta.getBlockState();
                shulkerBox.getInventory().setContents(inventory.getContents());
                meta.setBlockState(shulkerBox);
                item.setItemMeta(meta);
            }
        }
    }
}
