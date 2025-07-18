package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SetDurabilityCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SetDurabilityCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("setdurability").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setdurability <player> <durability>");
            return true;
        }

        // Get target player
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online.");
            return true;
        }

        // Parse durability value
        int durability;
        try {
            durability = Integer.parseInt(args[1]);
            if (durability < 0) {
                sender.sendMessage(ChatColor.RED + "Durability must be a positive number.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid durability value. Please enter a number.");
            return true;
        }

        // Get the item in the player's main hand
        ItemStack item = targetPlayer.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(ChatColor.RED + "The player must be holding an item.");
            return true;
        }

        // Use CustomDurabilityManager to set durability
        CustomDurabilityManager mgr = CustomDurabilityManager.getInstance();
        int maxDurability = mgr.getMaxDurability(item);
        if (durability > maxDurability) {
            sender.sendMessage(ChatColor.YELLOW + "Warning: The specified durability exceeds the maximum for this item (" + maxDurability + ").");
            durability = maxDurability;
        }
        mgr.setCustomDurability(item, durability, maxDurability);

        // Notify both the sender and the target player
        sender.sendMessage(ChatColor.GREEN + "Set durability of " + targetPlayer.getName() + "'s item to " + durability + "/" + maxDurability);
        targetPlayer.sendMessage(ChatColor.GREEN + "Your item's durability has been set to " + durability + "/" + maxDurability);

        return true;
    }
}