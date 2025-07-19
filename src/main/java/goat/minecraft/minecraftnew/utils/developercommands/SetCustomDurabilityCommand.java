package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Developer command to directly set custom durability values on the item held
 * in the player's main hand.
 */
public class SetCustomDurabilityCommand implements CommandExecutor {

    private final CustomDurabilityManager durabilityManager;

    public SetCustomDurabilityCommand(CustomDurabilityManager manager) {
        this.durabilityManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /setcustomdurability <current> <max>");
            return true;
        }
        int current;
        int max;
        try {
            current = Integer.parseInt(args[0]);
            max = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Values must be integers.");
            return true;
        }
        if (max <= 0) {
            player.sendMessage(ChatColor.RED + "Max durability must be positive.");
            return true;
        }
        if (current < 0) current = 0;
        if (current > max) current = max;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must hold an item.");
            return true;
        }
        durabilityManager.setCustomDurability(item, current, max);
        player.sendMessage(ChatColor.GREEN + "Set custom durability to " + current + "/" + max + ".");
        return true;
    }
}
