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
