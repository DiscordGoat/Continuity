package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetNearestCatalystTypeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        CatalystManager catalystManager = CatalystManager.getInstance();
        
        if (catalystManager == null) {
            player.sendMessage(ChatColor.RED + "Catalyst system is not initialized!");
            return true;
        }

        CatalystType nearestCatalyst = getNearestCatalystType(player, catalystManager);
        
        if (nearestCatalyst != null) {
            player.sendMessage(ChatColor.GREEN + "Nearest catalyst in range: " + nearestCatalyst.getColoredDisplayName());
        } else {
            player.sendMessage(ChatColor.YELLOW + "No catalyst in range.");
        }

        return true;
    }
    
    private CatalystType getNearestCatalystType(Player player, CatalystManager catalystManager) {
        CatalystType nearestType = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (CatalystType type : CatalystType.values()) {
            if (catalystManager.isNearCatalyst(player.getLocation(), type)) {
                var nearest = catalystManager.findNearestCatalyst(player.getLocation(), type);
                if (nearest != null) {
                    double distance = nearest.getLocation().distance(player.getLocation());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestType = type;
                    }
                }
            }
        }
        
        return nearestType;
    }
}