package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.forestry.SaplingManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetSaplingCooldownCommand implements CommandExecutor {
    private final SaplingManager manager;

    public SetSaplingCooldownCommand(JavaPlugin plugin, SaplingManager manager) {
        this.manager = manager;
        plugin.getCommand("setsaplingcooldownsecondsremaining").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /setsaplingcooldownsecondsremaining <seconds>");
            return true;
        }
        try {
            int secs = Integer.parseInt(args[0]);
            manager.setCooldownSecondsRemaining(secs);
            sender.sendMessage(ChatColor.GREEN + "Sapling cooldown set to " + secs + " seconds.");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number");
        }
        return true;
    }
}
