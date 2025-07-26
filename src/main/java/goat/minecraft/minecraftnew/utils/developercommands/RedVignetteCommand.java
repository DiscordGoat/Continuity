package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command: /redvignette
 * Gives the executing player a temporary red vignette effect by showing
 * a very small world border centered on them.
 */
public class RedVignetteCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public RedVignetteCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        // Create a personal world border centered on the player
        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(player.getLocation());
        border.setSize(1); // Very small to force the vignette overlay
        player.setWorldBorder(border);

        // Remove the border after 5 seconds (100 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.setWorldBorder(null), 100L);
        player.sendMessage(ChatColor.GRAY + "You feel the world closing in around you...");
        return true;
    }
}
