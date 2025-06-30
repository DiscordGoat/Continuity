package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Command: /previewparticle <particle> <style> <count> <frequency>
 * <p>
 * Spawns the given particle around the player in various styles until the player left clicks.
 */
public class PreviewParticleCommand implements CommandExecutor, Listener {

    private enum ParticleStyle {
        RING,
        AMBIENT,
        TRAIL,
        RAIN,
        BURST
    }

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> tasks = new HashMap<>();

    public PreviewParticleCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Integer taskId = tasks.get(player.getUniqueId());
        if (taskId == null) {
            return;
        }
        switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                Bukkit.getScheduler().cancelTask(taskId);
                tasks.remove(player.getUniqueId());
                player.sendMessage(ChatColor.GRAY + "Particle preview ended.");
            }
        }
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
        if (args.length != 4) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <particle> <style> <count> <frequency>");
            return true;
        }

        Particle particle;
        try {
            particle = Particle.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown particle type: " + args[0]);
            return true;
        }

        ParticleStyle style;
        try {
            style = ParticleStyle.valueOf(args[1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown style: " + args[1]);
            return true;
        }

        int count;
        try {
            count = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Count must be a number.");
            return true;
        }
        if (count < 1) count = 1;

        double frequency;
        try {
            frequency = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Frequency must be a number.");
            return true;
        }
        if (frequency <= 0) frequency = 1;

        // Cancel any existing preview for this player
        Integer existing = tasks.remove(player.getUniqueId());
        if (existing != null) {
            Bukkit.getScheduler().cancelTask(existing);
        }

        final Location[] center = {player.getLocation()};
        double radius = 1.5;
        int finalCount = count;
        long period = Math.max(1L, Math.round(20.0 / frequency));
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    tasks.remove(player.getUniqueId());
                    return;
                }

                switch (style) {
                    case RING -> {
                        if (player.getLocation().distanceSquared(center[0]) > 0.01) {
                            center[0] = player.getLocation();
                        }
                        for (int i = 0; i < finalCount; i++) {
                            double angle = (2 * Math.PI / finalCount) * i;
                            double x = radius * Math.cos(angle);
                            double z = radius * Math.sin(angle);
                            Location loc = center[0].clone().add(x, 0.5, z);
                            player.getWorld().spawnParticle(particle, loc, 0, 0, 0, 0, 0);
                        }
                    }
                    case AMBIENT -> {
                        Location base = player.getLocation();
                        for (int i = 0; i < finalCount; i++) {
                            double dx = (Math.random() - 0.5) * 4;
                            double dy = Math.random() * 2;
                            double dz = (Math.random() - 0.5) * 4;
                            Location loc = base.clone().add(dx, dy + 0.5, dz);
                            player.getWorld().spawnParticle(particle, loc, 0, 0, 0, 0, 0);
                        }
                    }
                    case TRAIL -> {
                        Location loc = player.getLocation().add(0, 0.1, 0);
                        player.getWorld().spawnParticle(particle, loc, finalCount, 0, 0, 0, 0);
                    }
                    case RAIN -> {
                        Location base = player.getLocation();
                        for (int i = 0; i < finalCount; i++) {
                            double dx = (Math.random() - 0.5) * 4;
                            double dz = (Math.random() - 0.5) * 4;
                            Location loc = base.clone().add(dx, 5 + Math.random() * 2, dz);
                            player.getWorld().spawnParticle(particle, loc, 0, 0, -0.2, 0, 0);
                        }
                    }
                    case BURST -> {
                        Vector back = player.getLocation().getDirection().multiply(-1);
                        Location start = player.getLocation().add(back);
                        for (int i = 0; i < finalCount; i++) {
                            Vector offset = new Vector((Math.random() - 0.5) * 0.5, Math.random() * 0.5,
                                    (Math.random() - 0.5) * 0.5);
                            Vector vel = offset.clone().multiply(0.5);
                            player.getWorld().spawnParticle(particle, start, 0,
                                    vel.getX(), vel.getY(), vel.getZ(), 0.1);
                        }
                    }
                }
            }
        };

        int id = runnable.runTaskTimer(plugin, 0L, period).getTaskId();
        tasks.put(player.getUniqueId(), id);
        player.sendMessage(ChatColor.GREEN + "Previewing " + particle + " in style " + style.name().toLowerCase(Locale.ROOT) +
                " with count " + count + " at " + frequency + " per second. Left click to stop.");
        return true;
    }
}
