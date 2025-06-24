package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import goat.minecraft.minecraftnew.other.trinkets.TrinketManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String title = ChatColor.stripColor(view.getTitle());

        // Cancel interaction with certain custom GUIs
        if (title.equals("Your Skills") || title.equals("Lottery Wheel")) {
            event.setCancelled(true); // Prevent any interaction
            return;
        }

        // Open backpack when right-clicked in player's inventory
        if (event.getClick() == ClickType.RIGHT
                && event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            ItemStack item = event.getCurrentItem();
            if (isBackpackItem(item)) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                CustomBundleGUI.getInstance().openBundleGUI(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        TrinketManager.getInstance().refreshBankLore(player);
                    }
                }.runTaskLater(MinecraftNew.getInstance(), 10L);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 10, 10);
            }
        }
    }

    private boolean isBackpackItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null
                && meta.hasDisplayName()
                && ChatColor.stripColor(meta.getDisplayName()).equals("Backpack");
    }
}
