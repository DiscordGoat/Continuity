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

        // Check if the clicked inventory is the Skills GUI
        if (title.equals("Your Skills")) {
            event.setCancelled(true); // Prevent any interaction
        }
    }
}
