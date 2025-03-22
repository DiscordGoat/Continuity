package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GiveCustomItem implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please specify an item name.");
            return true;
        }


        // Combine arguments into the item name using underscores
        // e.g. /givecustomitem Carrot_Seeder -> itemName = "Carrot_Seeder"
        String itemName = String.join("_", args);

        // Attempt to find the custom item by that name
        ItemStack customItem = ItemRegistry.getItemByName(itemName);

        if (customItem != null) {
            ItemMeta meta = customItem.getItemMeta();
            if (meta != null) {
                // Give it to the player
                player.getInventory().addItem(customItem);
                player.sendMessage(ChatColor.GREEN + "You have received: " + meta.getDisplayName());
            } else {
                player.sendMessage(ChatColor.RED + "The item " + itemName.replace("_", " ") + " does not have metadata!");
            }
        } else {
            // If item is null, no match was found
            player.sendMessage(ChatColor.RED + "Item not found: " + itemName.replace("_", " "));
        }

        return true;
    }
}
