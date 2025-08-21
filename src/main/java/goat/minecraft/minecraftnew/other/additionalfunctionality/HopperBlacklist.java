package goat.minecraft.minecraftnew.other.additionalfunctionality;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

public class HopperBlacklist implements Listener {

    private boolean isGenerator(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasLore()) return false;

        for (String line : item.getItemMeta().getLore()) {
            // Simple detection: any lore line containing "Generator"
            if (ChatColor.stripColor(line).toLowerCase().contains("generator")) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (isGenerator(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        if (isGenerator(item)) {
            event.setCancelled(true);
        }
    }
}
