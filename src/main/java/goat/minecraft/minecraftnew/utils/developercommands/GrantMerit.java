package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.utils.devtools.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.UUID;

public class GrantMerit implements CommandExecutor {

    private final JavaPlugin plugin;
    private final PlayerDataManager playerData;

    public GrantMerit(JavaPlugin plugin, PlayerDataManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @SuppressWarnings("deprecation") // for getOfflinePlayer(String)
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("merit.grant")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to grant merit points.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /grantmerit <playerName>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getName() == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID uuid = target.getUniqueId();
        int currentPoints = playerData.getMeritPoints(uuid);
        playerData.setMeritPoints(uuid, currentPoints + 1);

        sender.sendMessage(ChatColor.GREEN + "Granted 1 merit point to " + args[0]
                + ". New total: " + (currentPoints + 1));
        return true;
    }
}
