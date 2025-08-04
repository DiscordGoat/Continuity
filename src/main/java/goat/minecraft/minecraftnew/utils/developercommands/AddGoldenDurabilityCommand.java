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
 * Developer command to apply Golden Durability to the item in the player's
 * main hand. Usage: /addgoldendurability <amount>
 */
public class AddGoldenDurabilityCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public AddGoldenDurabilityCommand(JavaPlugin plugin) {
        this.plugin = plugin;
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
        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Amount must be positive.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item.");
            return true;
        }

        CustomDurabilityManager mgr = CustomDurabilityManager.getInstance();
        if (mgr.hasGoldenDurability(item)) {
            player.sendMessage(ChatColor.RED + "This item already has Golden Durability.");
            return true;
        }

        mgr.setGoldenDurability(item, amount);
        player.sendMessage(ChatColor.GOLD + "Added Golden Durability: " + amount + "/" + amount);
        return true;
    }
}
