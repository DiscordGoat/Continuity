package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ShulkerBox implements Listener {

    public static final List<Material> ALL_SHULKERS = List.of(
            Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
            Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX
    );

    // Track which shulker-item the player is editing, and what they had open before.
    private final Map<UUID, ItemStack>    openShulkers        = new HashMap<>();
    private final Map<UUID, Inventory>    previousInventories = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClick() != ClickType.RIGHT) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !ALL_SHULKERS.contains(clicked.getType())) return;

        Player p = (Player)e.getWhoClicked();
        // perk check
        PlayerMeritManager mgr = PlayerMeritManager.getInstance(MinecraftNew.getInstance());
        if (!mgr.hasPerk(p.getUniqueId(), "Shulkl Box")) return;

        e.setCancelled(true);
        BlockStateMeta meta = (BlockStateMeta) clicked.getItemMeta();
        org.bukkit.block.ShulkerBox box = (org.bukkit.block.ShulkerBox) meta.getBlockState();
        Inventory inv = Bukkit.createInventory(null, 27, "Shulker Box");
        inv.setContents(box.getInventory().getContents());

        openShulkers.put(p.getUniqueId(), clicked);
        previousInventories.put(p.getUniqueId(), e.getView().getTopInventory());
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        // only save our custom Shulker Box UI
        if (!e.getView().getTitle().equals("Shulker Box")) return;

        Player p = (Player)e.getPlayer();
        UUID id = p.getUniqueId();
        ItemStack shulkerItem = openShulkers.remove(id);
        Inventory oldTopInv = previousInventories.remove(id);

        if (shulkerItem == null || oldTopInv == null) return;

        // write back the contents
        BlockStateMeta meta = (BlockStateMeta) shulkerItem.getItemMeta();
        org.bukkit.block.ShulkerBox box = (org.bukkit.block.ShulkerBox) meta.getBlockState();
        box.getInventory().setContents(e.getInventory().getContents());
        meta.setBlockState(box);
        shulkerItem.setItemMeta(meta);

        // reopen their previous inventory (e.g. chest / crafting table / player inv)
        new BukkitRunnable() {
            @Override
            public void run() {
                p.openInventory(oldTopInv);
            }
        }.runTaskLater(MinecraftNew.getInstance(), 1L);
    }
}
