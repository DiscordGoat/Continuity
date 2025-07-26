package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * Developer command that gives a vanilla Minecraft item by name.
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

        // Parse amount (if provided) and item name
        int amount = 1;
        String itemName;
        try {
            if (args.length > 1) {
                int possibleAmount = Integer.parseInt(args[args.length - 1]);
                if (possibleAmount > 0) {
                    amount = possibleAmount;
                    // join all but last arg as the name
                    String[] nameParts = new String[args.length - 1];
                    System.arraycopy(args, 0, nameParts, 0, args.length - 1);
                    itemName = String.join("_", nameParts);
                } else {
                    player.sendMessage(ChatColor.RED + "Amount must be a positive number.");
                    return true;
                }
            } else {
                itemName = String.join("_", args);
            }
        } catch (NumberFormatException e) {
            // last arg wasn't a number, treat all args as name
            itemName = String.join("_", args);
        }

        // Lookup vanilla Material (case-insensitive)
        Material material = Material.matchMaterial(itemName.toUpperCase(Locale.ROOT));
        if (material == null || material == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Minecraft item not found: " + itemName.replace("_", " "));
            return true;
        }

        // Give the item
        ItemStack stack = new ItemStack(material, amount);
        player.getInventory().addItem(stack);

        // Feedback
        player.sendMessage(ChatColor.GREEN + "You have received: "
                + ChatColor.WHITE + material.name().toLowerCase().replace('_',' ')
                + ChatColor.GREEN + " (x" + amount + ")");
        return true;
    }
}
