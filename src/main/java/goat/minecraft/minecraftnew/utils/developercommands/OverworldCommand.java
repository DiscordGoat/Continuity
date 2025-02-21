package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OverworldCommand implements CommandExecutor {
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

        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You lack the required permission to do this!");
        return false;
        }

        World overworld = Bukkit.getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);

        if (overworld == null) {
            player.sendMessage("The Overworld is not available!");
            return true;
        }

        player.teleport(new Location(overworld, 0, 80, 0)); // Safe spawn height
        player.sendMessage("You have been teleported to The Overworld!");
        return true;
    }
}
