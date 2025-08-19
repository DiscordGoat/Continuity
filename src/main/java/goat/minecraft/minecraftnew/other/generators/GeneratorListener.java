package goat.minecraft.minecraftnew.other.generators;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Handles player interaction with generators via inventory GUIs.
 */
public class GeneratorListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || item == null) return;
        GeneratorManager mgr = GeneratorManager.getInstance();
        if (mgr == null || !mgr.isGenerator(item)) return;

        ClickType click = event.getClick();
        Player player = (Player) event.getWhoClicked();

        if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
            event.setCancelled(true);
            if (mgr.isActive(item)) {
                GeneratorService.getInstance().deactivate(item);
            } else {
                GeneratorService.getInstance().activate(item, clicked, event.getSlot());
            }
            event.setCurrentItem(item);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } else {
            GeneratorService.getInstance().onMove(item);
            event.setCurrentItem(item);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        GeneratorManager mgr = GeneratorManager.getInstance();
        if (mgr == null) return;
        for (ItemStack item : event.getNewItems().values()) {
            if (item != null && mgr.isGenerator(item)) {
                GeneratorService.getInstance().onMove(item);
                break;
            }
        }
    }
}

