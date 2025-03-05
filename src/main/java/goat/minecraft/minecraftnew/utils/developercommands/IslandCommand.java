package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class IslandCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        // The custom island world is assumed to be named "Sanctuary_Isle".
        World islandWorld = Bukkit.getWorld("Island");
        if (islandWorld == null) {
            player.sendMessage(ChatColor.RED + "Sanctuary_Isle world is not loaded!");
            return true;
        }
        player.teleport(islandWorld.getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported to Sanctuary Isle.");
        return true;
    }
}
