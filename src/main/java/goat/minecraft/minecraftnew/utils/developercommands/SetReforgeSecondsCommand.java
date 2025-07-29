package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.smithing.ReforgeSubsystem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetReforgeSecondsCommand implements CommandExecutor {
    private final ReforgeSubsystem subsystem;

    public SetReforgeSecondsCommand(JavaPlugin plugin) {
        this.subsystem = ReforgeSubsystem.getInstance(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /setreforgeseconds <seconds>");
            return true;
        }
        try {
            int sec = Integer.parseInt(args[0]);
            subsystem.setDevSeconds(sec);
            player.sendMessage(ChatColor.GREEN + "Reforge timers set to " + sec + " seconds for testing.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number.");
        }
        return true;
    }
}
