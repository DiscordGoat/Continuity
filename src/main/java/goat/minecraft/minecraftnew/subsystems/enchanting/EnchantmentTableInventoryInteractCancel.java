package goat.minecraft.minecraftnew.subsystems.enchanting;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class EnchantmentTableInventoryInteractCancel implements Listener {
    public EnchantmentTableInventoryInteractCancel() {
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("EnchantingTable")) {
            event.setCancelled(true);
        }

    }
}
