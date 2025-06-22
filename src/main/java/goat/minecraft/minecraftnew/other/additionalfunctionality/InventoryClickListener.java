package goat.minecraft.minecraftnew.other.additionalfunctionality;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String title = ChatColor.stripColor(view.getTitle());

        // Cancel interaction with certain custom GUIs
        if (title.equals("Your Skills") || title.equals("Lottery Wheel")) {
            event.setCancelled(true); // Prevent any interaction
        }
    }
}
