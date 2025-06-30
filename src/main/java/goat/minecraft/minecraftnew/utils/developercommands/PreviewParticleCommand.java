package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Command: /previewparticle <Particle> <intensity>
 * <p>
 * Spawns the given particle around the player until they move.
 */
public class PreviewParticleCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> tasks = new HashMap<>();

    public PreviewParticleCommand(JavaPlugin plugin) {
        this.plugin = plugin;
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
        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <particle> <intensity>");
            return true;
        }

        Particle particle;
        try {
            particle = Particle.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown particle type: " + args[0]);
            return true;
        }

        int intensity;
        try {
            intensity = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Intensity must be a number.");
            return true;
        }
        if (intensity < 1) intensity = 1;

        // Cancel any existing preview for this player
        Integer existing = tasks.remove(player.getUniqueId());
        if (existing != null) {
            Bukkit.getScheduler().cancelTask(existing);
        }

        Location start = player.getLocation();
        double radius = 1.5;
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    tasks.remove(player.getUniqueId());
                    return;
                }
                if (player.getLocation().distanceSquared(start) > 0.01) {
                    player.sendMessage(ChatColor.GRAY + "Particle preview ended.");
                    cancel();
                    tasks.remove(player.getUniqueId());
                    return;
                }
                for (int i = 0; i < intensity; i++) {
                    double angle = (2 * Math.PI / intensity) * i;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    Location loc = start.clone().add(x, 0.5, z);
                    player.getWorld().spawnParticle(particle, loc, 0, 0, 0, 0, 0);
                }
            }
        };

        int id = runnable.runTaskTimer(plugin, 0L, 2L).getTaskId();
        tasks.put(player.getUniqueId(), id);
        player.sendMessage(ChatColor.GREEN + "Previewing " + particle + " with intensity " + intensity + ". Move to cancel.");
        return true;
    }
}
