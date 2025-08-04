package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Admin command to assign Golden Durability to the item in the player's main hand.
 * Usage: /addgoldendurability <amount>
 */
public class AddGoldenDurabilityCommand implements CommandExecutor {
    public AddGoldenDurabilityCommand(JavaPlugin plugin) {
        plugin.getCommand("addgoldendurability").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <amount>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Amount must be an integer.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item.");
            return true;
        }

        CustomDurabilityManager.getInstance().setGoldenDurability(item, amount);
        player.sendMessage(ChatColor.GOLD + "Set golden durability to " + amount + ".");
        return true;
    }
}
