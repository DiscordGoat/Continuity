package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.subsystems.brewing.PotionBrewingSubsystem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Developer command that sets all active brewing sessions to finish within one second.
 */
public class FinishBrewsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public FinishBrewsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
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

        PotionBrewingSubsystem subsystem = PotionBrewingSubsystem.getInstance(plugin);
        int affected = subsystem.finishAllBrewsSoon();
        player.sendMessage(ChatColor.GREEN + "Set " + affected + " brew(s) to finish in 1 second.");
        return true;
    }
}
