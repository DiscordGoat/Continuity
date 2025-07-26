package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Developer command that gives a custom item by name.
 * Usage: /i <item name> [amount]
 */
public class ItemCommand implements CommandExecutor {

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

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <item_name> [amount]");
            return true;
        }

        int amount = 1;
        String itemName;

        // If the last argument is a number, treat it as the amount
        try {
            if (args.length > 1) {
                amount = Integer.parseInt(args[args.length - 1]);
                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "Amount must be a positive number.");
                    return true;
                }
                String[] nameParts = new String[args.length - 1];
                System.arraycopy(args, 0, nameParts, 0, args.length - 1);
                itemName = String.join("_", nameParts);
            } else {
                itemName = String.join("_", args);
            }
        } catch (NumberFormatException e) {
            // Amount wasn't provided; treat all args as part of the name
            itemName = String.join("_", args);
            amount = 1;
        }

        ItemStack customItem = ItemRegistry.getItemByName(itemName);
        if (customItem == null) {
            player.sendMessage(ChatColor.RED + "Item not found: " + itemName.replace("_", " "));
            return true;
        }

        customItem.setAmount(amount);
        ItemMeta meta = customItem.getItemMeta();
        player.getInventory().addItem(customItem);

        if (meta != null) {
            player.sendMessage(ChatColor.GREEN + "You have received: " + meta.getDisplayName()
                    + ChatColor.GREEN + " (x" + amount + ")");
        }
        return true;
    }
}
