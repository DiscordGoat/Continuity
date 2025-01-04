package goat.minecraft.minecraftnew.other.engineer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EngineeringProfessionListener implements Listener {

    private final JavaPlugin plugin;

    public EngineeringProfessionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity clickedEntity = event.getRightClicked();

        // We only care if the clicked entity is a Villager
        if (clickedEntity instanceof Villager) {
            // Make sure the player is holding an item (in main hand for simplicity)
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                return;
            }

            // Check if it's your custom 'Engineering Profession' item
            // (e.g., same type & same display name)
            if (itemInHand.getType() == Material.REDSTONE_TORCH
                    && itemInHand.hasItemMeta()
                    && itemInHand.getItemMeta().hasDisplayName()) {

                String itemName = itemInHand.getItemMeta().getDisplayName();
                String engineeringItemName = ChatColor.YELLOW + "Engineering Profession";

                if (itemName.equals(engineeringItemName)) {

                    // Now we know the player is right-clicking with your "Engineering Profession" item
                    Villager villager = (Villager) clickedEntity;

                    // Set Villager’s custom name
                    villager.setCustomName(ChatColor.RED + "Engineer");
                    villager.setCustomNameVisible(true);

                    // If you want to remove one item from the player's hand after use:
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                    event.setCancelled(true);
                }

            }
        }
    }
}
