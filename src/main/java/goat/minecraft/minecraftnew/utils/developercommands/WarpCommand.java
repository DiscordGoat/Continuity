package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Teleports a player to the specified world by name.
 */
public class WarpCommand implements CommandExecutor {
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

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <worldName>");
            return true;
        }

        String worldName = args[0];
        World targetWorld = Bukkit.getWorld(worldName);
        if (targetWorld == null) {
            player.sendMessage(ChatColor.RED + "World '" + worldName + "' not found!");
            return true;
        }

        player.teleport(targetWorld.getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported to world '" + targetWorld.getName() + "'.");
        return true;
    }
}
