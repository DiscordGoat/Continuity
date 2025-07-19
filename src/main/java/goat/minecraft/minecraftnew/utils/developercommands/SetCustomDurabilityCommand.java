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
 * Developer command to manually assign custom durability values to the item in
 * the player's main hand. Usage: /setcustomdurability <current> <max>
 */
public class SetCustomDurabilityCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public SetCustomDurabilityCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("setcustomdurability").setExecutor(this);
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

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <current> <max>");
            return true;
        }

        int current;
        int max;
        try {
            current = Integer.parseInt(args[0]);
            max = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Both values must be integers.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item.");
            return true;
        }

        CustomDurabilityManager.getInstance().setCustomDurability(item, current, max);
        player.sendMessage(ChatColor.GREEN + "Custom durability set to " + current + "/" + max + ".");
        return true;
    }
}
