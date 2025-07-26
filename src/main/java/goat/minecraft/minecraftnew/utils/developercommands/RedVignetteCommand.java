package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Command that briefly shows the red vignette overlay from the world border.
 * Usage: /redvignette [durationSeconds]
 */
public class RedVignetteCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public RedVignetteCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("redvignette").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        int durationTicks = 100; // default 5 seconds
        if (args.length > 0) {
            try {
                durationTicks = Integer.parseInt(args[0]) * 20;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Duration must be a number in seconds.");
                return true;
            }
        }

        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(player.getLocation());
        border.setSize(1.0);
        player.setWorldBorder(border);

        int finalDuration = durationTicks;
        new BukkitRunnable() {
            @Override
            public void run() {
                player.resetWorldBorder();
            }
        }.runTaskLater(plugin, finalDuration);
        player.sendMessage(ChatColor.GRAY + "Red vignette applied for " + (finalDuration / 20) + "s.");
        return true;
    }
}
