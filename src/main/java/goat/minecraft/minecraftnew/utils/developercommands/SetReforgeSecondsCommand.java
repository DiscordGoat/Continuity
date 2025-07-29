package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.smithing.reforge.ReforgeSubsystem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetReforgeSecondsCommand implements CommandExecutor {
    private final ReforgeSubsystem subsystem;

    public SetReforgeSecondsCommand(JavaPlugin plugin, ReforgeSubsystem subsystem) {
        this.subsystem = subsystem;
        plugin.getCommand("setreforgeseconds").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /setreforgeseconds <seconds>");
            return true;
        }
        try {
            int secs = Integer.parseInt(args[0]);
            subsystem.setDevSeconds(secs);
            sender.sendMessage(ChatColor.GREEN + "Reforge timers now use " + secs + " second(s).");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number");
        }
        return true;
    }
}
