package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Developer command that sets the amount of the item in the player's main hand.
 * Usage: /setamount <amount>
 */
public class SetAmountCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SetAmountCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("setamount").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <amount>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Amount must be a number.");
            return true;
        }

        if (amount < 1) {
            player.sendMessage(ChatColor.RED + "Amount must be at least 1.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item.");
            return true;
        }

        int max = item.getMaxStackSize();
        if (amount > max) {
            player.sendMessage(ChatColor.YELLOW + "Amount exceeds max stack size (" + max + "). Using " + max + ".");
            amount = max;
        }

        item.setAmount(amount);
        player.sendMessage(ChatColor.GREEN + "Set held item amount to " + amount + ".");
        return true;
    }
}
