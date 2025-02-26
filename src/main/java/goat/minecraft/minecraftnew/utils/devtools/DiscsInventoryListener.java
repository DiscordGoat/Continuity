package goat.minecraft.minecraftnew.utils.devtools;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DiscsInventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the inventory title is the one we set in our command
        if (event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Music Discs")) {
            event.setCancelled(true);
        }
    }
}
