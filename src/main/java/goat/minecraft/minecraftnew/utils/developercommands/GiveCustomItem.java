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

        // We require at least two arguments:
        // 1) The custom item name (which may contain spaces or underscores)
        // 2) The amount (an integer)
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givecustomitem <item_name> <amount>");
            return true;
        }

        // The last argument is the amount
        String amountArg = args[args.length - 1];
        int amount;
        try {
            amount = Integer.parseInt(amountArg);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + amountArg);
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be a positive number.");
            return true;
        }

        // The item name is all the arguments except for the last one, joined with underscores
        String[] nameParts = new String[args.length - 1];
        System.arraycopy(args, 0, nameParts, 0, args.length - 1);
        String itemName = String.join("_", nameParts);

        // Attempt to find the custom item by that name
        ItemStack customItem = ItemRegistry.getItemByName(itemName);

        if (customItem != null) {
            // Set the requested amount
            customItem.setAmount(amount);

            ItemMeta meta = customItem.getItemMeta();
            if (meta != null) {
                // Give it to the player
                player.getInventory().addItem(customItem);
                player.sendMessage(ChatColor.GREEN + "You have received: " + meta.getDisplayName()
                        + ChatColor.GREEN + " (x" + amount + ")");
            } else {
                player.sendMessage(ChatColor.RED + "The item " + itemName.replace("_", " ")
                        + " does not have metadata!");
            }
        } else {
            // If item is null, no match was found
            player.sendMessage(ChatColor.RED + "Item not found: " + itemName.replace("_", " "));
        }

        return true;
    }
}
