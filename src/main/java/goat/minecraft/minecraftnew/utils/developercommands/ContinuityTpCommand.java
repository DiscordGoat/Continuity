package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ContinuityTpCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        
        World continuityWorld = Bukkit.getWorld("continuity");
        if (continuityWorld == null) {
            player.sendMessage(ChatColor.RED + "Continuity world is not loaded!");
            player.sendMessage(ChatColor.YELLOW + "Use /generatecontinuityisland to install the world first.");
            return true;
        }
        
        player.teleport(continuityWorld.getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "Welcome to Continuity Island!");
        return true;
    }
}